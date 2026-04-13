package com.moeats.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.moeats.domain.GroupCartItem;
import com.moeats.domain.GroupOrder;
import com.moeats.domain.Member;
import com.moeats.domain.OrderRoom;
import com.moeats.domain.RoomParticipant;
import com.moeats.domain.Store;
import com.moeats.domain.StoreMenu;
import com.moeats.service.StoreMenuService;
import com.moeats.service.StoreService;
import com.moeats.services.GroupCartItemService;
import com.moeats.services.MenuService;
import com.moeats.services.OrderMemberQueryService;
import com.moeats.services.OrderRoomService;
import com.moeats.services.TransactionService;
import com.moeats.services.sse.SSEService;
import com.moeats.timer.OrderRoomTimer;

import lombok.extern.slf4j.Slf4j;


@Slf4j
@Controller
public class RoomController {

	@Autowired
	OrderMemberQueryService memberService;
	@Autowired
	OrderRoomService orderRoomService;
	@Autowired
	GroupCartItemService groupCartItemService;
	@Autowired
	MenuService menuService;
	@Autowired
	TransactionService transactionService;
	@Autowired
	SSEService sseService;
	@Autowired
	StoreMenuService storeMenuService;
	@Autowired
	StoreService storeService;
	@Autowired
	OrderRoomTimer orderRoomTimer;

	public record MemberParticipant(RoomParticipant roomParticipant, Member member) {
	}

	public record CartItem(GroupCartItem groupCartItem, StoreMenu storeMenu) {
	}

	public record MemberItem(RoomParticipant roomParticipant, Member member, List<CartItem> items,
			Integer totalAmount) {
	}
	
//	private void ensureUnselectedForEditing(OrderRoom orderRoom, Member member) {
//	    if (orderRoom == null || orderRoom.isJoinLocked()) {
//	        return;
//	    }
//	    if (!"OPEN".equals(orderRoom.getRoomStatus()) && !"SELECTING".equals(orderRoom.getRoomStatus())) {
//	        return;
//	    }
//
//	    RoomParticipant roomParticipant = orderRoomService.findJoinedRoomMember(
//	            orderRoom.getRoomIdx(),
//	            member.getMemberIdx()
//	    );
//
//	    if (roomParticipant == null || !"SELECTED".equals(roomParticipant.getSelectionStatus())) {
//	        return;
//	    }
//
//	    if (orderRoomService.unselect(roomParticipant.getRoomParticipantIdx()) > 0) {
//	        sseService.participantUpdate(orderRoom.getRoomIdx());
//	    }
//	}

	private String redirectToActiveRoomIfExists(Member member, RedirectAttributes ra) {
		OrderRoom activeRoom = orderRoomService.findActiveRoomByMember(member.getMemberIdx());
		if (activeRoom == null) {
			return null;
		}

		ra.addFlashAttribute("error", "이미 참여 중인 주문방이 있습니다.");
		return "redirect:/rooms/code/" + activeRoom.getRoomCode();
	}

	private String redirectToDifferentActiveRoom(Member member, String requestedRoomCode, RedirectAttributes ra) {
		OrderRoom activeRoom = orderRoomService.findActiveRoomByMember(member.getMemberIdx());
		if (activeRoom == null) {
			return null;
		}
		if (requestedRoomCode != null && requestedRoomCode.equals(activeRoom.getRoomCode())) {
			return null;
		}

		ra.addFlashAttribute("error", "이미 참여 중인 다른 주문방이 있습니다.");
		return "redirect:/rooms/code/" + activeRoom.getRoomCode();
	}

	@GetMapping("/rooms/new")
	public String roomCreateForm(Model model, @RequestParam(name = "storeIdx", defaultValue = "0") int storeIdx,
			@SessionAttribute("member") Member member, RedirectAttributes ra) {
		String activeRedirect = redirectToActiveRoomIfExists(member, ra);
		if (activeRedirect != null) {
			return activeRedirect;
		}

		model.addAttribute("storeIdx", storeIdx);
		return "room-create";
	}

	@PostMapping("/rooms")
	public String roomCreate(RedirectAttributes ra, OrderRoom orderRoom, @SessionAttribute("member") Member member) {
		String activeRedirect = redirectToActiveRoomIfExists(member, ra);
		if (activeRedirect != null) {
			return activeRedirect;
		}
		orderRoom.setLeaderMemberIdx(member.getMemberIdx());
		String code = null;
		int res = 0;
		for (int trial = 0; trial < 3 && res == 0; trial++) {
			code = orderRoomService.createCode();
			orderRoom.setRoomCode(code);
			res = orderRoomService.insert(orderRoom);
		}
		if (res == 0) {
			ra.addFlashAttribute("error", "방을 생성하는 중 오류가 발생했습니다");
			return "redirect:/rooms/new";
		}
		return "redirect:/rooms/code/" + code;
	}

	@GetMapping("/rooms/code/{room_code}")
	public String codeRoom(RedirectAttributes ra, Model model, @PathVariable("room_code") String roomCode,
			@SessionAttribute("member") Member member) {
		OrderRoom orderRoom = orderRoomService.findByCode(roomCode);
		if (orderRoom == null) {
			ra.addFlashAttribute("error", "해당하는 방을 찾을 수 없습니다");
			return "redirect:/rooms/join";
		}
		String activeRedirect = redirectToDifferentActiveRoom(member, roomCode, ra);
		if (activeRedirect != null) {
			return activeRedirect;
		}

		if (orderRoomService.findJoinedRoomMember(orderRoom.getRoomIdx(), member.getMemberIdx()) == null) {
		    RoomParticipant roomParticipant = new RoomParticipant();
		    roomParticipant.setRoomIdx(orderRoom.getRoomIdx());
		    roomParticipant.setMemberIdx(member.getMemberIdx());

		    if (orderRoom.getLeaderMemberIdx() == member.getMemberIdx()) {
		        roomParticipant.setParticipantRole("LEADER");
		    } else {
		        roomParticipant.setParticipantRole("PARTICIPANT");
		    }

		    if (orderRoom.isJoinLocked() || orderRoomService.join(roomParticipant) == 0) {
		        ra.addFlashAttribute("error", "이미 나갔거나 내보내진 주문방에는 다시 참여할 수 없습니다.");
		        return "redirect:/main";
		    }

		    safeRoomSse("participantUpdate", () -> sseService.participantUpdate(orderRoom.getRoomIdx()));
		}

		List<GroupCartItem> groupCartItems = groupCartItemService.findByRoom(orderRoom.getRoomIdx());
		Map<Integer, StoreMenu> storeMenuMap = menuService
				.findByIdxs(groupCartItems.stream().map(GroupCartItem::getMenuIdx).toList()).stream()
				.collect(Collectors.toMap(StoreMenu::getMenuIdx, storeMenu -> storeMenu));

		Map<Integer, List<CartItem>> cartItemMap = groupCartItems.stream()
				.collect(Collectors.groupingBy(GroupCartItem::getMemberIdx, Collectors.mapping(
						groupCartItem -> new CartItem(groupCartItem, storeMenuMap.get(groupCartItem.getMenuIdx())),
						Collectors.toList())));

		Map<Integer, RoomParticipant> roomParticipantMap = orderRoomService.findByRoom(orderRoom.getRoomIdx()).stream()
				.collect(Collectors.toMap(RoomParticipant::getMemberIdx, roomParticipant -> roomParticipant));

		RoomParticipant myState = roomParticipantMap.remove(member.getMemberIdx());
		List<CartItem> myCartItems = cartItemMap.getOrDefault(member.getMemberIdx(), List.of());

		List<MemberItem> otherCartItems = memberService.findByIdxs(roomParticipantMap.keySet()).stream()
				.map(roomMember -> {
					List<CartItem> cartItems = cartItemMap.getOrDefault(roomMember.getMemberIdx(), List.of());
					return new MemberItem(roomParticipantMap.get(roomMember.getMemberIdx()), roomMember, cartItems,
							cartItems.stream().collect(
									Collectors.summingInt(cartItem -> cartItem.groupCartItem().getItemTotalAmount())));
				}).toList();

		List<MemberParticipant> roomParticipants = memberService.findByIdxs(roomParticipantMap.keySet()).stream()
				.map(roomMember -> new MemberParticipant(roomParticipantMap.get(roomMember.getMemberIdx()), roomMember))
				.toList();

		model.addAttribute("myState", myState);
		model.addAttribute("myCartItems", myCartItems);
		model.addAttribute("otherCartItems", otherCartItems);
		model.addAttribute("roomParticipants", roomParticipants);

		int myCartTotal = myCartItems.stream().mapToInt(cartItem -> cartItem.groupCartItem().getItemTotalAmount())
				.sum();

		int otherCartTotal = otherCartItems.stream()
				.mapToInt(memberItem -> memberItem.totalAmount() == null ? 0 : memberItem.totalAmount()).sum();

		int participantCount = 1 + roomParticipants.size();

		long completedCount = (myState != null && "SELECTED".equals(myState.getSelectionStatus()) ? 1 : 0)
				+ roomParticipants.stream().filter(p -> "SELECTED".equals(p.roomParticipant().getSelectionStatus()))
						.count();

		boolean isLeader = myState != null && "LEADER".equals(myState.getParticipantRole());
		boolean allSelected = participantCount > 0 && completedCount == participantCount;

		Store store = storeService.getStoreByIdx(orderRoom.getStoreIdx());

		model.addAttribute("orderRoom", orderRoom);
		model.addAttribute("store", store);
		model.addAttribute("isLeader", isLeader);
		model.addAttribute("participantCount", participantCount);
		model.addAttribute("completedCount", completedCount);
		model.addAttribute("myCartTotal", myCartTotal);
		model.addAttribute("roomGrandTotal", myCartTotal + otherCartTotal);
		model.addAttribute("allSelected", allSelected);

		return "room-detail";
	}

	@GetMapping("/rooms/join")
	public String joinRoom(@RequestParam(name = "roomCode", required = false) String roomCode,
			@SessionAttribute("member") Member member, RedirectAttributes ra) {

		if (roomCode != null && !roomCode.isBlank()) {
			String normalized = roomCode.replaceAll("\\D", "");

			if (!normalized.matches("\\d{6}")) {
				ra.addFlashAttribute("error", "방 코드는 6자리 숫자여야 합니다");
				return "redirect:/main";
			}

			String activeRedirect = redirectToDifferentActiveRoom(member, normalized, ra);
			if (activeRedirect != null) {
				return activeRedirect;
			}

			return "redirect:/rooms/code/" + normalized;
		}

		String activeRedirect = redirectToActiveRoomIfExists(member, ra);
		if (activeRedirect != null) {
			return activeRedirect;
		}

		return "room-join";
	}

	@GetMapping("/rooms/code/{room_code}/cart")
	public String roomCart(@PathVariable("room_code") String roomCode,
			@RequestAttribute("orderRoom") OrderRoom orderRoom, @SessionAttribute("member") Member member,
			Model model) {

//		ensureUnselectedForEditing(orderRoom, member);
		
		List<StoreMenu> menuList = storeMenuService.menuListForUser(orderRoom.getStoreIdx());
		Store store = storeService.getStoreByIdx(orderRoom.getStoreIdx());

		Map<Integer, GroupCartItem> myCartItemMap = groupCartItemService
				.findRoomMember(orderRoom.getRoomIdx(), member.getMemberIdx()).stream().collect(Collectors.toMap(
						GroupCartItem::getMenuIdx, Function.identity(), (left, right) -> right, LinkedHashMap::new));

		int myCartTotal = myCartItemMap.values().stream().mapToInt(GroupCartItem::getItemTotalAmount).sum();

		model.addAttribute("menuList", menuList);
		model.addAttribute("store", store);
		model.addAttribute("orderRoom", orderRoom);
		model.addAttribute("myCartItemMap", myCartItemMap);
		model.addAttribute("myCartTotal", myCartTotal);

		return "room-cart";
	}

	@GetMapping("/rooms/code/{room_code}/confirm")
	public String confirmRoom(Model model, @PathVariable("room_code") String roomCode,
			@RequestAttribute("orderRoom") OrderRoom orderRoom, @SessionAttribute("member") Member member) {

//		ensureUnselectedForEditing(orderRoom, member);
		
		List<GroupCartItem> groupCartItems = groupCartItemService.findRoomMember(orderRoom.getRoomIdx(),
				member.getMemberIdx());

		Map<Integer, StoreMenu> storeMenuMap = menuService
				.findByIdxs(groupCartItems.stream().map(GroupCartItem::getMenuIdx).toList()).stream()
				.collect(Collectors.toMap(StoreMenu::getMenuIdx, storeMenu -> storeMenu));

		RoomParticipant myState = orderRoomService.findJoinedRoomMember(orderRoom.getRoomIdx(), member.getMemberIdx());

		List<CartItem> myCartItems = groupCartItems.stream()
				.map(groupCartItem -> new CartItem(groupCartItem, storeMenuMap.get(groupCartItem.getMenuIdx())))
				.toList();

		int myCartTotal = myCartItems.stream().mapToInt(item -> item.groupCartItem().getItemTotalAmount()).sum();

		model.addAttribute("orderRoom", orderRoom);
		model.addAttribute("myState", myState);
		model.addAttribute("myCartItems", myCartItems);
		model.addAttribute("myCartTotal", myCartTotal);

		return "room-confirm";
	}

	@PostMapping("/rooms/code/{room_code}/select")
	public String selectRoom(RedirectAttributes ra, @PathVariable("room_code") String roomCode,
			@RequestAttribute("orderRoom") OrderRoom orderRoom, @SessionAttribute("member") Member member) {
		RoomParticipant roomParticipant = orderRoomService.findJoinedRoomMember(orderRoom.getRoomIdx(),
				member.getMemberIdx());
		if (orderRoom.isJoinLocked() || roomParticipant == null
				|| orderRoomService.setSelect(roomParticipant.getRoomParticipantIdx()) == 0) {
			ra.addFlashAttribute("error", "선택을 확정하는 중 오류가 발생했습니다");
			return String.format("redirect:/rooms/code/%s/confirm", roomCode);
		}
		sseService.participantUpdate(orderRoom.getRoomIdx());
		return String.format("redirect:/rooms/code/%s", roomCode);
	}

	@PostMapping("/rooms/code/{room_code}/unselect")
	public String unselectRoom(RedirectAttributes ra, @PathVariable("room_code") String roomCode,
			@RequestAttribute("orderRoom") OrderRoom orderRoom, @SessionAttribute("member") Member member) {
		RoomParticipant roomParticipant = orderRoomService.findJoinedRoomMember(orderRoom.getRoomIdx(),
				member.getMemberIdx());
		if (orderRoom.isJoinLocked() || roomParticipant == null
				|| orderRoomService.unselect(roomParticipant.getRoomParticipantIdx()) == 0) {
			ra.addFlashAttribute("error", "선택을 취소하는 중 오류가 발생했습니다");
			return String.format("redirect:/rooms/code/%s", roomCode);
		}
		sseService.participantUpdate(orderRoom.getRoomIdx());
		return String.format("redirect:/rooms/code/%s/confirm", roomCode);
	}

	@PostMapping("/rooms/code/{room_code}/leave")
	public String leaveRoom(RedirectAttributes ra, @PathVariable("room_code") String roomCode,
			@RequestAttribute("orderRoom") OrderRoom orderRoom, @SessionAttribute("member") Member member) {
		RoomParticipant roomParticipant = orderRoomService.findJoinedRoomMember(orderRoom.getRoomIdx(),
				member.getMemberIdx());
		if (roomParticipant == null) {
			ra.addFlashAttribute("error", "잘못된 접근입니다");
			return "redirect:/main";
		}
		if (orderRoom.getLeaderMemberIdx() == member.getMemberIdx()) {
			ra.addFlashAttribute("error", "방장은 나갈 수 없습니다. 방 종료를 사용해 주세요.");
		} else if (orderRoomService.leave(roomParticipant.getRoomParticipantIdx()) == 0) {
			ra.addFlashAttribute("error", "방을 나가는데 실패했습니다");
		} else {
			sseService.participantUpdate(orderRoom.getRoomIdx());
			return "redirect:/main";
		}
		return String.format("redirect:/rooms/code/%s", roomCode);
	}

	@PostMapping("/rooms/code/{room_code}/kick")
	public String kickRoom(RedirectAttributes ra, @RequestParam(defaultValue = "0") int kickMemberIdx,
			@PathVariable("room_code") String roomCode, @RequestAttribute("orderRoom") OrderRoom orderRoom,
			@SessionAttribute("member") Member member) {
		if (orderRoom.getLeaderMemberIdx() != member.getMemberIdx()) {
			ra.addFlashAttribute("error", "잘못된 접근입니다");
			return "redirect:/main";
		}
		RoomParticipant targetParticipant = orderRoomService.findJoinedRoomMember(orderRoom.getRoomIdx(),
				kickMemberIdx);
		if (orderRoom.isJoinLocked() || kickMemberIdx == orderRoom.getLeaderMemberIdx() || targetParticipant == null
				|| orderRoomService.leave(targetParticipant.getRoomParticipantIdx()) == 0) {
			ra.addFlashAttribute("error", "사용자를 강퇴하는 중 오류가 발생했습니다");
		}
		sseService.participantUpdate(orderRoom.getRoomIdx());
		return String.format("redirect:/rooms/code/%s", roomCode);
	}

	@PostMapping("/rooms/code/{room_code}/close")
	public String closeRoom(RedirectAttributes ra, @PathVariable("room_code") String roomCode,
			@RequestAttribute("orderRoom") OrderRoom orderRoom, @SessionAttribute("member") Member member) {
		if (orderRoom.getLeaderMemberIdx() != member.getMemberIdx()) {
			ra.addFlashAttribute("error", "잘못된 접근입니다");
			return "redirect:/main";
		}
		if (orderRoomService.close(orderRoom.getRoomIdx()) == 0) {
			ra.addFlashAttribute("error", "방을 종료하는 중 오류가 발생했습니다");
			return String.format("redirect:/rooms/code/%s", roomCode);
		}
		sseService.cancelRoom(orderRoom.getRoomIdx());
		return "redirect:/main";
	}

	@PostMapping("/rooms/code/{room_code}/cancel")
	public String cancelRoom(RedirectAttributes ra, @PathVariable("room_code") String roomCode,
			@RequestAttribute("orderRoom") OrderRoom orderRoom, @SessionAttribute("member") Member member) {
		if (orderRoom.getLeaderMemberIdx() != member.getMemberIdx()) {
			ra.addFlashAttribute("error", "잘못된 접근입니다");
			return "redirect:/main";
		}
		if (orderRoomService.cancel(orderRoom.getRoomIdx()) == 0) {
			ra.addFlashAttribute("error", "방을 삭제하는 중 오류가 발생했습니다");
			return String.format("redirect:/rooms/code/%s", roomCode);
		}
		sseService.cancelRoom(orderRoom.getRoomIdx());
		return "redirect:/main";
	}

	@PostMapping("/rooms/code/{room_code}/checkout")
	public String checkout(RedirectAttributes ra, @PathVariable("room_code") String roomCode,
	        @RequestAttribute("orderRoom") OrderRoom orderRoom, @SessionAttribute("member") Member member) {
		if (orderRoom.getLeaderMemberIdx() != member.getMemberIdx()) {
			ra.addFlashAttribute("error", "잘못된 접근입니다");
			return String.format("redirect:/rooms/code/%s", roomCode);
		}
		if (!orderRoomService.findNotSelected(orderRoom.getRoomIdx()).isEmpty()) {
			ra.addFlashAttribute("error", "아직 선택이 완료되지 않은 참여자가 있습니다");
			return String.format("redirect:/rooms/code/%s", roomCode);
		}
		Store store = storeService.getStoreByIdx(orderRoom.getStoreIdx());
		if (store == null) {
			ra.addFlashAttribute("error", "가게 정보를 찾을 수 없습니다");
			return String.format("redirect:/rooms/code/%s", roomCode);
		}
		int activeRoomAmount = groupCartItemService.findRoomAmount(orderRoom.getRoomIdx());
		boolean hasActiveCart = groupCartItemService.findRoomMemberAmount(orderRoom.getRoomIdx()).stream()
				.anyMatch(groupCartItem -> groupCartItem.getItemTotalAmount() > 0);
		if (!hasActiveCart || activeRoomAmount <= 0) {
			ra.addFlashAttribute("error", "활성 참여자의 주문이 없어 결제를 진행할 수 없습니다");
			return String.format("redirect:/rooms/code/%s", roomCode);
		}
		if (activeRoomAmount < store.getMinimumOrderAmount()) {
			ra.addFlashAttribute("error", "최소주문금액을 충족해야 결제를 진행할 수 있습니다");
			return String.format("redirect:/rooms/code/%s", roomCode);
		}
		Map<String, Object> res;
		try {
			res = transactionService.beginPayment(orderRoom);
		} catch (Exception e) {
		    log.error(
		        "checkout failed. roomCode={}, roomIdx={}, roomStatus={}, leaderMemberIdx={}, selectedDeliveryAddressIdx={}",
		        roomCode,
		        orderRoom.getRoomIdx(),
		        orderRoom.getRoomStatus(),
		        orderRoom.getLeaderMemberIdx(),
		        orderRoom.getSelectedDeliveryAddressIdx(),
		        e
		    );
		    ra.addFlashAttribute("error", "오류가 발생하여 결제화면으로 넘어가지 못했습니다");
		    return String.format("redirect:/rooms/code/%s", roomCode);
		}
		GroupOrder groupOrder = (GroupOrder) res.get("groupOrder");
		orderRoomTimer.start(groupOrder.getOrderIdx(), orderRoom.getExpiresAt());
		safeRoomSse("beginOrder", () -> sseService.beginOrder(orderRoom.getRoomIdx(), groupOrder.getOrderIdx()));
		return String.format("redirect:/orders/%d/payment", groupOrder.getOrderIdx());
	}
	
	private void safeRoomSse(String action, Runnable task) {
	    try {
	        task.run();
	    } catch (Exception e) {
	        log.warn("SSE push skipped. action={}", action, e);
	    }
	}
	
	
	
}

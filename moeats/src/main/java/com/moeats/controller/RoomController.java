package com.moeats.controller;

import java.util.List;
import java.util.Map;
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
import com.moeats.domain.StoreMenu;
import com.moeats.service.StoreMenuService;
import com.moeats.services.GroupCartItemService;
import com.moeats.services.OrderMemberQueryService;
import com.moeats.services.MenuService;
import com.moeats.services.OrderRoomService;
import com.moeats.services.TransactionService;
import com.moeats.services.sse.SSEService;
import com.moeats.timer.OrderRoomTimer;

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
	OrderRoomTimer orderRoomTimer;

	public record MemberParticipant(RoomParticipant roomParticipant, Member member) {}

	public record CartItem(GroupCartItem groupCartItem, StoreMenu storeMenu) {}

	public record MemberItem(RoomParticipant roomParticipant, Member member, List<CartItem> items, Integer totalAmount) {}

	@GetMapping("/rooms/new")
	public String roomCreateForm(
			Model model,
			@RequestParam(name = "storeIdx", defaultValue = "0") int storeIdx) {
		model.addAttribute("storeIdx", storeIdx);
		return "room-create";
	}

	@PostMapping("/rooms")
	public String roomCreate(
			RedirectAttributes ra,
			OrderRoom orderRoom,
			@SessionAttribute("member") Member member) {
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
	public String codeRoom(
			RedirectAttributes ra,
			Model model,
			@PathVariable("room_code") String roomCode,
			@SessionAttribute("member") Member member) {
		OrderRoom orderRoom = orderRoomService.findByCode(roomCode);
		if (orderRoom == null) {
			ra.addFlashAttribute("error", "해당하는 방을 찾을 수 없습니다");
			return "redirect:/rooms/join";
		}
		// 방에 참가되지 않으면 자동으로 참가
		// 방 생성시 participantRole을 가져오는 로직 오류로 수정함. -영훈
		if (orderRoomService.findRoomMember(orderRoom.getRoomIdx(), member.getMemberIdx()) == null) {
		    RoomParticipant roomParticipant = new RoomParticipant();
		    roomParticipant.setRoomIdx(orderRoom.getRoomIdx());
		    roomParticipant.setMemberIdx(member.getMemberIdx());
		    // 방 생성 시에는 무조건 leader 이기 때문에 아래 추가.
		    if (orderRoom.getLeaderMemberIdx() == member.getMemberIdx()) {
		        roomParticipant.setParticipantRole("LEADER");
		    } else {
		        roomParticipant.setParticipantRole("PARTICIPANT");
		    }

		    // 방에 참가할 수 없으면 redirect
		    if (orderRoom.isJoinLocked() || orderRoomService.join(roomParticipant) == 0) {
		        ra.addFlashAttribute("error", "더이상 방에 참여할 수 없습니다");
		        return "redirect:/rooms/join";
		    }
		}
		/*
		 * if (orderRoomService.findRoomMember(orderRoom.getRoomIdx(),
		 * member.getMemberIdx()) == null) { RoomParticipant roomParticipant = new
		 * RoomParticipant(); roomParticipant.setRoomIdx(orderRoom.getRoomIdx());
		 * roomParticipant.setMemberIdx(member.getMemberIdx());
		 *  // 방에 참가할 수 없으면 redirect
		 * if (orderRoom.isJoinLocked() || orderRoomService.join(roomParticipant) == 0)
		 * { ra.addFlashAttribute("error", "더이상 방에 참여할 수 없습니다"); return
		 * "redirect:/rooms/join"; } }
		 */
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
					return new MemberItem(
							roomParticipantMap.get(roomMember.getMemberIdx()),
							roomMember,
							cartItems,
							cartItems.stream().collect(Collectors.summingInt(cartItem -> cartItem.groupCartItem().getItemTotalAmount())));
				}).toList();
		List<MemberParticipant> roomParticipants = memberService.findByIdxs(roomParticipantMap.keySet()).stream()
				.map(roomMember -> new MemberParticipant(roomParticipantMap.get(roomMember.getMemberIdx()), roomMember)).toList();

		// myState.selectionStatus = "SELECTED", "NOT_SELECTED"
		// myState.participantRole = "LEADER", "PARTICIPANT"
		// otherCartItems.totalAmount = 해당 사람의 장바구니 금액
		// roomParticipants.roomParticipant.selectionStatus = "SELECTED", "NOT_SELECTED"
		
		model.addAttribute("myState", myState);
		model.addAttribute("myCartItems", myCartItems);
		model.addAttribute("otherCartItems", otherCartItems);
		model.addAttribute("roomParticipants", roomParticipants);
		int myCartTotal = myCartItems.stream()
		        .mapToInt(cartItem -> cartItem.groupCartItem().getItemTotalAmount())
		        .sum();

		int otherCartTotal = otherCartItems.stream()
		        .mapToInt(memberItem -> memberItem.totalAmount() == null ? 0 : memberItem.totalAmount())
		        .sum();

		int participantCount = 1 + roomParticipants.size();

		long completedCount = (myState != null && "SELECTED".equals(myState.getSelectionStatus()) ? 1 : 0)
		        + roomParticipants.stream()
		            .filter(p -> "SELECTED".equals(p.roomParticipant().getSelectionStatus()))
		            .count();

		boolean isLeader = myState != null && "LEADER".equals(myState.getParticipantRole());
		boolean allSelected = participantCount > 0 && completedCount == participantCount;

		model.addAttribute("orderRoom", orderRoom);
		model.addAttribute("isLeader", isLeader);
		model.addAttribute("participantCount", participantCount);
		model.addAttribute("completedCount", completedCount);
		model.addAttribute("myCartTotal", myCartTotal);
		model.addAttribute("roomGrandTotal", myCartTotal + otherCartTotal);
		model.addAttribute("allSelected", allSelected);
		return "room-detail";
	}
	
	@GetMapping("/rooms/join")
	public String joinRoom() {
		return "room-join";
	}
	
	@GetMapping("/rooms/code/{room_code}/cart")
	public String roomCart(
	        @PathVariable("room_code") String roomCode,
	        @RequestAttribute("orderRoom") OrderRoom orderRoom,
	        @SessionAttribute("member") Member member,
	        Model model) {

	    List<StoreMenu> menuList = storeMenuService.menuListForUser(orderRoom.getStoreIdx());
	    model.addAttribute("menuList", menuList);
	    model.addAttribute("orderRoom", orderRoom);

	    return "room-cart";
	}

	
	
	
	// 이후는 Intercepter에서 코드를 처리함
	@PostMapping("/rooms/code/{room_code}/confirm")
	public String confirmRoom(
			Model model,
			@PathVariable("room_code") String roomCode,
			@RequestAttribute("orderRoom") OrderRoom orderRoom,
			@SessionAttribute("member") Member member) {

		List<GroupCartItem> groupCartItems = groupCartItemService.findRoomMember(orderRoom.getRoomIdx(),member.getMemberIdx());
		Map<Integer, StoreMenu> storeMenuMap = menuService
				.findByIdxs(groupCartItems.stream().map(GroupCartItem::getMenuIdx).toList()).stream()
				.collect(Collectors.toMap(StoreMenu::getMenuIdx, storeMenu -> storeMenu));
		
		RoomParticipant myState = orderRoomService.findRoomMember(orderRoom.getRoomIdx(),member.getMemberIdx());
		List<CartItem> myCartItems = groupCartItems.stream()
				.map(groupCartItem -> new CartItem(groupCartItem, storeMenuMap.get(groupCartItem.getMenuIdx()))).toList();

		model.addAttribute("myState", myState);
		model.addAttribute("myCartItems", myCartItems);
		
		return "room-confirm";
	}
	
	@PostMapping("/rooms/code/{room_code}/select")
	public String selectRoom(
			RedirectAttributes ra,
			@PathVariable("room_code") String roomCode,
			@RequestAttribute("orderRoom") OrderRoom orderRoom,
			@SessionAttribute("member") Member member) {
		RoomParticipant roomParticipant = orderRoomService.findRoomMember(orderRoom.getRoomIdx(),member.getMemberIdx());
		if (	orderRoom.isJoinLocked()
				|| roomParticipant == null
				|| orderRoomService.setSelect(roomParticipant.getRoomParticipantIdx()) == 0) {
			ra.addFlashAttribute("error", "선택을 확정하는 중 오류가 발생했습니다");
			return String.format("redirect:/rooms/code/%s/confirm",roomCode);
		}
		return String.format("redirect:/rooms/code/%s",roomCode);
	}
	
	@PostMapping("/rooms/code/{room_code}/unselect")
	public String unselectRoom(
			RedirectAttributes ra,
			@PathVariable("room_code") String roomCode,
			@RequestAttribute("orderRoom") OrderRoom orderRoom,
			@SessionAttribute("member") Member member) {
		RoomParticipant roomParticipant = orderRoomService.findRoomMember(orderRoom.getRoomIdx(),member.getMemberIdx());
		if (	orderRoom.isJoinLocked()
				|| roomParticipant == null
				|| orderRoomService.unselect(roomParticipant.getRoomParticipantIdx()) == 0) {
				ra.addFlashAttribute("error", "선택을 취소하는 중 오류가 발생했습니다");
			return String.format("redirect:/rooms/code/%s",roomCode);
		}
		return String.format("redirect:/rooms/code/%s/confirm",roomCode);
	}
	
	@PostMapping("/rooms/code/{room_code}/leave")
	public String leaveRoom(
			RedirectAttributes ra,
			@PathVariable("room_code") String roomCode,
			@RequestAttribute("orderRoom") OrderRoom orderRoom,
			@SessionAttribute("member") Member member) {
		RoomParticipant roomParticipant = orderRoomService.findRoomMember(orderRoom.getRoomIdx(),member.getMemberIdx());
		if (roomParticipant == null) {
			ra.addFlashAttribute("error", "잘못된 접근입니다");
			return "redirect:/main";
		}
		if (orderRoom.getLeaderMemberIdx() == member.getMemberIdx())
			ra.addFlashAttribute("error", "방장은 방을 나갈 수 없습니다");
		else if (orderRoomService.leave(roomParticipant.getRoomParticipantIdx()) == 0)
			ra.addFlashAttribute("error", "방을 나가는데 실패했습니다");
		else
			return "redirect:/main";
		return String.format("redirect:/rooms/code/%s",roomCode);
	}
	
	@PostMapping("/rooms/code/{room_code}/kick")
	public String kickRoom(
			RedirectAttributes ra,
			@RequestParam(defaultValue = "0") int kickMemberIdx,
			@PathVariable("room_code") String roomCode,
			@RequestAttribute("orderRoom") OrderRoom orderRoom,
			@SessionAttribute("member") Member member) {
		if (orderRoom.getLeaderMemberIdx() != member.getMemberIdx()) {
			ra.addFlashAttribute("error", "잘못된 접근입니다");
			return "redirect:/main";
		}
		RoomParticipant targetParticipant = orderRoomService.findRoomMember(orderRoom.getRoomIdx(), kickMemberIdx);
		if (	orderRoom.isJoinLocked()
				|| kickMemberIdx == orderRoom.getLeaderMemberIdx()
				|| targetParticipant == null
				|| orderRoomService.leave(targetParticipant.getRoomParticipantIdx()) == 0) {
			ra.addFlashAttribute("error", "사용자를 강퇴하는 중 오류가 발생했습니다");
		}
		return String.format("redirect:/rooms/code/%s",roomCode);
	}
	
	@PostMapping("/rooms/code/{room_code}/cancel")
	public String cancelRoom(
			RedirectAttributes ra,
			@PathVariable("room_code") String roomCode,
			@RequestAttribute("orderRoom") OrderRoom orderRoom,
			@SessionAttribute("member") Member member) {
		if (orderRoom.getLeaderMemberIdx() != member.getMemberIdx()) {
			ra.addFlashAttribute("error", "잘못된 접근입니다");
			return "redirect:/main";
		}
		if (orderRoomService.cancel(orderRoom.getRoomIdx()) == 0) {
			ra.addFlashAttribute("error", "방을 삭제하는 중 오류가 발생했습니다");
			return String.format("redirect:/rooms/code/%s",roomCode);
		}
		sseService.cancelRoom(orderRoom.getRoomIdx());
		return "redirect:/main";
	}
	
	@PostMapping("/rooms/code/{room_code}/checkout")
	public String checkout(
			RedirectAttributes ra,
			@PathVariable("room_code") String roomCode,
			@RequestParam(defaultValue = "0") int representativeMemberIdx,
			@RequestAttribute("orderRoom") OrderRoom orderRoom,
			@SessionAttribute("member") Member member) {
		if (orderRoom.getLeaderMemberIdx() != member.getMemberIdx()) {
			ra.addFlashAttribute("error", "잘못된 접근입니다");
			return String.format("redirect:/rooms/code/%s", roomCode);
		}
		if (!orderRoomService.findNotSelected(orderRoom.getRoomIdx()).isEmpty()) {
			ra.addFlashAttribute("error", "아직 선택이 완료되지 않은 참여자가 있습니다");
			return String.format("redirect:/rooms/code/%s", roomCode);
		}
		Map<String, Object> res;
		try {
			res = transactionService.beginPayment(orderRoom);
		} catch (Exception e) {
			ra.addFlashAttribute("error", "오류가 발생하여 결제화면으로 넘어가지 못했습니다");
			return String.format("redirect:/rooms/code/%s", roomCode);
			// e.printStackTrace();
		}
		GroupOrder groupOrder = (GroupOrder) res.get("groupOrder");
		orderRoomTimer.start(groupOrder.getOrderIdx(), orderRoom.getExpiresAt());
		sseService.beginOrder(orderRoom.getRoomIdx());
		return String.format("redirect:/orders/%d/payment", groupOrder.getOrderIdx());
	}
}

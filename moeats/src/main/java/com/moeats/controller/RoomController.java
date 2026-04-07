package com.moeats.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
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
import com.moeats.services.GroupCartItemService;
import com.moeats.services.MenuService;
import com.moeats.services.OrderMemberQueryService;
import com.moeats.services.OrderRoomService;
import com.moeats.services.TransactionService;
import com.moeats.services.sse.SSEService;
import com.moeats.timer.OrderRoomTimer;

@Controller
public class RoomController {

	private final OrderMemberQueryService memberService;
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
	OrderRoomTimer orderRoomTimer;

	RoomController(OrderMemberQueryService memberService) {
		this.memberService = memberService;
	}

	@GetMapping("/rooms/new")
	public String roomCreateForm() {
		return "room-create";
	}

	@PostMapping("/rooms")
	public String roomCreate(
			RedirectAttributes ra,
			@ModelAttribute OrderRoom orderRoom,
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
			ra.addFlashAttribute("error","방을 생성하는 중 오류가 발생했습니다");
			return "redirect:/rooms/new";
		}
		RoomParticipant roomParticipant = new RoomParticipant();
		roomParticipant.setRoomIdx(orderRoom.getRoomIdx());
		roomParticipant.setMemberIdx(member.getMemberIdx());
		roomParticipant.setParticipantRole("LEADER");
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
			ra.addFlashAttribute("error","해당하는 방을 찾을 수 없습니다");
			return "redirect:/rooms/join";
		}
		// 방에 참가되지 않으면 자동으로 참가
		if (orderRoomService.findRoomMember(orderRoom.getRoomIdx(), member.getMemberIdx()) == null) {
			RoomParticipant roomParticipant = new RoomParticipant();
			roomParticipant.setRoomIdx(orderRoom.getRoomIdx());
			roomParticipant.setMemberIdx(member.getMemberIdx());
			roomParticipant.setParticipantRole("PARTICIPANT");
			// 방에 참가할 수 없으면 redirect
			if (orderRoom.isJoinLocked()
					|| orderRoomService.join(roomParticipant) == 0) {
				ra.addFlashAttribute("error","더이상 방에 참여할 수 없습니다");
				return "redirect:/rooms/join";
			}
		}

		final record MemberParticipant(RoomParticipant roomParticipant, Member member) {}

		Map<Integer, RoomParticipant> roomParticipantMap = orderRoomService.findByRoom(orderRoom.getRoomIdx()).stream()
				.collect(Collectors.toMap(RoomParticipant::getMemberIdx, roomParticipant -> roomParticipant));
		List<MemberParticipant> roomParticipants = memberService.findByIdxs(roomParticipantMap.keySet()).stream().map(
				roomMember -> (new MemberParticipant(roomParticipantMap.get(roomMember.getMemberIdx()), roomMember)))
				.toList();

		model.addAttribute("orderRoom",orderRoom);
		model.addAttribute("roomParticipants",roomParticipants);
		return "room-detail";
	}

	@GetMapping("/rooms/join")
	public String joinRoom(Model model, String roomCode) {
		model.addAttribute("roomCode",roomCode);
		return "room-join";
	}

	// 이후는 Intercepter에서 코드를 처리함
	@PostMapping("/rooms/code/{room_code}/leave")
	public String leaveRoom(
			RedirectAttributes ra,
			@PathVariable("room_code") String roomCode,
			@RequestAttribute("orderRoom") OrderRoom orderRoom,
			@SessionAttribute("member") Member member) {
		RoomParticipant roomParticipant = orderRoomService.findRoomMember(orderRoom.getRoomIdx(),
				member.getMemberIdx());
		if (roomParticipant == null) {
			ra.addFlashAttribute("error","잘못된 접근입니다");
			return "redirect:/main";
		}
		if (orderRoom.getLeaderMemberIdx() == member.getMemberIdx())
			ra.addFlashAttribute("error","방장은 방을 나갈 수 없습니다");
		else if (orderRoomService.leave(roomParticipant.getRoomParticipantIdx()) == 0)
			ra.addFlashAttribute("error","방을 나가는데 실패했습니다");
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
			ra.addFlashAttribute("error","잘못된 접근입니다");
			return "redirect:/main";
		}

		RoomParticipant targetParticipant = orderRoomService.findRoomMember(orderRoom.getRoomIdx(), kickMemberIdx);

		if (orderRoom.isJoinLocked()
				|| kickMemberIdx == orderRoom.getLeaderMemberIdx()
				|| targetParticipant == null
				|| orderRoomService.leave(targetParticipant.getRoomParticipantIdx()) == 0) {
			ra.addFlashAttribute("error","사용자를 강퇴하는 중 오류가 발생했습니다");
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
			ra.addFlashAttribute("error","잘못된 접근입니다");
			return "redirect:/main";
		}
		if (orderRoomService.cancel(orderRoom.getRoomIdx()) == 0) {
			ra.addFlashAttribute("error","방을 삭제하는 중 오류가 발생했습니다");
			return String.format("redirect:/rooms/code/%s",roomCode);
		}
		sseService.cancelRoom(orderRoom.getRoomIdx());
		return "redirect:/main";
	}

	@GetMapping("/rooms/code/{room_code}/cart")
	public String roomCart(
			RedirectAttributes ra, Model model,
			@RequestAttribute("orderRoom") OrderRoom orderRoom,
			@SessionAttribute("member") Member member) {

		record CartItem(GroupCartItem groupCartItem, StoreMenu storeMenu) {}
		record MemberItem(RoomParticipant roomParticipant, Member member, List<CartItem> items) {}

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
				.map(roomMember -> new MemberItem(roomParticipantMap.get(roomMember.getMemberIdx()), roomMember,
						cartItemMap.getOrDefault(roomMember.getMemberIdx(), List.of())))
				.toList();

		model.addAttribute("orderRoom",orderRoom);
		model.addAttribute("myState",myState);
		model.addAttribute("myCartItems",myCartItems);
		model.addAttribute("otherCartItems",otherCartItems);
		return "room-cart";
	}

	@PostMapping("/rooms/code/{room_code}/cart")
	public String roomCartAdd(
			RedirectAttributes ra,
			@ModelAttribute GroupCartItem groupCartItem,
			@PathVariable("room_code") String roomCode,
			@RequestAttribute("orderRoom") OrderRoom orderRoom,
			@SessionAttribute("member") Member member) {
		groupCartItem.setMemberIdx(member.getMemberIdx());
		groupCartItem.setRoomIdx(orderRoom.getRoomIdx());
		if (orderRoom.isJoinLocked()
				|| groupCartItemService.insert(groupCartItem) == 0)
			ra.addFlashAttribute("error","메뉴를 추가하는 중 오류가 발생했습니다");
		else
			revertSelectionIfNeeded(orderRoom, member);

		return String.format("redirect:/rooms/code/%s/cart",roomCode);
	}

	@GetMapping("/rooms/code/{room_code}/cart/items/{cart_item_idx}/edit")
	public String cartItemEditForm(
			RedirectAttributes ra,
			Model model,
			@PathVariable("room_code") String roomCode,
			@PathVariable("cart_item_idx") int cartItemIdx,
			@RequestAttribute("orderRoom") OrderRoom orderRoom,
			@SessionAttribute("member") Member member) {
		GroupCartItem groupCartItem = groupCartItemService.findByIdx(cartItemIdx);
		if (orderRoom.isJoinLocked()
				|| groupCartItem == null
				|| groupCartItem.getRoomIdx() != orderRoom.getRoomIdx()
				|| groupCartItem.getMemberIdx() != member.getMemberIdx()) {
			ra.addFlashAttribute("error","해당 장바구니 항목을 찾을 수 없거나 잘못된 접근입니다");
			return String.format("redirect:/rooms/code/%s/cart",roomCode);
		}
		StoreMenu storeMenu = menuService.findByIdx(groupCartItem.getMenuIdx());
		model.addAttribute("groupCartItem",groupCartItem);
		model.addAttribute("storeMenu",storeMenu);
		return "cart-item-edit";
	}

	@PostMapping("/rooms/code/{room_code}/cart/items/{cart_item_idx}/edit")
	public String cartItemEdit(
			RedirectAttributes ra,
			GroupCartItem groupCartItem,
			@PathVariable("room_code") String roomCode,
			@PathVariable("cart_item_idx") int cartItemIdx,
			@RequestAttribute("orderRoom") OrderRoom orderRoom,
			@SessionAttribute("member") Member member) {
		GroupCartItem preExist = groupCartItemService.findByIdx(cartItemIdx);
		groupCartItem.setCartItemIdx(cartItemIdx);
		groupCartItem.setRoomIdx(orderRoom.getRoomIdx());
		groupCartItem.setMemberIdx(member.getMemberIdx());
		if (orderRoom.isJoinLocked()
				|| preExist == null
				|| preExist.getRoomIdx() != orderRoom.getRoomIdx()
				|| preExist.getMemberIdx() != member.getMemberIdx()
				|| groupCartItemService.update(groupCartItem) == 0)
			ra.addFlashAttribute("error","해당 장바구니 항목을 수정하는 중 오류가 발생했습니다");
		else
			revertSelectionIfNeeded(orderRoom, member);

		return String.format("redirect:/rooms/code/%s/cart",roomCode);
	}

	@PostMapping("/rooms/code/{room_code}/cart/items/{cart_item_idx}/delete")
	public String cartItemDelete(
			RedirectAttributes ra,
			@PathVariable("room_code") String roomCode,
			@PathVariable("cart_item_idx") int cartItemIdx,
			@RequestAttribute("orderRoom") OrderRoom orderRoom,
			@SessionAttribute("member") Member member) {
		GroupCartItem groupCartItem = groupCartItemService.findByIdx(cartItemIdx);
		if (orderRoom.isJoinLocked()
				|| groupCartItem == null
				|| groupCartItem.getRoomIdx() != orderRoom.getRoomIdx()
				|| groupCartItem.getMemberIdx() != member.getMemberIdx()
				|| groupCartItemService.remove(cartItemIdx) == 0)
			ra.addFlashAttribute("error","해당 장바구니 항목을 제거하는 중 오류가 발생했습니다");
		else
			revertSelectionIfNeeded(orderRoom, member);

		return String.format("redirect:/rooms/code/%s/cart",roomCode);
	}

	@PostMapping("/rooms/code/{room_code}/checkout")
	public String checkout(
			RedirectAttributes ra,
			@PathVariable("room_code") String roomCode,
			@RequestAttribute("orderRoom") OrderRoom orderRoom,
			@SessionAttribute("member") Member member) {
		if (orderRoom.getLeaderMemberIdx() != member.getMemberIdx()) {
			ra.addFlashAttribute("error","잘못된 접근입니다");
			return String.format("redirect:/rooms/code/%s",roomCode);
		}
		if (!orderRoomService.findNotSelected(orderRoom.getRoomIdx()).isEmpty()) {
			ra.addFlashAttribute("error","아직 선택이 완료되지 않은 참여자가 있습니다");
			return String.format("redirect:/rooms/code/%s",roomCode);
		}
		Map<String, Object> res;
		try {
			res = transactionService.beginPayment(orderRoom);
		} catch (Exception e) {
			ra.addFlashAttribute("error","오류가 발생하여 결제화면으로 넘어가지 못했습니다");
			return String.format("redirect:/rooms/code/%s",roomCode);
		}
		GroupOrder groupOrder = (GroupOrder) res.get("groupOrder");
		orderRoomTimer.start(groupOrder.getOrderIdx(),orderRoom.getExpiresAt());
		sseService.beginOrder(orderRoom.getRoomIdx());
		return String.format("redirect:/orders/%d/payment",groupOrder.getOrderIdx());
	}
	
	private void revertSelectionIfNeeded(OrderRoom orderRoom, Member member) {
		RoomParticipant roomParticipant = orderRoomService.findRoomMember(orderRoom.getRoomIdx(),member.getMemberIdx());
		if (roomParticipant != null)
			orderRoomService.unselect(roomParticipant.getRoomParticipantIdx());
	}
}

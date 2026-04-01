package com.moeats.controller;

import java.util.ArrayList;
import java.util.LinkedHashMap;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.moeats.domain.GroupCartItem;
import com.moeats.domain.GroupOrder;
import com.moeats.domain.Member;
import com.moeats.domain.OrderRoom;
import com.moeats.domain.RoomParticipant;
import com.moeats.services.GroupCartItemService;
import com.moeats.services.OrderRoomService;
import com.moeats.services.TransactionService;
import com.moeats.timer.OrderRoomTimer;

@Controller
public class RoomController {
	@Autowired
	OrderRoomService orderRoomService;
	@Autowired
	GroupCartItemService groupCartItemService;
	@Autowired
	TransactionService transactionService;
	
	@Autowired
	OrderRoomTimer orderRoomTimer;
	
	@GetMapping("/rooms/new")
	public String roomCreateForm() {
		return "room-create";
	}
	@PostMapping("/rooms")
	public String roomCreate(
			RedirectAttributes ra,
			@RequestBody OrderRoom orderRoom,
			@SessionAttribute("member") Member member) {
		orderRoom.setLeaderMemberIdx(member.getMemberIdx());
		String code = null;
		int res = 0;
		for( int trial = 0; trial < 3 && res==0 ; trial++ ) {
			code = orderRoomService.createCode();
			orderRoom.setRoomCode(code);
			res = orderRoomService.insert(orderRoom);
		}
		if(res==0) {
			ra.addFlashAttribute("error","방을 생성하는 중 오류가 발생했습니다");
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
		if( orderRoom==null) {
			ra.addFlashAttribute("error","해당하는 방을 찾을 수 없습니다");
			return "redirect:/rooms/join";
		}
		// 방에 참가되지 않으면 자동으로 참가
		if( orderRoomService.findRoomMember(orderRoom.getRoomIdx(),member.getMemberIdx())==null ) {
			RoomParticipant roomParticipant = new RoomParticipant();
			roomParticipant.setRoomIdx(orderRoom.getRoomIdx());
			roomParticipant.setMemberIdx(member.getMemberIdx());
			// 방에 참가할 수 없으면 redirect
			if( orderRoom.isJoinLocked() || orderRoomService.join(roomParticipant)==0 ) {
				ra.addFlashAttribute("error","더이상 방에 참여할 수 없습니다");
				return "redirect:/rooms/join";
			}
		}
		List<RoomParticipant> roomParticipants = orderRoomService.findParticipantByCode(roomCode);
		model.addAttribute("orderRoom",orderRoom);
		model.addAttribute("roomParticipants",roomParticipants);
		return "room-detail";
	}
	@GetMapping("/rooms/join")
	public String joinRoom(Model model,String roomCode) {
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
		RoomParticipant roomParticipant = orderRoomService.findRoomMember(orderRoom.getRoomIdx(), member.getMemberIdx());
		if( roomParticipant==null ) {
			ra.addFlashAttribute("error", "잘못된 접근입니다");
			return "redirect:/main";
		}
		if( orderRoom.getLeaderMemberIdx()==member.getMemberIdx() )
			ra.addFlashAttribute("error", "방장은 방을 나갈 수 없습니다");
		else if( orderRoomService.leave(roomParticipant.getRoomParticipantIdx())==0 )
			ra.addFlashAttribute("error", "방을 나가는데 실패했습니다");
		else
			return "redirect:/main";
		return "redirect:/rooms/code/"+roomCode;
	}
	@PostMapping("/rooms/code/{room_code}/kick")
	public String kickRoom(
			RedirectAttributes ra,
			@RequestParam(defaultValue = "0") int kickMemberIdx,
			@PathVariable("room_code") String roomCode,
			@RequestAttribute("orderRoom") OrderRoom orderRoom,
			@SessionAttribute("member") Member member) {
		if( orderRoom.getLeaderMemberIdx()!=member.getMemberIdx() ) {
			ra.addFlashAttribute("error", "잘못된 접근입니다");
			return "redirect:/main";
		}
		if( orderRoom.isJoinLocked() || kickMemberIdx==orderRoom.getLeaderMemberIdx() || orderRoomService.leave(kickMemberIdx)==0 )
			ra.addFlashAttribute("error", "사용자를 강퇴하는 중 오류가 발생했습니다");
		return "redirect:/rooms/code/"+roomCode;
	}
	@PostMapping("/rooms/code/{room_code}/select")
	public String selectRoom(
			RedirectAttributes ra,
			@PathVariable("room_code") String roomCode,
			@RequestAttribute("orderRoom") OrderRoom orderRoom,
			@SessionAttribute("member") Member member) {
		if( orderRoom.isJoinLocked() || orderRoomService.setSelect(member.getMemberIdx())==0 )
			ra.addFlashAttribute("error", "주문을 확정하는 중 오류가 발생했습니다");
		return "redirect:/rooms/code/"+roomCode;
	}
	@PostMapping("/rooms/code/{room_code}/unselect")
	public String unselectRoom(
			RedirectAttributes ra,
			@PathVariable("room_code") String roomCode,
			@RequestAttribute("orderRoom") OrderRoom orderRoom,
			@SessionAttribute("member") Member member) {
		if( orderRoom.isJoinLocked() || orderRoomService.unselect(member.getMemberIdx())==0 )
			ra.addFlashAttribute("error", "주문을 확정을 취소하는 중 오류가 발생했습니다");
		return "redirect:/rooms/code/"+roomCode;
	}
	@PostMapping("/rooms/code/{room_code}/cancel")
	public String cancelRoom(
			RedirectAttributes ra,
			@PathVariable("room_code") String roomCode,
			@RequestAttribute("orderRoom") OrderRoom orderRoom,
			@SessionAttribute("member") Member member) {
		if( orderRoom.getLeaderMemberIdx()!=member.getMemberIdx() ) {
			ra.addFlashAttribute("error", "잘못된 접근입니다");
			return "redirect:/main";
		}
		if( orderRoomService.cancel(orderRoom.getRoomIdx())==0 ) {
			ra.addFlashAttribute("error", "방을 삭제하는 중 오류가 발생했습니다");
			return "redirect:/rooms/code/"+roomCode;
		}else
			orderRoomTimer.stop(orderRoom.getRoomIdx());
		return "redirect:/main";
	}
	@GetMapping("/rooms/code/{room_code}/cart")
	public String roomCart(
			RedirectAttributes ra,
			Model model,
			@RequestAttribute("orderRoom") OrderRoom orderRoom,
			@SessionAttribute("member") Member member) {
		List<GroupCartItem> myCartItems = null;
		Map<RoomParticipant,List<GroupCartItem>> otherCartItems = new LinkedHashMap<>();
		Map<Integer,List<GroupCartItem>> groupCartitems = groupCartItemService.findByRoom(orderRoom.getRoomIdx()).stream().collect(Collectors.groupingBy(GroupCartItem::getMemberIdx));
		for(RoomParticipant roomParticipant : orderRoomService.findByRoom(orderRoom.getRoomIdx())){
			if(roomParticipant.getMemberIdx()==member.getMemberIdx())
				myCartItems = groupCartitems.getOrDefault(roomParticipant.getMemberIdx(), new ArrayList<>());
			otherCartItems.put(roomParticipant,groupCartitems.getOrDefault(roomParticipant.getMemberIdx(), List.of()));
		}
		model.addAttribute("orderRoom",orderRoom);
		model.addAttribute("myCartItems",myCartItems);
		model.addAttribute("otherCartItems",otherCartItems);
		return "room-cart";
	}
	@PostMapping("/rooms/code/{room_code}/cart")
	public String roomCartAdd(
			RedirectAttributes ra,
			GroupCartItem groupCartItem,
			@PathVariable("room_code") String roomCode,
			@RequestAttribute("orderRoom") OrderRoom orderRoom,
			@SessionAttribute("member") Member member) {
		groupCartItem.setMemberIdx(member.getMemberIdx());
		groupCartItem.setRoomIdx(orderRoom.getRoomIdx());
		if( orderRoom.isJoinLocked() || groupCartItemService.insert(groupCartItem)==0 )
			ra.addFlashAttribute("error", "메뉴를 추가하는 중 오류가 발생했습니다");
		// GET으로 다시 오게 만든다
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
		if( orderRoom.isJoinLocked() || groupCartItem==null || groupCartItem.getRoomIdx()!=orderRoom.getRoomIdx() || groupCartItem.getMemberIdx()!=member.getMemberIdx() ) {
			ra.addFlashAttribute("error","해당 장바구니 항목을 찾을 수 없거나 잘못된 접근입니다");
			return String.format("redirect:/rooms/code/%s/cart",roomCode);
		}
		model.addAttribute("groupCartItem", groupCartItem);
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
		if( orderRoom.isJoinLocked() || preExist==null || preExist.getRoomIdx()!=orderRoom.getRoomIdx() || preExist.getMemberIdx()!=member.getMemberIdx() || groupCartItemService.update(groupCartItem)==0 )
			ra.addFlashAttribute("error","해당 장바구니 항목을 수정하는 중 오류가 발생했습니다");
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
		if( orderRoom.isJoinLocked() || groupCartItem==null || groupCartItem.getRoomIdx()!=orderRoom.getRoomIdx() || groupCartItem.getMemberIdx()!=member.getMemberIdx() || groupCartItemService.remove(cartItemIdx)==0 )
			ra.addFlashAttribute("error","해당 장바구니 항목을 제거하는 중 오류가 발생했습니다");
		return String.format("redirect:/rooms/code/%s/cart",roomCode);
	}
	@PostMapping("/rooms/code/{room_code}/checkout")
	public String checkout(
			RedirectAttributes ra,
			@PathVariable("room_code") String roomCode,
			@RequestParam(defaultValue = "0") int representativeMemberIdx,
			@RequestAttribute("orderRoom") OrderRoom orderRoom,
			@SessionAttribute("member") Member member) {
		if( orderRoom.getLeaderMemberIdx()!=member.getMemberIdx() ) {
			ra.addFlashAttribute("error", "잘못된 접근입니다");
			return String.format("redirect:/rooms/code/%s",roomCode);
		}
		if(!orderRoomService.findNotSelected(orderRoom.getRoomIdx()).isEmpty()) {
			ra.addFlashAttribute("error", "아직 선택이 완료되지 않은 참여자가 있습니다");
			return String.format("redirect:/rooms/code/%s",roomCode);
		}
		Map<String, Object> res;
		try {
			res = transactionService.beginPayment(orderRoom,representativeMemberIdx);
		} catch (Exception e) {
			ra.addFlashAttribute("error","오류가 발생하여 결제화면으로 넘어가지 못했습니다");
			return String.format("redirect:/rooms/code/%s",roomCode);
			//e.printStackTrace();
		}
		GroupOrder groupOrder = (GroupOrder) res.get("groupOrder");
		return String.format("redirect:/orders/%s/payment",groupOrder.getOrderIdx());
	}
}
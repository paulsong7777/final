package com.moeats.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.moeats.MoeatsApplication;
import com.moeats.domain.DeliveryAddress;
import com.moeats.domain.GroupOrder;
import com.moeats.domain.GroupOrderItem;
import com.moeats.domain.OrderDelivery;
import com.moeats.domain.OrderRoom;
import com.moeats.domain.StoreMenu;
import com.moeats.mapper.StoreMenuMapper;
import com.moeats.mappers.GroupCartItemMapper;
import com.moeats.mappers.GroupOrderItemMapper;
import com.moeats.mappers.GroupOrderMapper;
import com.moeats.mappers.OrderDeliveryMapper;

@Service
public class GroupOrderService {
	/**	확정주문과 확정주문된 메뉴를 담당하는 서비스
	 */
	@Autowired
	GroupOrderMapper groupOrderMapper;
	@Autowired
	GroupOrderItemMapper groupOrderItemMapper;
	@Autowired
	OrderDeliveryMapper orderDeliveryMapper;
	
	public record GroupOrderRecord(GroupOrder groupOrder,List<GroupOrderItem> groupOrderItems,OrderDelivery orderDelivery) {}
	
	public GroupOrderRecord findRecordByIdx(int orderIdx) {
		GroupOrder groupOrder = groupOrderMapper.findByIdx(orderIdx);
		return new GroupOrderRecord(
				groupOrder,
				groupOrderItemMapper.findOrderItemAmount(groupOrder.getOrderIdx()),
				orderDeliveryMapper.findByOrder(groupOrder.getOrderIdx()));
	}
	public List<GroupOrderRecord> findRecordByStore(int storeIdx) {
		return groupOrderMapper.findByStore(storeIdx).stream()
				.map(groupOrder->new GroupOrderRecord(
						groupOrder,
						groupOrderItemMapper.findOrderItemAmount(groupOrder.getOrderIdx()),
						orderDeliveryMapper.findByOrder(groupOrder.getOrderIdx()))).toList();
	}
	
	public GroupOrder findByIdx(int orderIdx) {
		return groupOrderMapper.findByIdx(orderIdx);
	}
	public GroupOrder findByRoom(int roomIdx) {
		return groupOrderMapper.findByRoom(roomIdx);
	}
	public int insert(GroupOrder groupOrder) {
		return groupOrderMapper.insert(groupOrder);
	}
//	public int proceed(GroupOrder groupOrder) {
//		if(groupOrder==null)
//			return 0;
//		switch(groupOrder.getOrderStatus()) {
//		case "PAID":
//			return accept(groupOrder.getOrderIdx());
//		case "ACCEPTED":
//			return prepare(groupOrder.getOrderIdx());
//		case "PREPARING":
//			return deliver(groupOrder.getOrderIdx());
//		case "READY":
//			return ready(groupOrder.getOrderIdx());
//		case "DELIVERING":
//			return complete(groupOrder.getOrderIdx());
//		}
//		return 0;
//	}
	// tier1에서 delivery 구현이 안되는 상황이기 때문에 간소화. 영훈
	public int proceed(GroupOrder groupOrder) {
	    if (groupOrder == null) {
	        return 0;
	    }

	    switch (groupOrder.getOrderStatus()) {
	        case "PAID":
	            return accept(groupOrder.getOrderIdx());
	        case "ACCEPTED":
	            return prepare(groupOrder.getOrderIdx());
	        case "PREPARING":
	            return ready(groupOrder.getOrderIdx());
	        case "READY":
	            return complete(groupOrder.getOrderIdx());
	        default:
	            return 0;
	    }
	}
	
	public int accept(int orderIdx) {
		return groupOrderMapper.accept(orderIdx);
	}
	public int prepare(int orderIdx){
		return groupOrderMapper.prepare(orderIdx);
	}
	public int deliver(int orderIdx){
		return groupOrderMapper.deliver(orderIdx);
	}
	public int ready(int orderIdx){
		return groupOrderMapper.ready(orderIdx);
	}
	public int complete(int orderIdx) {
		return groupOrderMapper.complete(orderIdx);
	}
	
	public List<GroupOrderItem> findByOrder(int orderIdx){
		return groupOrderItemMapper.findByOrder(orderIdx);
	}
	public List<GroupOrderItem> findOrderMemberAmount(int orderIdx){
		return groupOrderItemMapper.findOrderMemberAmount(orderIdx);
	}
	public List<GroupOrderItem> findOrderItemAmount(int orderIdx){
		return groupOrderItemMapper.findOrderItemAmount(orderIdx);
	}
	public List<GroupOrderItem> findRoomMemberAmount(int roomIdx){
		GroupOrder groupOrder = groupOrderMapper.findByRoom(roomIdx);
		if(groupOrder==null)
			return new ArrayList<>();
		return groupOrderItemMapper.findOrderMemberAmount(groupOrder.getOrderIdx());
	}
	
	public OrderDelivery findDeliveryByIdx(int orderDeliveryIdx) {
		return orderDeliveryMapper.findByIdx(orderDeliveryIdx);
	}
//	public OrderDelivery findDeliveryByOrder(int orderIdx){
//		return orderDeliveryMapper.findByOrder(orderIdx);
//	}
	public int insertDelivery(OrderDelivery orderDelivery) {
		return orderDeliveryMapper.insert(orderDelivery);
	}

	public int cancel(int orderIdx) {
		return groupOrderMapper.cancel(orderIdx);
	}
	
	public int delete(int orderIdx) {
		int res = orderDeliveryMapper.delete(orderIdx);
		res += groupOrderItemMapper.delete(orderIdx);
		res += groupOrderMapper.delete(orderIdx);
		return res;
	}
}

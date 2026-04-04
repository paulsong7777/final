package com.moeats.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.moeats.MoeatsApplication;
import com.moeats.domain.DeliveryAddress;
import com.moeats.domain.GroupOrder;
import com.moeats.domain.GroupOrderItem;
import com.moeats.domain.OrderDelivery;
import com.moeats.domain.OrderRoom;
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
	
	public GroupOrder findByIdx(int orderIdx) {
		return groupOrderMapper.findByIdx(orderIdx);
	}
	public GroupOrder findByRoom(int roomIdx) {
		return groupOrderMapper.findByRoom(roomIdx);
	}
	public int insert(GroupOrder groupOrder) {
		return groupOrderMapper.insert(groupOrder);
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
	
	public int delete(int orderIdx) {
		int res = orderDeliveryMapper.delete(orderIdx);
		res += groupOrderItemMapper.delete(orderIdx);
		res += groupOrderMapper.delete(orderIdx);
		return res;
	}
}

package com.moeats.services.sse;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter.SseEventBuilder;

import com.moeats.services.GroupOrderService;

@Service
public class SSEService {
	@Autowired
	GroupOrderService groupOrderService;
	
	Map<Integer, List<SseEmitter>> roomMap = new ConcurrentHashMap<>();
	Map<Integer, List<SseEmitter>> orderMap = new ConcurrentHashMap<>();
	Map<Integer, List<SseEmitter>> storeMap = new ConcurrentHashMap<>();
	
	private void remove(Map<Integer, List<SseEmitter>> map, int idx, SseEmitter sseEmitter) {
		map.computeIfPresent(idx,(key,list)->{
			list.remove(sseEmitter);
			return list.isEmpty() ? null : list;
		});
	}
	private SseEmitter join(Map<Integer, List<SseEmitter>> map,int idx){
		SseEmitter sseEmitter = new SseEmitter(60L * 60 * 1000);
		sseEmitter.onCompletion(() -> remove(map,idx,sseEmitter));
		sseEmitter.onTimeout(() -> remove(map,idx,sseEmitter));
		if(!map.containsKey(idx))
			map.put(idx, new CopyOnWriteArrayList<SseEmitter>());
		try {
			sseEmitter.send(SseEmitter.event().name("connect"));
			map.computeIfAbsent(idx, k -> new CopyOnWriteArrayList<>()).add(sseEmitter);
		} catch (IOException e) {}
		return sseEmitter;
	}
	private int send(Map<Integer, List<SseEmitter>> map,int idx,SseEventBuilder message) {
		int sent = 0;
		message.id(String.valueOf(System.currentTimeMillis()));
		for( SseEmitter sseEmitter : map.get(idx) )
			try {
				sseEmitter.send(message);
				sent++;
			}catch (IOException e) {
				sseEmitter.complete();
				remove(map,idx,sseEmitter);
			}
		return sent;
	}
	
	public SseEmitter joinRoom(int roomIdx){
		return join(roomMap,roomIdx);
	}
	public SseEmitter joinOrder(int roomIdx){
		return join(orderMap,roomIdx);
	}
	public SseEmitter joinStore(int storeIdx){
		return join(storeMap,storeIdx);
	}
	
	public int beginOrder(int roomIdx) {
		return send(roomMap,roomIdx,SseEmitter.event().name("to_order"));
	}
	public int cancelRoom(int roomIdx) {
		return send(roomMap,roomIdx,SseEmitter.event().name("cancel"));
	}
	public int expireOrder(int roomIdx) {
		return send(roomMap,roomIdx,SseEmitter.event().name("expire"));
	}
	
	public int payComplete(int orderIdx,int storeIdx) {
		int res = send(orderMap,orderIdx,SseEmitter.event().name("complete"));
		res += send(storeMap,storeIdx,SseEmitter.event().name("new").data(groupOrderService.findRecordByIdx(orderIdx)));
		return res;
	}
	public int payOrder(int orderIdx,int paymentShareIdx) {
		return send(orderMap,orderIdx,SseEmitter.event().name("paid").data(Map.of("paymentShareIdx",paymentShareIdx)));
	}
	public int cancelOrder(int orderIdx) {
		return send(orderMap,orderIdx,SseEmitter.event().name("cancel"));
	}
	public int statusChangeOrder(int orderIdx,String status) {
		return send(orderMap,orderIdx,SseEmitter.event().name("change").data(status));
	}
	
}

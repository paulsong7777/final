package com.moeats.services.sse;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter.SseEventBuilder;

@Service
public class SSEService {
	Map<Integer, List<SseEmitter>> roomMap = new ConcurrentHashMap<>();
	Map<Integer, List<SseEmitter>> orderMap = new ConcurrentHashMap<>();
	
	private SseEmitter join(Map<Integer, List<SseEmitter>> map,int idx){
		SseEmitter sseEmitter = new SseEmitter();
		sseEmitter.onCompletion(() -> map.get(idx).remove(sseEmitter));
		sseEmitter.onTimeout(() -> map.get(idx).remove(sseEmitter));
		if(!map.containsKey(idx))
			map.put(idx, new CopyOnWriteArrayList<SseEmitter>());
		try {
			sseEmitter.send(SseEmitter.event().name("connect"));
			map.get(idx).add(sseEmitter);
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
				map.get(idx).remove(sseEmitter);
			}
		return sent;
	}
	
	public SseEmitter joinRoom(int roomIdx){
		return join(roomMap,roomIdx);
	}
	public SseEmitter joinOrder(int roomIdx){
		return join(orderMap,roomIdx);
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
	public int completeOrder(int orderIdx) {
		return send(orderMap,orderIdx,SseEmitter.event().name("complete"));
	}
	public int payOrder(int orderIdx,int paymentShareIdx) {
		return send(orderMap,orderIdx,SseEmitter.event().name("paid").data(Map.of("paymentShareIdx",paymentShareIdx)));
	}
	public int cancelOrder(int orderIdx) {
		return send(orderMap,orderIdx,SseEmitter.event().name("cancel"));
	}
}

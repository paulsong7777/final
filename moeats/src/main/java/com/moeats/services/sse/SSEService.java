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
	
	
	// 주문방에서 sse가 안먹어서 애먹는중..ㅅㅂ ㅅㅂ
	
	private SseEmitter join(Map<Integer, List<SseEmitter>> map, int idx) {
	    SseEmitter sseEmitter = new SseEmitter(0L); // 무제한

	    map.computeIfAbsent(idx, key -> new CopyOnWriteArrayList<>());
	    map.get(idx).add(sseEmitter);

	    sseEmitter.onCompletion(() -> removeEmitter(map, idx, sseEmitter));
	    sseEmitter.onTimeout(() -> removeEmitter(map, idx, sseEmitter));
	    sseEmitter.onError(e -> removeEmitter(map, idx, sseEmitter));

	    try {
	        sseEmitter.send(
	            SseEmitter.event()
	                .name("connect")
	                .data("connected")
	        );
	    } catch (IOException e) {
	        removeEmitter(map, idx, sseEmitter);
	        sseEmitter.complete();
	    }

	    return sseEmitter;
	}

	private void removeEmitter(Map<Integer, List<SseEmitter>> map, int idx, SseEmitter emitter) {
	    List<SseEmitter> emitters = map.get(idx);
	    if (emitters != null) {
	        emitters.remove(emitter);
	        if (emitters.isEmpty()) {
	            map.remove(idx);
	        }
	    }
	}
	
//	private SseEmitter join(Map<Integer, List<SseEmitter>> map,int idx){
//		SseEmitter sseEmitter = new SseEmitter();
//		sseEmitter.onCompletion(() -> map.get(idx).remove(sseEmitter));
//		sseEmitter.onTimeout(() -> map.get(idx).remove(sseEmitter));
//		if(!map.containsKey(idx))
//			map.put(idx, new CopyOnWriteArrayList<SseEmitter>());
//		try {
//			sseEmitter.send(
//				    SseEmitter.event()
//				        .name("connect")
//				        .data("connected")
//				);
//			map.get(idx).add(sseEmitter);
//		} catch (IOException e) {}
//		return sseEmitter;
//	}
	
	
	
	private int send(Map<Integer, List<SseEmitter>> map, int idx, SseEventBuilder message) {
	    List<SseEmitter> emitters = map.get(idx);
	    if (emitters == null || emitters.isEmpty()) {
	        System.out.println("[SSE] no subscribers. idx=" + idx);
	        return 0;
	    }

	    int sent = 0;
	    message.id(String.valueOf(System.currentTimeMillis()));

	    for (SseEmitter sseEmitter : emitters) {
	        try {
	            sseEmitter.send(message);
	            sent++;
	        } catch (IOException e) {
	            sseEmitter.complete();
	            emitters.remove(sseEmitter);
	        }
	    }

	    System.out.println("[SSE] sent=" + sent + ", idx=" + idx);
	    return sent;
	}
	
	
//	private int send(Map<Integer, List<SseEmitter>> map,int idx,SseEventBuilder message) {
//		if (!map.containsKey(idx) || map.get(idx) == null || map.get(idx).isEmpty()) {
//	        return 0;
//	    }
//		int sent = 0;
//		message.id(String.valueOf(System.currentTimeMillis()));
//		for( SseEmitter sseEmitter : map.get(idx) )
//			try {
//				sseEmitter.send(message);
//				sent++;
//			}catch (IOException e) {
//				sseEmitter.complete();
//				map.get(idx).remove(sseEmitter);
//			}
//		return sent;
//	}
	
	public SseEmitter joinRoom(int roomIdx){
		return join(roomMap,roomIdx);
	}
	public SseEmitter joinOrder(int roomIdx){
		return join(orderMap,roomIdx);
	}
	
	public int beginOrder(int roomIdx, int orderIdx) {
	    return send(
	        roomMap,
	        roomIdx,
	        SseEmitter.event()
	            .name("to_order")
	            .data(Map.of("orderIdx", orderIdx))
	    );
	}
	public int participantUpdate(int roomIdx) {
	    return send(
	        roomMap,
	        roomIdx,
	        SseEmitter.event()
	            .name("participantUpdate")
	            .data("updated")
	    );
	}

	public int cancelRoom(int roomIdx) {
	    return send(
	        roomMap,
	        roomIdx,
	        SseEmitter.event()
	            .name("cancel")
	            .data("cancelled")
	    );
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

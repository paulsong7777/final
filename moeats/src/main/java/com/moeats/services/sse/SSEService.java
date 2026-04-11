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
	    } catch (IOException | IllegalStateException e) {
	        removeEmitter(map, idx, sseEmitter);
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
	        } catch (IOException | IllegalStateException e) {
	            removeEmitter(map, idx, sseEmitter);
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
	public SseEmitter joinOrder(int orderIdx) {
	    return join(orderMap, orderIdx);
	}
	public SseEmitter joinStore(int storeIdx){
		return join(storeMap,storeIdx);
	}
	
	public int participantUpdate(int roomIdx) {
	    try {
	        return send(
	            roomMap,
	            roomIdx,
	            SseEmitter.event()
	                .name("participantUpdate")
	                .data("updated")
	        );
	    } catch (Exception e) {
	        return 0;
	    }
	}

	public int beginOrder(int roomIdx, int orderIdx) {
	    try {
	        return send(
	            roomMap,
	            roomIdx,
	            SseEmitter.event()
	                .name("to_order")
	                .data(Map.of("orderIdx", orderIdx))
	        );
	    } catch (Exception e) {
	        return 0;
	    }
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
	public int expireOrder(int orderIdx) {
	    return send(
	        orderMap,
	        orderIdx,
	        SseEmitter.event()
	            .name("expire")
	            .data("expired")
	    );
	}
	
	public int payComplete(int orderIdx, int storeIdx) {
	    int res = send(
	        orderMap,
	        orderIdx,
	        SseEmitter.event()
	            .name("complete")
	            .data("completed")
	    );

	    res += send(
	        storeMap,
	        storeIdx,
	        SseEmitter.event()
	            .name("new")
	            .data(groupOrderService.findRecordByIdx(orderIdx))
	    );

	    return res;
	}
	public int payOrder(int orderIdx, int paymentShareIdx) {
	    return send(
	        orderMap,
	        orderIdx,
	        SseEmitter.event()
	            .name("paid")
	            .data(Map.of("paymentShareIdx", paymentShareIdx))
	    );
	}
	public int cancelOrder(int orderIdx) {
	    return send(
	        orderMap,
	        orderIdx,
	        SseEmitter.event()
	            .name("cancel")
	            .data("cancelled")
	    );
	}
	public int statusChangeOrder(int orderIdx, String status) {
	    return send(
	        orderMap,
	        orderIdx,
	        SseEmitter.event()
	            .name("change")
	            .data(status)
	    );
	}
	
}

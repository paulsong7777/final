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
	
	public SseEmitter join(int roomIdx){
		SseEmitter sseEmitter = new SseEmitter();
		sseEmitter.onCompletion(() -> roomMap.get(roomIdx).remove(sseEmitter));
		sseEmitter.onTimeout(() -> roomMap.get(roomIdx).remove(sseEmitter));
		if(!roomMap.containsKey(roomIdx))
		roomMap.put(roomIdx, new CopyOnWriteArrayList<SseEmitter>());
		try {
			sseEmitter.send(SseEmitter.event().name("connnect"));
			roomMap.get(roomIdx).add(sseEmitter);
		} catch (IOException e) {}
		 return sseEmitter;
	}
	public int send(int roomIdx,SseEventBuilder message) {
		int sent = 0;
		message.id(String.valueOf(System.currentTimeMillis()));
		for( SseEmitter sseEmitter : roomMap.get(roomIdx) )
			try {
				sseEmitter.send(message);
				sent++;
			}catch (IOException e) {
				sseEmitter.complete();
				roomMap.get(roomIdx).remove(sseEmitter);
			}
		return sent;
	}
	public int beginOrder(int roomIdx) {
		return send(roomIdx,SseEmitter.event().name("to_order"));
	}
}

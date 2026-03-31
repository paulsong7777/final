package com.moeats.services.sse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter.SseEventBuilder;

@Service
public class SSEService {
	Map<Integer, List<SseEmitter>> roomMap = new HashMap<>();
	
	public SseEmitter join(int roomIdx){
		 SseEmitter sseEmitter = new SseEmitter();
		 if(!roomMap.containsKey(roomIdx))
			 roomMap.put(roomIdx, new ArrayList<SseEmitter>());
		 roomMap.get(roomIdx).add(sseEmitter);
		 return sseEmitter;
	}
	public int send(int roomIdx,SseEventBuilder message) {
		int sent = 0;
		synchronized (roomMap.get(roomIdx)) {
			for(Iterator<SseEmitter> it = roomMap.get(roomIdx).iterator();it.hasNext();)
				try {
					SseEmitter sseEmitter = it.next();
					sseEmitter.send(message);
					sent += 1;
				}catch (IOException e) {
					it.remove();
				}
		}
		return sent;
	}
}

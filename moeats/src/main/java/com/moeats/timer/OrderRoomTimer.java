package com.moeats.timer;

import java.sql.Timestamp;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.moeats.services.sse.SSEService;

@Component
public class OrderRoomTimer {
	Map<Integer, ScheduledFuture<?>> timers = new ConcurrentHashMap<>();
	ThreadPoolTaskScheduler scheduler;
	
	@Autowired
	SSEService sseService;
	
	public OrderRoomTimer() {
		scheduler = new ThreadPoolTaskScheduler();
		scheduler.setPoolSize(50);
		scheduler.initialize();
	}
	public void start(int roomIdx,Timestamp expire) {
		stop(roomIdx);
		ScheduledFuture<?> timer = scheduler.schedule(() -> {
			sseService.send(roomIdx,SseEmitter.event().name("expired"));
		}, expire.toInstant());
		timers.put(roomIdx, timer);
	}
	public void stop(int roomIdx) {
		ScheduledFuture<?> timer = timers.remove(roomIdx);
		if( timer!=null && !timer.isDone() ) timer.cancel(false);
	}
}

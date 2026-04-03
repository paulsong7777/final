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
	public void start(int orderIdx,Timestamp expire) {
		stop(orderIdx);
		ScheduledFuture<?> timer = scheduler.schedule(() -> {
			sseService.expireOrder(orderIdx);
		}, expire.toInstant());
		timers.put(orderIdx, timer);
	}
	public void stop(int orderIdx) {
		ScheduledFuture<?> timer = timers.remove(orderIdx);
		if( timer!=null && !timer.isDone() ) timer.cancel(false);
	}
}

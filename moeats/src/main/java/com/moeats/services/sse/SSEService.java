package com.moeats.services.sse;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.moeats.services.GroupOrderService;

@Service
public class SSEService {

    @Autowired
    GroupOrderService groupOrderService;

    Map<Integer, List<SseEmitter>> roomMap = new ConcurrentHashMap<>();
    Map<Integer, List<SseEmitter>> orderMap = new ConcurrentHashMap<>();
    Map<Integer, List<SseEmitter>> storeMap = new ConcurrentHashMap<>();

    private SseEmitter join(Map<Integer, List<SseEmitter>> map, int idx) {
        SseEmitter emitter = new SseEmitter(0L);

        map.computeIfAbsent(idx, key -> new CopyOnWriteArrayList<>());
        map.get(idx).add(emitter);

        emitter.onCompletion(() -> removeEmitter(map, idx, emitter));
        emitter.onTimeout(() -> removeEmitter(map, idx, emitter));
        emitter.onError(e -> removeEmitter(map, idx, emitter));

        try {
            emitter.send(SseEmitter.event()
                .name("connect")
                .data("connected"));
        } catch (Throwable e) {
            safeComplete(emitter);
            removeEmitter(map, idx, emitter);
        }

        return emitter;
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

    private void safeComplete(SseEmitter emitter) {
        try {
            emitter.complete();
        } catch (Throwable ignore) {
        }
    }

    private int send(Map<Integer, List<SseEmitter>> map, int idx, Supplier<SseEmitter.SseEventBuilder> builderSupplier) {
        List<SseEmitter> emitters = map.get(idx);
        if (emitters == null || emitters.isEmpty()) {
            System.out.println("[SSE] no subscribers. idx=" + idx);
            return 0;
        }

        int sent = 0;

        for (SseEmitter emitter : emitters) {
            try {
                SseEmitter.SseEventBuilder builder = builderSupplier.get()
                    .id(String.valueOf(System.currentTimeMillis()));
                emitter.send(builder);
                sent++;
            } catch (Throwable e) {
                safeComplete(emitter);
                removeEmitter(map, idx, emitter);
            }
        }

        System.out.println("[SSE] sent=" + sent + ", idx=" + idx);
        return sent;
    }

    public SseEmitter joinRoom(int roomIdx) {
        return join(roomMap, roomIdx);
    }

    public SseEmitter joinOrder(int orderIdx) {
        return join(orderMap, orderIdx);
    }

    public SseEmitter joinStore(int storeIdx) {
        return join(storeMap, storeIdx);
    }

    public int participantUpdate(int roomIdx) {
        return send(roomMap, roomIdx,
            () -> SseEmitter.event()
                .name("participantUpdate")
                .data("updated"));
    }

    public int beginOrder(int roomIdx, int orderIdx) {
        return send(roomMap, roomIdx,
            () -> SseEmitter.event()
                .name("to_order")
                .data(Map.of("orderIdx", orderIdx)));
    }

    public int cancelRoom(int roomIdx) {
        return send(roomMap, roomIdx,
            () -> SseEmitter.event()
                .name("cancel")
                .data("cancelled"));
    }

    public int expireOrder(int orderIdx) {
        return send(orderMap, orderIdx,
            () -> SseEmitter.event()
                .name("expire")
                .data("expired"));
    }

    public int payComplete(int orderIdx, int storeIdx) {
        int res = send(orderMap, orderIdx,
            () -> SseEmitter.event()
                .name("complete")
                .data("completed"));

        res += send(storeMap, storeIdx,
            () -> SseEmitter.event()
                .name("new")
                .data(groupOrderService.findRecordByIdx(orderIdx)));

        return res;
    }

    public int payOrder(int orderIdx, int paymentShareIdx) {
        return send(orderMap, orderIdx,
            () -> SseEmitter.event()
                .name("paid")
                .data(Map.of("paymentShareIdx", paymentShareIdx)));
    }

    public int cancelOrder(int orderIdx) {
        return send(orderMap, orderIdx,
            () -> SseEmitter.event()
                .name("cancel")
                .data("cancelled"));
    }

    public int statusChangeOrder(int orderIdx, String status) {
        return send(orderMap, orderIdx,
            () -> SseEmitter.event()
                .name("change")
                .data(status));
    }
}
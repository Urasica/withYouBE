package com.capstone.withyou.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class ClientWebSocketServer extends TextWebSocketHandler {

    private final KoreaInvestWebSocketClient koreaInvestWebSocketClient;
    private static final Map<WebSocketSession, Set<String>> subscriptions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("클라이언트 WebSocket 연결됨: {}", session.getId());
        subscriptions.put(session, ConcurrentHashMap.newKeySet());
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {
        JSONObject json = new JSONObject(message.getPayload());
        String action = json.getString("action");
        String stockCode = json.getString("stockCode");

        switch (action) {
            case "subscribe" -> {
                subscriptions.get(session).add(stockCode);
                koreaInvestWebSocketClient.subscribe(stockCode);
                log.info("{} 사용자가 {} 구독", session.getId(), stockCode);
            }

            case "unsubscribe" -> {
                Set<String> codes = subscriptions.get(session);
                if (codes != null) {
                    codes.remove(stockCode);
                    // 다른 세션들도 해당 종목을 여전히 구독 중인지 확인
                    boolean stillSubscribed = subscriptions.values().stream()
                            .anyMatch(set -> set.contains(stockCode));

                    if (!stillSubscribed) {
                        koreaInvestWebSocketClient.unsubscribe(stockCode);
                    }
                    log.info("{} 사용자가 {} 구독 취소", session.getId(), stockCode);
                }
            }
            default -> log.warn("알 수 없는 액션 요청: {}", action);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Set<String> codes = subscriptions.remove(session);

        if (codes != null && !codes.isEmpty()) {
            codes.forEach(stockCode -> {
                boolean stillSubscribed = subscriptions.values().stream()
                        .anyMatch(set -> set.contains(stockCode));

                if (!stillSubscribed) {
                    koreaInvestWebSocketClient.unsubscribe(stockCode);
                }
            });
            log.info("세션 {} 연결 종료로 구독 취소 완료: {}", session.getId(), codes);
        } else {
            log.info("세션 {} 연결 종료. 구독된 종목 없음", session.getId());
        }
    }

    // 외부에서 메시지 전송
    public static void broadcastToSubscribers(String stockCode, String message) {
        subscriptions.forEach((session, codes) -> {
            if (session.isOpen() && codes.contains(stockCode)) {
                try {
                    session.sendMessage(new TextMessage(message));
                } catch (IOException e) {
                    log.error("메시지 전송 실패", e);
                }
            }
        });
    }
}

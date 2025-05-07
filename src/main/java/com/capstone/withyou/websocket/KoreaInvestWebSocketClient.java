package com.capstone.withyou.websocket;

import com.capstone.withyou.service.TokenService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.websocket.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.LinkedHashMap;

import static com.capstone.withyou.websocket.ClientWebSocketServer.broadcastToSubscribers;

@ClientEndpoint
@Slf4j
@Component
@RequiredArgsConstructor
public class KoreaInvestWebSocketClient {

    private final TokenService tokenService;
    private Session session;
    private String approvalKey;

    //WebSocket 연결 요청(최초 1번만)
    private void connect(String approvalKey){
        if(session != null && session.isOpen()){
            log.info("이미 WebSocket 연결됨");
            return;
        }

        this.approvalKey = approvalKey;

        try {
            URI uri = new URI("ws://ops.koreainvestment.com:21000");
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(this, uri);
            log.info("WebSocket 연결 시도중..");
        } catch (Exception e){
            log.info("WebSocket 연결 실패");
        }
    }

    // 구독 요청
    public void subscribe(String stockCode) {
        sendSubscriptionMessage(stockCode, true);
    }

    // 구독 취소 요청
    public void unsubscribe(String stockCode) {
        sendSubscriptionMessage(stockCode, false);
    }

    private void sendSubscriptionMessage(String stockCode, boolean isSubscribe) {
        try {
            if (session == null || !session.isOpen()) {
                log.warn("WebSocket 닫혀 있음. 재연결 시도 중...");
                String approvalKey = tokenService.getApprovalKey(); // approvalKey 새로 받기
                connect(approvalKey); // 재연결 시도
            }

            String trType = isSubscribe ? "1" : "2"; //1:구독, 2:구독취소
            String trId;
            String trKey;

            if(stockCode.chars().allMatch(Character::isDigit)){
                trId = "H0STCNT0";
                trKey = stockCode;
            } else {
                // 해외 주식
                trId = "HDFSASP0";
                trKey = "RBAQ" + stockCode;
            }

            String json = createSubscriptionJson(approvalKey, trType, trId, trKey);
            session.getBasicRemote().sendText(json);

            String action = isSubscribe ? "구독" : "구독 취소";
            log.info("{} {} 요청 전송 완료", stockCode, action);

        } catch (Exception e) {
            log.error("구독{} 요청 실패", isSubscribe ? "" : " 취소", e);
        }
    }

    // 국내 주식 구독
    private String createSubscriptionJson(String approvalKey, String trType, String trId, String tr_key) {
        return """
            {
              "header": {
                "approval_key": "%s",
                "custtype": "P",
                "tr_type": "%s",
                "content-type": "utf-8"
              },
              "body": {
                "input": {
                  "tr_id": "%s",
                  "tr_key": "%s"
                }
              }
            }
            """.formatted(approvalKey, trType, trId, tr_key);
    }

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        log.info("WebSocket 연결됨");
    }

    @OnMessage
    public void onMessage(String message) throws JsonProcessingException {
        log.info("수신된 메시지: {}", message);

        // 국내 주식 처리
        if (message.startsWith("0|H0STCNT0")) {
            handleStockPriceUpdate(message, 3, 2, 0);
        }

        // 해외 주식 처리
        if (message.startsWith("0|HDFSASP0")) {
            handleStockPriceUpdate(message, 3, 11, 1);
        }
    }

    // 주식 체결가 추출 및 발송
    private void handleStockPriceUpdate(String message, int dataIndex, int priceIndex, int stockType) throws JsonProcessingException {
        // "|"로 파트 분리
        String[] parts = message.split("\\|");
        if (parts.length > dataIndex) {
            String dataPart = parts[dataIndex];  // 파트에서 데이터 추출
            String[] dataFields = dataPart.split("\\^");

            if (dataFields.length > priceIndex) {
                String stockCode = dataFields[stockType];  // 종목 코드
                String currentPrice = dataFields[priceIndex];  // 체결가
                log.info("{}의 실시간 체결가: {}", stockCode, currentPrice);

                // 실시간 가격을 프론트에 전달
                broadcastToSubscribers(stockCode, new ObjectMapper().writeValueAsString(
                        new LinkedHashMap<String, Object>() {{
                            put("stockCode", stockCode);
                            put("currentPrice", currentPrice);
                        }}
                ));

            }
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        log.info("WebSocket 종료: {}" ,reason);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        log.error("WebSocket 오류", throwable);
    }
}
package com.capstone.withyou.service;

import com.capstone.withyou.dao.ChatLog;
import com.capstone.withyou.dto.ChatgptRequestDTO;
import com.capstone.withyou.dto.ChatgptResponseDTO;
import com.capstone.withyou.repository.ChatLogRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class ChatBotService {
    private final ChatLogRepository chatLogRepository;
    private final WebClient webClient;
    private final ApiService apiService;
    private final UserService userService;

    private static final List<String> API_FUNCTIONS = List.of(
            "주식현재가 조회", "상승률 순위데이터", "하락률 순위데이터",
            "거래량 순위데이터", "주식 종목 관련 뉴스", "주식 종목 상세정보", "질문"
    );

    @Value("${api.open-ai}")
    private String apiKey;

    @Autowired
    public ChatBotService(ChatLogRepository chatLogRepository,
                          WebClient.Builder webClientBuilder,
                          ApiService apiService,
                          UserService userService) {
        this.chatLogRepository = chatLogRepository;
        this.webClient = webClientBuilder.baseUrl("https://api.openai.com/v1").build();
        this.apiService = apiService;
        this.userService = userService;
    }

    public String processUserQuery(String message, String userName) {
        if(userService.getUser(userName).isEmpty())
            return null;

        IntentAnalysisResult result = analyzeWithChatGPT(message);
        String stockName = result.getParameters().get("stockName");
        String scope = result.getParameters().get("scope");
        String rank = result.getParameters().get("rank");

        // 질문 채팅로그 저장
        chatLogRepository.save(new ChatLog(userName, LocalDate.now(), message));

        // API 호출 및 응답 저장
        ChatLog response = new ChatLog(userName, LocalDate.now(), "");
        String apiResponse = switch (result.getIntent()) {
            case "주식현재가 조회" -> apiService.getStockCurPrice(stockName);
            case "상승률 순위데이터" -> apiService.getRisingStocks(scope, rank);
            case "하락률 순위데이터" -> apiService.getFallingStocks(scope, rank);
            case "거래량 순위데이터" -> apiService.getTradeRankStocks(scope, rank);
            case "주식 종목 관련 뉴스" -> apiService.getNews(stockName);
            case "주식 종목 상세정보" -> apiService.getStockInfo(stockName);
            case "질문" -> result.getParameters().get("answer");
            default -> "사용자의 질문을 이해하지 못했거나 부적절한 질문입니다.";
        };

        response.setMessage(apiResponse);
        chatLogRepository.save(response);
        return apiResponse;
    }

    private IntentAnalysisResult parseGptResponse(String gptResponse) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode root = mapper.readTree(gptResponse);
            String intent = root.get("intent").asText();
            Map<String, String> parameters = mapper.convertValue(root.get("parameters"), Map.class);
            return new IntentAnalysisResult(intent, parameters);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("GPT 응답 파싱 실패", e);
        }
    }

    private IntentAnalysisResult analyzeWithChatGPT(String message) {
        String prompt = "주식 모의투자 앱의 챗봇으로써 응답합니다. 사용자의 질문을 분석하여 주식 관련 API 요청인지, 일반적인 질문인지 구분하세요. "
                + "가능한 API 목록: " + API_FUNCTIONS.toString()
                + "응답은 JSON 형식으로 제공하세요. "
                + "예시1: {\"intent\": \"주식현재가 조회\", \"parameters\": {\"stockName\": \"삼성전자\"}}, "
                + "예시2: {\"intent\": \"질문\", \"parameters\": {\"answer\": \"(사용자의 질문에 대한 자연스러운 대답)\"}}, "
                + "예시3: {\"intent\": \"상승률 순위데이터\", \"parameters\": {\"scope\": \"국내(또는 해외)\", \"rank\":\"1(국내:최대 30, 해외:최대 100)\"}"
                + "채팅: " + message;

        String gptResponse = callChatgptApi(prompt);
        System.out.println(gptResponse);
        return parseGptResponse(gptResponse);
    }

    private String callChatgptApi(String prompt) {
        ChatgptRequestDTO request = new ChatgptRequestDTO(
                "gpt-3.5-turbo",
                Collections.singletonList(new ChatgptRequestDTO.Message("user", prompt)));

        return webClient.post()
                .uri("/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(ChatgptResponseDTO.class)
                .map(response -> response.getChoices().get(0).getMessage().getContent())
                .block();
    }

    public Slice<ChatLog> getChatLog(String userName, int size, Long lastId) {
        Pageable pageable = PageRequest.of(0, size, Sort.by(Sort.Order.desc("date"), Sort.Order.desc("id")));
        if (lastId == null) {
            return chatLogRepository.findChatsByUser(userName, pageable);
        } else {
            return chatLogRepository.findNextChats(userName, lastId, pageable);
        }
    }

    @Getter
    @Setter
    @AllArgsConstructor
    static class IntentAnalysisResult {
        private String intent;
        private Map<String, String> parameters;
    }
}

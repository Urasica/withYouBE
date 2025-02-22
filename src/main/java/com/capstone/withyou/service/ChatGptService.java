package com.capstone.withyou.service;

import com.capstone.withyou.dto.ChatgptRequestDTO;
import com.capstone.withyou.dto.ChatgptResponseDTO;
import com.capstone.withyou.dto.NewsDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;
import java.util.List;

@Service
public class ChatGptService {

    private final WebClient webClient;

    @Value("${api.open-ai}")
    private String apiKey;

    public ChatGptService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://api.openai.com/v1").build();
    }

    // 회사 정보 추출
    public String fetchCompanyDescription(String companyName) {
        String prompt = companyName + " 회사에 대해 한국어로 4줄 한문단으로 간단하게 설명해줘.";

        return callChatgptApi(prompt);
    }

    // 뉴스 기반 주식 예측 결과 추출
    public String predictStockResult(String stockName, List<NewsDTO> newsList) {
        StringBuilder newsContent = new StringBuilder();

        for (NewsDTO news : newsList) {
            newsContent.append(news.getTitle()).append("\n")
                    .append(news.getSummary()).append("\n\n");
        }
        String prompt = "다음 뉴스들을 바탕으로 "+ stockName + "의 주식 예상 결과를 3줄 한문단으로 간단하게 알려줘:" +
                "\n\n" + newsContent;

        return callChatgptApi(prompt);
    }

    // Chatgpt 호출
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
}

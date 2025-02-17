package com.capstone.withyou.service;

import com.capstone.withyou.dto.ChatgptRequestDTO;
import com.capstone.withyou.dto.ChatgptResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;

@Service
public class ChatGptService {

    private final WebClient webClient;

    @Value("${api.open-ai}")
    private String apiKey;

    public ChatGptService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://api.openai.com/v1").build();
    }

    public String fetchCompanyDescription(String companyName) {
        String prompt = companyName + " 회사에 대해 한국어로 4줄 한문단으로 간단하게 설명해줘.";

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
                .block(); // 동기 처리
    }
}

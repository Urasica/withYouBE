package com.capstone.withyou.controller;

import com.capstone.withyou.dao.ChatLog;
import com.capstone.withyou.service.ChatBotService;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("chatbot")
public class ChatBotController {
    private final ChatBotService chatBotService;

    public ChatBotController(ChatBotService chatBotService) {
        this.chatBotService = chatBotService;
    }

    @GetMapping("/chat-log")
    public ResponseEntity<Slice<ChatLog>> getChats(
            @RequestParam String userName,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) Long lastId) {
        if (userName == null || userName.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(chatBotService.getChatLog(userName, size, lastId));
    }

    @GetMapping("/ask")
    public ResponseEntity<String> chatBot(@RequestParam String message, @RequestParam String username) {
        String response = chatBotService.processUserQuery(message, username);

        if(response == null) {
            return ResponseEntity.badRequest().body("유저를 찾을 수 없습니다.");
        }

        return ResponseEntity.ok(response);
    }
}

package com.epam.training.gen.ai.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.epam.training.gen.ai.services.ChatBotService;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
public class ChatBotController {

    @Autowired
    private ChatBotService chatBotService;

    @GetMapping("/chat")
    public Mono<PromptResponse> getChatResponse(@RequestParam String prompt,
            @RequestParam(defaultValue = "false") boolean newSession) throws Exception {

        return chatBotService.getResponse(prompt, newSession).map(response -> new PromptResponse(prompt, response));
    }
}

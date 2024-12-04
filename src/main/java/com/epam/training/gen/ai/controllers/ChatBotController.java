package com.epam.training.gen.ai.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.epam.training.gen.ai.services.ChatBotService;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
public class ChatBotController {

    @Autowired
    private ChatBotService chatBotService;

    @PostMapping(path = "/chat", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<PromptResponse> getChatResponse(@RequestBody PromptRequest request) throws Exception {

        return chatBotService.getResponse(request.prompt(), request.newSession())
                .map(response -> new PromptResponse(request.prompt(), response));
    }
}

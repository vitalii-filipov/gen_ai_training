package com.epam.training.gen.ai.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.epam.training.gen.ai.models.ScoreResult;
import com.epam.training.gen.ai.services.ChatBotService;
import com.epam.training.gen.ai.services.EmbeddingService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
public class ChatBotController {

    @Autowired
    private ChatBotService chatBotService;

    @Autowired
    private EmbeddingService embeddingService;

    @PostMapping(path = "/chat", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<PromptResponse> getChatResponse(@RequestBody PromptRequest request) throws Exception {

        return chatBotService.getResponse(request.prompt(), request.newSession())
                .map(response -> new PromptResponse(request.prompt(), response));
    }

    @PostMapping(path = "/embedding/array", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Flux<List<Float>> getEmbeddingArrayResponse(@RequestBody PromptRequest request) throws Exception {

        return embeddingService.buildEmbedding(request.prompt());
    }

    @PostMapping(path = "/embedding/store", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Flux<String> getEmbeddingStoreResponse(@RequestBody PromptRequest request) throws Exception {

        return embeddingService.buildEmbeddingAndStore(request.prompt());
    }

    @PostMapping(path = "/embedding/search", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Flux<ScoreResult> getEmbeddingSearchResponse(@RequestBody PromptRequest request) throws Exception {

        return embeddingService.searchEmbedding(request.prompt());
    }
}

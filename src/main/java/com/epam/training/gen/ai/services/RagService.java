package com.epam.training.gen.ai.services;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.orchestration.InvocationContext;
import com.microsoft.semantickernel.services.ServiceNotFoundException;
import com.microsoft.semantickernel.services.chatcompletion.ChatCompletionService;
import com.microsoft.semantickernel.services.chatcompletion.ChatHistory;
import com.microsoft.semantickernel.services.chatcompletion.ChatMessageContent;

import reactor.core.publisher.Mono;

@Service
public class RagService {

    private static final Logger logger = LoggerFactory.getLogger(RagService.class);

    private static final String SYSTEM_MESSAGE = """
                You are a helpful assistant. Provide answers from CONTEXT only. 
                If there is no enough information is present in the CONTEXT then return "I do not know"
            """;

    @Autowired
    private Kernel semanticKernel;

    @Autowired
    private InvocationContext invocationContext;

    @Autowired
    private EmbeddingService embeddingService;

    private AtomicReference<ChatHistory> historyRef = new AtomicReference<>(createHistory());

    public Mono<String> getResponse(String prompt, boolean newHistory) throws ServiceNotFoundException {
        var context = getContext(prompt);
        ChatHistory chatHistory = getChatHistory(newHistory);
        chatHistory.addUserMessage("""
            <CONTEXT>
                ${context}
            </CONTEXT>
            <USER_QUERY>
                ${prompt}
            </USER_QUERY>
        """
            .replace("${prompt}", prompt)
            .replace("${context}", context)
        );
        ChatCompletionService chatCompletionService = semanticKernel.getService(ChatCompletionService.class);
        return chatCompletionService
                .getChatMessageContentsAsync(chatHistory, semanticKernel, invocationContext)
                .doOnNext(this::updateHistory)
                .map(RagService::convertMessagesToString)
                .doOnNext(response -> logger.info("""
                        Input prompt: ```
                        ${prompt}
                        ```
                        Model response: ```
                        ${response}
                        ```
                        """
                        .replace("${prompt}", prompt)
                        .replace("${response}", response)));
    }

    private void updateHistory(List<ChatMessageContent<?>> chatMessageList) {
        ChatHistory chatHistory = getChatHistory(false);
        chatHistory.addAll(chatMessageList);
    }

    private static String convertMessagesToString(List<ChatMessageContent<?>> chatMessageList) {
        return chatMessageList.stream()
                .filter(content -> content.getContent() != null)
                .map(content -> content.getContent()).collect(Collectors.joining("\n"));
    }

    private ChatHistory getChatHistory(boolean newHistory) {
        if (newHistory) {
            return historyRef.updateAndGet(prev -> createHistory());
        }
        return historyRef.get();
    }

    private ChatHistory createHistory() {
        return new ChatHistory(SYSTEM_MESSAGE);
    }

    private String getContext(String prompt) {
        return embeddingService.searchEmbedding(prompt)
            .map(result -> result.originalText())
            .reduce((a, b) -> a + "\n" + b)
            .block();
    }
}
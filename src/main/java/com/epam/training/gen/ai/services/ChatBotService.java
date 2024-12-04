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
public class ChatBotService {

    private static final Logger logger = LoggerFactory.getLogger(ChatBotService.class);

    private static final String SYSTEM_MESSAGE = "You are a helpful assistant.";

    @Autowired
    private Kernel semanticKernel;

    @Autowired
    private InvocationContext invocationContext;

    private AtomicReference<ChatHistory> historyRef = new AtomicReference<>(createHistory());

    public Mono<String> getResponse(String prompt, boolean newHistory) throws ServiceNotFoundException {
        ChatHistory chatHistory = getChatHistory(newHistory);
        chatHistory.addUserMessage(prompt);
        ChatCompletionService chatCompletionService = semanticKernel.getService(ChatCompletionService.class);
        return chatCompletionService
                .getChatMessageContentsAsync(chatHistory, semanticKernel, invocationContext)
                .doOnNext(this::updateHistory)
                .map(ChatBotService::convertMessagesToString)
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
}
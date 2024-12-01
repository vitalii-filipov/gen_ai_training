package com.epam.training.gen.ai.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.orchestration.FunctionResult;

@RestController
@RequestMapping("/api")
public class ChatBotController {

    @Autowired
    private Kernel semanticKernel;

    @GetMapping("/chat-response")
    public PromptResponse getChatResponse(@RequestParam String prompt) {

        FunctionResult<String> response = semanticKernel.invokePromptAsync(prompt)
                .withResultType(String.class).block();
        return new PromptResponse(prompt, response.getResult());
    }
}

package com.epam.training.gen.ai.config;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.epam.training.gen.ai.plugins.BuyTrainTicketPlugin;
import com.epam.training.gen.ai.plugins.SimplePlugin;
import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.aiservices.openai.chatcompletion.OpenAIChatCompletion;
import com.microsoft.semantickernel.orchestration.InvocationContext;
import com.microsoft.semantickernel.orchestration.InvocationContext.Builder;
import com.microsoft.semantickernel.orchestration.PromptExecutionSettings;
import com.microsoft.semantickernel.orchestration.ToolCallBehavior;
import com.microsoft.semantickernel.plugin.KernelPlugin;
import com.microsoft.semantickernel.plugin.KernelPluginFactory;
import com.microsoft.semantickernel.services.chatcompletion.ChatCompletionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration class for setting up Semantic Kernel components.
 * <p>
 * This configuration provides several beans necessary for the interaction with
 * Azure OpenAI services and the creation of kernel plugins. It defines beans
 * for
 * chat completion services, kernel plugins, kernel instance, invocation
 * context,
 * and prompt execution settings.
 */
@Configuration
public class SemanticKernelConfiguration {

    /**
     * Creates a {@link ChatCompletionService} bean for handling chat completions
     * using Azure OpenAI.
     *
     * @param deploymentOrModelName the Azure OpenAI deployment or model name
     * @param openAIAsyncClient     the {@link OpenAIAsyncClient} to communicate
     *                              with Azure OpenAI
     * @return an instance of {@link ChatCompletionService}
     */

    @Bean
    public ChatCompletionService chatCompletionService(
            @Value("${client-openai-deployment-name}") String deploymentOrModelName,
            OpenAIAsyncClient openAIAsyncClient) {
        return OpenAIChatCompletion.builder()
                .withModelId(deploymentOrModelName)
                .withOpenAIAsyncClient(openAIAsyncClient)
                .build();
    }

    /**
     * Creates a {@link KernelPlugin} bean using a simple plugin.
     *
     * @return an instance of {@link KernelPlugin}
     */
    @Bean
    public KernelPlugin kernelPlugin() {
        return KernelPluginFactory.createFromObject(
                new SimplePlugin(), "SimplePlugin");
    }

    /**
     * Creates a {@link Kernel} bean to manage AI services and plugins.
     *
     * @param chatCompletionService the {@link ChatCompletionService} for handling
     *                              completions
     * @param kernelPlugin          the {@link KernelPlugin} to be used in the
     *                              kernel
     * @return an instance of {@link Kernel}
     */
    @Bean
    public Kernel kernel(ChatCompletionService chatCompletionService, KernelPlugin kernelPlugin) {
        return Kernel.builder()
                .withAIService(ChatCompletionService.class, chatCompletionService)
                .withPlugin(kernelPlugin)
                .withPlugin(KernelPluginFactory.createFromObject(new BuyTrainTicketPlugin(), "BuyTrainTicketPlugin"))
                .build();
    }

    /**
     * Creates an {@link InvocationContext} bean with default prompt execution
     * settings.
     *
     * @return an instance of {@link InvocationContext}
     */
    @Bean
    public InvocationContext invocationContext(@Value("${tool-call-enabled}") boolean toolCallEnabled) {
        Builder contextBuilder = InvocationContext.builder()
                .withPromptExecutionSettings(PromptExecutionSettings.builder()
                        .withTemperature(1.0)
                        .build());
        if (toolCallEnabled) {
            contextBuilder
                    .withToolCallBehavior(ToolCallBehavior.allowAllKernelFunctions(true));
        }
        return contextBuilder.build();
    }

    /**
     * Creates a map of {@link PromptExecutionSettings} for different models.
     *
     * @param deploymentOrModelName the Azure OpenAI deployment or model name
     * @return a map of model names to {@link PromptExecutionSettings}
     */
    @Bean
    public Map<String, PromptExecutionSettings> promptExecutionsSettingsMap(
            @Value("${client-openai-deployment-name}") String deploymentOrModelName) {
        return new HashMap<String, PromptExecutionSettings>() {
            {
                put(deploymentOrModelName, PromptExecutionSettings.builder()
                        .withTemperature(1.0)
                        .build());
                put("gpt-3.5-turbo", PromptExecutionSettings.builder()
                        .withMaxTokens(1_000)
                        .withTemperature(0d)
                        .build());
                put("gpt-4", PromptExecutionSettings.builder()
                        .withModelId("gpt-4-1106-preview")
                        .withMaxTokens(8_000)
                        .withTemperature(0.3d)
                        .build());
            }
        };
    }
}

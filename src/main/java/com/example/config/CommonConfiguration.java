package com.example.config;

import com.example.constants.SystemConstants;
import com.example.tools.DocTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class CommonConfiguration {

    // 1. Configure client for general chat/test
    @Bean
    public ChatClient chatClient(OpenAiChatModel model) {
        return ChatClient
                .builder(model) // Create ChatClient factory instance
                .defaultSystem("你是一个医疗智能小助手，致力于帮助用户解答各种问题。")
                //.defaultSystem(SystemConstants.CUSTOMER_SERVICE_PROMPT)
                .defaultAdvisors(
                        new SimpleLoggerAdvisor()
                ) // Configure log Advisor
                .build(); // Build ChatClient instance
    }

    // 2. Agent + Function Calling
    @Bean
    public ChatClient serviceChatClient(OpenAiChatModel model, DocTools docTools) {
        return ChatClient
                .builder(model) // Create ChatClient factory instance
                .defaultSystem(SystemConstants.CUSTOMER_SERVICE_PROMPT) // Set system prompt
                .defaultAdvisors(
                        new SimpleLoggerAdvisor()
                ) // Configure log Advisor
                .defaultTools(docTools) // Register tool class
                .build(); // Build ChatClient instance
    }

    // 3. Google Maps API WebClient
    @Bean
    public WebClient googleWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl("https://maps.googleapis.com")
                .build();
    }
}

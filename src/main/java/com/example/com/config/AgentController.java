package com.example.com.config;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RequiredArgsConstructor
@RestController
@RequestMapping("/ai")
public class AgentController {

    private final ChatClient serviceChatClient;

    @RequestMapping(value = "/agent", produces = "text/html;charset=UTF-8")
    public Flux<String> service(String prompt, String chatId) {

        return serviceChatClient.prompt()
                .user(prompt)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, chatId) )
                .stream()
                .content();
    }
}

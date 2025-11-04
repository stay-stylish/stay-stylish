package org.example.staystylish.common.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * - chatClientGemini (@Primary): 기본 (OpenAI)
 */
@Configuration
public class AiClientsConfig {

    //chatClient (OpenAI) - 이제 이 빈이 기본이 됨
    @Bean(name = "chatClientOpenAi")
    public ChatClient chatClientOpenAi(@Qualifier("openAiChatModel") ChatModel openAiModel) {
        return ChatClient.builder(openAiModel).build();
    }
}

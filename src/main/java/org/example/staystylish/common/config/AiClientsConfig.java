package org.example.staystylish.common.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * - chatClientGemini (@Primary): 기본 (Gemini) / chatClientOpenAi : traveloutfit 도메인에서 사용 (OpenAI)
 */
@Configuration
public class AiClientsConfig {

    // 기본 ChatModel을 Gemini로 고정
    @Bean
    @Primary
    public ChatModel primaryChatModel(@Qualifier("vertexAiGeminiChat") ChatModel gemini) {

        return gemini;
    }

    // 기본 ChatClient (Gemini)
    @Bean(name = "chatClientGemini")
    @Primary
    public ChatClient chatClientGemini(@Qualifier("vertexAiGeminiChat") ChatModel geminiModel) {
        return ChatClient.builder(geminiModel).build();
    }

    // traveloutfit 도메인 chatClient (OpenAI)
    @Bean(name = "chatClientOpenAi")
    public ChatClient chatClientOpenAi(@Qualifier("openAiChatModel") ChatModel openAiModel) {
        return ChatClient.builder(openAiModel).build();
    }
}

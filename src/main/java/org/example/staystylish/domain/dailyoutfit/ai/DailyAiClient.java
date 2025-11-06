package org.example.staystylish.domain.dailyoutfit.ai;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.staystylish.common.exception.GlobalException;
import org.example.staystylish.domain.dailyoutfit.code.DailyOutfitErrorCode;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DailyAiClient {

    @Qualifier("chatClientOpenAi")
    private final ChatClient chatClient;

    @Cacheable(value = "dailyAi", key = "{#systemPrompt, #userPrompt}")
    @Retry(name = "dailyAiApi", fallbackMethod = "fallbackCallForJson")
    @CircuitBreaker(name = "dailyAiApi", fallbackMethod = "fallbackCallForJson")
    public String callForJson(String systemPrompt, String userPrompt) {
        log.info("Calling Daily AI");
        String jsonResponse = chatClient.prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .call()
                .content();

        // AI 응답에서 JSON 부분만 추출
        int startIndex = jsonResponse.indexOf('{');
        int endIndex = jsonResponse.lastIndexOf('}');
        if (startIndex != -1 && endIndex != -1 && startIndex < endIndex) {
            return jsonResponse.substring(startIndex, endIndex + 1);
        }
        return jsonResponse;
    }

    public String fallbackCallForJson(String systemPrompt, String userPrompt, Throwable e) {
        log.error("[CircuitBreaker] AI 호출 차단. cause={}", e.toString());
        throw new GlobalException(DailyOutfitErrorCode.SERVICE_UNAVAILABLE);
    }
}

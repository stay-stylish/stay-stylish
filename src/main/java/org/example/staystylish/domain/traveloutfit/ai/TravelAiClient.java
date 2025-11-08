package org.example.staystylish.domain.traveloutfit.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.example.staystylish.common.exception.GlobalException;
import org.example.staystylish.domain.traveloutfit.code.TravelOutfitErrorCode;
import org.example.staystylish.domain.traveloutfit.dto.response.AiTravelJsonResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

/**
 * AI 모델과 통신 및 응답 처리 담당 Client
 */
@Slf4j
@Component
public class TravelAiClient {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    // 생성자 주입
    public TravelAiClient(
            @Qualifier("chatClientOpenAi") ChatClient chatClient,
            ObjectMapper objectMapper
    ) {
        this.chatClient = chatClient;
        this.objectMapper = objectMapper;
    }

    // chatClient를 사용해서 프롬프트를 AI 모델로 전송하고 응답 받기
    @Cacheable(value = "travelAi", key = "#prompt")
    @Retry(name = "travelAiApi", fallbackMethod = "fallbackCallForJson")
    @CircuitBreaker(name = "travelAiApi", fallbackMethod = "fallbackCallForJson")
    public String callForJson(String prompt) {

        String content = chatClient.prompt(prompt).call().content();

        // 불필요한 마크다운을 제거
        return content.replaceAll("^```json\\s*", "")
                .replaceAll("\\s*```$", "")
                .trim();
    }

    public AiTravelJsonResponse parse(String json) {

        try {
            return objectMapper.readValue(json, AiTravelJsonResponse.class);
        } catch (Exception e) {
            throw new IllegalStateException("AI 응답 JSON 파싱 실패: " + e.getMessage(), e);
        }
    }

    public String fallbackCallForJson(String prompt, Throwable e) {
        log.error("[CircuitBreaker] AI 호출 차단. cause={}", e.toString());

        throw new GlobalException(TravelOutfitErrorCode.SERVICE_UNAVAILABLE);
    }

}

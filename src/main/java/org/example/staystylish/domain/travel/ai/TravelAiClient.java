package org.example.staystylish.domain.travel.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.staystylish.domain.travel.dto.response.TravelOutfitResponse.AiOutfit;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

/**
 * AI 모델과 통신 및 응답 처리 담당 Client
 */
@Component
@RequiredArgsConstructor
public class TravelAiClient {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    // chatClient를 사용해서 프롬프트를 AI 모델로 전송하고 응답 받기
    public String callForJson(String prompt) {

        String content = chatClient.prompt(prompt).call().content();

        // 불필요한 마크다운을 제거
        return content.replaceAll("^```json\\s*", "")
                .replaceAll("\\s*```$", "")
                .trim();
    }

    public AiOutfit parse(String json) {

        try {
            return objectMapper.readValue(json, AiOutfit.class);
        } catch (Exception e) {
            throw new IllegalStateException("AI 응답 JSON 파싱 실패: " + e.getMessage(), e);
        }
    }

}

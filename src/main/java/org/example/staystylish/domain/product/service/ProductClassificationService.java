package org.example.staystylish.domain.product.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.staystylish.domain.product.dto.request.ProductClassificationRequest;
import org.example.staystylish.domain.product.dto.response.ProductClassificationResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

/**
 * 상품 분류와 관련된 비즈니스 로직을 처리하는 서비스 클래스입니다.
 * AI 모델을 활용하여 상품명을 분석하고 분류 결과를 반환합니다.
 */
@Service
public class ProductClassificationService {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    // 시스템 지시사항 부분
    private final String systemPrompt = """
            ### 역할 및 규칙 ###
            너는 패션 상품 데이터를 분석하고 구조화하는 전문 데이터 분석가(조교)다.
            주어진 상품 정보를 분석하여 'category', 'sub_category', 'style_tags'를 포함한 JSON 객체로 출력해야 한다.
            사용 가능한 style_tags는 ["캐주얼", "미니멀", "스트릿", "포멀", "스포티"] 중에서만 선택해야 한다.
            결과 외에 어떤 부가적인 설명도 붙이지 말고, 오직 JSON 객체만 반환해야 한다.
            
            ### 모범 답안 (예시) ###
            [입력]: "루즈핏 피그먼트 맨투맨"
            [출력]: { "category": "상의", "sub_category": "맨투맨", "style_tags": ["캐주얼", "스트릿"] }
            
            [입력]: "쿨맥스 와이드 밴딩 슬랙스"
            [출력]: { "category": "하의", "sub_category": "슬랙스", "style_tags": ["미니멀", "포멀"] }
            """;

    public ProductClassificationService(ChatClient chatClient, ObjectMapper objectMapper) {
        this.chatClient = chatClient;
        this.objectMapper = objectMapper;
    }

    public ProductClassificationResponse classify(ProductClassificationRequest request) {

        // 사용자 요청 부분
        String userMessage = "### 실제 과제 ###\\n[입력]: \"" + request.productName() + "\"\\n[출력]:";

        // 시스템, 사용자 요청을 구분하여 AI 호출
        String response = chatClient.prompt()
                .system(systemPrompt)
                .user(userMessage)
                .call()
                .content();

        // AI 응답에서 JSON 부분만 추출
        int startIndex = response.indexOf('{');
        int endIndex = response.lastIndexOf('}');
        String jsonResponse = response;
        if (startIndex != -1 && endIndex != -1 && startIndex < endIndex) {
            jsonResponse = response.substring(startIndex, endIndex + 1);
        }

        try {
            ProductClassificationResponse classificationResponse = objectMapper.readValue(jsonResponse, ProductClassificationResponse.class);

            return classificationResponse;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("AI 응답을 파싱하는 데 실패했습니다.", e);
        }
    }
}
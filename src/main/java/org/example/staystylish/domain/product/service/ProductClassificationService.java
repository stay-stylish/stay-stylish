package org.example.staystylish.domain.product.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.staystylish.domain.product.dto.request.ProductClassificationRequest;
import org.example.staystylish.domain.product.dto.response.ProductClassificationResponse;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ProductClassificationService {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    private final String promptTemplate = """
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
            
            ### 실제 과제 ###
            [입력]: "{productName}"
            [출력]:
            """;

    public ProductClassificationService(ChatClient chatClient, ObjectMapper objectMapper) {
        this.chatClient = chatClient;
        this.objectMapper = objectMapper;
    }

    public ProductClassificationResponse classify(ProductClassificationRequest request) {

        PromptTemplate template = new PromptTemplate(promptTemplate);
        Prompt prompt = template.create(Map.of("productName", request.productName()));

        String response = chatClient.call(prompt).getResult().getOutput().getContent();
        
        try {
            return objectMapper.readValue(response, ProductClassificationResponse.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("AI 응답을 파싱하는 데 실패했습니다.", e);
        }
    }
}

package org.example.staystylish.domain.productclassification.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.staystylish.domain.dailyoutfit.dto.response.DailyOutfitRecommendationResponse;
import lombok.RequiredArgsConstructor;
import org.example.staystylish.domain.dailyoutfit.dto.response.DailyOutfitRecommendationResponse;
import org.example.staystylish.domain.productclassification.dto.request.ProductClassificationRequest;
import org.example.staystylish.domain.productclassification.dto.response.ProductClassificationResponse;
import org.example.staystylish.domain.productclassification.entity.Product;
import org.example.staystylish.domain.productclassification.repository.ProductClassificationRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import java.util.List;

/**
 * 상품 분류와 관련된 비즈니스 로직을 처리하는 서비스 클래스
 * AI 모델을 활용하여 상품명을 분석하고 분류 결과를 반환
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductClassificationService {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;
    private final ProductClassificationRepository productClassificationRepository;

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

    // 상품 분류 요청을 받아 AI 모델을 통해 분류 결과를 반환하고 DB에 저장합니다.
    @Transactional
    public ProductClassificationResponse classify(ProductClassificationRequest request) {
        // 1. 상품명으로 상품을 찾거나 새로 생성
        Product product = productClassificationRepository.findByName(request.productName())
                .orElseGet(() -> Product.create(request.productName()));

        // 2. AI를 통해 상품 정보 분류
        ProductClassificationResponse classificationResponse = getClassificationFromAI(request.productName());

        // 3. 엔티티에 분류 결과 업데이트
        product.updateClassification(classificationResponse);

        // 4. DB에 저장 (새 상품이거나 내용이 변경된 경우)
        productClassificationRepository.save(product);

        // 5. 결과 반환
        return classificationResponse;
    }

    private ProductClassificationResponse getClassificationFromAI(String productName) {
        // 사용자 요청 부분
        String userMessage = "### 실제 과제 ###\n[입력]: \"" + productName + "\"\n[출력]:";

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
            // AI 응답을 ProductClassificationResponse 객체로 파싱
            return objectMapper.readValue(jsonResponse, ProductClassificationResponse.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("AI 응답을 파싱하는 데 실패했습니다.", e);
        }
    }


    // DailyOutfitRecommendationResponse 형태로 변환
    /**
     * classifyAndRecommend
     *
     * - classify() 결과를 DailyOutfitRecommendationResponse로 변환
     * - DailyOutfitController/Service에서 추천 텍스트 + 추천 카테고리 생성 시 사용
     *
     * @param request ProductClassificationRequest
     * @return DailyOutfitRecommendationResponse
     */
    public DailyOutfitRecommendationResponse classifyAndRecommend(ProductClassificationRequest request) {
        ProductClassificationResponse classificationResponse = classify(request);

        //추천 문구 생성 (DailyOutfitRecommendationResponse에서 사용)
        String recommendationText = "나에게 맞는 " + classificationResponse.subCategory() + " 보러가기";
        //추천 카테고리 목록: styleTags 사용
        List<String> recommendedCategories = classificationResponse.styleTags();
        // DailyOutfitRecommendationResponse로 변환
        return DailyOutfitRecommendationResponse.from(recommendationText, recommendedCategories);
    }
}
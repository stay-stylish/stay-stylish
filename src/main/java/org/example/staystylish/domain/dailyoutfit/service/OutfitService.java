package org.example.staystylish.domain.dailyoutfit.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.staystylish.common.consts.CommonErrorCode;
import org.example.staystylish.common.exception.GlobalException;
import org.example.staystylish.domain.dailyoutfit.dto.response.OutfitRecommendationResponse;
import org.example.staystylish.domain.dailyoutfit.entity.UserItemFeedback;
import org.example.staystylish.domain.dailyoutfit.enums.LikeStatus;
import org.example.staystylish.domain.dailyoutfit.exception.OutfitErrorCode;
import org.example.staystylish.domain.dailyoutfit.repository.UserItemFeedbackRepository;
import org.example.staystylish.domain.localweather.dto.GpsRequest;
import org.example.staystylish.domain.localweather.dto.UserWeatherResponse;
import org.example.staystylish.domain.localweather.service.WeatherService;
import org.example.staystylish.domain.productclassification.entity.Product;
import org.example.staystylish.domain.productclassification.repository.ProductRepository;
import org.example.staystylish.domain.user.entity.User;
import org.example.staystylish.domain.user.exception.UserErrorCode;
import org.example.staystylish.domain.user.exception.UserException;
import org.example.staystylish.domain.user.repository.UserRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 의상 추천과 관련된 비즈니스 로직을 처리하는 서비스 클래스입니다.
 * 날씨 정보, 사용자 피드백, AI 모델을 활용하여 맞춤형 의상을 추천합니다.
 */
@Service
@Transactional
public class OutfitService {

    private final UserItemFeedbackRepository userItemFeedbackRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ChatClient chatClient;
    private final WeatherService weatherService; // WeatherService 주입
    private final ObjectMapper objectMapper;

    public OutfitService(UserItemFeedbackRepository userItemFeedbackRepository, ProductRepository productRepository,
                         UserRepository userRepository, ChatClient chatClient,
                         ObjectMapper objectMapper, WeatherService weatherService) {
        this.userItemFeedbackRepository = userItemFeedbackRepository;
        this.userRepository = userRepository;
        this.chatClient = chatClient;
        this.objectMapper = objectMapper;
        this.weatherService = weatherService;
        this.productRepository = productRepository;
    }

    /**
     * 사용자 정보와 위도, 경도 정보를 기반으로 옷차림을 추천합니다.
     * 날씨 정보는 위도, 경도를 통해 WeatherService에서 조회합니다.
     *
     * @param userId    사용자 ID
     * @param latitude  위도
     * @param longitude 경도
     * @return 추천된 옷차림 정보
     */
    @Transactional(readOnly = true)
    public OutfitRecommendationResponse getOutfitRecommendation(
            Long userId,
            double latitude,
            double longitude
    ) {
        // 1. 사용자 정보 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        // 2. 날씨 정보 조회
        GpsRequest gpsRequest = new GpsRequest(latitude, longitude);
        UserWeatherResponse userWeatherResponse = weatherService.getWeatherByLatLon(gpsRequest)
                .block(); // Mono<UserWeatherResponse>를 블로킹하여 가져옴. 실제 서비스에서는 비동기 처리 고려 필요.

        if (userWeatherResponse == null) {
            throw new GlobalException(OutfitErrorCode.WEATHER_INFO_NOT_FOUND);
        }

        // 3. 사용자 피드백 기반 아이템 조회 (선택 사항)
        // TODO: 사용자 피드백을 기반으로 아이템을 필터링하거나 가중치를 부여하는 로직 추가
        List<Product> userFeedbackItems = productRepository.findAll(); // 현재는 모든 아이템 조회

        // 4. AI를 활용한 옷차림 추천
        String aiRecommendation = getAiOutfitRecommendation(
                user, // User 객체 직접 전달
                userWeatherResponse, // 날씨 정보 DTO 전달
                userFeedbackItems
        );

        // 5. AI 추천 결과를 파싱하여 OutfitRecommendationResponse 생성
        try {
            Map<String, Object> aiResult = objectMapper.readValue(aiRecommendation, new TypeReference<>() {
            });
            String recommendationText = (String) aiResult.get("recommendation_text");
            List<String> recommendedCategories = objectMapper.convertValue(aiResult.get("recommended_categories"), new TypeReference<>() {
            });

            OutfitRecommendationResponse response = new OutfitRecommendationResponse(
                    recommendationText,
                    recommendedCategories
            );
            return response;
        } catch (Exception e) {
            throw new GlobalException(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private String getOutfitSystemPrompt() {
        return """
                당신은 '일타 패션'입니다. 친절하고 트렌디한 패션 추천 도우미입니다.
                당신의 목표는 사용자 정보와 오늘의 날씨를 기반으로 개인화된 의상 추천을 제공하는 것입니다.
                엄격한 사고의 사슬(Chain of Thought)을 따라야 합니다: 첫째, 날씨를 분석합니다. 둘째, 사용자 프로필과 피드백을 분석합니다. 셋째, 추천으로 결론을 내립니다.
                최종 출력은 추가 텍스트나 설명 없이 단일하고 원시적인 JSON 객체여야 합니다. JSON 객체는 다음 두 가지 키를 포함해야 합니다:
                1. `recommendation_text`: 친근하고 대화적인 한국어로 작성된 추천 메시지.
                2. `recommended_categories`: 2-3개의 특정 의류 아이템 카테고리 목록 (예: ["반팔 티셔츠", "데님 팬츠", "스니커즈"]).
                """;
    }

    private String buildOutfitUserPrompt(
            User user,
            UserWeatherResponse userWeatherResponse,
            List<Product> userFeedbackItems
    ) {
        StringBuilder sb = new StringBuilder();

        sb.append("### 사용자 및 날씨 정보 ###\n");

        // 날씨 데이터
        if (userWeatherResponse != null) {
            sb.append("- 지역: ").append(userWeatherResponse.province()).append(" ").append(userWeatherResponse.city()).append(" ").append(userWeatherResponse.district()).append("\n");
            sb.append("- 날씨: ").append(userWeatherResponse.sky()).append(" (").append(userWeatherResponse.pty()).append(")\n");
            sb.append("- 온도: ").append(String.format("%.1f°C", userWeatherResponse.temperature())).append("\n");
            sb.append("- 습도: ").append(String.format("%.1f%%", userWeatherResponse.humidity())).append("\n");
            sb.append("- 풍속: ").append(String.format("%.1f m/s", userWeatherResponse.windSpeed())).append("\n");
            sb.append("- 강수량: ").append(String.format("%.1f mm", userWeatherResponse.rainfall())).append("\n");
        } else {
            sb.append("- 날씨: 날씨 데이터를 가져올 수 없습니다.\n");
        }

        // 사용자 데이터
        sb.append("- 성별: ").append(user.getGender() != null ? user.getGender().name() : "지정되지 않음").append("\n");
        sb.append("- 선호 스타일: ")
                .append(StringUtils.hasText(user.getStylePreference()) ? user.getStylePreference() : "지정되지 않음")
                .append("\n");

        // 피드백 데이터
        if (userFeedbackItems != null && !userFeedbackItems.isEmpty()) {
            sb.append("- 최근 피드백:\n");
            for (Product product : userFeedbackItems) {
                sb.append("  - ").append(product.getName()).append("\n"); // Product에는 getCategory()가 없으므로 name만 사용
            }
        }

        sb.append("\n### 작업 ###\n");
        sb.append("위 정보를 바탕으로 시스템 프롬프트에 정의된 사고의 사슬(Chain of Thought)과 출력 형식에 따라 의상 추천을 제공하세요.");

        return sb.toString();
    }

    /**
     * AI 모델을 사용하여 옷차림을 추천받습니다.
     *
     * @param user                사용자 정보
     * @param userWeatherResponse 사용자 날씨 정보 DTO
     * @param userFeedbackItems   사용자 피드백 기반 아이템 목록
     * @return AI 모델의 옷차림 추천 결과 문자열
     */
    private String getAiOutfitRecommendation(
            User user,
            UserWeatherResponse userWeatherResponse,
            List<Product> userFeedbackItems
    ) {
        String userPrompt = buildOutfitUserPrompt(user, userWeatherResponse, userFeedbackItems);

        List<Message> messages = List.of(
                new SystemMessage(getOutfitSystemPrompt()),
                new UserMessage(userPrompt)
        );

        Prompt prompt = new Prompt(messages);

        String aiResponse = chatClient.prompt(prompt)
                .call()
                .content();
        return aiResponse;
    }

    public void addFeedback(
            Long userId,
            Long itemId,
            LikeStatus likeStatus
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        Product product = productRepository.findById(itemId)
                .orElseThrow(() -> new GlobalException(OutfitErrorCode.ITEM_NOT_FOUND));

        Optional<UserItemFeedback> existingFeedback = userItemFeedbackRepository.findByUserIdAndProductId(userId,
                itemId);

        existingFeedback.ifPresentOrElse(
                feedback -> {
                    if (feedback.getLikeStatus() != likeStatus) {
                        feedback.setLikeStatus(likeStatus);
                    }
                },
                () -> {
                    UserItemFeedback newFeedback = UserItemFeedback.create(user, product, likeStatus);
                    userItemFeedbackRepository.save(newFeedback);
                }
        );
    }

    public void removeFeedback(
            Long userId,
            Long itemId,
            LikeStatus likeStatus
    ) {
        userItemFeedbackRepository.findByUserIdAndProductId(userId, itemId)
                .ifPresent(feedback -> {
                    if (feedback.getLikeStatus() == likeStatus) {
                        userItemFeedbackRepository.delete(feedback);
                    }
                });
    }
}
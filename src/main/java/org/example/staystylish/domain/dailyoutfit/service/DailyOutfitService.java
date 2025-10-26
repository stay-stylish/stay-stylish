package org.example.staystylish.domain.dailyoutfit.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.staystylish.common.exception.GlobalException;
import org.example.staystylish.domain.dailyoutfit.code.DailyOutfitErrorCode;
import org.example.staystylish.domain.dailyoutfit.enums.ShoppingMallLink;
import org.example.staystylish.domain.dailyoutfit.dto.request.FeedbackInfoRequest;
import org.example.staystylish.domain.dailyoutfit.dto.response.DailyOutfitRecommendationResponse;
import org.example.staystylish.domain.dailyoutfit.entity.UserItemFeedback;
import org.example.staystylish.domain.dailyoutfit.enums.LikeStatus;
import org.example.staystylish.domain.dailyoutfit.repository.UserItemFeedbackRepository;
import org.example.staystylish.domain.localweather.dto.GpsRequest;
import org.example.staystylish.domain.localweather.dto.UserWeatherResponse;
import org.example.staystylish.domain.localweather.service.LocalWeatherService;
import org.example.staystylish.domain.productclassification.entity.Product;
import org.example.staystylish.domain.productclassification.repository.ProductClassificationRepository;
import org.example.staystylish.domain.productclassification.service.ProductClassificationService;
import org.example.staystylish.domain.user.code.UserErrorCode;
import org.example.staystylish.domain.user.entity.User;
import org.example.staystylish.domain.user.exception.UserException;
import org.example.staystylish.domain.user.repository.UserRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 의상 추천과 관련된 비즈니스 로직을 처리하는 서비스 클래스
 * 날씨 정보, 사용자 피드백, AI 모델을 활용하여 맞춤형 의상을 추천
 */
@Service
@Transactional
public class DailyOutfitService {

    private final UserItemFeedbackRepository userItemFeedbackRepository;
    private final ProductClassificationRepository productClassificationRepository;
    private final UserRepository userRepository;
    private final LocalWeatherService weatherService;
    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;
    private final ProductClassificationService productClassificationService;

    public DailyOutfitService(UserItemFeedbackRepository userItemFeedbackRepository, ProductClassificationRepository productClassificationRepository,
                              UserRepository userRepository, LocalWeatherService weatherService, ChatClient chatClient,  ProductClassificationService productClassificationService,
                              ObjectMapper objectMapper) {
        this.userItemFeedbackRepository = userItemFeedbackRepository;
        this.productClassificationRepository = productClassificationRepository;
        this.userRepository = userRepository;
        this.weatherService = weatherService;
        this.chatClient = chatClient;
        this.objectMapper = objectMapper;
        this.productClassificationService = productClassificationService;
    }

    @Transactional(readOnly = true)
    public DailyOutfitRecommendationResponse getOutfitRecommendation(Long userId, Double latitude, Double longitude) {

        // 1. 사용자 정보 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        // 2. 날씨 정보 조회 (localweather 도메인 사용)
        GpsRequest gpsRequest = new GpsRequest(latitude, longitude);
        UserWeatherResponse todayWeather = weatherService.getWeatherByLatLon(gpsRequest)
                .blockOptional()
                .orElseThrow(() -> new GlobalException(DailyOutfitErrorCode.WEATHER_INFO_NOT_FOUND));

        // 3. 최근 피드백 조회 (최신순 정렬)
        List<FeedbackInfoRequest> recentFeedbacks = userItemFeedbackRepository.findByUserId(userId)
                .stream()
                .sorted((f1, f2) -> f2.getCreatedAt().compareTo(f1.getCreatedAt())) // 최신순으로 정렬
                .limit(5) // 5개만 선택
                .map(feedback -> new FeedbackInfoRequest(feedback.getProduct().getName(),
                        feedback.getLikeStatus().name()))
                .collect(Collectors.toList());

        // 4. 프롬프트 생성
        String systemPrompt = getSystemPrompt();
        String userPrompt = buildUserPrompt(user, todayWeather, recentFeedbacks);

        // 5. AI 호출
        String jsonResponse = chatClient.prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .call()
                .content();

        // 6. AI 응답 파싱
        try {
            // AI 응답에서 JSON 부분만 추출
            int startIndex = jsonResponse.indexOf('{');
            int endIndex = jsonResponse.lastIndexOf('}');
            if (startIndex != -1 && endIndex != -1 && startIndex < endIndex) {
                jsonResponse = jsonResponse.substring(startIndex, endIndex + 1);
            }

            DailyOutfitRecommendationResponse response = objectMapper.readValue(jsonResponse, DailyOutfitRecommendationResponse.class);

            return response;
        } catch (Exception e) {
            // 파싱 실패 시, 대체 응답 반환
            DailyOutfitRecommendationResponse fallbackResponse = DailyOutfitRecommendationResponse.from(
                    "AI 응답을 처리하는 데 문제가 발생했습니다. 잠시 후 다시 시도해주세요.", List.of());

            return fallbackResponse;
        }
    }

    // 변환용 메서드
    @Transactional(readOnly = true)
    public DailyOutfitRecommendationResponse getOutfitRecommendationWithLinks(Long userId, Double latitude, Double longitude) {

        // 1️⃣ 기존 GPS 기반 추천 로직 수행 (여기서 AI 추천 응답을 받아야 함)
        DailyOutfitRecommendationResponse aiRecommendation = getOutfitRecommendation(userId, latitude, longitude);
        String recommendationText = aiRecommendation.recommendationText();
        List<String> recommendedCategories = aiRecommendation.recommendedCategories();

        // 2️⃣ 추천 카테고리별로 쇼핑몰 링크 생성
        List<String> categoriesWithLinks = recommendedCategories.stream()
                .map(category -> {
                    // 각 쇼핑몰의 링크를 생성하여 문자열로 조합
                    StringBuilder linkBuilder = new StringBuilder(category + ": ");
                    for (ShoppingMallLink mall : ShoppingMallLink.values()) {
                        String url = mall.getUrl(category);
                        linkBuilder.append(mall.name()).append("=").append(url).append("; ");
                    }
                    return linkBuilder.toString().trim();
                })
                .collect(Collectors.toList());

        // 3️⃣ AI 응답에 링크가 포함된 리스트를 담아 반환
        // 실제로는 새로운 DTO를 만들거나, DailyOutfitRecommendationResponse의 구조를 변경
        // 현재는 편의상 문자열에 링크 정보를 모두 포함
        return DailyOutfitRecommendationResponse.from(recommendationText, categoriesWithLinks);
    }


    // AI 시스템 프롬프트를 반환합니다.
    private String getSystemPrompt() {

        return """
                당신은 '일타 패션'입니다. 친절하고 트렌디한 패션 추천 도우미입니다.
                당신의 목표는 사용자 정보와 오늘의 날씨를 기반으로 개인화된 의상 추천을 제공하는 것입니다.
                엄격한 사고의 사슬(Chain of Thought)을 따라야 합니다: 첫째, 날씨를 분석합니다. 둘째, 사용자 프로필과 피드백을 분석합니다. 셋째, 추천으로 결론을 내립니다.
                최종 출력은 추가 텍스트나 설명 없이 단일하고 원시적인 JSON 객체여야 합니다. JSON 객체는 다음 두 가지 키를 포함해야 합니다:
                1. `recommendation_text`: 친근하고 대화적인 한국어로 작성된 추천 메시지.
                2. `recommended_categories`: 2-3개의 특정 의류 아이템 카테고리 목록 (예: ["반팔 티셔츠", "데님 팬츠", "스니커즈"]).
                """;
    }

    // AI 사용자 프롬프트를 생성합니다.
    private String buildUserPrompt(User user, UserWeatherResponse weather, List<FeedbackInfoRequest> feedbacks) {

        StringBuilder sb = new StringBuilder();

        sb.append("### 사용자 및 날씨 정보 ###\n");

        // 날씨 데이터 추가
        if (weather != null) {
            sb.append("- 날씨: ").append(weather.sky()).append(" (").append(weather.pty()).append(")\n");
            sb.append("- 온도: ").append(String.format("%.1f°C", weather.temperature())).append("\n");
            sb.append("- 습도: ").append(String.format("%.1f%%", weather.humidity())).append("\n");
            sb.append("- 시간당 강수량: ").append(weather.rainfall()).append("mm\n");
        } else {
            sb.append("- 날씨: 날씨 데이터를 가져올 수 없습니다.\n");
        }

        // 사용자 데이터 추가
        sb.append("- 성별: ").append(user.getGender() != null ? user.getGender().name() : "지정되지 않음").append("\n");
        sb.append("- 선호 스타일: ")
                .append(StringUtils.hasText(user.getStylePreference()) ? user.getStylePreference() : "지정되지 않음")
                .append("\n");

        // 피드백 데이터 추가
        if (feedbacks != null && !feedbacks.isEmpty()) {
            sb.append("- 최근 피드백:\n");
            for (FeedbackInfoRequest feedback : feedbacks) {
                sb.append("  - ").append(feedback.productName()).append(": ").append(feedback.likeStatus())
                        .append("\n");
            }
        }

        sb.append("\n### 작업 ###\n");
        sb.append("위 정보를 바탕으로 시스템 프롬프트에 정의된 사고의 사슬(Chain of Thought)과 출력 형식에 따라 의상 추천을 제공하세요.");

        return sb.toString();
    }

    // 사용자 피드백을 생성하거나 업데이트합니다.
    public void createFeedback(Long userId, Long itemId, LikeStatus likeStatus) {

        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        // 상품 조회
        Product product = productClassificationRepository.findById(itemId)
                .orElseThrow(() -> new GlobalException(DailyOutfitErrorCode.ITEM_NOT_FOUND));

        // 기존 피드백 조회
        Optional<UserItemFeedback> existingFeedback = userItemFeedbackRepository.findByUserIdAndProductId(userId, itemId);

        // 피드백이 존재하면 업데이트, 없으면 새로 생성
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

    // 사용자 피드백을 삭제합니다.
    public void deleteFeedback(Long userId, Long itemId, LikeStatus likeStatus) {

        // 사용자 피드백 조회 후 삭제
        userItemFeedbackRepository.findByUserIdAndProductId(userId, itemId)
                .ifPresent(feedback -> {
                    if (feedback.getLikeStatus() == likeStatus) {
                        userItemFeedbackRepository.delete(feedback);
                    }
                });
    }
}

package org.example.staystylish.domain.outfit.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.staystylish.common.exception.GlobalException;
import org.example.staystylish.domain.outfit.dto.request.FeedbackInfoRequest;
import org.example.staystylish.domain.outfit.dto.response.OutfitRecommendationResponse;
import org.example.staystylish.domain.outfit.entity.UserItemFeedback;
import org.example.staystylish.domain.outfit.enums.LikeStatus;
import org.example.staystylish.domain.outfit.exception.OutfitErrorCode;
import org.example.staystylish.domain.outfit.repository.UserItemFeedbackRepository;
import org.example.staystylish.domain.product.entity.Product;
import org.example.staystylish.domain.product.repository.ProductRepository;
import org.example.staystylish.domain.user.entity.User;
import org.example.staystylish.domain.user.exception.UserErrorCode;
import org.example.staystylish.domain.user.exception.UserException;
import org.example.staystylish.domain.user.repository.UserRepository;
import org.example.staystylish.domain.weather.client.WeatherApiClient;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
    private final WeatherApiClient weatherApiClient;
    private final ChatClient.Builder chatClientBuilder;
    private final ObjectMapper objectMapper;

    public OutfitService(UserItemFeedbackRepository userItemFeedbackRepository, ProductRepository productRepository, UserRepository userRepository, WeatherApiClient weatherApiClient, ChatClient.Builder chatClientBuilder, ObjectMapper objectMapper) {
        this.userItemFeedbackRepository = userItemFeedbackRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.weatherApiClient = weatherApiClient;
        this.chatClientBuilder = chatClientBuilder;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public OutfitRecommendationResponse getOutfitRecommendation(Long userId) {
        // 1. 사용자 정보 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        // 2. 날씨 정보 조회
        String location = StringUtils.hasText(user.getRegion()) ? user.getRegion() : "Seoul";
        LocalDate today = LocalDate.now();
        WeatherApiClient.Daily todayWeather = weatherApiClient.getDailyForecast(location, today, today)
                .blockOptional()
                .flatMap(list -> list.stream().findFirst())
                .orElse(null);

        // 3. 최근 피드백 조회
        List<FeedbackInfoRequest> recentFeedbacks = userItemFeedbackRepository.findRecentFeedbackByUserId(userId, PageRequest.of(0, 5))
                .stream()
                .map(feedback -> new FeedbackInfoRequest(feedback.getProduct().getName(), feedback.getLikeStatus().name()))
                .collect(Collectors.toList());

        // 4. 프롬프트 생성
        ChatClient chatClient = chatClientBuilder.build();
        String systemPrompt = getSystemPrompt();
        String userPrompt = buildUserPrompt(user, todayWeather, recentFeedbacks);

        // 5. AI 호출 및 응답 파싱
        String jsonResponse = chatClient.prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .call()
                .content();

        try {
            // AI 응답에서 JSON 부분 추출
            int startIndex = jsonResponse.indexOf('{');
            int endIndex = jsonResponse.lastIndexOf('}');
            if (startIndex != -1 && endIndex != -1 && startIndex < endIndex) {
                jsonResponse = jsonResponse.substring(startIndex, endIndex + 1);
            }
            OutfitRecommendationResponse response = objectMapper.readValue(jsonResponse, OutfitRecommendationResponse.class);
            return response;
        } catch (Exception e) {
            // 파싱 실패 시, 대체 응답 반환
            // AI 응답 형식이 잘못된 경우 사용자에게 오류가 표시되지 않도록 방지
            OutfitRecommendationResponse fallbackResponse = OutfitRecommendationResponse.from("AI 응답을 처리하는 데 문제가 발생했습니다. 잠시 후 다시 시도해주세요.", List.of());
            return fallbackResponse;
        }
    }

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

    private String buildUserPrompt(
            User user,
            WeatherApiClient.Daily weather,
            List<FeedbackInfoRequest> feedbacks
    ) {
        StringBuilder sb = new StringBuilder();

        sb.append("### 사용자 및 날씨 정보 ###\n");

        // 날씨 데이터
        if (weather != null) {
            sb.append("- 날씨: ").append(weather.conditionText()).append("\n");
            sb.append("- 온도: ").append(String.format("%.1f°C", weather.avgTempC())).append("\n");
            sb.append("- 습도: ").append(String.format("%.1f%%", weather.avgHumidity())).append("\n");
            sb.append("- 강수 확률: ").append(weather.rainChance()).append("%\n");
        } else {
            sb.append("- 날씨: 날씨 데이터를 가져올 수 없습니다.\n");
        }

        // 사용자 데이터
        sb.append("- 성별: ").append(user.getGender() != null ? user.getGender().name() : "지정되지 않음").append("\n");
        sb.append("- 선호 스타일: ").append(StringUtils.hasText(user.getStylePreference()) ? user.getStylePreference() : "지정되지 않음").append("\n");

        // 피드백 데이터
        if (feedbacks != null && !feedbacks.isEmpty()) {
            sb.append("- 최근 피드백:\n");
            for (FeedbackInfoRequest feedback : feedbacks) {
                sb.append("  - ").append(feedback.productName()).append(": ").append(feedback.likeStatus()).append("\n");
            }
        }

        sb.append("\n### 작업 ###\n");
        sb.append("위 정보를 바탕으로 시스템 프롬프트에 정의된 사고의 사슬(Chain of Thought)과 출력 형식에 따라 의상 추천을 제공하세요.");

        return sb.toString();
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

        Optional<UserItemFeedback> existingFeedback = userItemFeedbackRepository.findByUserIdAndProductId(userId, itemId);

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
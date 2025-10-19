package org.example.staystylish.domain.outfit.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.staystylish.domain.outfit.dto.internal.FeedbackInfo;
import org.example.staystylish.domain.outfit.dto.response.OutfitRecommendationResponse;
import org.example.staystylish.domain.outfit.exception.ItemNotFoundException;
import org.example.staystylish.domain.outfit.model.LikeStatus;
import org.example.staystylish.domain.outfit.model.UserItemFeedback;
import org.example.staystylish.domain.outfit.repository.UserItemFeedbackRepository;
import org.example.staystylish.domain.product.entity.Product;
import org.example.staystylish.domain.product.repository.ProductRepository;
import org.example.staystylish.domain.user.entity.User;
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

    public OutfitRecommendationResponse getOutfitRecommendation(Long userId) {
        // 1. Fetch User
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // 2. Fetch Weather
        String location = StringUtils.hasText(user.getRegion()) ? user.getRegion() : "Seoul";
        LocalDate today = LocalDate.now();
        WeatherApiClient.Daily todayWeather = weatherApiClient.getDailyForecast(location, today, today)
                .blockOptional()
                .flatMap(list -> list.stream().findFirst())
                .orElse(null);

        // 3. Fetch Recent Feedback
        List<FeedbackInfo> recentFeedbacks = userItemFeedbackRepository.findRecentFeedbackByUserId(userId, PageRequest.of(0, 5))
                .stream()
                .map(feedback -> new FeedbackInfo(feedback.getProduct().getName(), feedback.getLikeStatus().name()))
                .collect(Collectors.toList());

        // 4. Construct Prompt
        ChatClient chatClient = chatClientBuilder.build();
        String systemPrompt = getSystemPrompt();
        String userPrompt = buildUserPrompt(user, todayWeather, recentFeedbacks);

        // 5. Call AI & Parse Response
        String jsonResponse = chatClient.prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .call()
                .content();

        try {
            // Extract JSON part from AI response
            int startIndex = jsonResponse.indexOf('{');
            int endIndex = jsonResponse.lastIndexOf('}');
            if (startIndex != -1 && endIndex != -1 && startIndex < endIndex) {
                jsonResponse = jsonResponse.substring(startIndex, endIndex + 1);
            }
            return objectMapper.readValue(jsonResponse, OutfitRecommendationResponse.class);
        } catch (Exception e) {
            // In case of parsing failure, return a fallback response
            // This prevents the user from seeing an error if the AI response is malformed
            return new OutfitRecommendationResponse("AI 응답을 처리하는 데 문제가 발생했습니다. 잠시 후 다시 시도해주세요.", List.of());
        }
    }

    private String getSystemPrompt() {
        return """
        You are 'Ilta Fashion', a friendly and trendy fashion recommendation assistant.
        Your goal is to provide a personalized outfit recommendation based on the user's information and today's weather.
        You must follow a strict Chain of Thought: First, analyze the weather. Second, analyze the user's profile and feedback. Third, conclude with a recommendation.
        Your final output must be a single, raw JSON object with no extra text or explanations. The JSON object must have two keys:
        1. `recommendation_text`: A friendly, conversational recommendation message written in Korean.
        2. `recommended_categories`: A list of 2-3 specific clothing item categories in Korean (e.g., ["반팔 티셔츠", "데님 팬츠", "스니커즈"]).
        """;
    }

    private String buildUserPrompt(User user, WeatherApiClient.Daily weather, List<FeedbackInfo> feedbacks) {
        StringBuilder sb = new StringBuilder();
        sb.append("### User & Weather Information ###\n");

        // Weather Data
        if (weather != null) {
            sb.append("- Weather: ").append(weather.conditionText()).append("\n");
            sb.append("- Temperature: ").append(String.format("%.1f°C", weather.avgTempC())).append("\n");
            sb.append("- Humidity: ").append(String.format("%.1f%%", weather.avgHumidity())).append("\n");
            sb.append("- Chance of Rain: ").append(weather.rainChance()).append("%\n");
        } else {
            sb.append("- Weather: Unable to fetch weather data.\n");
        }

        // User Data
        sb.append("- Gender: ").append(user.getGender() != null ? user.getGender().name() : "Not specified").append("\n");
        sb.append("- Preferred Style: ").append(StringUtils.hasText(user.getStylePreference()) ? user.getStylePreference() : "Not specified").append("\n");

        // Feedback Data
        if (feedbacks != null && !feedbacks.isEmpty()) {
            sb.append("- Recent Feedback:\n");
            for (FeedbackInfo feedback : feedbacks) {
                sb.append("  - ").append(feedback.productName()).append(": ").append(feedback.likeStatus()).append("\n");
            }
        }

        sb.append("\n### Task ###\n");
        sb.append("Based on the information above, provide an outfit recommendation following the Chain of Thought and output format defined in the system prompt.");

        return sb.toString();
    }

    public void addFeedback(Long userId, Long itemId, LikeStatus likeStatus) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Product product = productRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException("피드백할 아이템을 찾을 수 없습니다."));

        Optional<UserItemFeedback> existingFeedback = userItemFeedbackRepository.findByUserIdAndProductId(userId, itemId);

        existingFeedback.ifPresentOrElse(
                feedback -> {
                    if (feedback.getLikeStatus() != likeStatus) {
                        feedback.setLikeStatus(likeStatus);
                    }
                },
                () -> {
                    UserItemFeedback newFeedback = UserItemFeedback.builder()
                            .user(user)
                            .product(product)
                            .likeStatus(likeStatus)
                            .build();
                    userItemFeedbackRepository.save(newFeedback);
                }
        );
    }

    public void removeFeedback(Long userId, Long itemId, LikeStatus likeStatus) {
        userItemFeedbackRepository.findByUserIdAndProductId(userId, itemId)
                .ifPresent(feedback -> {
                    if (feedback.getLikeStatus() == likeStatus) {
                        userItemFeedbackRepository.delete(feedback);
                    }
                });
    }
}

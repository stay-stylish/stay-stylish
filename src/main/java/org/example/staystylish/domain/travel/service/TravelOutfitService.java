package org.example.staystylish.domain.travel.service;

import static java.time.temporal.ChronoUnit.DAYS;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.example.staystylish.common.exception.GlobalException;
import org.example.staystylish.domain.travel.ai.TravelAiClient;
import org.example.staystylish.domain.travel.ai.TravelAiPromptBuilder;
import org.example.staystylish.domain.travel.consts.TravelOutfitErrorCode;
import org.example.staystylish.domain.travel.dto.request.TravelOutfitRequest;
import org.example.staystylish.domain.travel.dto.response.AiTravelJson;
import org.example.staystylish.domain.travel.dto.response.TravelOutfitResponse;
import org.example.staystylish.domain.travel.dto.response.TravelOutfitResponse.AiOutfit;
import org.example.staystylish.domain.travel.entity.TravelOutfit;
import org.example.staystylish.domain.travel.repository.TravelOutfitRepository;
import org.example.staystylish.domain.user.entity.Gender;
import org.example.staystylish.domain.weather.client.WeatherApiClient;
import org.example.staystylish.domain.weather.client.WeatherApiClient.Daily;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class TravelOutfitService {

    private final WeatherApiClient weatherApiClient;
    private final TravelOutfitRepository travelOutfitRepository;
    private final TravelAiClient aiClient;
    private final TravelAiPromptBuilder promptBuilder;
    private final ObjectMapper objectMapper;

    @Transactional
    public TravelOutfitResponse createRecommendation(Long userId,
                                                     TravelOutfitRequest request,
                                                     Gender gender) {

        // 기간 검증 로직 (여행 기간이 1일 미만이거나 14일 초과 시 예외 처리 -> 날씨 API가 14일 예보만 가능)
        long days = DAYS.between(request.startDate(), request.endDate()) + 1;
        if (days < 1 || days > 14) {
            throw new GlobalException(TravelOutfitErrorCode.INVALID_PERIOD);
        }

        // 날씨 조회 로직
        Mono<List<Daily>> mono = weatherApiClient.getDailyForecast(
                request.city(), request.startDate(), request.endDate());

        List<Daily> dailyList = mono.block();
        if (dailyList == null || dailyList.isEmpty()) {
            throw new GlobalException(TravelOutfitErrorCode.WEATHER_FETCH_FAILED);
        }

        // 여행 일 기간 동안의 날씨 정보 평균 계산
        double totalTemp = 0.0;
        double totalHumidity = 0.0;
        double totalRainProb = 0.0;

        for (Daily daily : dailyList) {
            Double temp = daily.avgTempC();
            Double humidity = daily.avgHumidity();
            Integer rainChance = daily.rainChance();

            if (temp != null) {
                totalTemp += temp;
            }
            if (humidity != null) {
                totalHumidity += humidity;
            }
            if (rainChance != null) {
                totalRainProb += rainChance;
            }
        }

        int daysSize = dailyList.size();
        double avgTemp = (daysSize > 0) ? totalTemp / daysSize : 0.0;
        int avgHumidity = (daysSize > 0) ? (int) Math.round(totalHumidity / daysSize) : 0;
        int avgRainProb = (daysSize > 0) ? (int) Math.round(totalRainProb / daysSize) : 0;

        // 여행 일중 가장 많이 나타나는 날씨 상태를 찾음
        String condition = dailyList.stream()
                .map(Daily::conditionText)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(s -> s, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("알 수 없음");

        // Gender ENUM을 한글 문자열 변환
        String userGender = toKorean(gender);

        // 프롬프트 생성
        String prompt = promptBuilder.buildPrompt(
                request.country(), request.city(),
                request.startDate(), request.endDate(),
                userGender, condition, avgTemp, avgRainProb
        );

        // ai 호출 및 파싱
        String aiJson;
        AiTravelJson aiTravelJson;

        try {
            aiJson = aiClient.callForJson(prompt); // AI 호출하여 JSON 문자열 받기
            aiTravelJson = aiClient.parse(aiJson); // json 문자열을 파싱해서 객체로 변환
        } catch (Exception e) {
            throw new GlobalException(TravelOutfitErrorCode.AI_PARSE_FAILED);
        }

        // AI 응답 및 문화 정보 등을 DB에 저장하기 위해서 JsonNode 형태로 변환
        var aiOutfit = new AiOutfit(aiTravelJson.summary(), aiTravelJson.outfits());
        var culturalConstraints = aiTravelJson.culturalConstraints();
        var aiNode = objectMapper.valueToTree(aiOutfit);
        var cultureNode = objectMapper.valueToTree(culturalConstraints);

        // 엔티티 생성
        var entity = TravelOutfit.create(userId, request.country(), request.city(),
                request.startDate(), request.endDate(),
                avgTemp, avgHumidity, avgRainProb, condition, cultureNode, aiNode);

        // DB 저장
        var saved = travelOutfitRepository.save(entity);

        // DTO 생성
        return new TravelOutfitResponse(saved.getId(), saved.getUserId(), saved.getCountry(),
                saved.getCity(), saved.getStartDate(), saved.getEndDate(),
                new TravelOutfitResponse.WeatherSummary(
                        saved.getAvgTemperature(),
                        saved.getAvgHumidity(),
                        saved.getRainProbability(),
                        saved.getCondition()
                ),
                culturalConstraints,
                aiOutfit,
                aiTravelJson.safetyNotes(),
                saved.getCreatedAt()
        );
    }

    private String toKorean(Gender gender) {

        return switch (gender) {
            case FEMALE -> "여성";
            case MALE -> "남성";
        };
    }
}

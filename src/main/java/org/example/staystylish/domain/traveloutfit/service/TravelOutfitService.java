package org.example.staystylish.domain.traveloutfit.service;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.util.Comparator.comparing;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.example.staystylish.common.exception.GlobalException;
import org.example.staystylish.domain.globalweather.client.WeatherApiClient;
import org.example.staystylish.domain.globalweather.client.WeatherApiClient.Daily;
import org.example.staystylish.domain.traveloutfit.ai.TravelAiClient;
import org.example.staystylish.domain.traveloutfit.ai.TravelAiPromptBuilder;
import org.example.staystylish.domain.traveloutfit.consts.TravelOutfitErrorCode;
import org.example.staystylish.domain.traveloutfit.dto.request.TravelOutfitRequest;
import org.example.staystylish.domain.traveloutfit.dto.response.AiTravelJson;
import org.example.staystylish.domain.traveloutfit.dto.response.TravelOutfitDetailResponse;
import org.example.staystylish.domain.traveloutfit.dto.response.TravelOutfitResponse;
import org.example.staystylish.domain.traveloutfit.dto.response.TravelOutfitResponse.AiOutfit;
import org.example.staystylish.domain.traveloutfit.dto.response.TravelOutfitResponse.RainAdvisory;
import org.example.staystylish.domain.traveloutfit.dto.response.TravelOutfitSummaryResponse;
import org.example.staystylish.domain.traveloutfit.entity.TravelOutfit;
import org.example.staystylish.domain.traveloutfit.repository.TravelOutfitRepository;
import org.example.staystylish.domain.user.entity.Gender;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TravelOutfitService {

    private static final int MAX_FORECAST_DAYS = 14;
    private static final int MUST_UMBRELLA = 70;
    private static final int PACK_UMBRELLA = 30;
    private final WeatherApiClient weatherApiClient;
    private final TravelOutfitRepository travelOutfitRepository;
    private final TravelAiClient aiClient;
    private final TravelAiPromptBuilder promptBuilder;
    private final ObjectMapper objectMapper;

    @Transactional
    public TravelOutfitResponse createRecommendation(Long userId,
                                                     TravelOutfitRequest request,
                                                     Gender gender) {

        // 기간 검증 로직1 (종료일이 시작일보다 앞이면 오류 발생)
        final LocalDate start = request.startDate();
        final LocalDate end = request.endDate();

        if (end.isBefore(start)) {
            throw new GlobalException(TravelOutfitErrorCode.INVALID_PERIOD);
        }

        // 기간 검증 로직2 (14일 초과 시 예외 처리 -> 날씨 API가 14일 예보만 가능)
        final long days = DAYS.between(start, end) + 1;
        if (days > MAX_FORECAST_DAYS) {
            throw new GlobalException(TravelOutfitErrorCode.INVALID_PERIOD);
        }

        // 날씨 조회 로직
        List<Daily> dailyList = weatherApiClient.getDailyForecast(request.city(), start, end);

        if (dailyList == null || dailyList.isEmpty()) {
            throw new GlobalException(TravelOutfitErrorCode.WEATHER_FETCH_FAILED);
        }

        // 여행 일 기간 동안의 날씨 정보 평균 계산
        double totalTemp = 0.0;
        double totalHumidity = 0.0;
        double totalRainProb = 0.0;

        for (Daily daily : dailyList) {
            if (daily.avgTempC() != null) {
                totalTemp += daily.avgTempC();
            }
            if (daily.avgHumidity() != null) {
                totalHumidity += daily.avgHumidity();
            }
            if (daily.rainChance() != null) {
                totalRainProb += daily.rainChance();
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

        // 일자별 우산 가이드, 요약문 생성 로직 호출
        List<RainAdvisory> advisories = toRainAdvisories(dailyList);
        String umbrellaSummary = buildUmbrellaSummary(advisories);

        // 프롬프트 생성
        String prompt = promptBuilder.buildPrompt(
                request.country(), request.city(),
                request.startDate(), request.endDate(),
                userGender, condition, avgTemp, umbrellaSummary
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
                        saved.getCondition(),
                        advisories,
                        umbrellaSummary
                ),
                culturalConstraints,
                aiOutfit,
                aiTravelJson.safetyNotes(),
                saved.getCreatedAt()
        );
    }

    @Transactional(readOnly = true)
    public Page<TravelOutfitSummaryResponse> getMyRecommendationsSummary(Long userId, Pageable pageable) {

        Page<TravelOutfit> page = travelOutfitRepository.findByUserId(userId, pageable);

        return page.map(TravelOutfitSummaryResponse::from);
    }

    @Transactional(readOnly = true)
    public TravelOutfitDetailResponse getRecommendationDetail(Long userId, Long travelId) {

        TravelOutfit outfit = travelOutfitRepository.findByIdAndUserId(travelId, userId)
                .orElseThrow(() -> new GlobalException(TravelOutfitErrorCode.RECOMMENDATION_NOT_FOUND));

        return TravelOutfitDetailResponse.from(outfit);
    }

    private String toKorean(Gender gender) {

        return switch (gender) {
            case FEMALE -> "여성";
            case MALE -> "남성";
        };
    }

    private List<RainAdvisory> toRainAdvisories(List<Daily> dailyList) {

        return dailyList.stream()
                .filter(d -> d != null && d.date() != null && d.rainChance() != null)
                .map(d -> {
                    int rainChance = d.rainChance();
                    String advice = (rainChance >= MUST_UMBRELLA) ? "우산 필수"
                            : (rainChance >= PACK_UMBRELLA) ? "우산 챙기면 좋아요"
                                    : "우산 없어도 무방해요";
                    return new RainAdvisory(d.date(), rainChance, advice);
                })
                .sorted(comparing(RainAdvisory::date))
                .toList();
    }

    private String buildUmbrellaSummary(List<RainAdvisory> list) {

        if (list == null || list.isEmpty()) {
            return null;
        }

        var formatter = DateTimeFormatter.ofPattern("M/d");

        return list.stream()
                .map(a -> a.date().format(formatter) + " (" + a.rainProbability() + "%) " + a.advice())
                .collect(Collectors.joining(" / "));
    }
}

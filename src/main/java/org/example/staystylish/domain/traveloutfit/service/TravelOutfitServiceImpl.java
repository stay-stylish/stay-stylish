package org.example.staystylish.domain.traveloutfit.service;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.util.Comparator.comparing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.staystylish.common.exception.GlobalException;
import org.example.staystylish.domain.globalweather.client.GlobalWeatherApiClient;
import org.example.staystylish.domain.globalweather.client.GlobalWeatherApiClient.Daily;
import org.example.staystylish.domain.traveloutfit.ai.TravelAiClient;
import org.example.staystylish.domain.traveloutfit.ai.TravelAiPromptBuilder;
import org.example.staystylish.domain.traveloutfit.code.TravelOutfitErrorCode;
import org.example.staystylish.domain.traveloutfit.dto.request.TravelOutfitRequest;
import org.example.staystylish.domain.traveloutfit.dto.response.AiTravelJsonResponse;
import org.example.staystylish.domain.traveloutfit.dto.response.TravelOutfitResponse;
import org.example.staystylish.domain.traveloutfit.dto.response.TravelOutfitResponse.AiOutfit;
import org.example.staystylish.domain.traveloutfit.dto.response.TravelOutfitResponse.CulturalConstraints;
import org.example.staystylish.domain.traveloutfit.dto.response.TravelOutfitResponse.RainAdvisory;
import org.example.staystylish.domain.traveloutfit.dto.response.TravelOutfitResponse.WeatherSummary;
import org.example.staystylish.domain.traveloutfit.dto.response.TravelOutfitSummaryResponse;
import org.example.staystylish.domain.traveloutfit.dto.response.WeatherAverages;
import org.example.staystylish.domain.traveloutfit.entity.RecommendationStatus;
import org.example.staystylish.domain.traveloutfit.entity.TravelOutfit;
import org.example.staystylish.domain.traveloutfit.repository.TravelOutfitRepository;
import org.example.staystylish.domain.user.entity.Gender;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 여행 옷차림 추천 도메인의 비즈니스 로직 추천 결과 생성/목록/상세 조회 제공
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TravelOutfitServiceImpl implements TravelOutfitService {

    private static final int MAX_FORECAST_DAYS = 14;
    private static final int MUST_UMBRELLA = 70;
    private static final int PACK_UMBRELLA = 30;
    private final GlobalWeatherApiClient globalWeatherApiClient;
    private final TravelOutfitRepository travelOutfitRepository;
    private final TravelAiClient aiClient;
    private final TravelAiPromptBuilder promptBuilder;
    private final ObjectMapper objectMapper;
    private final TravelOutfitStatusUpdater statusUpdater;
    private final WeatherAveragesCalculator weatherAveragesCalculator;

    // (동기) 추천 요청 접수
    public TravelOutfit requestRecommendation(Long userId, TravelOutfitRequest request) {

        log.info("추천 요청 접수: userId = {}, city = {}", userId, request.city());

        // 기간 검증 로직
        validatePeriod(request.startDate(), request.endDate());

        // 처리중 상태의 엔티티 생성
        TravelOutfit pendingOutfit = TravelOutfit.createPending(
                userId,
                request.country(),
                request.city(),
                request.startDate(),
                request.endDate()
        );

        return travelOutfitRepository.save(pendingOutfit);
    }

    // (비동기) 실제 추천 생성 로직 - 별도 스레드, 트랜잭션으로 실행
    @Async("travelRecommendationExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void processRecommendation(Long travelId, TravelOutfitRequest request, Gender gender) {

        log.info("추천 생성 시작: travelId={}", travelId);

        try {

            // 처리중인 상태의 엔티티 조회
            TravelOutfit outfit = travelOutfitRepository.findById(travelId)
                    .orElseThrow(() -> new GlobalException(TravelOutfitErrorCode.RECOMMENDATION_NOT_FOUND));

            // 날씨 조회 로직
            List<Daily> dailyList = globalWeatherApiClient.getDailyForecast(request.city(), request.startDate(),
                    request.endDate());

            if (dailyList == null || dailyList.isEmpty()) {
                throw new GlobalException(TravelOutfitErrorCode.WEATHER_FETCH_FAILED);
            }

            // 평균 날씨 계산 로직 호출
            WeatherAverages averages = weatherAveragesCalculator.calculate(dailyList);

            // Gender ENUM을 한글 문자열 변환
            String userGender = toKorean(gender);

            // 일자별 우산 가이드, 요약문 생성 로직 호출
            List<RainAdvisory> advisories = toRainAdvisories(dailyList);
            String umbrellaSummary = buildUmbrellaSummary(advisories);

            // 프롬프트 생성
            String prompt = promptBuilder.buildPrompt(
                    request.country(), request.city(),
                    request.startDate(), request.endDate(),
                    userGender, averages.condition(), averages.avgTemp(), umbrellaSummary
            );

            // ai 호출 및 파싱
            String aiJson = aiClient.callForJson(prompt);
            AiTravelJsonResponse aiTravelJsonResponse = aiClient.parse(aiJson);

            // AI 응답 및 문화 정보 등을 DB에 저장하기 위해서 JsonNode 형태로 변환
            var aiOutfit = new TravelOutfitResponse.AiOutfit(aiTravelJsonResponse.summary(),
                    aiTravelJsonResponse.outfits());
            var culturalConstraints = aiTravelJsonResponse.culturalConstraints();
            var aiNode = objectMapper.valueToTree(aiOutfit);
            var cultureNode = objectMapper.valueToTree(culturalConstraints);

            var safetyNotes = aiTravelJsonResponse.safetyNotes();
            var safetyNotesNode = objectMapper.valueToTree(safetyNotes);

            // 엔티티 상태를 성공으로 변경
            outfit.complete(averages.avgTemp(), averages.avgHumidity(), averages.avgRainProb(), averages.condition(),
                    cultureNode, aiNode,
                    safetyNotesNode, umbrellaSummary);
            travelOutfitRepository.save(outfit);
            log.info("추천 생성 완료: travelId={}", travelId);

        } catch (GlobalException ge) {

            // 모든 GlobalException을 여기서 처리
            log.error("추천 생성 실패 (GlobalException): travelId={}. 오류 코드: {}, 메시지: {}",
                    travelId, ge.getErrorCode(), ge.getMessage());

            // 별도 트랜잭션을 가진 statusUpdater를 호출하여 상태를 FAILED로 변경
            statusUpdater.updateStatusToFailed(travelId, ge.getErrorCode().getMessage());

        } catch (Exception e) {

            // 그 외 예기치 못한 Exception 처리
            log.error("추천 생성 중 알 수 없는 오류: travelId={}. 오류: {}", travelId, e.getMessage(), e);

            // 알 수 없는 오류에 대한 기본 메시지
            statusUpdater.updateStatusToFailed(travelId, "알 수 없는 서버 오류가 발생했습니다.");
        }
    }


    // 내 추천 목록(요약) 페이징 조회
    @Transactional(readOnly = true)
    public Page<TravelOutfitSummaryResponse> getMyRecommendationsSummary(Long userId, Pageable pageable) {

        Page<TravelOutfit> page = travelOutfitRepository.findByUserId(userId, pageable);

        return page.map(TravelOutfitSummaryResponse::from);
    }

    // 추천 상세 조회
    @Transactional(readOnly = true)
    public TravelOutfitResponse getRecommendationDetail(Long userId, Long travelId) {

        TravelOutfit outfit = travelOutfitRepository.findByIdAndUserId(travelId, userId)
                .orElseThrow(() -> new GlobalException(TravelOutfitErrorCode.RECOMMENDATION_NOT_FOUND));

        // 파싱할 객체들 초기화
        WeatherSummary summary = null;
        CulturalConstraints constraints = null;
        AiOutfit aiOutfit = null;
        List<String> notes = null;

        // 추천 상태가 완료된 경우에만 파싱 로직 수행
        if (outfit.getStatus() == RecommendationStatus.COMPLETED) {

            // WeatherSummary 생성
            summary = new WeatherSummary(
                    outfit.getAvgTemperature(),
                    outfit.getAvgHumidity(),
                    outfit.getRainProbability(),
                    outfit.getCondition(),
                    null,
                    outfit.getUmbrellaSummary()
            );

            // JSON 파싱
            try {
                TypeReference<List<String>> listTypeRef = new TypeReference<>() {
                };

                if (outfit.getCulturalConstraintsJson() != null) {
                    constraints = objectMapper.treeToValue(outfit.getCulturalConstraintsJson(),
                            CulturalConstraints.class);
                }
                if (outfit.getAiOutfitJson() != null) {
                    aiOutfit = objectMapper.treeToValue(outfit.getAiOutfitJson(),
                            AiOutfit.class);
                }
                if (outfit.getSafetyNotesJson() != null) {
                    notes = objectMapper.treeToValue(outfit.getSafetyNotesJson(), listTypeRef);
                }
            } catch (JsonProcessingException e) {
                // 파싱 실패 시, 서비스 레이어에서 예외 처리
                log.error("TravelOutfit JSON 파싱 실패: travelId={}, userId={}", travelId, userId, e);
                throw new GlobalException(TravelOutfitErrorCode.AI_PARSE_FAILED);
            }
        }

        return TravelOutfitResponse.from(outfit, summary, constraints, aiOutfit, notes);
    }

    // 성별을 한국어 문자열로 변환
    private String toKorean(Gender gender) {

        return switch (gender) {
            case FEMALE -> "여성";
            case MALE -> "남성";
        };
    }

    // 일자별 강수확률에 따른 우산 가이드 생성 메서드
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

    // 여러 일자의 우산 가이드를 요약 문자열로 변환 (예: 10/23 (80%) 우산 필수 / 10/24 (0%) 우산 없어도 됨)
    private String buildUmbrellaSummary(List<RainAdvisory> list) {

        if (list == null || list.isEmpty()) {
            return null;
        }

        var formatter = DateTimeFormatter.ofPattern("M/d");

        return list.stream()
                .map(a -> a.date().format(formatter) + " (" + a.rainProbability() + "%) " + a.advice())
                .collect(Collectors.joining(" / "));
    }

    private void validatePeriod(LocalDate start, LocalDate end) {

        // 기간 검증 로직1 (종료일이 시작일보다 앞이면 오류 발생)
        if (end.isBefore(start)) {
            throw new GlobalException(TravelOutfitErrorCode.INVALID_PERIOD);
        }

        // 기간 검증 로직2 (14일 초과 시 예외 처리 -> 날씨 API가 14일 예보만 가능)
        final long days = DAYS.between(start, end) + 1;
        if (days > MAX_FORECAST_DAYS) {
            throw new GlobalException(TravelOutfitErrorCode.INVALID_PERIOD);
        }
    }
}

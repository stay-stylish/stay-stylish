package org.example.staystylish.domain.traveloutfit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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
import org.example.staystylish.domain.traveloutfit.dto.response.TravelOutfitResponse.CulturalConstraints;
import org.example.staystylish.domain.traveloutfit.dto.response.TravelOutfitSummaryResponse;
import org.example.staystylish.domain.traveloutfit.entity.TravelOutfit;
import org.example.staystylish.domain.traveloutfit.repository.TravelOutfitRepository;
import org.example.staystylish.domain.user.entity.Gender;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class TravelOutfitServiceTest {

    private final Long USER_ID = 1L;
    private final Long TRAVEL_ID = 1L;
    private final LocalDate START_DATE = LocalDate.now().plusDays(1);
    private final LocalDate END_DATE = LocalDate.now().plusDays(3);
    private final String COUNTRY = "일본";
    private final String CITY = "Tokyo";
    private final Gender GENDER = Gender.MALE;
    private final JsonNode mockJsonNode = new ObjectMapper().createObjectNode();
    private final TravelOutfit mockOutfit = TravelOutfit.create(
            USER_ID, COUNTRY, CITY, START_DATE, END_DATE,
            20.0, 60, 10, "맑음", mockJsonNode, mockJsonNode
    );
    @Mock
    TravelAiClient aiClient;
    @InjectMocks // 실제 테스트 대상
    private TravelOutfitService travelOutfitService;
    @Mock
    private WeatherApiClient weatherApiClient;
    @Mock
    private TravelOutfitRepository travelOutfitRepository;
    @Mock
    private TravelAiPromptBuilder promptBuilder;
    @Mock
    private ObjectMapper objectMapper;

    /**
     * 여행 옷차림 추천 요청 API 테스트 코드
     */
    @Test
    @DisplayName("여행 옷차림 추천 생성 성공")
    void createRecommendation_Success() {

        // given
        var request = new TravelOutfitRequest(COUNTRY, CITY, START_DATE, END_DATE);
        String expectedAiJson = "{\"summary\":\"테스트 AI 응답\"}";

        // 날씨 Mock
        var weather = new Daily(START_DATE, 20.0, 60.0, 10, "맑음");
        when(weatherApiClient.getDailyForecast(CITY, START_DATE, END_DATE))
                .thenReturn(List.of(weather));

        // AI 프롬프트 Mock
        when(promptBuilder.buildPrompt(
                anyString(), anyString(), any(LocalDate.class), any(LocalDate.class),
                anyString(), anyString(), anyDouble(), anyString()
        )).thenReturn("테스트 프롬프트");
        when(aiClient.callForJson("테스트 프롬프트")).thenReturn(expectedAiJson);

        var mockCulturalConstraints = new CulturalConstraints("문화/종교 조건", Collections.emptyList());
        var aiTravelJson = new AiTravelJson("요약", Collections.emptyList(), mockCulturalConstraints,
                Collections.emptyList());

        when(aiClient.parse(expectedAiJson)).thenReturn(aiTravelJson);

        when(objectMapper.valueToTree(any(CulturalConstraints.class))).thenReturn(mockJsonNode);
        when(objectMapper.valueToTree(any(TravelOutfitResponse.AiOutfit.class))).thenReturn(mockJsonNode);

        // Repository Mock
        TravelOutfit saved = TravelOutfit.create(USER_ID, COUNTRY, CITY, START_DATE, END_DATE,
                20.0, 60, 10, "맑음", mockJsonNode, mockJsonNode);
        when(travelOutfitRepository.save(any(TravelOutfit.class))).thenReturn(saved);

        // when
        TravelOutfitResponse response = travelOutfitService.createRecommendation(USER_ID, request, GENDER);

        // then
        assertThat(response).isNotNull();
        assertThat(response.country()).isEqualTo(COUNTRY);
        assertThat(response.city()).isEqualTo(CITY);
        assertThat(response.weatherSummary().avgTemperature()).isEqualTo(20.0);
        assertThat(response.aiOutfitJson().summary()).isEqualTo("요약");

        verify(travelOutfitRepository).save(any(TravelOutfit.class));
    }

    @Test
    @DisplayName("여행 기간이 14일을 초과하는 경우 실패")
    void createRecommendation_Fail_Invalid_Period() {
        // given
        var invalidEndDate = START_DATE.plusDays(15);
        var request = new TravelOutfitRequest(COUNTRY, CITY, START_DATE, invalidEndDate);

        // when & then
        assertThatThrownBy(() -> travelOutfitService.createRecommendation(USER_ID, request, GENDER))
                .isInstanceOf(GlobalException.class)
                .hasMessage(TravelOutfitErrorCode.INVALID_PERIOD.getMessage());
    }

    @Test
    @DisplayName("여행 종료일이 시작일보다 빠른 경우 실패")
    void createRecommendation_Fail_EndDate_Before_StartDate() {

        // given
        var invalidEndDate = START_DATE.minusDays(1);
        var request = new TravelOutfitRequest(COUNTRY, CITY, START_DATE, invalidEndDate);

        // when & then
        assertThatThrownBy(() -> travelOutfitService.createRecommendation(USER_ID, request, GENDER))
                .isInstanceOf(GlobalException.class)
                .hasMessage(TravelOutfitErrorCode.INVALID_PERIOD.getMessage());
    }


    @Test
    @DisplayName("날씨 정보를 가져오지 못한 경우 실패")
    void createRecommendation_Fail_Weather_Fetch_Failed() {

        // given
        var request = new TravelOutfitRequest(COUNTRY, CITY, START_DATE, END_DATE);

        when(weatherApiClient.getDailyForecast(CITY, START_DATE, END_DATE))
                .thenReturn(Collections.emptyList());

        // when & then
        assertThatThrownBy(() -> travelOutfitService.createRecommendation(USER_ID, request, GENDER))
                .isInstanceOf(GlobalException.class)
                .hasMessage(TravelOutfitErrorCode.WEATHER_FETCH_FAILED.getMessage());
    }

    @Test
    @DisplayName("AI 응답 파싱에 실패한 경우")
    void createRecommendation_Fail_AiParseFailed() {

        // given
        var request = new TravelOutfitRequest(COUNTRY, CITY, START_DATE, END_DATE);

        // 날씨 Mock
        var weather = new Daily(START_DATE, 20.0, 60.0, 10, "맑음");
        when(weatherApiClient.getDailyForecast(CITY, START_DATE, END_DATE))
                .thenReturn(List.of(weather));

        // AI 프롬프트 Mock
        when(promptBuilder.buildPrompt(anyString(), anyString(), any(), any(), anyString(), anyString(), anyDouble(),
                anyString()))
                .thenReturn("테스트 프롬프트");

        // AI Client가 예외 발생
        when(aiClient.callForJson("테스트 프롬프트")).thenThrow(new IllegalStateException("AI 파싱 실패"));

        // when & then
        assertThatThrownBy(() -> travelOutfitService.createRecommendation(USER_ID, request, GENDER))
                .isInstanceOf(GlobalException.class)
                .hasMessage(TravelOutfitErrorCode.AI_PARSE_FAILED.getMessage());
    }

    /**
     * 추천 기록 목록 조회 테스트 코드
     */
    @Test
    @DisplayName("나의 추천 기록 목록 조회 성공")
    void getMyRecommendationsSummary_Success() {

        // given
        Pageable pageable = PageRequest.of(0, 5);

        Page<TravelOutfit> responsePage = new PageImpl<>(List.of(mockOutfit), pageable, 1);
        when(travelOutfitRepository.findByUserId(USER_ID, pageable)).thenReturn(responsePage);

        // when
        Page<TravelOutfitSummaryResponse> result = travelOutfitService.getMyRecommendationsSummary(USER_ID, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).city()).isEqualTo(CITY);
    }

    /**
     * 추천 기록 단건 상세 조회 테스트 코드
     */
    @Test
    @DisplayName("추천 기록 단건 상세 조회 성공")
    void getRecommendationDetail_Success() {

        // given
        when(travelOutfitRepository.findByIdAndUserId(TRAVEL_ID, USER_ID))
                .thenReturn(Optional.of(mockOutfit));

        // when
        TravelOutfitDetailResponse response = travelOutfitService.getRecommendationDetail(USER_ID, TRAVEL_ID);

        // then
        assertThat(response).isNotNull();
        assertThat(response.travelId()).isEqualTo(mockOutfit.getId());
        assertThat(response.city()).isEqualTo(mockOutfit.getCity());
    }

    @Test
    @DisplayName("존재 하지 않은 추천 기록 또는 권한이 없는 추천 기록 상세 조회 시 실패")
    void getRecommendationDetail_Fail_Not_Found() {

        // given
        when(travelOutfitRepository.findByIdAndUserId(TRAVEL_ID, USER_ID))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> travelOutfitService.getRecommendationDetail(USER_ID, TRAVEL_ID))
                .isInstanceOf(GlobalException.class)
                .hasMessage(TravelOutfitErrorCode.RECOMMENDATION_NOT_FOUND.getMessage());
    }
}

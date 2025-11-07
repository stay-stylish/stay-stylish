package org.example.staystylish.domain.traveloutfit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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
import org.example.staystylish.domain.traveloutfit.dto.response.TravelOutfitSummaryResponse;
import org.example.staystylish.domain.traveloutfit.entity.RecommendationStatus;
import org.example.staystylish.domain.traveloutfit.entity.TravelOutfit;
import org.example.staystylish.domain.traveloutfit.repository.TravelOutfitRepository;
import org.example.staystylish.domain.user.entity.Gender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class TravelOutfitServiceImplTest {

    private final Long USER_ID = 1L;
    private final Long TRAVEL_ID = 1L;
    private final LocalDate START_DATE = LocalDate.now().plusDays(1);
    private final LocalDate END_DATE = LocalDate.now().plusDays(3);
    private final String COUNTRY = "일본";
    private final String CITY = "Tokyo";
    private final Gender GENDER = Gender.MALE;
    private final JsonNode mockJsonNode = new ObjectMapper().createObjectNode();

    @Mock
    TravelAiClient aiClient;
    @InjectMocks // 실제 테스트 대상
    private TravelOutfitServiceImpl travelOutfitServiceImpl;
    @Mock
    private GlobalWeatherApiClient globalWeatherApiClient;
    @Mock
    private TravelOutfitRepository travelOutfitRepository;
    @Mock
    private TravelAiPromptBuilder promptBuilder;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private TravelOutfitStatusUpdater statusUpdater;

    private TravelOutfit pendingOutfit;
    private TravelOutfit completedOutfit;
    private TravelOutfitRequest request;
    private AiTravelJsonResponse mockAiResponse;

    @BeforeEach
    void setUp() throws JsonProcessingException {

        request = new TravelOutfitRequest(COUNTRY, CITY, START_DATE, END_DATE);
        pendingOutfit = TravelOutfit.createPending(USER_ID, COUNTRY, CITY, START_DATE, END_DATE);
        ReflectionTestUtils.setField(pendingOutfit, "id", TRAVEL_ID);

        completedOutfit = TravelOutfit.builder()
                .id(TRAVEL_ID)
                .userId(USER_ID)
                .country(COUNTRY)
                .city(CITY)
                .startDate(START_DATE)
                .endDate(END_DATE)
                .avgTemperature(20.0)
                .avgHumidity(60)
                .rainProbability(10)
                .condition("맑음")
                .umbrellaSummary("우산 없어도 됨")
                .culturalConstraintsJson(mockJsonNode)
                .aiOutfitJson(mockJsonNode)
                .safetyNotesJson(mockJsonNode)
                .status(RecommendationStatus.COMPLETED)
                .build();

        ReflectionTestUtils.setField(completedOutfit, "createdAt", LocalDateTime.now());

        mockAiResponse = new AiTravelJsonResponse(
                "요약",
                Collections.emptyList(),
                new CulturalConstraints("문화/종교 조건", Collections.emptyList()),
                List.of("안전 노트 1")
        );

    }

    /**
     * 여행 옷차림 추천 요청 API 테스트 코드
     */
    @Test
    @DisplayName("추천 요청 접수(동기) 성공")
    void requestRecommendation_Success() {

        // given
        when(travelOutfitRepository.save(any(TravelOutfit.class))).thenReturn(pendingOutfit);

        // when
        TravelOutfit result = travelOutfitServiceImpl.requestRecommendation(USER_ID, request);

        // then
        ArgumentCaptor<TravelOutfit> captor = ArgumentCaptor.forClass(TravelOutfit.class);
        verify(travelOutfitRepository).save(captor.capture());
        TravelOutfit savedEntity = captor.getValue();

        assertThat(savedEntity.getStatus()).isEqualTo(RecommendationStatus.PENDING);
        assertThat(savedEntity.getUserId()).isEqualTo(USER_ID);
        assertThat(savedEntity.getCity()).isEqualTo(CITY);

        assertThat(result).isEqualTo(pendingOutfit);
        assertThat(result.getId()).isEqualTo(TRAVEL_ID);
    }

    @Test
    @DisplayName("여행 기간이 14일을 초과하는 경우 실패")
    void requestRecommendation_Fail_Invalid_Period() {

        // given
        var invalidEndDate = START_DATE.plusDays(15);
        var invalidRequest = new TravelOutfitRequest(COUNTRY, CITY, START_DATE, invalidEndDate);

        // when & then
        assertThatThrownBy(() -> travelOutfitServiceImpl.requestRecommendation(USER_ID, invalidRequest))
                .isInstanceOf(GlobalException.class)
                .hasMessage(TravelOutfitErrorCode.INVALID_PERIOD.getMessage());

        verify(travelOutfitRepository, never()).save(any());
    }

    @Test
    @DisplayName("여행 종료일이 시작일보다 빠른 경우 실패")
    void requestRecommendation_Fail_EndDate_Before_StartDate() {

        // given
        var invalidEndDate = START_DATE.minusDays(1);
        var invalidRequest = new TravelOutfitRequest(COUNTRY, CITY, START_DATE, invalidEndDate);

        // when & then
        assertThatThrownBy(() -> travelOutfitServiceImpl.requestRecommendation(USER_ID, invalidRequest))
                .isInstanceOf(GlobalException.class)
                .hasMessage(TravelOutfitErrorCode.INVALID_PERIOD.getMessage());

        verify(travelOutfitRepository, never()).save(any());
    }


    // processRecommendation (비동기) 테스트
    @Test
    @DisplayName("추천 생성(비동기) 성공")
    void processRecommendation_Success() throws JsonProcessingException {

        // given
        when(travelOutfitRepository.findById(TRAVEL_ID)).thenReturn(Optional.of(pendingOutfit));

        var weather = new Daily(START_DATE, 20.0, 60.0, 10, "맑음");
        when(globalWeatherApiClient.getDailyForecast(CITY, START_DATE, END_DATE))
                .thenReturn(List.of(weather));

        when(promptBuilder.buildPrompt(
                anyString(), anyString(), any(LocalDate.class), any(LocalDate.class),
                eq("남성"), eq("맑음"), eq(20.0), anyString()
        )).thenReturn("테스트 프롬프트");
        when(aiClient.callForJson("테스트 프롬프트")).thenReturn("{\"summary\":\"...\"}");
        when(aiClient.parse("{\"summary\":\"...\"}")).thenReturn(mockAiResponse);

        when(objectMapper.valueToTree(any(AiOutfit.class))).thenReturn(mockJsonNode);
        when(objectMapper.valueToTree(any(CulturalConstraints.class))).thenReturn(mockJsonNode);
        when(objectMapper.valueToTree(any(List.class))).thenReturn(mockJsonNode); // safetyNotes

        when(travelOutfitRepository.save(any(TravelOutfit.class))).thenReturn(completedOutfit);

        // when
        travelOutfitServiceImpl.processRecommendation(TRAVEL_ID, request, GENDER);

        // then
        ArgumentCaptor<TravelOutfit> captor = ArgumentCaptor.forClass(TravelOutfit.class);
        verify(travelOutfitRepository).save(captor.capture());
        TravelOutfit savedEntity = captor.getValue();

        assertThat(savedEntity.getId()).isEqualTo(TRAVEL_ID);
        assertThat(savedEntity.getStatus()).isEqualTo(RecommendationStatus.COMPLETED);
        assertThat(savedEntity.getAvgTemperature()).isEqualTo(20.0);
        assertThat(savedEntity.getAvgHumidity()).isEqualTo(60);
        assertThat(savedEntity.getRainProbability()).isEqualTo(10);
        assertThat(savedEntity.getCondition()).isEqualTo("맑음");
        assertThat(savedEntity.getAiOutfitJson()).isEqualTo(mockJsonNode);
        assertThat(savedEntity.getCulturalConstraintsJson()).isEqualTo(mockJsonNode);
        assertThat(savedEntity.getSafetyNotesJson()).isEqualTo(mockJsonNode);
        assertThat(savedEntity.getUmbrellaSummary()).isNotNull();

        verify(statusUpdater, never()).updateStatusToFailed(anyLong(), anyString());
    }

    @Test
    @DisplayName("추천 생성 실패 - PENDING 엔티티 조회 실패")
    void processRecommendation_Fail_Pending_Entity_Not_Found() {

        // given
        when(travelOutfitRepository.findById(TRAVEL_ID)).thenReturn(Optional.empty());

        // when
        travelOutfitServiceImpl.processRecommendation(TRAVEL_ID, request, GENDER);

        // then
        verify(statusUpdater).updateStatusToFailed(
                eq(TRAVEL_ID),
                eq(TravelOutfitErrorCode.RECOMMENDATION_NOT_FOUND.getMessage())
        );
        verify(globalWeatherApiClient, never()).getDailyForecast(any(), any(), any());
        verify(aiClient, never()).callForJson(anyString());
        verify(travelOutfitRepository, never()).save(any());
    }

    @Test
    @DisplayName("추천 생성 실패 - 날씨 정보 조회 실패")
    void processRecommendation_Fail_Weather_Fetch_Failed() {

        // given
        when(travelOutfitRepository.findById(TRAVEL_ID)).thenReturn(Optional.of(pendingOutfit));

        when(globalWeatherApiClient.getDailyForecast(CITY, START_DATE, END_DATE))
                .thenReturn(Collections.emptyList());

        // when
        travelOutfitServiceImpl.processRecommendation(TRAVEL_ID, request, GENDER);

        // then
        verify(statusUpdater).updateStatusToFailed(
                eq(TRAVEL_ID),
                eq(TravelOutfitErrorCode.WEATHER_FETCH_FAILED.getMessage())
        );
        verify(travelOutfitRepository, never()).save(any());
    }

    @Test
    @DisplayName("추천 생성 실패 - AI 응답 파싱 실패")
    void processRecommendation_Fail_AiParseFailed() {

        // given
        when(travelOutfitRepository.findById(TRAVEL_ID)).thenReturn(Optional.of(pendingOutfit));

        var weather = new Daily(START_DATE, 20.0, 60.0, 10, "맑음");
        when(globalWeatherApiClient.getDailyForecast(CITY, START_DATE, END_DATE))
                .thenReturn(List.of(weather));

        when(promptBuilder.buildPrompt(anyString(), anyString(), any(), any(), anyString(), anyString(), anyDouble(),
                anyString()))
                .thenReturn("테스트 프롬프트");
        when(aiClient.callForJson("테스트 프롬프트")).thenReturn("잘못된 JSON");

        when(aiClient.parse("잘못된 JSON")).thenThrow(new IllegalStateException("AI 파싱 실패"));

        // when
        travelOutfitServiceImpl.processRecommendation(TRAVEL_ID, request, GENDER);

        // then
        // 'Exception' catch 블록에서 처리
        verify(statusUpdater).updateStatusToFailed(
                eq(TRAVEL_ID),
                eq("알 수 없는 서버 오류가 발생했습니다.")
        );
        verify(travelOutfitRepository, never()).save(any());
    }

    /**
     * 추천 기록 목록 조회 테스트 코드
     */
    @Test
    @DisplayName("나의 추천 기록 목록 조회 성공")
    void getMyRecommendationsSummary_Success() {

        // given
        Pageable pageable = PageRequest.of(0, 5);
        Page<TravelOutfit> responsePage = new PageImpl<>(List.of(completedOutfit), pageable, 1);
        when(travelOutfitRepository.findByUserId(USER_ID, pageable)).thenReturn(responsePage);

        // when
        Page<TravelOutfitSummaryResponse> result = travelOutfitServiceImpl.getMyRecommendationsSummary(USER_ID,
                pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).city()).isEqualTo(CITY);
        assertThat(result.getContent().get(0).weatherSummary().avgTemperature()).isEqualTo(20.0);
    }

    /**
     * 추천 기록 단건 상세 조회 테스트 코드
     */
    @Test
    @DisplayName("추천 기록 단건 상세 조회 성공")
    void getRecommendationDetail_Success_Completed() throws JsonProcessingException {

        // given
        when(travelOutfitRepository.findByIdAndUserId(TRAVEL_ID, USER_ID))
                .thenReturn(Optional.of(completedOutfit));

        when(objectMapper.treeToValue(eq(mockJsonNode), eq(CulturalConstraints.class)))
                .thenReturn(mockAiResponse.culturalConstraints());
        when(objectMapper.treeToValue(eq(mockJsonNode), eq(AiOutfit.class)))
                .thenReturn(new AiOutfit(mockAiResponse.summary(), mockAiResponse.outfits()));
        when(objectMapper.treeToValue(eq(mockJsonNode), any(TypeReference.class)))
                .thenReturn(mockAiResponse.safetyNotes());

        // when
        TravelOutfitResponse response = travelOutfitServiceImpl.getRecommendationDetail(USER_ID, TRAVEL_ID);

        // then
        assertThat(response).isNotNull();
        assertThat(response.travelId()).isEqualTo(completedOutfit.getId());
        assertThat(response.city()).isEqualTo(completedOutfit.getCity());
        assertThat(response.status()).isEqualTo(RecommendationStatus.COMPLETED);
        assertThat(response.weatherSummary().avgTemperature()).isEqualTo(20.0);
        assertThat(response.aiOutfitJson().summary()).isEqualTo("요약");
        assertThat(response.culturalConstraints().notes()).isEqualTo("문화/종교 조건");
        assertThat(response.safetyNotes()).contains("안전 노트 1");
    }

    @Test
    @DisplayName("존재 하지 않은 추천 기록 또는 권한이 없는 추천 기록 상세 조회 시 실패")
    void getRecommendationDetail_Fail_Not_Found() {

        // given
        when(travelOutfitRepository.findByIdAndUserId(TRAVEL_ID, USER_ID))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> travelOutfitServiceImpl.getRecommendationDetail(USER_ID, TRAVEL_ID))
                .isInstanceOf(GlobalException.class)
                .hasMessage(TravelOutfitErrorCode.RECOMMENDATION_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("상세 조회 시 PENDING 상태가 아니면 날씨, AI 정보는 null")
    void getRecommendationDetail_Pending_Returns_Null_Data() {

        // given
        when(travelOutfitRepository.findByIdAndUserId(TRAVEL_ID, USER_ID))
                .thenReturn(Optional.of(pendingOutfit));

        // when
        TravelOutfitResponse response = travelOutfitServiceImpl.getRecommendationDetail(USER_ID, TRAVEL_ID);

        // then
        assertThat(response).isNotNull();
        assertThat(response.travelId()).isEqualTo(TRAVEL_ID);
        assertThat(response.status()).isEqualTo(RecommendationStatus.PENDING);
        assertThat(response.weatherSummary()).isNull();
        assertThat(response.aiOutfitJson()).isNull();
        assertThat(response.culturalConstraints()).isNull();
        assertThat(response.safetyNotes()).isNull();
    }
}

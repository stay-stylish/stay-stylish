/*
package org.example.staystylish.domain.localweather;

import org.example.staystylish.domain.localweather.dto.GpsRequest;
import org.example.staystylish.domain.localweather.dto.LocalWeatherItem;
import org.example.staystylish.domain.localweather.dto.LocalWeatherResponse;
import org.example.staystylish.domain.localweather.dto.UserWeatherResponse;
import org.example.staystylish.domain.localweather.entity.LocalWeather;
import org.example.staystylish.domain.localweather.entity.Region;
import org.example.staystylish.domain.localweather.repository.LocalWeatherRepository;
import org.example.staystylish.domain.localweather.repository.RegionRepository;
import org.example.staystylish.domain.localweather.service.LocalWeatherServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.reactive.function.client.*;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class WeatherServiceTest {

    @Mock
    private WebClient.Builder webClientBuilder;
    @Mock
    private WebClient webClient;
    @Mock
    private WebClient.RequestHeadersUriSpec<?> uriSpec;
    @Mock
    private WebClient.RequestHeadersSpec<?> headersSpec;
    @Mock
    private WebClient.ResponseSpec responseSpec;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private LocalWeatherRepository localWeatherRepository;

    @Mock
    private RegionRepository regionRepository;

    @InjectMocks
    private LocalWeatherServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // WebClient Mock 설정
        when(webClientBuilder.baseUrl(anyString())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);

        when(webClient.get()).thenReturn(uriSpec);
        when(uriSpec.uri(anyString())).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);

        // 💡 bodyToMono Mocking은 테스트마다 내용이 다르므로 각 테스트에서 설정합니다.
    }

    @Test
    @DisplayName("W01 — 현재 위치 날씨 조회 성공")
    void testCurrentWeatherSuccess() {

        // given
        GpsRequest request = new GpsRequest(37.5665, 126.9780);

        Region region = Region.builder()
                .id(1L)
                .province("서울")
                .city("종로구")
                .district("사직동")
                .longitude(126.9780)
                .latitude(37.5665)
                .build();

        when(regionRepository.findNearestRegions(anyDouble(), anyDouble(), any(PageRequest.class)))
                .thenReturn(List.of(region));

        when(redisTemplate.opsForValue().get(anyString())).thenReturn(null); // 캐시 미스

        // 💡 실제 서비스에서 파싱하는 최소한의 날씨 항목을 포함하여 XML 응답 구성 (T1H, REH, PTY 등)
        String fakeXml = """
            <response>
                <header><resultCode>00</resultCode></header>
                <body><items><item>
                    <category>T1H</category>
                    <obsrValue>12.3</obsrValue>
                    <baseDate>20250101</baseDate>
                    <baseTime>1130</baseTime>
                </item><item>
                    <category>REH</category>
                    <obsrValue>70</obsrValue>
                </item><item>
                    <category>RN1</category>
                    <obsrValue>0</obsrValue>
                </item><item>
                    <category>WSD</category>
                    <obsrValue>1.5</obsrValue>
                </item></items></body>
            </response>
        """;

        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(fakeXml));

        // 💡 LocalWeather 엔티티에 저장될 예상 값
        LocalWeather savedEntity = LocalWeather.builder()
                .id(1L)
                .region(region)
                .temperature(12.3)
                .humidity(70.0)
                .rainfall(0.0)
                .windSpeed(1.5)
                .forecastTime(LocalDateTime.of(2025, 1, 1, 11, 30))
                .build();

        when(localWeatherRepository.save(any(LocalWeather.class))).thenReturn(savedEntity);

        // when
        UserWeatherResponse result = service.getWeatherByLatLon(request).block();

        // then
        assertNotNull(result);
        assertEquals("서울", result.province());
        assertEquals("종로구", result.city());
        assertEquals("사직동", result.district());
        assertEquals(12.3, result.temperature()); // 💡 온도 검증 추가
        verify(localWeatherRepository, times(1)).save(any(LocalWeather.class)); // 저장 호출 검증
    }

    @Test
    @DisplayName("W03 — GPS 기반 Region 조회 실패 시 오류 반환") // 💡 이름 변경 및 W02 통합
    void testRegionNotFoundFailure() {

        // given: Region DB에서 매칭되는 지역이 없는 상황 (0.0, 0.0과 같은 유효하지 않은 좌표 포함)
        GpsRequest request = new GpsRequest(0.0, 0.0);

        when(regionRepository.findNearestRegions(anyDouble(), anyDouble(), any(PageRequest.class)))
                .thenReturn(List.of()); // Region 조회 실패

        // when & then: 예외 발생 확인
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.getWeatherByLatLon(request).block());

        assertTrue(ex.getMessage().contains("Region not found"));
    }

    @Test
    @DisplayName("W09 — 같은 지역 재조회 시 Redis 캐시 사용")
    void testWeatherCacheHit() {

        // given
        GpsRequest request = new GpsRequest(37.5, 127.0);

        Region region = Region.builder()
                .id(1L)
                .province("서울")
                .city("종로구")
                .district("사직동")
                .longitude(127.0)
                .latitude(37.5)
                .build();

        when(regionRepository.findNearestRegions(anyDouble(), anyDouble(), any(PageRequest.class)))
                .thenReturn(List.of(region));

        // 💡 Redis에 캐시된 응답 DTO (온도 10.0으로 설정)
        LocalWeatherResponse cached = new LocalWeatherResponse(
                List.of(new LocalWeatherItem("T1H", "10.0", "20250101", "1130")),
                Map.of()
        );

        when(redisTemplate.opsForValue().get(anyString())).thenReturn(cached);

        // when
        UserWeatherResponse response = service.getWeatherByLatLon(request).block();

        // then
        assertNotNull(response);
        // 💡 캐시된 DTO의 값이 최종 응답에 반영되었는지 검증
        assertEquals(10.0, response.temperature());

        // 💡 WebClient 호출이나 DB 저장이 일어나지 않았는지 검증
        verify(responseSpec, never()).bodyToMono(any());
        verify(localWeatherRepository, never()).save(any());
    }
}*/

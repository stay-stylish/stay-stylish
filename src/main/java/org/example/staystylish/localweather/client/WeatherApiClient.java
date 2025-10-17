package org.example.staystylish.weather.client;

import lombok.RequiredArgsConstructor;
import org.example.staystylish.weather.dto.KmaResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
public class WeatherApiClient {

    private final RestTemplate restTemplate;
    @Value("${weather.api.key}")
    private String serviceKey;

    private static final String BASE_URL =
            "https://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getVilageFcst";

    public KmaResponse fetchWeather(String baseDate, String baseTime, int nx, int ny) {
        UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(BASE_URL)
                .queryParam("serviceKey", serviceKey)
                .queryParam("numOfRows", 10)
                .queryParam("pageNo", 1)
                .queryParam("dataType", "JSON")
                .queryParam("base_date", baseDate)
                .queryParam("base_time", baseTime)
                .queryParam("nx", nx)
                .queryParam("ny", ny);

        ResponseEntity<KmaResponse> response =
                restTemplate.exchange(uri.toUriString(), HttpMethod.GET, null, KmaResponse.class);

        return response.getBody();
    }
}
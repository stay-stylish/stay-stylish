package org.example.staystylish.domain.localweather.controller;

import lombok.RequiredArgsConstructor;
import org.example.staystylish.domain.localweather.dto.GpsRequest;
import org.example.staystylish.domain.localweather.dto.UserWeatherResponse;
import org.example.staystylish.domain.localweather.entity.Region;
import org.example.staystylish.domain.localweather.service.WeatherService;
import org.example.staystylish.domain.localweather.util.WeatherMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;


/**
 * POST /api/v1/weather/weather-by-gps
 * - 사용자 위도/경도를 받아 날씨 정보 조회
 * - 비동기 Mono<ResponseEntity<WeatherResponse>> 반환
 */

/**
        * POST /api/v1/weather/weather-by-gps
 * - 사용자 위도/경도를 받아 날씨 정보 조회
 * - 비동기 Mono<ResponseEntity<UserWeatherResponse>> 반환
 */

@CrossOrigin(origins = "*") // 개발용: 프론트엔드 다른 포트 허용
@RestController
@RequestMapping("/api/v1/weather")
@RequiredArgsConstructor
public class WeatherController {

    private final WeatherService weatherService;

    @PostMapping("/weather-by-gps")
    public Mono<ResponseEntity<UserWeatherResponse>> weatherByGps(@RequestBody GpsRequest req) {
        if (req.latitude() == null || req.longitude() == null) {
            return Mono.just(ResponseEntity.badRequest()
                    .body(new UserWeatherResponse("", "", "", null, null, null, null, "", "")));
        }

        return weatherService.getWeatherByLatLon(req)
                .map(userWeatherResponse -> {
                    // 💡 Service Layer에서 이미 UserWeatherResponse DTO를 생성했기 때문에
                    //    컨트롤러에서는 추가적인 변환 없이 바로 반환합니다.
                    return ResponseEntity.ok(userWeatherResponse);
                });
    }
}
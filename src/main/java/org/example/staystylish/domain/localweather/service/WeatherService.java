package org.example.staystylish.domain.localweather.service;

import org.example.staystylish.domain.localweather.dto.GpsRequest;
import org.example.staystylish.domain.localweather.dto.WeatherResponse;
import org.example.staystylish.domain.localweather.entity.Region;
import org.example.staystylish.domain.localweather.util.KmaGridConverter;
import org.example.staystylish.domain.localweather.repository.RegionRepository;
import reactor.core.publisher.Mono;

public interface WeatherService {

    /**
     * 위도/경도 기반 날씨 조회
     *
     * @param lat 위도
     * @param lon 경도
     * @return WeatherResponse를 감싼 Mono
     */
    Mono<WeatherResponse> getWeatherByLatLon(GpsRequest request) {

        // 1️⃣ DB에서 가장 가까운 region 조회
        Region region = regionRepository.findNearestRegion(request.latitude(), request.longitude())
                .orElseThrow(() -> new RuntimeException("Region not found"));

        String regionName = region.getCity().isEmpty() ? region.getProvince() : region.getCity();

        // 2️⃣ 기존 KMA API 호출 로직
        int[] xy = KmaGridConverter.latLonToGrid(region.getLatitude(), region.getLongitude());
        int nx = xy[0];
        int ny = xy[1];

        // 3️⃣ Weather 조회 및 저장
        return getWeatherByLatLon(region.getLatitude(), region.getLongitude());
    }
}
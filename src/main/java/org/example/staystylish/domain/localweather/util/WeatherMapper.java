package org.example.staystylish.domain.localweather.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.example.staystylish.domain.localweather.dto.UserWeatherResponse;
import org.example.staystylish.domain.localweather.dto.WeatherItem;
import org.example.staystylish.domain.localweather.entity.Region;
import org.example.staystylish.domain.localweather.entity.Weather;

/**
 * WeatherMapper
 * - 외부 API(WeatherItem 리스트) 또는 DB 엔티티(Weather) 데이터를
 *   애플리케이션 내부 DTO(UserWeatherResponse)나 엔티티로 변환하는 유틸 클래스
 */

public class WeatherMapper {

    /**
     * WeatherItem 리스트 + Region 객체를 기반으로
     * UserWeatherResponse DTO 생성
     *
     * @param items WeatherItem 리스트 (API 응답)
     * @param region Region 엔티티 (DB 조회)
     * @return UserWeatherResponse DTO (프론트엔드 전달용)
     */

    public static UserWeatherResponse toUserWeatherResponse(List<WeatherItem> items, Object region) {
        String province = "", city = "", district = "";

        // region 객체가 Region 엔티티인 경우 필드 추출
        if (region instanceof org.example.staystylish.domain.localweather.entity.Region r) {
            province = r.getProvince();
            city = r.getCity();
            district = r.getDistrict();
        }

        Double temp = null, hum = null, wind = null, rain = null;
        String sky = "맑음", pty = "없음";

        // WeatherItem 리스트에서 각 항목별 값 매핑
        for (WeatherItem item : items) {
            switch (item.category()) {
                case "T1H": temp = Double.valueOf(item.obsrValue()); break;
                case "REH": hum = Double.valueOf(item.obsrValue()); break;
                case "RN1": rain = Double.valueOf(item.obsrValue()); break;
                case "WSD": wind = Double.valueOf(item.obsrValue()); break;
                case "PTY": pty = mapPty(item.obsrValue()); break;
                case "SKY": sky = mapSky(item.obsrValue()); break;
            }
        }

        // DTO 생성 후 반환
        return new UserWeatherResponse(province, city, district, temp, hum, rain, wind, sky, pty);
    }

    /**
     * WeatherItem 리스트 + Region 객체를 기반으로
     * Weather 엔티티 생성 (DB 저장용)
     *
     * @param items WeatherItem 리스트
     * @param region Region 엔티티
     * @return Weather 엔티티
     */


    public static Weather toWeather(List<WeatherItem> items, Region region) {
        Double temp = null, hum = null, wind = null, rain = null;
        LocalDateTime forecastTime = LocalDateTime.now();

        // 항목별 값 매핑
        for (WeatherItem item : items) {
            switch (item.category()) {
                case "T1H": temp = Double.valueOf(item.obsrValue()); break;
                case "REH": hum = Double.valueOf(item.obsrValue()); break;
                case "RN1": rain = Double.valueOf(item.obsrValue()); break;
                case "WSD": wind = Double.valueOf(item.obsrValue()); break;
            }
        }

        if (!items.isEmpty()) {
            var first = items.get(0);
            forecastTime = LocalDateTime.parse(first.baseDate() + first.baseTime(),
                    DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
        }

        return Weather.builder()
                .region(region)
                .temperature(temp)
                .humidity(hum)
                .rainfall(rain)
                .windSpeed(wind)
                .forecastTime(forecastTime)
                .build();
    }

    /**
     * 강수 형태 코드 변환 (PTY)
     * @param code PTY 코드
     * @return 유저 친화적 문자열 (없음, 비, 눈 등)
     */

    private static String mapPty(String code) {
        return switch (code) {
            case "0" -> "없음";
            case "1" -> "비";
            case "2" -> "비/눈";
            case "3" -> "눈";
            case "4" -> "소나기";
            default -> "알수없음";
        };
    }

    /**
     * 하늘 상태 코드 변환 (SKY)
     * @param code SKY 코드
     * @return 유저 친화적 문자열 (맑음, 구름조금 등)
     */

    private static String mapSky(String code) {
        return switch (code) {
            case "1" -> "맑음";
            case "2" -> "구름조금";
            case "3" -> "구름많음";
            case "4" -> "흐림";
            default -> "알수없음";
        };
    }
}
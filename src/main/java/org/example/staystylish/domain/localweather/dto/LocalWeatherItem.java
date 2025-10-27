package org.example.staystylish.domain.localweather.dto;

/**
 * 기상청 관측값(OBS) API 응답 중 개별 항목(Item)을 담는 DTO
 * <p>
 * 각 개별 항목은 기상청 API허브 "초단기실황조회" 요청항목 참조
 *
 * @param category  항목명 (예: "T1H(기온)", "REH(습도)", "PTY(강수형태)" 등)
 * @param obsrValue 관측 값
 * @param baseDate  관측 날짜 (YYYYMMDD)
 * @param baseTime  관측 시각 (HHMM)
 */
public record LocalWeatherItem(
        String category,
        String obsrValue,
        String baseDate,
        String baseTime
) {
}
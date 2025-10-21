package org.example.staystylish.domain.localweather.util;

/**
 * 기상청 격자 좌표(nx, ny)를 간단히 지역 이름으로 매핑
 * 이거 더 구체화해야해
 */
public class GridToRegionConverter {

    public static String toRegion(int nx, int ny) {

        if (nx >= 60 && nx <= 70 && ny >= 120 && ny <= 130) {
            return "Seoul";
        } else if (nx >= 50 && nx <= 60 && ny >= 110 && ny <= 120) {
            return "Busan";
        } else {
            return "Unknown";
        }
    }
}
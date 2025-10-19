package org.example.staystylish.localweather.util;

/**
 * KMA 격자 <-> 위경도 변환 (LCC 투영법 기반)
 * 변환 코드는 공개된 Gist/참고자료를 기반으로 구현.
 */
//위 수식/상수(XO, YO)는 기상청에서 사용하는 표준값을 사용했습니다. 실무에서는 반드시 APHub 문서나 첨부자료의 표준값을 확인하세요.
public class KmaGridConverter {

    // 상수 (기상청 기준)
    private static final double RE = 6371.00877; // Earth radius (km)
    private static final double GRID = 5.0;      // grid spacing (km)
    private static final double SLAT1 = 30.0;    // projection latitude 1 (deg)
    private static final double SLAT2 = 60.0;    // projection latitude 2 (deg)
    private static final double OLON = 126.0;    // origin lon (deg)
    private static final double OLAT = 38.0;     // origin lat (deg)
    private static final double XO = 43;         // origin X coordinate (GRID) - KMA 기준값
    private static final double YO = 136;        // origin Y coordinate (GRID)

    private static final double DEGRAD = Math.PI / 180.0;

    public static int[] latLonToGrid(double lat, double lon) {
        // Implementation from public gist/reference
        double re = RE / GRID;
        double slat1 = SLAT1 * DEGRAD;
        double slat2 = SLAT2 * DEGRAD;
        double olon = OLON * DEGRAD;
        double olat = OLAT * DEGRAD;

        double sn = Math.tan(Math.PI * 0.25 + slat2 * 0.5) / Math.tan(Math.PI * 0.25 + slat1 * 0.5);
        sn = Math.log(Math.cos(slat1) / Math.cos(slat2)) / Math.log(sn);
        double sf = Math.tan(Math.PI * 0.25 + slat1 * 0.5);
        sf = Math.pow(sf, sn) * Math.cos(slat1) / sn;
        double ro = Math.tan(Math.PI * 0.25 + olat * 0.5);
        ro = re * sf / Math.pow(ro, sn);

        double ra = Math.tan(Math.PI * 0.25 + (lat) * DEGRAD * 0.5);
        ra = re * sf / Math.pow(ra, sn);
        double theta = lon * DEGRAD - olon;
        if (theta > Math.PI) theta -= 2.0 * Math.PI;
        if (theta < -Math.PI) theta += 2.0 * Math.PI;
        theta *= sn;

        double x = ra * Math.sin(theta) + XO + 0.5;
        double y = ro - ra * Math.cos(theta) + YO + 0.5;

        return new int[] {(int)Math.floor(x), (int)Math.floor(y)};
    }
}
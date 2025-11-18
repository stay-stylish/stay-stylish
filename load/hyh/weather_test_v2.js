import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend, Counter } from 'k6/metrics';

// === 커스터마이징 섹션 ===
const BASELINE_URL = __ENV.BASELINE_URL || 'http://host.docker.internal:8080';
const OPTIMIZED_URL = __ENV.OPTIMIZED_URL || 'http://host.docker.internal:8080';
const PAYLOAD = JSON.stringify({ latitude: 37.56, longitude: 126.97 });
const HEADERS = { 'Content-Type': 'application/json' };

// === 사용자 정의 메트릭 ===
const reqDuration = new Trend('req_duration_ms');
const errors = new Counter('errors_total');

// === 부하 시나리오 ===
// 기상청 API 제한 고려: 1초에 1개 요청만 허용
export const options = {
    stages: [
        { duration: '20s', target: 20 },   // ramp-up
        { duration: '30s', target: 50 },
        { duration: '60s', target: 100 },  // 최대 100명 VU
        { duration: '20s', target: 0 }     // ramp-down
    ],
    thresholds: {
        errors_total: ['rate<0.02'],  // 오류율 2% 이하
        req_duration_ms: ['p(95)<5000'], // 95% 요청 5초 이하
    },
};

// === 헬퍼 함수: retry + timeout + pacing ===
function fetchWithRetry(url, retries = 3, timeoutSec = 10) {
    for (let i = 0; i < retries; i++) {
        let res = http.post(url, PAYLOAD, { headers: HEADERS, timeout: `${timeoutSec}s` });
        reqDuration.add(res.timings.duration);

        if (res.status === 200) return res;

        errors.add(1);
        sleep(1); // 실패 시 잠깐 쉬고 재시도
    }
    return null;
}

// === 메인 VU 루프 ===
export default function () {
    // 1. Baseline 요청
    let baselineRes = fetchWithRetry(BASELINE_URL + '/api/v1/weather/weather-by-gps');
    check(baselineRes, { 'baseline status is 200': r => r && r.status === 200 });

    // 2. Optimized 요청
    let optimizedRes = fetchWithRetry(OPTIMIZED_URL + '/api/v1/weather/weather-by-gps');
    check(optimizedRes, { 'optimized status is 200': r => r && r.status === 200 });

    // === pacing 조절 ===
    // 기상청 API 제한: 1초에 1개 요청
    sleep(1);
}

import http from 'k6/http';
import { check, sleep, group } from 'k6';
import { Trend, Counter } from 'k6/metrics';

// === 커스터마이징 섹션 ===
// 목표(테스트 대상) URL: 예) http://localhost:8080
const BASELINE_URL = __ENV.BASELINE_URL ||  'http://host.docker.internal:8080';
const OPTIMIZED_URL = __ENV.OPTIMIZED_URL ||  'http://host.docker.internal:8080';

// 요청 바디 (위도/경도)
const PAYLOAD = JSON.stringify({ latitude: 37.56, longitude: 126.97 });

// 공통 헤더
const HEADERS = { 'Content-Type': 'application/json' };

// === 사용자 지정 메트릭 ===
const reqDuration = new Trend('req_duration_ms');
const errors = new Counter('errors_total');

// === k6 옵션: 시나리오 순서 정의 ===
export let options = {
    thresholds: {
        // p95 응답시간 5000ms 이하
        'req_duration_ms': ['p(95)<5000'],
        // 에러율 2% 이하
        'errors_total': ['rate<0.02']
    },
    scenarios: {
        // 1) 워밍업: 1분 동안 VU를 서서히 올려 시스템 데우기
        warmup: {
            executor: 'ramping-vus',
            startVUs: 0,
            stages: [
                { duration: '20s', target: 10 },
                { duration: '20s', target: 30 },
                { duration: '20s', target: 50 }
            ],
            exec: 'warmupScenario'
        },

        // 2) Baseline: 현재(비최적화) 환경 측정 (동일한 부하)
        baseline: {
            executor: 'constant-vus',
            vus: 50,             // 동시 사용자 수 (필요시 조절)
            duration: '3m',      // 각 시나리오 실행 시간
            startTime: '3m30s',  // warmup이 끝난 뒤 시작
            exec: 'baselineScenario'
        },

        // 3) Optimized: 최적화된 환경(동일 부하)
        optimized: {
            executor: 'constant-vus',
            vus: 50,
            duration: '3m',
            startTime: '7m00s',  // baseline 끝난 뒤 시작
            exec: 'optimizedScenario'
        },

        // 4) Failure: 외부 API 지연/오류 시나리오
        failure: {
            executor: 'constant-vus',
            vus: 30,
            duration: '2m',
            startTime: '10m30s',
            exec: 'failureScenario'
        }
    }
};

// === 시나리오 함수 ===
export function warmupScenario() {
    // 간단한 GET/POST로 시스템 데우기
    let res = http.post(`${BASELINE_URL}/api/v1/weather/weather-by-gps`, PAYLOAD, { headers: HEADERS });
    check(res, {
        'warmup status is 2xx': (r) => r.status >= 200 && r.status < 300
    }) || errors.add(1);
    reqDuration.add(res.timings.duration);
    sleep(0.5);
}

export function baselineScenario() {
    group('baseline', () => {
        let res = http.post(`${BASELINE_URL}/api/v1/weather/weather-by-gps`, PAYLOAD, { headers: HEADERS });
        const ok = check(res, {
            'baseline status is 200': (r) => r.status === 200
        });
        if (!ok) errors.add(1);
        reqDuration.add(res.timings.duration);
        // think time
        sleep(0.2);
    });
}

export function optimizedScenario() {
    group('optimized', () => {
        //
        let res = http.post(`${OPTIMIZED_URL}/api/v1/weather/weather-by-gps`, PAYLOAD, { headers: HEADERS });
        const ok = check(res, {
            'optimized status is 200': (r) => r.status === 200
        });
        if (!ok) errors.add(1);
        reqDuration.add(res.timings.duration);
        sleep(0.2);
    });
}

export function failureScenario() {
    group('failure-simulation', () => {
        // WireMock 같은 곳에 외부 API 지연/오류를 만들고 (예: /kma/slow)
        // 서비스가 외부 호출 실패시 어떻게 동작하는지 봄
        let res = http.post(`${BASELINE_URL}/api/v1/weather/weather-by-gps?simulateKma=slow`, PAYLOAD, { headers: HEADERS });
        // 성공/실패 여부를 로그로 남김
        const ok = check(res, {
            'failure scenario status < 500': (r) => r.status < 500
        });
        if (!ok) errors.add(1);
        reqDuration.add(res.timings.duration);
        sleep(0.2);
    });
}

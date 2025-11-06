import http from 'k6/http';
import { check, fail, sleep } from 'k6';

// 1. 테스트 옵션 설정
export const options = {
    vus: Number(__ENV.VUS || 10),
    duration: __ENV.DURATION || '30s',
    thresholds: {
        http_req_failed: ['rate<0.02'],
        http_req_duration: ['p(95)<5000'],
    },
};

// 2. 기본 URL 및 타임아웃 설정
const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080/api/v1';
const REQUEST_TIMEOUT = (__ENV.REQUEST_TIMEOUT_SEC || '30') + 's';

// 3. 테스트 시작 전 실행되는 setup 함수 (로그인 및 토큰 발급)
export function setup() {
    const loginPayload = JSON.stringify({
        email: __ENV.EMAIL || 'leejiho0114@gmail.com',
        password: __ENV.PASSWORD || 'password123',
    });

    const loginParams = {
        headers: { 'Content-Type': 'application/json' },
        timeout: '15s',
    };

    const res = http.post(`${BASE_URL}/auth/login`, loginPayload, loginParams);
    console.log(`Login Response: ${res.status} - ${res.body}`); // Added log

    if (!res || (res.status !== 200 && res.status !== 201)) {
        fail(`Login failed: status=${res && res.status}, body=${res && res.body}`);
    }
    const token = res.json('data.accessToken');
    console.log(`Extracted Token: ${token}`); // Added log
    if (!token) {
        fail(`Access token not found in response: body=${res.body}`);
    }
    return { token };
}

// 4. 각 가상 사용자가 실행할 테스트 로직
export default function (data) {
    console.log(`Using Token: ${data.token}`); // Added log
    const url = `${BASE_URL}/outfits/recommendation?latitude=37.5665&longitude=126.9780`;

    const params = {
        headers: {
            'Authorization': `Bearer ${data.token}`,
        },
        timeout: REQUEST_TIMEOUT,
        tags: { endpoint: 'recommendation', name: 'GET /outfits/recommendation' },
    };

    console.log("Sending request with params:", JSON.stringify(params)); // Added log
    const res = http.get(url, params);

    if (!res || res.status === 0) {
        return;
    }

    check(res, {
        '응답 상태 코드 200 확인': (r) => r.status === 200,
    });

    if (res.status !== 200) {
        console.log(`Request failed: ${res.status} - ${res.body}`);
    }

    sleep(1);
}

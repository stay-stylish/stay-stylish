import http from 'k6/http';
import {check, fail, sleep} from 'k6';
import {Trend} from 'k6/metrics';
import {randomIntBetween} from 'https://jslib.k6.io/k6-utils/1.4.0/index.js';

// 측정할 커스텀 메트릭 정의
const recommendationDuration = new Trend('recommendation_duration', true);

// 테스트 환경 설정
export const options = {
    vus: Number(__ENV.VUS || 5),     // 동시 사용자 수 (환경 변수로 제어)
    duration: __ENV.DURATION || '1m', // 테스트 시간 (환경 변수로 제어)
    thresholds: {
        'http_req_failed{endpoint:create}': ['rate<0.02'], // 생성 요청 실패율 2% 미만
        'http_req_failed{endpoint:polling}': ['rate<0.05'], // 폴링 요청 실패율 5% 미만
        'recommendation_duration{endpoint:create}': ['p(95)<15000'], // 전체 추천 완료 시간 95%가 15초 미만
    },
};

// 테스트 환경 변수
const BASE_URL = __ENV.BASE_URL || 'http://host.docker.internal:8080/api/v1';
const LOGIN_EMAIL = __ENV.EMAIL || 'isp1229@naver.com';     // 테스트 계정 이메일
const LOGIN_PASSWORD = __ENV.PASSWORD || 'password123!'; // 테스트 계정 비밀번호

// API 타임아웃
const LOGIN_TIMEOUT = '15s';
const CREATE_TIMEOUT = '10s';
const POLLING_TIMEOUT = '10s'; // GET 요청 타임아웃
const MAX_POLLING_ATTEMPTS = 60; // 최대 60초 대기

// 테스트 데이터셋
const CITIES = [
    {country: 'Japan', city: 'Tokyo'},
    {country: 'Japan', city: 'Osaka'},
    {country: 'France', city: 'Paris'},
    {country: 'Thailand', city: 'Bangkok'},
    {country: 'Italy', city: 'Rome'},
    {country: 'United States', city: 'New York'},
];

// 테스트 시작 전 1회 실행 (로그인)
export function setup() {
    console.log(`[INFO] 테스트 시작: VUs=${options.vus}, Duration=${options.duration}, Target=${BASE_URL}`);
    console.log(`[INFO] 로그인 시도: ${LOGIN_EMAIL}`);

    const res = http.post(`${BASE_URL}/auth/login`, JSON.stringify({
        email: LOGIN_EMAIL,
        password: LOGIN_PASSWORD,
    }), {
        headers: {'Content-Type': 'application/json'},
        timeout: LOGIN_TIMEOUT,
    });

    if (!res || res.status !== 200) {
        fail(`로그인 실패: status=${res && res.status}, body=${res && res.body}`);
    }

    const token = res.json('data.accessToken');
    if (!token) {
        fail(`accessToken을 찾을 수 없음: body=${res.body}`);
    }

    console.log('[INFO] 로그인 성공. 토큰 확보 완료.');
    return {token: token}; // default 함수로 토큰 전달
}

// 테스트 본체 (VU가 반복 실행)
export default function (data) {
    const {startDate, endDate} = pickTripWindow();
    const pick = CITIES[randomIntBetween(0, CITIES.length - 1)];

    const payload = JSON.stringify({
        country: pick.country,
        city: pick.city,
        startDate,
        endDate,
    });

    const params = {
        headers: {
            'Content-Type': 'application/json',
            Authorization: `Bearer ${data.token}`,
        },
        timeout: CREATE_TIMEOUT,
    };

    // 추천 생성 요청 (POST)
    const startTime = Date.now(); // 전체 시간 측정 시작
    const createRes = http.post(`${BASE_URL}/travel-outfits/recommendations`, payload, {
        ...params,
        tags: {endpoint: 'create'},
    });

    if (!createRes || createRes.status === 0) return; // 요청 실패 시 VU 즉시 중단

    check(createRes, {
        '[POST /travel-outfits] 200 OK (Accepted)': (r) => r.status === 200,
    });

    // [수정] 202가 아닌 200 OK를 확인합니다.
    if (createRes.status !== 200) {
        console.error(`[FAIL] 생성 요청 실패: ${createRes.status} ${createRes.body}`);
        return;
    }

    const travelId = createRes.json('data.travelId');
    if (!travelId) {
        console.error(`[FAIL] travelId를 찾을 수 없음: ${createRes.body}`);
        return;
    }

    // 추천 완료/실패 시까지 폴링
    let finalStatus = 'PENDING';
    for (let i = 0; i < MAX_POLLING_ATTEMPTS; i++) {
        sleep(1); // 1초 대기 후 상태 조회

        const pollRes = http.get(`${BASE_URL}/travel-outfits/recommendations/${travelId}`, {
            ...params,
            timeout: POLLING_TIMEOUT,
            tags: {endpoint: 'polling'}, // 이 요청에 'polling' 태그 부착
        });

        if (pollRes.status === 200) {
            const status = pollRes.json('data.status');
            if (status === 'COMPLETED' || status === 'FAILED') {
                finalStatus = status; // 최종 상태 기록
                break; // 루프 종료
            }
            // 'PENDING' 상태면 루프 계속
        } else {
            // 200이 아닌 응답 (e.g., 404, 500)
            console.warn(`[WARN] 폴링 실패: ${pollRes.status} ${pollRes.body}`);
        }
    }

    // 결과 집계
    const endTime = Date.now();
    const totalDuration = endTime - startTime; // ms 단위

    recommendationDuration.add(totalDuration, {endpoint: 'create'});

    check({finalStatus}, {
        '[Polling] 최종 상태 COMPLETED': (vars) => vars.finalStatus === 'COMPLETED',
    });

    if (finalStatus !== 'COMPLETED') {
        console.warn(`[WARN] 추천이 완료되지 못함: travelId=${travelId}, status=${finalStatus}, duration=${totalDuration}ms`);
    }

    sleep(randomIntBetween(1, 3));
}

// 헬퍼 함수
function pickTripWindow() {
    const startOffset = randomIntBetween(1, 7); // 1~7일 뒤 출발
    const tripLen = randomIntBetween(2, 5);     // 2~5일 여정

    const start = new Date(Date.now() + startOffset * 86400000);
    const end = new Date(start.getTime() + (tripLen - 1) * 86400000);

    const fmt = (d) => d.toISOString().slice(0, 10);
    return {startDate: fmt(start), endDate: fmt(end)};
}
import http from 'k6/http';
import { sleep, check } from 'k6';

export const options = {
    vus: 5,          // 동시 사용자 수
    duration: '15s', // 테스트 시간
};

const BASE_URL = __ENV.BASE_URL || 'http://app:8080';

export default function () {
    // 로그인 요청
    const loginRes = http.post(`${BASE_URL}/api/v1/auth/login`, JSON.stringify({
        email: 'digeu42@gmail.com',
        password: 'password123!',
    }), { headers: { 'Content-Type': 'application/json' } });

    check(loginRes, { 'login success': (r) => r.status === 200 });

    const parsed = loginRes.json();
    const accessToken = parsed.data?.accessToken;
    const refreshToken = parsed.data?.refreshToken;

    if (!accessToken || !refreshToken) {
        console.error('로그인 실패 또는 토큰 누락:', loginRes.body);
        return;
    }

    const headers = { Authorization: `Bearer ${accessToken}`, 'Content-Type': 'application/json' };

    // 내 정보 조회
    const meRes = http.get(`${BASE_URL}/api/v1/users/me`, { headers });
    check(meRes, { 'get me success': (r) => r.status === 200 });

    // 내 정보 수정
    const updateRes = http.put(`${BASE_URL}/api/v1/users/me`, JSON.stringify({
        nickname: `tester-${Math.floor(Math.random() * 1000)}`,
        stylePreference: 'minimal',
        gender: 'FEMALE',
    }), { headers });
    check(updateRes, { 'update me success': (r) => r.status === 200 });

    // access 토큰 재발급 (body에 refreshToken 포함)
    const refreshRes = http.post(`${BASE_URL}/api/v1/auth/refresh`, JSON.stringify({
        refreshToken
    }), { headers });
    check(refreshRes, { 'token refresh success': (r) => r.status === 200 });

    sleep(1);
}

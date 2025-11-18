import http from 'k6/http';
import { sleep, check } from 'k6';

export const options = {
    vus: 5,
    duration: '15s',
};

const BASE_URL = __ENV.BASE_URL || 'http://host.docker.internal:8080';

export default function () {
    const loginRes = http.post(`${BASE_URL}/api/v1/auth/login`, JSON.stringify({
        email: 'fuitshine@naver.com',
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

    http.get(`${BASE_URL}/api/v1/users/me`, { headers });
    http.put(`${BASE_URL}/api/v1/users/me`, JSON.stringify({
        nickname: `tester-${Math.floor(Math.random() * 1000)}`,
        stylePreference: 'minimal',
        gender: 'FEMALE',
    }), { headers });

    http.post(`${BASE_URL}/api/v1/auth/refresh`, JSON.stringify({ refreshToken }), { headers });

    sleep(1);
}

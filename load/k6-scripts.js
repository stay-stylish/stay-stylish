import http from 'k6/http';
import {check, sleep} from 'k6';

export const options = {
    duration: '10s', // 테스트 지속 시간
    vus: 5,        // 가상 유저 수
};


const BASE_URL = 'http://host.docker.internal:8080';

export default function () {
    const res = http.get(`${BASE_URL}/api/v1/users/me`, {
        // headers: { 'Authorization': 'Bearer YOUR_JWT_TOKEN' }
    });

    check(res, {
        'status was 200': (r) => r.status == 200,
    });
    sleep(1); // 1초 대기
}
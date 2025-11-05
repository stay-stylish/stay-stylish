import http from 'k6/http';
import {check, sleep} from 'k6';

// 테스트 옵션 설정
export const options = {
    // 가상 사용자 수 (Virtual Users)
    vus: 10,
    // 테스트 지속 시간
    duration: '30s',
    // 성능 임계값 (Thresholds) 설정
    thresholds: {
        // 요청 실패율이 2% 미만이어야 함
        http_req_failed: ['rate<0.02'],
        // 95%의 요청 응답 시간이 5000ms (5초) 미만이어야 함
        http_req_duration: ['p(95)<5000'],
    },
};

// 기본 URL 설정
const BASE_URL = 'http://localhost:8080/api/v1/outfits';
// 인증 토큰 (Bearer Token)
const TOKEN = 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJsZWVqaWhvMDExNEBnbWFpbC5jb20iLCJpYXQiOjE3NjIzMjQ3OTAsImV4cCI6MTc2MjMyODM5MH0.qMUNxh5YN7xW-l953hv7ikkpOk8G2CBtUd6f2meQcmA';

// 각 가상 사용자가 실행할 테스트 로직
export default function () {
    // 요청할 URL (위도, 경도 파라미터 포함)
    const url = `${BASE_URL}/recommendation?latitude=37.5665&longitude=126.9780`;
    // 요청 헤더 설정 (인증 토큰 포함)
    const params = {
        headers: {
            'Authorization': `Bearer ${TOKEN}`,
        },
    };

    // HTTP GET 요청 실행 및 응답 저장
    const res = http.get(url, params);

    // 응답 상태 코드가 200인지 확인
    check(res, {
        '응답 상태 코드 200 확인': (r) => r.status === 200,
    });

    // 다음 요청까지 1초 대기
    sleep(1);
}

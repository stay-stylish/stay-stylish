import http from 'k6/http';
import { sleep, check } from 'k6';

export const options = {
    vus: 5,          // 동시 사용자 수
    duration: '15s', // 테스트 시간
};

const BASE_URL = __ENV.BASE_URL || 'http://app:8080';

export default function () {
    // 로그인
    const loginRes = http.post(`${BASE_URL}/api/v1/auth/login`, JSON.stringify({
        email: 'digeu42@gmail.com',
        password: 'password123!',
    }), { headers: { 'Content-Type': 'application/json' } });

    check(loginRes, { 'login success': (r) => r.status === 200 });

    const parsed = JSON.parse(loginRes.body);
    const accessToken = parsed.data?.accessToken;
    const refreshToken = parsed.data?.refreshToken;

    if (!accessToken || !refreshToken) {
        console.error('로그인 실패 또는 토큰 누락:', loginRes.body);
        return;
    }

    const headers = {
        Authorization: `Bearer ${accessToken}`,
        'Content-Type': 'application/json',
    };

    // 게시글 작성
    const postRes = http.post(`${BASE_URL}/api/v1/posts`, JSON.stringify({
        title: `테스트 제목 ${Math.random().toFixed(3)}`,
        content: 'k6 부하 테스트 중입니다.',
    }), { headers });

    check(postRes, { 'create post success': (r) => r.status === 200 || r.status === 201 });

    const postId = JSON.parse(postRes.body)?.data?.id;
    if (!postId) {
        console.warn('게시글 생성 후 ID를 받지 못했습니다. 이후 단계 생략');
        return;
    }

    // 게시글 상세 조회
    const getRes = http.get(`${BASE_URL}/api/v1/posts/${postId}`, { headers });
    check(getRes, { 'get post success': (r) => r.status === 200 });

    // 게시글 수정
    const putRes = http.put(`${BASE_URL}/api/v1/posts/${postId}`, JSON.stringify({
        title: `수정된 제목 ${Math.random().toFixed(2)}`,
        content: '수정된 내용입니다.',
    }), { headers });
    check(putRes, { 'update post success': (r) => r.status === 200 });

    // 게시글 목록 조회
    const listRes = http.get(`${BASE_URL}/api/v1/posts`, { headers });
    check(listRes, { 'get posts success': (r) => r.status === 200 });

    // 게시글 삭제
    const delRes = http.del(`${BASE_URL}/api/v1/posts/${postId}`, null, { headers });
    check(delRes, { 'delete post success': (r) => r.status === 200 });

    sleep(1);
}

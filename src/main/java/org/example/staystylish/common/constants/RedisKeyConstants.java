package org.example.staystylish.common.constants;

/**
 * Redis Key 접두사 상수 관리 클래스
 * - 모든 Redis 키는 이 클래스에서 중앙 관리
 * - 키 충돌 방지 및 일관성 유지
 */
public final class RedisKeyConstants {

    private RedisKeyConstants() {
        throw new AssertionError("상수 클래스는 인스턴스화할 수 없습니다.");
    }

    // ============ Auth 관련 ============
    /** OAuth2 일회용 코드 (TTL: 5분) */
    public static final String OAUTH_CODE = "oauth:code:";

    /** Refresh Token (TTL: 14일) */
    public static final String REFRESH_TOKEN = "refresh:";

    /** 이메일 인증 토큰 (TTL: 10분) */
    public static final String EMAIL_VERIFY = "email:verify:";

    /** 회원가입 중복 방지 락 (TTL: 5초) */
    public static final String SIGNUP_LOCK = "lock:signup:";

    // ============ 게시글 관련 ============
    /** 게시글 좋아요 수 */
    public static final String POST_LIKE = "post:like:";

    /** 게시글 공유 수 */
    public static final String POST_SHARE = "post:share:";

    /** 좋아요 업데이트 대상 Set */
    public static final String POST_UPDATE_LIKE = "post:update:like";

    /** 공유 업데이트 대상 Set */
    public static final String POST_UPDATE_SHARE = "post:update:share";

    // ============ 캐시 관련 ============
    /** 날씨 정보 캐시 */
    public static final String WEATHER_CACHE = "weather:";

    // ============ 헬퍼 메서드 ============
    /**
     * OAuth 코드 전체 키 생성
     * @param code 일회용 코드
     * @return "oauth:code:{code}"
     */
    public static String oauthCodeKey(String code) {
        return OAUTH_CODE + code;
    }

    /**
     * Refresh Token 전체 키 생성
     * @param email 사용자 이메일
     * @return "refresh:{email}"
     */
    public static String refreshTokenKey(String email) {
        return REFRESH_TOKEN + email;
    }

    /**
     * 이메일 인증 토큰 전체 키 생성
     * @param token 인증 토큰
     * @return "email:verify:{token}"
     */
    public static String emailVerifyKey(String token) {
        return EMAIL_VERIFY + token;
    }

    /**
     * 회원가입 락 키 생성
     * @param email 사용자 이메일
     * @return "lock:signup:{email}"
     */
    public static String signupLockKey(String email) {
        return SIGNUP_LOCK + email;
    }

    /**
     * 게시글 좋아요 키 생성
     * @param postId 게시글 ID
     * @return "post:like:{postId}"
     */
    public static String postLikeKey(Long postId) {
        return POST_LIKE + postId;
    }

    /**
     * 게시글 공유 키 생성
     * @param postId 게시글 ID
     * @return "post:share:{postId}"
     */
    public static String postShareKey(Long postId) {
        return POST_SHARE + postId;
    }
}
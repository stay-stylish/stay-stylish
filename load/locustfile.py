import random
import logging
import time  # [수정] time 임포트 추가
from locust import HttpUser, task, between
from datetime import datetime, timedelta

# --- [!] 사용자 설정 필요 [!] ---
TEST_USER_EMAIL = "youngrae0317@gmail.com"
TEST_USER_PASSWORD = "password123!"
# ---------------------------------

# [신규] 폴링 설정
POLL_INTERVAL_SECONDS = 3  # 3초마다 GET 요청
POLL_TIMEOUT_SECONDS = 90  # 최대 90초까지 대기 (AI 응답 시간 + 버퍼)

class TravelOutfitCreatorUser(HttpUser):
    """
    '해외 여행 옷차림 추천 생성' (POST -> GET 폴링) 시나리오를 테스트합니다.

    1. (on_start) 로그인하여 JWT 토큰 발급
    2. (task)
       a. 랜덤 정보로 추천 생성 요청 (POST)
       b. 202 응답과 travelId 획득
       c. travelId로 상세 조회 (GET)를 반복 (폴링)
       d. status가 'COMPLETED'가 되면 최종 성공
       e. status가 'FAILED' 또는 'TIMEOUT' 시 최종 실패
    """

    # wait_time은 이전과 동일하게 유지합니다. (AI API 비용 고려)
    wait_time = between(60, 120)

    auth_header = None
    test_destinations = [
        {"country": "Japan", "city": "Tokyo"},
        {"country": "France", "city": "Paris"},
        {"country": "USA", "city": "New York"},
        {"country": "UK", "city": "London"},
        {"country": "Thailand", "city": "Bangkok"},
        {"country": "Spain", "city": "Barcelona"},
        {"country": "Italy", "city": "Rome"}
    ]

    def on_start(self):
        """테스트 사용자가 시작될 때 1회 실행됩니다. (로그인)"""
        try:
            response = self.client.post("/api/v1/auth/login", json={
                "email": TEST_USER_EMAIL,
                "password": TEST_USER_PASSWORD
            })
            response.raise_for_status()

            data = response.json().get("data")
            if not data or "accessToken" not in data:
                logging.error(f"Login failed: 'data.accessToken' not found. {response.text}")
                self.locust.runner.quit()
                return

            token = data["accessToken"]
            self.auth_header = {"Authorization": f"Bearer {token}"}
            logging.info(f"Login successful for user: {TEST_USER_EMAIL}")

        except Exception as e:
            logging.error(f"Login failed for user {TEST_USER_EMAIL}: {e}")
            self.locust.runner.quit()

    @task
    def create_and_poll_recommendation(self): # [수정] 메서드명 변경
        """(Task) 추천 생성(POST) 후 완료될 때까지 폴링(GET)합니다."""
        if not self.auth_header:
            return

        try:
            # --- 1. 랜덤 페이로드 생성 (기존과 동일) ---
            destination = random.choice(self.test_destinations)
            today = datetime.now()
            duration_days = random.randint(3, 7)
            max_start_offset = 14 - (duration_days + 1)
            start_day_offset = random.randint(1, max(1, max_start_offset))
            start_date = today + timedelta(days=start_day_offset)
            end_date = start_date + timedelta(days=duration_days)

            payload = {
                "country": destination["country"],
                "city": destination["city"],
                "startDate": start_date.strftime("%Y-%m-%d"),
                "endDate": end_date.strftime("%Y-%m-%d")
            }

            # [수정] Locust 리포트에서 POST와 GET을 하나로 묶기 위한 name 정의
            transaction_name = "/api/v1/travel-outfits/recommendations (Async Transaction)"

            # --- 2. POST 요청 (추천 접수) ---
            with self.client.post(
                "/api/v1/travel-outfits/recommendations",
                headers=self.auth_header,
                json=payload,
                name=transaction_name, # [수정] 트랜잭션 이름 사용
                catch_response=True
            ) as response:

                # [수정] 202 Accepted 상태 코드를 기대합니다.
                if response.status_code != 202:
                    response.failure(f"POST failed with {response.status_code} - {response.text}")
                    return

                # [수정] travelId 추출
                try:
                    travel_id = response.json().get("data", {}).get("travelId")
                    if not travel_id:
                        response.failure(f"POST 202, but no travelId in response: {response.text}")
                        return
                except Exception as e:
                    response.failure(f"POST 202, but response JSON parsing failed: {e}")
                    return

                # --- 3. GET 폴링 (완료 확인) ---
                poll_url = f"/api/v1/travel-outfits/recommendations/{travel_id}"
                start_time = time.time()

                while time.time() - start_time < POLL_TIMEOUT_SECONDS:

                    time.sleep(POLL_INTERVAL_SECONDS) # [신규] 폴링 간격 대기

                    # [신규] GET 요청
                    with self.client.get(
                        poll_url,
                        headers=self.auth_header,
                        name=transaction_name, # [수정] POST와 동일한 트랜잭션 name 사용 (리포트 그룹화)
                        catch_response=True
                    ) as get_response:

                        if not get_response.ok:
                            # 404, 500 등 GET 요청 자체가 실패하면 다음 폴링 시도
                            continue

                        try:
                            get_data = get_response.json().get("data", {})
                            status = get_data.get("status")

                            if status == "COMPLETED":
                                # [최종 성공]
                                # POST 요청(response)에 대해 성공으로 기록합니다.
                                response.success()
                                return

                            if status == "FAILED":
                                # [최종 실패]
                                error_msg = get_data.get("errorMessage", "Job failed")
                                response.failure(f"Polling OK, but job FAILED: {error_msg}")
                                return

                            if status == "PENDING":
                                # [진행 중]
                                continue # while 루프 계속

                        except Exception as e:
                            # GET 응답의 JSON 파싱 실패
                            logging.warning(f"Polling JSON parse error: {e}")
                            continue # 다음 폴링 시도

                # [최종 실패 - 타임아웃]
                # while 루프가 타임아웃으로 종료됨
                response.failure(f"Job TIMEOUT after {POLL_TIMEOUT_SECONDS}s (travelId: {travel_id})")

        except Exception as e:
            logging.error(f"Task failed with exception: {e}")
            if 'response' in locals():
                response.failure(str(e))
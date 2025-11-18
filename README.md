# Stay Stylish 👕

> AI 기반 개인화 OOTD / 여행 코디 추천 플랫폼

**Stay Stylish**는 날씨, 개인의 스타일 선호도, 여행 일정 등 복합적인 요소를 고려하여 사용자에게 최적화된 패션 스타일링을 제안하는 AI 기반 추천 플랫폼

단순히 날씨에 맞는 옷을 추천하는 것을 넘어, 사용자의 피드백을 학습하고, 방문하는 국가의 문화적 특성(여행)까지 고려하여 "오늘 뭐 입지?"라는 일상적인 고민을 해결합니다.

<br>

## 📋 목차

1. [🚀 서비스 개요](#-서비스-개요)
2. [✨ 핵심 기능](#-핵심-기능)
3. [🏗️ 아키텍처](#-아키텍처)
4. [📑 설계 문서](#-설계-문서)
5. [🛠️ 기술 스택](#-기술-스택)
6. [🤔 기술적 의사결정](#-기술적-의사결정)
7. [💣 트러블 슈팅](#-트러블-슈팅)
8. [📈 성능 개선](#-성능-개선)
9. [🧑‍💻 팀원 소개](#-팀원-소개)

<br>

## 🚀 서비스 개요

매일 아침 날씨에 맞는 옷을 고민하거나, 익숙하지 않은 곳으로 여행을 떠날 때 옷을 어떻게 입어야 될지 모르는 어려움을 겪곤하는데, **Stay Stylish**는 이러한 고민을 AI 기술로 해결.

* **오늘의 OOTD:** 현재 위치의 실시간 날씨(기온, 습도, 강수)와 사용자의 스타일 피드백(좋아요/싫어요)을 기반으로 AI가 맞춤형 데일리룩 추천.
* **여행 옷차림 추천:** 여행지의 일정, 평균 날씨, 그리고 현지 문화(종교, 에티켓)까지 고려한 스마트한 여행 코디 제안.
* **커뮤니티:** 자신만의 스타일을 공유하고, 다른 사용자의 코디에 '좋아요'나 '공유'를 누르며 패션 정보를 교류할 수 있는 공간 제공.

<br>

## ✨ 핵심 기능

### 1\. 👕 오늘의 OOTD 추천 (Daily)

* **GPS 기반 날씨 분석:** 사용자의 현재 위도/경도를 기반으로 국내 기상청 API를 호출하여 실시간 날씨 정보 조회.
* **AI 스타일링 제안:** 날씨, 사용자 선호 스타일, 성별 및 최근 피드백(좋아요/싫어요)을 종합하여 OpenAI (GPT-5-mini)가 오늘의 OOTD 텍스트와 추천 아이템 카테고리 생성.
* **쇼핑몰 연동:** AI가 추천한 카테고리를 기반으로 무신사, W컨셉 등 주요 쇼핑몰의 검색 결과 페이지로 바로 이동 가능한 링크 제공.
* **피드백 시스템:** 추천된 카테고리에 대해 '좋아요'/'싫어요' 피드백 기록. 이 데이터를 다음 추천 시 AI 프롬프트에 반영.

### 2\. ✈️ 해외 여행 옷차림 추천 (Travel)

* **비동기 추천 시스템:** 사용자가 국가, 도시, 일정을 선택해 추천 요청 시, 서버는 요청을 즉시 접수하고 백그라운드에서 비동기(`@Async`)로 추천 생성 처리.
* **글로벌 날씨 분석:** WeatherAPI를 통해 최대 14일간의 여행지 일별 평균 기온, 습도, 강수 확률, 날씨 상태 조회.
* **AI 코디 및 문화/안전 팁:** 날씨 정보와 사용자 성별을 기반으로 AI가 여행을 위한 3가지 스타일링 세트, 문화/종교적 복장 주의사항, 현지 안전 팁을 생성.

### 3\. 🧑‍🤝‍🧑 커뮤니티

* **CRUD:** JWT 인증을 기반으로 게시글을 작성, 조회, 수정, 삭제.
* **좋아요/공유:** 게시글에 '좋아요' 또는 외부 플랫폼으로 '공유'.
* **Redis 카운터:** '좋아요'와 '공유' 수는 Redis의 원자적 카운터(`INCR`/`DECR`)로 실시간 처리.
* **정렬:** 최신순(기본) 또는 Redis의 Sorted Set을 활용한 '좋아요순'으로 게시글 정렬 조회.

### 4\. 🔑 회원 및 인증

* **로컬 회원가입:** 이메일, 비밀번호(BCrypt 암호화) 기반의 회원가입 지원. 이메일 인증 토큰(Redis 저장)을 발송하여 계정 활성화.
* **OAuth 2.0:** Google 소셜 로그인 지원.
* **JWT (Access/Refresh):** 로그인 시 Access Token과 Refresh Token 발급. Refresh Token은 Redis에 저장하여 관리.
* **프로필 관리:** 닉네임, 선호 스타일, 성별 등 개인 정보 수정 및 회원 탈퇴(Soft Delete) 기능.

<br>

## 🏗️ 아키텍처

![image.png](src/main/resources/image.png)

* **CI/CD & Monitoring:**
    * **GitHub Actions:** `dev` 브랜치 Push 시 자동 Docker 이미지 빌드/Push 및 AWS SSM을 통한 ECS 배포.
    * **Docker / Docker Compose:** 로컬 및 개발 환경에서 Prometheus, Grafana, Loki 등 모니터링 스택과 DB 일관되게 관리.
    * **Prometheus & Grafana:** Spring Boot Actuator를 통해 JVM, API 응답 시간 등 핵심 메트릭 수집 및 시각화.
    * **Loki & Promtail:** EC2 인스턴스 및 로컬 Docker의 애플리케이션 로그를 수집하여 Grafana에서 검색 및 분석.
* **External APIs:**
    * **OpenAI (GPT):** 오늘의 OOTD 및 여행 코디 추천 생성 담당.
    * **KMA (기상청):** 국내 실시간 날씨 조회 담당.
    * **WeatherAPI:** 해외 여행지 날씨 예보 조회 담당.

<br>

## 📑 설계 문서

* **API 명세서:** [Swagger 링크](https://api.staystylish.store/swagger-ui/index.html) (
  OpenAPI 3.0)
* **DB 스키마 (ERD):** ![OOTD.png](src/main/resources/OOTD.png)
    * (Flyway 마이그레이션 스크립트: `V1__init_schema.sql`, `V2__add_performance_indexes.sql`)
* **GitHub 프로젝트 관리:**
    * [Issue 템플릿 (기능)]([feat-템플릿.md](.github/ISSUE_TEMPLATE/feat-%ED%85%9C%ED%94%8C%EB%A6%BF.md))
    * [Issue 템플릿 (버그)]([bug-fix--템플릿.md](.github/ISSUE_TEMPLATE/bug-fix--%ED%85%9C%ED%94%8C%EB%A6%BF.md))
    * [PR 템플릿]([pull_request_template.md](.github/pull_request_template.md))

<br>

# 🛠️ 기술 스택

## 🛠️ Language

|                                                                                                            |   |   |
|------------------------------------------------------------------------------------------------------------|---|---|
| <img src="https://img.shields.io/badge/Java-17-007396?style=for-the-badge&logo=openjdk&logoColor=white" /> |   |   |

---

## ⚙️ Backend

|                                                                                                                           |                                                                                                                       |                                                                                                                                |
|---------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------|
| <img src="https://img.shields.io/badge/Spring%20Boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" />       | <img src="https://img.shields.io/badge/Spring%20Web-6DB33F?style=for-the-badge&logo=spring&logoColor=white" />        | <img src="https://img.shields.io/badge/Spring%20WebClient-6DB33F?style=for-the-badge&logo=spring&logoColor=white" />           |
| <img src="https://img.shields.io/badge/Spring%20Data%20JPA-59666C?style=for-the-badge&logo=hibernate&logoColor=white" />  | <img src="https://img.shields.io/badge/Spring%20AI-6DB33F?style=for-the-badge&logo=spring&logoColor=white" />         | <img src="https://img.shields.io/badge/Spring%20Security-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white" />    |
| <img src="https://img.shields.io/badge/Spring%20OAuth%20Client-6DB33F?style=for-the-badge&logo=spring&logoColor=white" /> | <img src="https://img.shields.io/badge/Spring%20Validation-6DB33F?style=for-the-badge&logo=spring&logoColor=white" /> | <img src="https://img.shields.io/badge/Spring%20Cache%20(Caffeine)-B82030?style=for-the-badge&logo=cakephp&logoColor=white" /> |
| <img src="https://img.shields.io/badge/Resilience4j-000000?style=for-the-badge&logo=azurepipelines&logoColor=white" />    | <img src="https://img.shields.io/badge/JJWT-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white" />         | <img src="https://img.shields.io/badge/Lombok-BC2E1A?style=for-the-badge&logo=lombok&logoColor=white" />                       |

---

## 🗄️ Database & Cache

|                                                                                                                  |                                                                                                        |                                                                                                          |
|------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------|
| <img src="https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white" /> | <img src="https://img.shields.io/badge/Redis-D92C20?style=for-the-badge&logo=redis&logoColor=white" /> | <img src="https://img.shields.io/badge/Flyway-CC0200?style=for-the-badge&logo=flyway&logoColor=white" /> |

---

## 🔐 Security

|                                                                                                                             |                                                                                                              |                                                                                                               |
|-----------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------|
| <img src="https://img.shields.io/badge/Spring%20Security-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white" /> | <img src="https://img.shields.io/badge/JWT-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white" /> | <img src="https://img.shields.io/badge/OAuth%202.0-4285F4?style=for-the-badge&logo=google&logoColor=white" /> |

---

## ☁️ Cloud Service

|                                                                                                                |                                                                                                                                    |                                                                                                                |
|----------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------|
| <img src="https://img.shields.io/badge/Ubuntu-E95420?style=for-the-badge&logo=ubuntu&logoColor=white" />       | <img src="https://img.shields.io/badge/AWS%20SSM%20Parameter%20Store-FF9900?style=for-the-badge&logo=amazonaws&logoColor=white" /> | <img src="https://img.shields.io/badge/AWS%20ECR-FF9900?style=for-the-badge&logo=amazonecr&logoColor=white" /> |
| <img src="https://img.shields.io/badge/AWS%20ECS-FF9900?style=for-the-badge&logo=amazonecs&logoColor=white" /> | <img src="https://img.shields.io/badge/AWS%20RDS-527FFF?style=for-the-badge&logo=amazonrds&logoColor=white" />                     | <img src="https://img.shields.io/badge/AWS%20S3-569A31?style=for-the-badge&logo=amazons3&logoColor=white" />   ||                                                                                                                |

---

## 🏗️ Infra & CI/CD

|                                                                                                          |                                                                                                                    |                                                                                                                           |
|----------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------|
| <img src="https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white" /> | <img src="https://img.shields.io/badge/Docker%20Compose-2496ED?style=for-the-badge&logo=docker&logoColor=white" /> | <img src="https://img.shields.io/badge/GitHub%20Actions-2088FF?style=for-the-badge&logo=githubactions&logoColor=white" /> |

---

## 📚 API & Documentation

|                                                                                                            |                                                                                                                                  |   |
|------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------|---|
| <img src="https://img.shields.io/badge/Postman-FF6C37?style=for-the-badge&logo=postman&logoColor=white" /> | <img src="https://img.shields.io/badge/springdoc%20OpenAPI-6DB33F?style=for-the-badge&logo=openapiinitiative&logoColor=white" /> |   |

---

## 🔍 Monitoring & Logging

|                                                                                                             |                                                                                                                     |                                                                                                              |
|-------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------|
| <img src="https://img.shields.io/badge/Grafana-F46800?style=for-the-badge&logo=grafana&logoColor=white" />  | <img src="https://img.shields.io/badge/Prometheus-E6522C?style=for-the-badge&logo=prometheus&logoColor=white" />    | <img src="https://img.shields.io/badge/Loki-000000?style=for-the-badge&logo=loki&logoColor=white" />         |
| <img src="https://img.shields.io/badge/Promtail-000000?style=for-the-badge&logo=loki&logoColor=white" />    | <img src="https://img.shields.io/badge/Spring%20Actuator-6DB33F?style=for-the-badge&logo=spring&logoColor=white" /> | <img src="https://img.shields.io/badge/InfluxDB-22ADF6?style=for-the-badge&logo=influxdb&logoColor=white" /> |
| <img src="https://img.shields.io/badge/Logback-000000?style=for-the-badge&logo=logstash&logoColor=white" /> |                                                                                                                     |                                                                                                              |

---

## 🧪 Test

|                                                                                                          |                                                                                                            |                                                                                                  |
|----------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------|
| <img src="https://img.shields.io/badge/JUnit5-25A162?style=for-the-badge&logo=junit5&logoColor=white" /> | <img src="https://img.shields.io/badge/Mockito-0175C2?style=for-the-badge&logo=mockito&logoColor=white" /> | <img src="https://img.shields.io/badge/k6-7D64FF?style=for-the-badge&logo=k6&logoColor=white" /> |
| <img src="https://img.shields.io/badge/Locust-006400?style=for-the-badge&logo=python&logoColor=white" /> |                                                                                                            |                                                                                                  |

---

## 🤝 Collaboration

|                                                                                                                   |                                                                                                           |                                                                                                        |
|-------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------|
| <img src="https://img.shields.io/badge/Git-F05032?style=for-the-badge&logo=git&logoColor=white" />                | <img src="https://img.shields.io/badge/GitHub-181717?style=for-the-badge&logo=github&logoColor=white" />  | <img src="https://img.shields.io/badge/Slack-4A154B?style=for-the-badge&logo=slack&logoColor=white" /> |
| <img src="https://img.shields.io/badge/Notion-000000?style=for-the-badge&logo=notion&logoColor=white" />          | <img src="https://img.shields.io/badge/Zep-4B32C3?style=for-the-badge&logo=googlechat&logoColor=white" /> | <img src="https://img.shields.io/badge/Figma-F24E1E?style=for-the-badge&logo=figma&logoColor=white" /> |
| <img src="https://img.shields.io/badge/ERD%20Cloud-1A5FB4?style=for-the-badge&logo=databricks&logoColor=white" /> |                                                                                                           |                                                                                                        |

---

## 🧑‍💻 IDE

|                                                                                                                         |                                                                                                                       |   |
|-------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------|---|
| <img src="https://img.shields.io/badge/IntelliJ%20IDEA-000000?style=for-the-badge&logo=intellijidea&logoColor=white" /> | <img src="https://img.shields.io/badge/VS%20Code-007ACC?style=for-the-badge&logo=visualstudiocode&logoColor=white" /> |   |

---

## 🌐 Open API

|                                                                                                                   |                                                                                                                         |                                                                                                              |
|-------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------|
| <img src="https://img.shields.io/badge/Google%20OAuth2-4285F4?style=for-the-badge&logo=google&logoColor=white" /> | <img src="https://img.shields.io/badge/KMA%20API%20(기상청)-0052CC?style=for-the-badge&logo=cloudflare&logoColor=white" /> | <img src="https://img.shields.io/badge/WeatherAPI-00AEEF?style=for-the-badge&logo=icloud&logoColor=white" /> |
| <img src="https://img.shields.io/badge/OpenAI%20API-412991?style=for-the-badge&logo=openai&logoColor=white" />    |                                                                                                                         |                                                                                                              |

<br>

## 🤔 기술적 의사결정

<details>
<summary><strong>🤖 AI 모델 (GPT-5 mini)</strong></summary>
<br>

# 1. 개요

- 핵심 AI 기능을 구현하기 위해, `GPT-5 mini`, `GPT-4o mini`, `ChatGPT-4o`
  세 가지 모델을 비교 검토하고, `GPT-5 mini`를 최종 채택

# 2. 핵심 AI 기능

1. **데일리 OOTD 추천 (`DailyOutfitService`)**

- **요구사항**: 사용자의 위치(날씨), 성별, 선호 스타일, 최근 피드백 등을 입력받아,
  추천 멘트와 추천 카테고리 목록이 포함된 JSON 형식의 응답을 생성
- **필요 능력**: 어느 정도의 추론 능력, 안정적인 JSON 출력.

2. **해외 여행 옷차림 추천 (`TravelOutfitService`)**

- **요구사항**: 가장 복잡하고 핵심적인 AI 기능. 여행지(국가, 도시), 기간, 성별 정보를 받아, `GlobalWeatherApiClient`를 통해 날씨 정보를 조회하고,
  `TravelAiPromptBuilder`가 생성한 복잡한 프롬프트를 처리
- **AI 결과**: 코디 3세트, 문화적 제약사항, 안전 노트 등이 모두 포함된
  중첩된 JSON 형식(`AiTravelJsonResponse`)
- **필요 능력**: 높은 수준의 추론 능력 (날씨, 문화, 기간, 성별 동시 고려해야 하므로),
  매우 안정적인 JSON 출력 (파싱 실패 시 기능 자체가 실패)

3. 상품 분류 **(`ProductClassificationService`)**

- **요구사항**: 상품명을 입력받아 카테고리, 하위 카테고리, 스타일 태그를
  **JSON 형식**으로 분류
- **필요 능력**: 정확한 분류 능력, 안정적인 JSON 출력.

**핵심 결론**: 우리 프로젝트의 모든 AI 기능은 AI가 안정적으로 JSON을 생성하는 능력에
크게 의존하며, '여행 옷차림 추천' 기능은 높은 수준의 추론 능력을 요구

# 3. AI 모델 비교 분석

1. 모델 별 특징 및 비용

| **항목**                     | **GPT-5 mini**              | **GPT-4o mini**    | **ChatGPT-4o**               |
|:---------------------------|:----------------------------|:-------------------|:-----------------------------|
| **특징**                     | GPT-5의 경량/가성비 버전. 추론 능력 괜찮음 | 빠르고 저렴한 소형 모델. 실용적 | ChatGPT에서 기본으로 쓰이는 모델. 품질 좋음 |
| **Reasoning (추론)**         | **중간**                      | 낮음                 | 높음                           |
| **Intelligence (지능)**      | 중간                          | 낮음                 | 높음                           |
| **Speed (속도)**             | 빠름                          | **매우 빠름**          | 빠름                           |
| **입력 (Input)**             | 텍스트, 이미지                    | 텍스트, 이미지           | 텍스트, 이미지                     |
| **출력 (Output)**            | 텍스트, 이미지                    | 텍스트, 이미지           | 텍스트, 이미지                     |
| **Reasoning Tokens 사용**    | 가능                          | 불가                 | 불가                           |
| **입력 비용 (1M tokens)**      | $0.25                       | **$0.15**          | $5.00                        |
| **캐시 입력 비용**               | **$0.03**                   | $0.08              | -                            |
| **출력 비용 (1M tokens)**      | $2.00                       | **$0.60**          | $15.00                       |
| **Context Window**         | **400,000 tokens**          | 128,000 tokens     | 128,000 tokens               |
| **최대 출력 토큰**               | **128,000**                 | 16,384             | 16,384                       |
| **지식 컷오프**                 | **2024.05.31**              | 2023.10.01         | 2023.10.01                   |
| **v1/chat-completions 지원** | ✅                           | ✅                  | ✅                            |
| **v1/responses 지원**        | ✅                           | ✅                  | ✅                            |
| **v1/assistants**          | ❌                           | ✅                  | ❌                            |
| **v1/batch**               | ❌                           | ✅                  | ❌                            |
| **Fine-tuning 가능 여부**      | ❌                           | ✅                  | ❌                            |
| **Function Calling**       | ✅                           | ✅                  | ✅                            |
| **Structured Output**      | ✅                           | ✅                  | ❌                            |
| **이미지 입력**                 | ✅                           | ✅                  | ✅                            |

1. 모델별 Rate Limits (TPM)

| **Tier**      | **GPT-5 mini**      | **GPT-4o mini** | **ChatGPT-4o** |
|:--------------|:--------------------|:----------------|:---------------|
| **Free (무료)** | -                   | **40,000 TPM**  | -              |
| **Tier 1**    | **500,000 TPM**     | 200,000 TPM     | 30,000 TPM     |
| **Tier 2**    | 2,000,000 TPM       | 2,000,000 TPM   | 450,000 TPM    |
| **Tier 3**    | 4,000,000 TPM       | 4,000,000 TPM   | 800,000 TPM    |
| **Tier 4**    | 10,000,000 TPM      | 10,000,000 TPM  | 2,000,000 TPM  |
| **Tier 5**    | **180,000,000 TPM** | 150,000,000 TPM | 30,000,000 TPM |

# 4. 모델 선정 이유

### 1. `ChatGPT-4o` 제외 이유

- **비용 문제**: `ChatGPT-4o`는 입력($5.00), 출력($15.00) 비용이 `GPT-5 mini` 대비 각각 20배, 7.5배 높아 **현재 우리 규모의 서비스 운영 비용 측면에서 비현실적**
- `ChatGPT-4o`는 'Structured Output'을 지원하지 않음.
  우리 프로젝트의 모든 AI 기능은 AI의 JSON 응답을 파싱하는 것이 중요하므로
  이 모델은 **기술적으로 채택이 불가능**

---

### 2. `GPT-4o mini` 제외 사유

- **낮은 추론 능력**:
    - `GPT-4o mini`의 추론 능력은 낮다고 평가됨.
    - 이는 '데일리 OOTD 추천'이나 '상품 분류' 같은 단순 작업에는 충분할 수 있지만
      다양한 옷을 추천하는데 제한이 있음.
    - 해외 여행 옷차림 추천 기능의 복합적인 변수(날씨, 문화, 기간, 성별)를 동시 고려하여 복잡한 중첩 JSON을 생성하는 작업에는 실패할 위험이 큼.
        - 추론이 실패하면 JSON 파싱 실패가 되므로, 사용자에게 추천 실패를 응답
- **트렌드 민감도 (지식 컷오프)**:
    - 이 모델은 지식 컷오프가 '2023.10.01' 이므로, OOTD와 패션 트렌드를 다룰 때
      1년 이상 지난 데이터를 기반으로 추천하는 것은 현재 트렌드에 따라갈 수 없음.

---

### 3. `GPT-5 mini` 최종 채택 사유

`GPT-5 mini`는 위 두 모델의 단점을 보완하며 우리 프로젝트의 요구사항을 가장 균형 있게 만족하는 모델

1. **핵심 요구사항 충족**:
   추론 능력이 "중간" 등급이므로 `GPT-4o mini`의 "낮음"보다 훨씬 높아, 복잡한 프롬프트를 **안정적으로 처리하고 고품질의 JSON을 생성**할 수 있는 최소한의 성능을 보장.
2. **아키텍처 호환**:
   `Structured Output`을 완벽하게 지원하여, 프로젝트의 모든 AI 기능과 100% 호환
3. **최신성**:
   지식 컷오프가 '2024.05.31'로, 비교 모델 중 가장 최신.
   이는 다른 저렴한 모델보다 훨씬 최신 트렌드를 반영해줄 것이다.
4. **비용 합리성 및 속도**:

- 비용($0.25/$2.00)은 `ChatGPT-4o`보다 압도적으로 저렴하여 운영 부담이 적음.
- `GPT-4o mini`보다 비싸긴 하지만,
  → '추론 품질'과 '안정성'을 확보하기 위함이다.

</details>

<details>
<summary><strong>📡 비동기 API 호출 (WebClient)</strong></summary>
<br>

# 1. 도입 배경

- 우리 프로젝트는 핵심 기능을 위해 다수의 외부 API(기상청, WeatherAPI, OpenAI 등) 호출에 크게 의존한다.
- 이러한 외부 API 호출은 네트워크 지연이나 응답 실패가 발생하기 쉬운 대표적인 **I/O Bound** 작업이다.
- 기존의 동기 방식인 `RestTemplate`을 사용할 경우, API 응답을 기다리는 동안 스레드가 차단되어 서버 전체의 성능과 동시 처리 능력이 저하될 수 있다.
- 사용자에게 더 나은 응답성을 제공하고 한정된 서버 자원을 효율적으로 사용하기 위해, 비동기 논블로킹(Non-Blocking) HTTP 클라이언트의 도입이 필요했다.

<br>

# 2. 기술적 요구사항

1. **비동기 논블로킹(Non-Blocking) I/O 지원**: 외부 API 호출 시 스레드를 차단하지 않고, 응답이 왔을 때 콜백 방식으로 처리하여 서버 자원을 효율적으로 사용해야 한다.
2. **Spring Boot 3+ 호환성**: `RestTemplate`은 Spring 5부터 유지보수 모드이며, Spring Boot 3+ 환경에서는 `WebClient`가 공식적으로 권장된다.
3. **유연한 API 및 다양한 응답 처리**: JSON뿐만 아니라 XML(기상청 API) 등 다양한 형식의 응답을 유연하게 처리할 수 있어야 한다.
4. **Resilience4j 통합**: 외부 API 장애에 대비하여 `Retry` (재시도), `CircuitBreaker` (서킷 브레이커) 패턴을 손쉽게 통합할 수 있어야 한다.
5. **설정의 용이성**: API별로 `baseUrl`, `timeout`, 응답 버퍼 크기 등을 유연하게 설정할 수 있어야 한다.

<br>

# 3. 의사결정 과정 (대안 비교)

| 항목             | `RestTemplate` (대안 1)          | `WebClient` (대안 2)                       |
|:---------------|:-------------------------------|:-----------------------------------------|
| **패러다임**       | 동기(Synchronous), 블로킹(Blocking) | 비동기(Asynchronous), 논블로킹(Non-Blocking)    |
| **스레드 모델**     | 요청당 스레드 1개 점유 (응답 대기 시 차단)     | 적은 수의 스레드로 다수 요청 처리 (Event Loop)         |
| **Spring 권장**  | **Legacy** (유지보수 모드)           | **Modern** (Spring 5+ 표준)                |
| **Resilience** | `RetryTemplate` 등 별도 설정 필요     | Resilience4j의 Reactive 모듈과 궁합이 좋음        |
| **응답 타입**      | `T` (즉시 반환)                    | `Mono<T>` / `Flux<T>` (Reactive Streams) |

- **`RestTemplate` 제외 이유**:
    - 가장 치명적인 단점으로, API 호출 시 스레드를 블로킹한다. 이는 서비스의 전체 처리량을 심각하게 저하시킨다.
    - `AsyncRestTemplate`이 존재했으나 Spring 5에서 Deprecated 되었으며 `WebClient` 사용이 권장된다.
- **`WebClient` 채택 이유**:
    - Spring WebFlux 모듈에 포함되어 있으며, 논블로킹 I/O를 지원하여 I/O 대기 시간을 효율적으로 활용, 더 적은 스레드로 높은 동시성을 처리할 수 있다.
    - 현대적인 Spring 애플리케이션의 표준 HTTP 클라이언트이다.

<br>

# 4. 최종 선택 이유 (WebClient)

`WebClient`는 위 요구사항을 가장 잘 만족하는 현대적인 HTTP 클라이언트이다.

1. **성능 및 확장성 (논블로킹 I/O)**

- 외부 API 호출이 많은 우리 서비스 특성상, I/O 대기 시간을 차단하지 않는 `WebClient`의 논블로킹 방식은 필수적이었다. 이를 통해 적은 수의 스레드로도 많은 동시 요청을 처리할 수 있어 서버 자원을
  매우 효율적으로 사용한다.

2. **현대적인 표준 및 유지보수성**

- `RestTemplate` 대신 Spring Boot 3+ 환경에서 공식적으로 권장되는 표준 클라이언트이다.
- `WebClient.Builder`를 통해 API별로 `baseUrl`이나 응답 버퍼(`ExchangeStrategies`)를 커스터마이징하기 용이하다.

3. **유연한 통합**

- 우리 프로젝트는 대부분의 서비스 로직이 동기(`@Service`, `@Transactional`) 방식으로 동작한다.
- `WebClient`는 비동기(`Mono`, `Flux`)가 기본이지만, 필요에 따라 `.block()` 또는 `.blockOptional()`을 사용하여 **동기식 서비스 로직에 유연하게 통합**할 수 있었다.
- `GlobalWeatherApiClientImpl`에서는 `.block()`을 사용해 동기적으로 날씨 정보를 가져오며, 이 로직은 `@Async`로 동작하는 `TravelOutfitServiceImpl` 내부에서
  호출되어 메인 스레드를 차단하지 않는다.
- `LocalWeatherServiceImpl`은 `Mono`를 반환하여 비동기 처리가 가능하도록 설계했으나, 호출부인 `DailyOutfitService`에서는 `.blockOptional()`을 사용해
  동기적으로 결과를 기다린다.
- 이처럼 `WebClient`는 전체 아키텍처를 바꾸지 않고도 I/O 성능을 개선할 수 있는 유연성을 제공한다.

</details>

<details>
<summary><strong>🗄️ 데이터베이스 (PostgreSQL)</strong></summary>
<br>

# 1. 도입 배경

- **Stay Stylish**는 사용자의 기본 정보(회원, 인증), 스타일 선호도, 게시글(커뮤니티), AI 추천 결과(OOTD, 여행) 등 다양한 형태의 데이터를 관리해야 한다.
- 데이터의 일관성과 무결성이 매우 중요하며, 사용자와 게시글, 좋아요, 피드백 간의 관계를 명확하게 정의하고 관리할 필요가 있다.
- 이러한 요구사항을 충족하기 위해 관계형 데이터베이스(RDBMS)의 도입이 필수적이었다.
- 동시에, AI가 생성하는 복잡하고 중첩된 추천 결과(JSON)를 유연하게 저장하고 처리할 수 있는 능력 또한 요구되었다.

<br>

# 2. 기술적 요구사항

1. **관계형 데이터 무결성**: ACID 트랜잭션을 완벽하게 지원하고, Foreign Key(외래 키) 제약 조건을 통해 데이터의 일관성을 보장해야 한다.
2. **Spring 호환성**: Spring Data JPA, Hibernate 등 프로젝트의 핵심 데이터 접근 기술과 완벽하게 호환되어야 한다.
3. **JSON 데이터 타입 지원**: AI가 생성하는 중첩된 JSON 응답(`AiTravelJsonResponse` 등)을 변환 없이 원본 그대로, 효율적으로 저장하고 인덱싱할 수 있어야 한다. (V1 스키마의
   `jsonb` 타입)
4. **오픈소스 및 비용 효율성**: 라이선스 비용이 없는 강력한 오픈소스 RDBMS여야 한다.
5. **클라우드 호환성 (AWS RDS)**: 프로덕션 환경인 AWS의 관리형 데이터베이스 서비스(RDS)에서 완벽하게 지원되어야 한다.

<br>

# 3. 의사결정 과정 (대안 비교)

| 항목            | **MySQL** (대안 1) | **MongoDB (NoSQL)** (대안 2) | **PostgreSQL** (대안 3) |
|:--------------|:-----------------|:---------------------------|:----------------------|
| **유형**        | RDBMS            | Document DB                | ORDBMS (객체-관계형)       |
| **트랜잭션**      | **지원 (ACID)**    | 제한적 지원 (RDBMS 대비 복잡)       | **지원 (ACID)**         |
| **JPA 호환성**   | **우수**           | 불가능 (JPA는 SQL용)            | **우수**                |
| **JSON 지원**   | `JSON` 타입 지원     | **매우 우수 (Native)**         | **`jsonb` 타입 (최상급)**  |
| **클라우드(RDS)** | **지원**           | (RDS 미지원, DocumentDB로 대체)  | **지원**                |
| **특징**        | 가장 대중적, 빠른 읽기 속도 | 유연한 스키마, 수평 확장 용이          | 표준 SQL + 강력한 확장 기능    |

- **`MongoDB` 제외 이유**:
    - 회원, 게시글, 좋아요 간의 복잡한 관계와 데이터 무결성, 트랜잭션을 관리하기에 부적합하다.
    - Spring Data JPA를 사용할 수 없어 데이터 접근 계층을 이원화해야 하는 복잡성이 발생한다.
- **`MySQL` 제외 이유**:
    - 훌륭한 RDBMS이지만, `jsonb` 타입을 제공하는 PostgreSQL 대비 JSON 데이터 처리(인덱싱, 내부 필드 쿼리) 성능과 유연성이 상대적으로 부족하다고 평가된다.
    - 우리 프로젝트는 AI가 생성한 JSON을 DB에 저장하는 것이 핵심 기능 중 하나이므로 이 부분이 중요했다.

<br>

# 4. 최종 선택 이유 (PostgreSQL)

PostgreSQL은 "관계형 데이터"와 "JSON 데이터"라는 두 가지 상이한 요구사항을 동시에 가장 잘 만족하는 데이터베이스이다.

1. **하이브리드 데이터 처리 (RDBMS + NoSQL)**

- 회원 정보, 게시글 등은 RDBMS의 강력한 트랜잭션과 정합성을 보장받는다.
- 동시에, `TravelOutfit` 엔티티의 `aiOutfitJson`, `culturalConstraintsJson` 컬럼에 `jsonb` 타입을 사용하여, AI가 생성한 복잡한 중첩 JSON을 스키마 변경
  없이 그대로 저장할 수 있다. 이는 개발 유연성과 성능을 크게 향상시킨다.

2. **Spring 및 Flyway 생태계 완벽 호환**

- `build.gradle`에 `postgresql` JDBC 드라이버 및 `flyway-database-postgresql` 의존성을 추가하여 Spring Boot 애플리케이션과 완벽하게 통합된다.
- `V1__init_schema.sql`, `V2__add_performance_indexes.sql` 등 Flyway 마이그레이션 스크립트를 통해 스키마를 체계적으로 관리할 수 있다.

3. **오픈소스 및 클라우드 지원**

- 가장 진보적인 오픈소스 RDBMS로 평가받으며, 라이선스 비용 부담이 없다.
- `docker-compose.yml`을 통해 로컬 개발 환경(postgres:15)을 쉽게 구축할 수 있다.
- `README.md`와 `application-dev.yml`에서 명시하듯, AWS RDS를 통해 프로덕션 환경에서도 안정적으로 운영할 수 있다.

4. **검증된 안정성 및 확장성**

- 복잡한 쿼리, 인덱싱(GIN 인덱스 등) 및 데이터 무결성 측면에서 높은 신뢰도를 제공한다.

</details>

<details>
<summary><strong>📈 부하 테스트 도구 (k6)</strong></summary>
<br>

# 1. 도입 배경

- Stay Stylish 서비스는 AI 추천, 외부 API 호출, 실시간 Redis 카운터 등 복잡하고 성능에 민감한 기능을 다수 포함한다.
- 특히 '여행 옷차림 추천' 기능은 요청 접수(동기) 후 AI 처리(비동기)가 완료될 때까지 클라이언트가 폴링(polling)하는 복잡한 시나리오를 가진다.
- 실제 사용자 트래픽이 몰렸을 때의 API 응답 시간을 보장하고, 병목 지점을 파악하며, 안정성을 검증하기 위한 부하 테스트 도구의 도입이 필수적이었다.

<br>

# 2. 기술적 요구사항

1. **고성능 및 리소스 효율성**: JMeter의 스레드 기반 모델과 달리, 적은 리소스로도 수백, 수천 개의 가상 사용자(VU)를 시뮬레이션할 수 있어야 한다.
2. **스크립트 기반 테스트**: 개발자가 복잡한 테스트 시나리오(e.g., 로그인 → JWT 획득 → API 호출 → 비동기 결과 폴링)를 코드로 쉽게 작성하고 버전 관리(Git)할 수 있어야 한다.
3. **JavaScript/TypeScript 지원**: 백엔드(Java) 개발자도 쉽게 배울 수 있고, 프론트엔드와 유사한 언어(JavaScript)로 스크립트를 작성할 수 있어야 한다. (Python,
   Groovy 등 추가 언어 스택 배제)
4. **모니터링 통합**: 테스트 결과를 `InfluxDB`로 전송하고 `Grafana`에서 실시간으로 시각화할 수 있는 네이티브 통합을 지원해야 한다. (프로젝트의 `docker-compose.yml` 내
   모니터링 스택과 일치)
5. **CI/CD 친화성**: GUI 없이 CLI 명령어로 모든 테스트를 실행하고, 테스트 실패 시(Thresholds) CI 파이프라인을 중단시킬 수 있어야 한다.

<br>

# 3. 의사결정 과정 (대안 비교)

| 항목         | **JMeter** (대안 1) | **Locust** (대안 2) | **k6** (대안 3)                |
|:-----------|:------------------|:------------------|:-----------------------------|
| **개발 언어**  | Java              | Python            | Go                           |
| **스크립트**   | GUI / XML         | **Python**        | **JavaScript**               |
| **리소스 모델** | **스레드 기반 (고비용)**  | 코루틴 기반 (효율적)      | **이벤트 기반 (매우 효율적)**          |
| **모니터링**   | 플러그인 필요           | Web UI / 플러그인     | **InfluxDB/Grafana 네이티브 지원** |
| **CI/CD**  | CLI 지원 (다소 무거움)   | CLI 지원            | **CLI 네이티브 (매우 가벼움)**        |

- **`JMeter` 제외 이유**:
    - 제공된 표에서 알 수 있듯이, 스레드 기반 모델은 가상 사용자(VU) 수만큼 스레드를 생성하여 **상당한 리소스가 필요**하다. 높은 동시성 테스트에 불리하다.
    - GUI 기반 설정과 XML 스크립트는 Git으로 버전 관리하기 어렵고 CI/CD 연동에 k6보다 불편하다.
- **`Locust` 제외 이유**:
    - 리소스 효율성은 좋으나, 스크립트 작성을 위해 **Python** 학습이 필요하다.
    - 우리 프로젝트는 Java(백엔드)와 JavaScript(k6 스크립트)로 기술 스택을 통일하는 것이 유지보수 측면에서 유리하다고 판단했다.

<br>

# 4. 최종 선택 이유 (k6)

`k6`는 위에서 정의한 모든 기술적 요구사항을 가장 완벽하게 만족하는 도구이다.

1. **압도적인 성능과 리소스 효율성**

- `k6`는 Go 언어로 작성되었으며, 이벤트 기반의 비동기 I/O 모델을 사용한다.
- 이는 JMeter의 스레드 모델보다 훨씬 적은 CPU와 메모리로 수천 개의 동시 접속을 시뮬레이션할 수 있게 하여, 테스트 환경 구성 비용을 획기적으로 낮춘다.

2. **JavaScript 기반의 유연한 스크립트**

- `k6-travel-recommendations.js`의 `setup` 함수에서 로그인(JWT 획득)을 처리하고, `default` 함수에서 POST 요청과 `http.get`을 이용한 1초 단위 폴링(
  Polling)을 구현하는 등, **복잡한 비동기 시나리오를 직관적인 코드로 작성**할 수 있었다.

3. **현대적인 모니터링 스택과 통합**

- `docker-compose.yml`에 정의된 `k6`, `influxdb`, `grafana` 서비스 구성은 k6가 네이티브로 지원하는 **"k6 -> InfluxDB -> Grafana"** 파이프라인을
  그대로 활용한다.
- k6는 테스트 결과를 InfluxDB로 실시간 전송(`--out influxdb=...`)할 수 있으며, Grafana는 이 데이터를 즉시 시각화하여 테스트 중 병목 지점을 실시간으로 분석할 수 있게 한다.

</details>

<details>
<summary><strong>⚡ Redis</strong></summary>
<br>

# 1. 도입 배경

- **Stay Stylish** 프로젝트는 높은 성능과 실시간 데이터 처리를 요구하는 다양한 기능을 포함한다.
- **첫째**, API 응답 속도 향상을 위해 외부 API(기상청, WeatherAPI, OpenAI) 호출 결과의 캐싱이 필요했다.
- **둘째**, JWT Refresh Token, 이메일 인증 토큰, OAuth 세션 코드 등 만료 시간(TTL)이 필요한 임시 데이터를 안정적으로 관리할 저장소가 필요했다.
- **셋째**, 커뮤니티 기능의 '좋아요'와 '공유' 수는 관계형 데이터베이스(PostgreSQL)에 매번 쓰기(Write) 작업을 수행하기엔 성능 부하가 너무 크다. 이를 위한 **Atomic Counter**가
  필요했다.
- **넷째**, '좋아요순' 게시글 정렬을 위해 실시간으로 업데이트되는 랭킹 보드 기능이 요구되었다.
- **다섯째**, 여러 서버 인스턴스 간에 상태를 공유하고, 동시성 문제를 제어하기 위한 **분산 락(Distributed Lock)**이 필요했다 (e.g., 회원가입 시 이메일 중복 검사).

<br>

# 2. 기술적 요구사항

1. **고속 I/O**: 인메모리(In-Memory) 기반으로 빠른 읽기/쓰기 속도를 보장해야 한다.
2. **다양한 자료구조**: 단순 Key-Value 외에 `Sets`, `Sorted Sets`, `Hashes` 등 커뮤니티 기능에 필요한 고급 자료구조를 지원해야 한다.
3. **Atomic 연산**: `INCR`, `DECR` 등 원자적 연산을 지원하여 Race Condition 없이 카운터를 관리할 수 있어야 한다.
4. **데이터 만료 (TTL)**: 캐시, 인증 토큰 등에 필수적인 TTL 기능을 제공해야 한다.

<br>

# 3. 의사결정 과정 (대안 비교)

| 구분             | RDBMS (PostgreSQL)     | In-Memory Local Cache | Redis (채택)                     |
|:---------------|:-----------------------|:----------------------|:-------------------------------|
| **실시간 데이터 처리** | 성능 저하 (Disk I/O, Lock) | 불가 (인스턴스 간 공유 불가)     | ✅ **Atomic 명령어로 원자적 처리**       |
| **분산 락**       | 비효율적 (DB Lock)         | 불가 (공유 불가)            | ✅ **SETNX 기반의 효율적 분산 락**       |
| **캐싱**         | 제한적                    | 매우 빠름 (공유 불가)         | ✅ **모든 인스턴스가 공유하는 고성능 캐시**     |
| **다양한 자료구조**   | 스키마에 종속                | 단순 Key-Value          | ✅ **Sorted Set 등 다양한 자료구조 지원** |

- **RDBMS (PostgreSQL) 제외 이유**: '좋아요' 클릭마다 `UPDATE`를 실행하거나 `ORDER BY like_count`로 정렬하는 것은 DB에 치명적인 부하를 유발한다. 분산 락 구현도
  가능은 하나 성능상 비효율적이다.
- **In-Memory Local Cache (Caffeine) 제외 이유**: `RedisConfig`에서 보듯이 Caffeine은 이미 "userProfile" 등 단순 조회용 로컬 캐시로 사용 중이다. 하지만
  JVM 프로세스에 종속되어 인스턴스 간 **데이터 공유가 불가능**하므로, 분산 락이나 실시간 카운터, 토큰 공유 용도로는 사용할 수 없다.

<br>

# 4. 최종 선택 이유 (Redis)

Redis는 캐시, 세션 관리, 실시간 데이터 처리 등 다양한 요구사항을 해결할 수 있는 가장 표준적인 인메모리 데이터 저장소이다.

1. **다목적(Multi-Purpose) 활용**:

- **캐싱**: `LocalWeatherServiceImpl` 및 `RedisConfig`에서 보듯이, 외부 API 응답을 캐시하여 성능 향상 및 비용 절감.
- **세션/토큰 관리**: `RefreshTokenService`, `EmailVerificationService`, `AuthService`에서 TTL이 적용된 인증/세션 데이터를 안정적으로 관리.
- **분산 락**: `AuthService`에서 `setIfAbsent`를 활용, 회원가입 시 이메일 중복 처리를 위한 분산 락으로 사용.
- **실시간 데이터 처리**: `PostCounterService`에서 핵심 기능을 수행한다.

2. **고급 자료구조 활용 (커뮤니티 최적화)**

- **Atomic Counters (`INCR`/`DECR`)**: `PostCounterService`에서 '좋아요', '공유' 수를 트랜잭션 없이 원자적으로 관리하여 DB 부하를 원천 차단한다.
- **Sorted Sets (`ZSET`)**: `post:like:sorted` 키를 통해 '좋아요' 수를 score로 하는 실시간 랭킹 보드를 구현, `PostService`에서 '좋아요순' 정렬 조회 시 DB
  쿼리 없이 Redis에서 바로 결과를 반환한다.
- **Sets (`SET`)**: `post:update:like` 같은 'dirty set'을 관리하여, 변경된 데이터만 주기적으로 DB에 동기화(`syncToDB`)하는 Write-Back 캐시 전략을
  효율적으로 구현했다.

</details>

<details>
<summary><strong>🔑 소셜 로그인 (OAuth 2.0)</strong></summary>
<br>

# 1. 도입 배경

- 현재 소셜 로그인은 사용자의 가입 장벽을 낮추고 편의성을 극대화하는 필수 기능이다.
- 사용자가 Google과 같은 신뢰할 수 있는 ID 공급자(IdP)를 통해, 별도의 회원가입 절차 없이 간편하게 로그인할 수 있도록 지원하고, 이 사용자를 **기존 JWT 인증 시스템에 완벽하게 통합**시킬 필요가
  있었다.

<br>

# 2. 기술적 요구사항

1. **표준 프로토콜 준수**: OAuth 2.0 및 OIDC(OpenID Connect) 표준을 준수해야 한다.
2. **주요 IdP 지원**: 사용자가 선호하는 주요 소셜 로그인(e.g., Google)을 지원해야 한다.
3. **기존 인증 시스템과 통합**: 소셜 로그인 성공 시, 서버는 **세션(Session)이 아닌** 프로젝트의 표준 인증 방식인 **Access Token / Refresh Token (JWT)을 발급**해야
   한다.
4. **신규/기존 사용자 식별**: 소셜 로그인 사용자가 신규 가입자인지(DB에 이메일이 없는지) 또는 기존 회원인지 식별하고 다르게 처리할 수 있어야 한다.
5. **Stateless 아키텍처 유지**: 인증 처리 과정이 서버의 세션 상태에 의존하지 않아야 한다.
6. **안전한 토큰 전달**: 서버에서 발급된 JWT가 프론트엔드(SPA)로 안전하게 전달되어야 한다. (e.g., URL 파라미터를 통한 토큰 노출 방지)

<br>

# 3. 최종 선택 이유

- 우리 프로젝트는 **일회용 코드 교환** 방식을 채택했다. 이는 Stateless 아키텍처를 유지하면서 가장 안전하게 토큰을 전달하는 방식이다.

1. **Stateless 아키텍처 및 JWT 통합 (`OAuth2SuccessHandler`)**

- `SecurityConfig`에 `OAuth2SuccessHandler`를 커스텀 핸들러로 등록했다.
- 인증 성공 시, 이 핸들러는 **서버 세션을 사용하지 않는다.** 대신 Access/Refresh Token(JWT)을 서버에서 직접 발급한다.

2. **Redis를 통한 안전한 토큰 교환 (보안성)**

- 발급된 JWT와 사용자 정보(`isNewUser` 등)를 `ObjectMapper`로 직렬화한다.
- `UUID.randomUUID()`로 **일회용 인증 코드(Code)**를 생성한다.
- 토큰 정보(JSON)를 이 일회용 코드(Key)에 매핑하여 **Redis에 5분(TTL)간 저장**한다 (`RedisKeyConstants.OAUTH_CODE`).
- 클라이언트(브라우저)에게는 이 **안전한 일회용 코드**만 포함된 URL로 리다이렉트시킨다.

3. **프론트엔드 연동 및 유연성 (`/auth/oauth/exchange`)**

- 프론트엔드는 리다이렉트된 URL에서 일회용 코드를 파싱한 후, 즉시 서버의 `/api/v1/auth/oauth/exchange` 엔드포인트로 이 코드를 전송하여(POST) 실제 JWT를 교환
  한다.
- `AuthService`의 `exchangeOAuthCode`는 Redis에서 코드를 검증하고, 사용된 코드를 즉시 삭제(일회용 보장)한 뒤, 저장되어 있던 JWT를 프론트엔드로 안전하게 반환한다.
- 이 과정에서 `OAuth2SuccessHandler`는 `userPrincipal.isNewUser()` 값을 확인하여, 신규 사용자인 경우 프론트엔드의 추가 정보 입력 페이지(
  `/oauth/success/signup/additional`)로 리다이렉트시키는 등 유연한 UX 분기 처리가 가능하다.

</details>

<details>
<summary><strong>🛡️ Retry, Circuit Breaker</strong></summary>
<br>

# 1. 도입 배경

- **Stay Stylish** 프로젝트의 핵심 기능은 **OpenAI(AI), 기상청(KMA), WeatherAPI(해외 날씨)**라는 3개 이상의 외부 API에 의존한다.
- 이 외부 API들은 네트워크, 트래픽, 자체 서버 이슈 등으로 인해 **일시적인 오류**가 발생하거나, **응답이 지연**되거나, **지속적인 장애** 상태에 빠질 수 있다.
- 이러한 외부 장애가 발생할 때 아무런 방어 메커니즘이 없다면, 장애가 내부 시스템으로 전파(Cascading Failure)된다.
- **첫째**, 스레드가 외부 응답을 무한정 기다리며 전체 애플리케이션의 스레드 풀이 고갈되어 서비스 전체가 마비될 수 있다.
- **둘째**, 사용자는 오류 응답을 받거나 무한 로딩을 경험하여 UX가 심각하게 저하된다.
- 따라서, 외부 서비스의 장애로부터 내부 시스템을 보호하고 서비스 연속성을 보장하는 **장애 내성** 패턴의 도입이 필수적이었다.

<br>

# 2. 기술적 요구사항

1. **재시도 (Retry)**: 네트워크 불안정 등 **일시적인 오류**에 대해, 설정된 횟수(e.g., 3회)만큼 자동으로 재시도하여 성공률을 높여야 한다. (`@Retry`)
2. **서킷 브레이커 (Circuit Breaker)**: **지속적인 오류**가 일정 비율(e.g., 50%) 이상 발생하면, "회로(Circuit)"를 "차단(Open)"하여 즉시 실패 응답을 반환해야 한다.
   이는 실패할 호출을 계속 시도하여 자원을 낭비하는 것을 막고, 외부 서비스에 복구할 시간을 준다. (`@CircuitBreaker`)
3. **폴백 (Fallback)**: 재시도에 최종 실패하거나 서킷 브레이커가 열렸을 때, 사용자에게 500 오류 대신 사전에 정의된 응답(e.g., "AI 추천에 실패했습니다. 잠시 후 다시 시도해주세요.")을
   반환하여 **서비스 연속성**을 제공해야 한다.
4. **선언적 적용 (AOP)**: 비즈니스 로직(Service, Client) 코드에 `try-catch` 재시도 로직을 하드코딩하는 것이 아니라, `@Retry` 어노테이션 등을 통해 AOP로 깔끔하게
   분리/적용할 수 있어야 한다.
5. **유연한 설정**: API별(KMA, OpenAI)로 재시도 횟수, 서킷 차단 임계값, 열림 시간 등을 `application.yml`을 통해 유연하게 설정할 수 있어야 한다.

<br>

# 3. 의사결정 과정 (대안 비교)

### 📈 Retry 3회, Threshold 50% 설정 근거

- **`maxAttempts: 3` (재시도 3회)**
    - **이유**: 1~2회 재시도는 일시적인 네트워크 오류(502, 504)나 타임아웃에 대응하기에 부족할 수 있다. 반면 5회 이상의 과도한 재시도는 (1) 사용자의 최종 응답 시간을 심각하게
      지연시키고, (2) 이미 장애가 발생한 외부 서비스에 불필요한 부하를 가중시킨다.
    - **결론**: **3회**는 **일시적 오류 복구 기회**와 **시스템 전체의 안정성** 사이의 가장 합리적이고 표준적인 절충안이다.

- **`failureRateThreshold: 50` (실패율 50% 임계값)**
    - **이유**: 이 설정은 `slidingWindowSize` (e.g., 최근 10회) 동안의 호출 중 **절반(5회) 이상**이 실패하면 서킷을 차단(Open)한다는 의미이다.
    - 10%~20%처럼 너무 낮은 임계값은 사소한 일시적 오류에도 서킷이 열려 과도하게 방어적으로 동작할 수 있다. 반대로 80% 이상은 이미 서비스가 심각한 장애 상태임에도 계속 호출을 시도하여 내부 시스템의
      자원을 낭비하게 된다.
    - **결론**: **50%**는 "호출이 성공할 확률과 실패할 확률이 같아진" 명백한 장애 상황을 의미하며, 이 시점에서 호출을 차단하고 즉시 폴백(Fallback)으로 전환하는 것이 가장 합리적인
      기준점이다.

<br>

# 4. 최종 선택 이유 (Resilience4j)

Resilience4j는 위에서 정의한 모든 기술적 요구사항을 가장 완벽하게 만족하는 Spring Boot 3+ 환경의 표준 장애 내성 라이브러리이다.

1. **AOP 기반의 깔끔한 로직 분리**

- `GlobalWeatherApiClientImpl`, `DailyAiClient`, `TravelAiClient`, `LocalWeatherServiceImpl` 등 **모든 외부 API 클라이언트**에
  `@Retry`와 `@CircuitBreaker` 어노테이션이 적용되었다.
- 이를 통해 비즈니스 로직은 API 호출에만 집중하고, 장애 처리는 AOP(Spring AOP)가 알아서 처리하도록 완벽하게 분리했다.

2. **Fallback 메커니즘**

- 모든 `@Retry`/`@CircuitBreaker`는 `fallbackMethod` 속성을 지정하고 있다.
- 예를 들어 `TravelAiClient`에서 OpenAI API 호출이 최종 실패하면, `fallback` 메소드가 호출되어 사용자 정의 예외(`ExternalApiException`)를 발생시킨다. 이는
  `GlobalExceptionHandler`에 의해 503 오류 대신 '외부 API 호출에 실패'했다는 일관된 응답으로 변환되어 사용자에게 전달된다. 이는 서비스가 오류 상황에서도 '죽지 않고' 정상적인 응답
  흐름을 유지하게 한다.

3. **API별 세분화된 설정 (`application.yml`)**

- 이는 각 API의 특성에 맞춰 최적의 장애 대응 전략을 구성할 수 있게 한다.

</details>
-----

## 💣 트러블 슈팅

<details>
<summary><strong>[k6 테스트 시 401 Unauthorized 오류 발생]</strong></summary>
<br>

# 문제 상황

k6 부하 테스트 스크립트 실행 시 모든 API 요청이 401 Unauthorized 오류로 100% 실패했습니다.

분명 토큰 검증 실패로 인한 401 오류임에도 불구하고, JwtProvider가 실패 시 남기도록 설계된 `[JWT] 유효하지 않은 토큰` 오류 로그가 전혀 기록되지 않았습니다.

# 문제 분석

서버가 401 Unauthorized를 반환했다는 것은 JwtProvider.validateToken() 메서드가 false를 반환했어야 함을 의미합니다.

하지만 애플리케이션 로그에는 JwtProvider가 남기는 `[JWT] 유효하지 않은 토큰`이라는 오류 로그가 단 한 줄도 찍히지 않았습니다. 이는 `validateToken()` 메서드의 catch 블록이 실행되지
않았음을 의미합니다.

여기서 모순점이 있음을 인지했습니다.

토큰 검증은 실패했는데 로그는 토큰 검증이 실패하지 않았다고 말하는 논리적 교착 상태에 빠졌습니다. 디버깅을 시도해도 `JwtAuthenticationFilter`에서 Authorization 헤더가 `null`로
잡히는 등 원인 파악이 어려웠습니다.

# 해결 방법

코드의 문제가 아닌 PC 환경 문제(보안 프로그램, 프록시, 환경 변수)를 의심하고 여러 시도를 했으나 해결되지 않았습니다.

이 과정에서 토큰 검증 로직 자체의 문제가 아니라, `DailyOutfitService` 내에 혼재된 AI 관련 코드(ChatClient)와 다른 빈들의 복잡한 의존성 문제가 Spring Security 필터 체인의
정상적인 동작을 방해하고 있다고 가정했습니다.

따라서 `DailyOutfitService` 내에 있던 AI API 호출 관련 로직을 별도의 `@Component`인 `DailyAIClient`로 완전히 분리했습니다. 이로써 `DailyOutfitService`는
이제 `DailyAiClient`에만 의존하게 되어 `ChatClient`와 직접적인 의존 관계가 사라지고 내부 구조가 단순화되었습니다.

```java

@Slf4j
@Component
@RequiredArgsConstructor
public class DailyAiClient {

    @Qualifier("chatClientOpenAi")
    private final ChatClient chatClient;

    @Cacheable(value = "dailyAi", key = "{#systemPrompt, #userPrompt}")
    @Retry(name = "dailyAiApi", fallbackMethod = "fallbackCallForJson")
    @CircuitBreaker(name = "dailyAiApi", fallbackMethod = "fallbackCallForJson")
    public String callForJson(String systemPrompt, String userPrompt) {
        log.info("Calling Daily AI");
        String jsonResponse = chatClient.prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .call()
                .content();

        // AI 응답에서 JSON 부분만 추출
        int startIndex = jsonResponse.indexOf('{');
        int endIndex = jsonResponse.lastIndexOf('}');
        if (startIndex != -1 && endIndex != -1 && startIndex < endIndex) {
            return jsonResponse.substring(startIndex, endIndex + 1);
        }
        return jsonResponse;
    }

    public String fallbackCallForJson(String systemPrompt, String userPrompt, Throwable e) {
        log.error("[CircuitBreaker] AI 호출 차단. cause={}", e.toString());
        throw new GlobalException(DailyOutfitErrorCode.SERVICE_UNAVAILABLE);
    }
}
```

이 리팩토링 이후, JwtAuthenticationFilter가 k6 요청의 Authorization 헤더를 정상적으로 수신하기 시작했으며 401 Unauthorized 오류가 완전히 해결되었습니다.

</details>

<details>
<summary><strong>[개발 서버 Redis 연결 오류]</strong></summary>
<br>

## 문제 상황

```less
org.springframework.data.redis.RedisConnectionFailureException: Unable to connect to Redis
Caused by: io.lettuce.core.RedisConnectionException: Unable to connect to localhost

/
<
unresolved > :

6379
```

- AWS EC2에 Spring Boot 애플리케이션을 배포한 후
  국내 날씨 조회 API 테스트 중 오류 발생
- 레디스 서버에 연결을 시도했지만 거부

## 원인 파악

- `application-dev.yml` :  Redis 호스트와 비밀번호를 AWS Parameter Store에서 가져오도록 `${REDIS_HOST}`, `${REDIS_PASSWORD}` 변수를 사용
    - 처음에는 이게 문제였나 싶었지만 계속 [localhost](http://localhost) 로 나타나는 것을 봄
- `RedisConfig` 코드 확인
  - 

        ```less
        public RedisConnectionFactory redisConnectionFactory() {
        
            return new LettuceConnectionFactory("localhost", 6379)
        
        ```

    - `redisConnectionFactory` 빈 설정이 문제인것을 확인
        - 레디스 연결 정보가 하드 코딩 되어 있었음.
        - 하드코딩 된 값들 때문에 파라미터 스토어에서 가져온 값이 실제 연결에
          적용되지 않음.

## 해결 과정

- Redis 연결 정보를 하드코딩에서  `@Value` 어노테이션을 사용하여 환경 설정 값을
  주입받도록 수정

```less
// application.yml 또는 Parameter Store 등에서 설정값 주입 받기
@Value(

"${spring.data.redis.host}"
)
private String redisHost

;

@Value(

"${spring.data.redis.port}"
)
private int redisPort

;

@Value(

"${spring.data.redis.password:#{null}}"
)
private String redisPassword

;

@Bean
public RedisConnectionFactory redisConnectionFactory() {
  RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration(redisHost, redisPort);

  if (StringUtils.hasText(redisPassword)) {
  redisConfig.setPassword

(redisPassword);
}

  return new LettuceConnectionFactory(redisConfig);
}
```

</details>

<details>
<summary><strong>[Redis / WebClient JSON → DTO 변환 문제]</strong></summary>
<br>

```jsx
java.lang.ClassCastException
:

class java

.
util.LinkedHashMap
cannot
be
cast
to

class org

.
example.staystylish.domain.localweather.dto.WeatherResponse

```

코드에서 `LinkedHashMap` 객체를 `WeatherResponse`로 직접 변환하려고 해서 발생한 오류

. Spring에서 `RestTemplate`이나 `WebClient`로 외부 API를 호출할 때 JSON을 `Map`으로 반환하면 생긴다고 함.

```jsx
org.example.staystylish.domain.localweather.service.WeatherServiceImpl.getWeatherByLatLon(WeatherServiceImpl.java
:
100
)

```

100번째 줄에서 `LinkedHashMap`을 `WeatherResponse`로 캐스팅하고 있음.

![image.png](attachment:c7c48349-7a16-4600-9303-7e6877ab9d09:image.png)

## ✅ 문제 상황

```java
java.lang.ClassCastException:

class java.
util.LinkedHashMap cannot
be cast
to

class org.example.staystylish.domain.localweather.dto.WeatherResponse

```

- 코드 위치:

```java
WeatherResponse cached = (WeatherResponse) redisTemplate.opsForValue().get(cacheKey);

```

- 원인:
    - RedisTemplate로 저장한 객체를 꺼낼 때, **직렬화/역직렬화 방식 문제**로 `LinkedHashMap`으로 반환됨
    - 직렬화 방식이 `Jackson2JsonRedisSerializer` 등 DTO 매핑을 지원하지 않으면 발생

---

## 🔍 원인 분석

1. `RestTemplate` / `WebClient` 사용 시

- JSON을 Map으로 받으면 `LinkedHashMap`으로 반환

2. Redis에 저장

- RedisTemplate 기본 직렬화: `JdkSerializationRedisSerializer` → Map으로 꺼낼 수 있음

3. 캐스팅

- `LinkedHashMap` → DTO 직접 캐스팅 → `ClassCastException`

---

## ✅ 해결 방법

### 1️⃣ WebClient / RestTemplate에서 **직접 DTO로 변환**

```java
WeatherResponse response = restTemplate.getForObject(url, WeatherResponse.class);

```

- 외부 API 호출 시 JSON을 바로 `WeatherResponse` 객체로 변환

---

### 2️⃣ Map → DTO 수동 변환

```java
LinkedHashMap map = restTemplate.getForObject(url, LinkedHashMap.class);
WeatherResponse response = new ObjectMapper().convertValue(map, WeatherResponse.class);

```

- Redis에서 `LinkedHashMap`으로 꺼낼 때도 동일하게 적용 가능

```java
LinkedHashMap map = (LinkedHashMap) redisTemplate.opsForValue().get(cacheKey);
WeatherResponse cached = new ObjectMapper().convertValue(map, WeatherResponse.class);

```

---

### 3️⃣ RedisTemplate 직렬화 설정

- `Jackson2JsonRedisSerializer` 또는 `GenericJackson2JsonRedisSerializer` 사용

```java

@Bean
public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
    RedisTemplate<String, Object> template = new RedisTemplate<>();
    template.setConnectionFactory(factory);
    template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
    return template;
}

```

- 이렇게 하면 꺼낼 때도 DTO로 직접 변환 가능

---

### ✅ 핵심 포인트

| 문제                                                  | 원인                       | 해결                                                                                     |
|-----------------------------------------------------|--------------------------|----------------------------------------------------------------------------------------|
| `LinkedHashMap` → DTO 직접 캐스팅 시 `ClassCastException` | Redis / WebClient 직렬화 방식 | 1. WebClient/RestTemplate에서 바로 DTO로 변환2. Map → DTO 수동 변환3. RedisTemplate Serializer 변경 |

> 요약: Map으로 반환되는 JSON 객체는 직접 캐스팅 금지 → ObjectMapper 변환 또는 Serializer 설정 필수

</details>

<details>
<summary><strong>[JPQL에서 수학 함수 사용과 Hibernate/PostgreSQL 호환 문제]</strong></summary>
<br>

[JPQL에서 수학 함수 사용과 Hibernate/PostgreSQL 호환 문제]

# JPQL에서 `function('pow', ...)` 문제

## ✅ 문제 상황

```java

@Query(value = """
        SELECT r
        FROM Region r
        ORDER BY function('pow', r.latitude - :lat, 2) + function('pow', r.longitude - :lon, 2) ASC
        """)
List<Region> findNearestRegions(...);

```

- JPQL에서 `function('pow', ...)`를 사용했지만 Hibernate가 제대로 인식하지 못함
- Hibernate 6+에서는 사용자 정의 SQL 함수를 호출할 수 있는 `function()` 지원
- 그러나 일부 DB(PostgreSQL 등)에서는 JPQL에서 바로 `pow` 매핑 불가
- 결과: 쿼리 실행 시 오류 발생 또는 원하는 정렬 불가

---

## 🔍 원인

1. JPQL은 **엔티티 필드 기준**으로 SQL을 추상화
2. 수학 함수(`pow`) 등 일부 DB 함수는 JPQL에서 직접 지원되지 않음
3. Hibernate에서 function() 호출 가능하더라도 DB마다 매핑 차이 존재

---

## ✅ 해결 방법: Native Query 사용

- PostgreSQL 기준으로 직접 SQL 작성

```java
public interface RegionRepository extends JpaRepository<Region, Long> {

    @Query(value = """
            SELECT *
            FROM region
            ORDER BY ((latitude - :lat)*(latitude - :lat) + (longitude - :lon)*(longitude - :lon)) ASC
            """, nativeQuery = true)
    List<Region> findNearestRegions(@Param("lat") Double lat,
                                    @Param("lon") Double lon,
                                    Pageable pageable);
}

```

- `nativeQuery = true` → DB에서 바로 SQL 실행
- `Pageable` → 조회할 개수 제한 가능 (예: 1건만 가져오기)

---

## ✅ 핵심 포인트

| 문제                                    | 원인                           | 해결                                                             |
|---------------------------------------|------------------------------|----------------------------------------------------------------|
| JPQL에서 `function('pow', ...)` 사용 시 오류 | Hibernate/DB에서 JPQL 함수 매핑 불가 | Native Query로 직접 SQL 작성 (PostgreSQL: `((lat - :lat)^2 + ...)`) |

> 요약: JPQL에서는 일부 DB 함수 지원 제한 → 수학 계산이나 특수 함수는 Native Query 사용이 가장 안정적.

</details>

<details>
<summary><strong>[Spring AI 모델 충돌 문제]</strong></summary>
<br>

## 개요

- `Spring Ai` 환경에서 `Gemini` 모델을 기본으로 사용하고 `OpenAi` 를 TravelOutfit 도메인에서 사용하려고 할 때  `ChatModel` 문제가 발생
- `openAiChatModel`과 `vertexAiGeminiChat` 두 개의 `ChatModel` 빈이 감지되어 Spring AI 자동 설정이 기본 빈을 선택하지 못하고 충돌.

## 원인 분석

- `spring-ai-openai-starter`와 `spring-ai-vertex-ai-gemini-starter` 의존성이 모두 활성화되어 `ChatModel` 타입의 빈이 2개(
  `openAiChatModel`, `vertexAiGeminiChat`) 생성

⇒ Spring의 의존성 주입 컨테이너가 어느 빈을 사용해야될지 몰라서 예외가 발생하여
어플리케이션 오류 발생

```json
No qualifying bean of type 'org.springframework.ai.chat.model.ChatModel' available:
expected single matching bean but found 2: openAiChatModel, vertexAiGeminiChat
```

## 해결 목표

기본 모델을 설정해두고 특정 도메인에 사용하는 모델을 용도에 맞게 분리

- **기본 모델 (Gemini):** `ProductClassificationService`, `OutfitService` 등의 따로 명시적으로 주입 하지 않은 서비스에서는 `@Primary`로 지정된
  Gemini를 기본으로 사용
- **특정 도메인 모델 (OpenAI):** `traveloutfit` 도메인에서 특정 **OpenAI** 모델을 사용
  (추후 다른 도메인에서 명시적으로 주입만 해주면 사용 가능)

## 해결 방안

- `AiClientsConfig` 파일을 생성하여 `chatModel` 을 만들고 primary 지정
    1. `ChatModel` 기본 빈 `@Primary` 로 지정

    - 충돌 문제를 해결하기 위해 `ChatModel` 빈 중 하나를 기본값으로 지정
    - `vertextAiGeminiChat` 을 `@Primary` 로 선언하여 스프링 컨테이너가
      `Gemini` 를 기본 모델로 사용하도록 설정

      ```java
      @Bean
          @Primary
          public ChatModel primaryChatModel(@Qualifier("vertexAiGeminiChat") ChatModel gemini) {
              return gemini;
          }
      ```

    2. `ChatClient` 빈 등록

    - 서비스에서는 `ChatModel` 대신 `ChatClient`를 사용하므로, 각 모델에 대한 `ChatClient` 빈을 각각 등록

      ```java
          // 기본 ChatClient (Gemini)
          @Bean(name = "chatClientGemini")
          @Primary
          public ChatClient chatClientGemini(@Qualifier("vertexAiGeminiChat") ChatModel geminiModel) {
              return ChatClient.builder(geminiModel).build();
          }
      
          // traveloutfit 도메인 chatClient (OpenAI)
          @Bean(name = "chatClientOpenAi")
          public ChatClient chatClientOpenAi(@Qualifier("openAiChatModel") ChatModel openAiModel) {
              return ChatClient.builder(openAiModel).build();
          }
      
      ```

    3. `traveloutfit` 도메인에서 `@Qualifier` 로 명시적 주입

    - OpenAI를 사용하는 `TravelAiClient` 에서 `@Qualifier` 를 사용하여 빈을 명시적으로 주입 받도록 수정

      ```java
          private final ChatClient chatClient;
          private final ObjectMapper objectMapper;
      
          // 생성자 주입
          public TravelAiClient(
                  @Qualifier("chatClientOpenAi") ChatClient chatClient,
                  ObjectMapper objectMapper
          ) {
              this.chatClient = chatClient;
              this.objectMapper = objectMapper;
          }
      ㅇㅇ
      
      ```

## 최종 결과

- `ProductClassificationService` 및 `OutfitService`
    - `@Primary`로 지정된 `Gemini` `ChatModel`을 자동으로 사용하여 `ChatClient`를 생성
- `TravelAiClient`
    - 생성자에서 `@Qualifier("chatClientOpenAi")`를 통해 `OpenAI` ****`ChatClient` 빈을 명시적으로 주입

</details>

<details>
<summary><strong>[XML 파싱 및 단일/다수 item 처리 이슈]</strong></summary>
<br>

## ✅ XML 파싱 및 단일/다수 `item` 처리 이슈

### 📌 문제 상황

- 기상청 OBS API XML 응답을 `XmlMapper`로 `JsonNode`로 변환
- 기존 코드:

```java
Object itemsObj = body.get("items");
if(itemsObj instanceof Map){
Map<String, Object> itemsMap = (Map<String, Object>) itemsObj;
Object itemObj = itemsMap.get("item");
    ...
            }

```

- 문제:
    - XML 응답에서 `<item>`이 여러 개 있으면 `itemsObj`가 이미 List로 변환됨
    - Map으로 캐스팅하려 하면서 `ClassCastException` 또는 null 발생

---

### ✅ 개선 방향

1. **`items`를 Object로 받지 말고, `item`을 바로 List로 처리**
2. **Jackson 옵션으로 단일/다수 항목 모두 List로 처리**

```java
xmlMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);

```

1. 반복문 처리:

```java
if(itemNodes.isArray()){
        for(
JsonNode itemNode :itemNodes){
        items.

add(mapToWeatherItemNode(itemNode));
        }
        }else if(itemNodes.

isObject()){
        items.

add(mapToWeatherItemNode(itemNodes));
        }else{
        System.out.

println("⚠️ No 'item' nodes found in XML body: "+itemsContainer);
}

```

> ✅ 핵심: 단일 item도 배열로 강제 처리 → null/예외 방지
>

---

### 📌 파싱 성공 후 직렬화 / Redis / API 반환 문제

1. **Redis 캐시 저장**

```java
Set<String> keys = redisTemplate.keys("weather:*");
if(keys !=null&&!keys.

isEmpty()){
        redisTemplate.

delete(keys);
    System.out.

println("🧹 Redis 날씨 캐시 초기화 완료 ("+keys.size() +"건)");
        }else{
        System.out.

println("⚠️ 삭제할 날씨 캐시가 없습니다.");
}

```

- 캐시 초기화 후 저장 과정에서 객체 직렬화 문제 가능
- `WeatherItem` 또는 `WeatherResponse`가 **직렬화 가능하지 않으면 Redis 저장 실패**
- `record` 또는 DTO 객체가 `Serializable`을 구현해야 할 수 있음

1. **Spring MVC / WebClient 반환 문제**

- Controller에서 `Mono<ResponseEntity<WeatherResponse>>`로 반환할 때
- JSON 변환 시 Jackson이 **Map/List 구조**를 직렬화 실패하거나, `null` 처리 가능
- 파싱된 `JsonNode`를 바로 DTO로 변환하지 않고 Map/record로 넘길 경우 문제 발생

---

### ✅ 안전한 처리 전략

1. **XML → DTO 직접 매핑**

```java
List<WeatherItem> items = xmlMapper.readValue(
        xml, new TypeReference<List<WeatherItem>>() {
        }
);

```

- Jackson `ACCEPT_SINGLE_VALUE_AS_ARRAY` 옵션 사용

1. **Redis 저장 가능 객체 확인**

- DTO/record가 `Serializable` 구현 여부
- WebClient / Controller 반환 시 DTO로 변환 후 반환

1. **디버깅용 로그**

```java
System.out.println(xmlMapper.readTree(xml).

toPrettyString());
        System.out.

println("items size = "+items.size());

```

1. **예외 처리**

- item 없거나 형식이 잘못된 경우 → 빈 리스트 반환
- Redis / Controller 단계에서 NPE 방지

---

### ✅ 요약

| 문제                                    | 원인                        | 해결                                         |
|---------------------------------------|---------------------------|--------------------------------------------|
| XML `<item>`이 단일/다수 → Map/List 캐스팅 문제 | `itemsObj`를 Map으로 강제 캐스팅  | `ACCEPT_SINGLE_VALUE_AS_ARRAY` 옵션 + 반복문 처리 |
| Redis 캐시 저장 실패 가능                     | DTO/record 직렬화 불가         | Serializable 구현 / DTO 변환 후 저장              |
| Controller 반환 시 null 또는 JSON 직렬화 문제   | JsonNode → Response 변환 미흡 | DTO로 변환 후 `ResponseEntity` 반환              |

> 핵심: 단일/다수 item 모두 안전하게 배열로 처리 → DTO 변환 → Redis/Controller 반환까지 일관성 유지

</details>



-----

## 📈 성능 개선

<details>
<summary><strong>📈 해외 여행 옷차림 추천: API 동기 처리 → 비동기 처리로 아키텍처 변경</strong></summary>
<br>

### 1. 문제점: 동기 API의 병목 현상 (1차 부하 테스트)

초기 `해외 여행 옷차림 추천 API` (`POST /api/v1/travel-outfits/recommendations`)는 사용자의 요청을 받으면, 해당 스레드에서 즉시 **GlobalWeatherAPI**와 *
*OpenAI (GPT) API**를 순차적으로 호출하는 **동기(Synchronous)** 방식으로 동작했습니다.

K6를 사용한 1차 부하 테스트(VUs: 10) 결과, 이 구조는 심각한 병목 현상을 유발했습니다.

* **테스트 결과 (1차):**
    * **VUs (동시 사용자):** 10
    * **P95 응답 시간:** **19.99초** (설정한 타임아웃 20초에 도달)
    * **요청 실패율:** **12.50%**
    * **RPS (초당 요청 수):** 약 0.52/s

![1차 K6 테스트 결과]![img.png](src/main/resources/img.png)

10명의 동시 사용자만으로도 서버 스레드가 모두 외부 API 응답을 기다리며 **대기(Blocking) 상태**에 빠졌습니다. 이로 인해 P95 응답 시간이 20초에 육박하고, 요청의 12.5%가 타임아웃으로
실패했습니다. 이 구조로는 실제 트래픽을 감당할 수 없다고 판단했습니다.

<br>

### 2. 해결: 비동기 요청/폴링 패턴 도입

이 문제를 해결하기 위해, API의 구조를 **비동기(Asynchronous) 요청/폴링** 패턴으로 전면 수정했습니다.

1. **`POST /.../recommendations` (요청 접수):**

* `TravelOutfitController`는 이제 AI 호출을 직접 수행하지 않습니다.
* 대신 `TravelOutfit` 엔티티를 `RecommendationStatus.PENDING` 상태로 DB에 생성하고, `travelId`를 즉시 클라이언트에 반환합니다 (
  `TravelOutfitServiceImpl.requestRecommendation`).
* 이 작업은 DB Insert만 수행하므로 매우 빠릅니다. (2차 테스트 결과: P95 **11.59ms**)

2. **`@Async` (백그라운드 처리):**

* 실제 외부 API(WeatherAPI, OpenAI) 호출 및 AI 응답 파싱 로직은 `TravelOutfitServiceImpl.processRecommendation` 메서드로 분리했습니다.
* 이 메서드에 `@Async("travelRecommendationExecutor")` 어노테이션을 적용하여, `AsyncConfig`에 정의된 별도의 스레드 풀에서 백그라운드 작업으로 실행되도록 했습니다.
* 이제 외부 API 호출이 30초가 걸리더라도, 메인 HTTP 스레드는 즉시 반환되어 다른 사용자 요청을 받을 수 있습니다.

3. **Client Polling (결과 확인):**

* 클라이언트(K6 스크립트)는 `travelId`를 받은 직후, `GET /.../{travelId}` API를 1초마다 **폴링(Polling)**하여 `status` 필드를 확인합니다.
* 백그라운드 작업이 완료되어 `status`가 `COMPLETED` (또는 `FAILED`)로 변경되면, 클라이언트는 최종 추천 데이터를 가져오고 E2E(End-to-End) 시간 측정을 종료합니다.

<br>

### 3. 개선 결과 (2차 부하 테스트)

아키텍처 변경 후, VUs를 2배(20명)로 늘리고 비동기 폴링 스크립트(`k6-travel-recommendations.js`)로 2차 테스트를 수행했습니다.

* **테스트 결과 (2차):**
    * **VUs (동시 사용자):** 20
    * **요청 실패율:** **0.00%** (12.50% → 0%)
    * **API 응답 속도 (P95):** **11.59ms** (요청 접수 속도)
    * **E2E 완료 시간 (P95):** **49.73초** (신규 `recommendation_duration` 메트릭)

| 항목                  | 1차 테스트 (동기)      | 2차 테스트 (비동기) | 개선점         |
|:--------------------|:-----------------|:-------------|:------------|
| **VUs**             | 10               | **20**       | 2배 증가       |
| **요청 실패율**          | 12.50%           | **0.00%**    | **안정성 확보**  |
| **API 응답 시간 (P95)** | 19.99초 (Timeout) | **11.59ms**  | **즉각적인 응답** |
| **E2E 완료 (P95)**    | (측정 불가)          | 49.73초       | 병목 지점 명확화   |

<br>

### 4. 결론

* **안정성 및 확장성 확보:** 비동기 아키텍처로 변경한 후, 동시 사용자가 2배로 늘어났음에도 불구하고 **API 요청 실패율이 0%**로 수렴했습니다. 서버 스레드가 더 이상 외부 API에 의해 차단되지 않아
  안정적인 요청 처리가 가능해졌습니다.
* **사용자 경험(UX) 개선:** 사용자는 "요청 접수"에 대해 **11.59ms**라는 즉각적인 응답을 받게 되어, 20초간 로딩 화면에서 대기할 필요가 없어졌습니다. (프론트엔드는 이후 폴링을 통해 로딩
  UI를 표시)
* **명확한 병목 식별:** Grafana 대시보드 분석 결과, DB 커넥션 풀과 JVM 메모리는 매우 안정적이었습니다. 2차 테스트의 `recommendation_duration` (P95 약 50초)을 통해,
  **서버의 병목이 아닌 외부 OpenAI API의 응답 속도**가 전체 E2E 시간의 병목임을 명확히 식별할 수 있었습니다.

![Grafana 대시보드 - 2차 테스트]![대시보드1.JPG.jpg](src/main/resources/%EB%8C%80%EC%8B%9C%EB%B3%B4%EB%93%9C1.JPG.jpg)![대시보드3.JPG.jpg](src/main/resources/%EB%8C%80%EC%8B%9C%EB%B3%B4%EB%93%9C3.JPG.jpg)
![대시보드5.JPG.jpg](src/main/resources/%EB%8C%80%EC%8B%9C%EB%B3%B4%EB%93%9C5.JPG.jpg)*(2차 테스트 당시 Grafana 대시보드. HTTP 응답(녹색)은
빠르고, E2E 시간(노란색)은 길며, DB(파란색)는 부하가 없는 것을 확인)*

</details>

<details>
<summary><strong>📈 커뮤니티 기능: Redis 캐싱 및 쿼리 최적화를 통한 성능 고도화</strong></summary>
<br>

### 1. 문제점: 느린 응답 시간과 높은 에러율 (최적화 전)

초기 커뮤니티 기능 API는 매 요청마다 데이터베이스에 직접 접근하는 방식으로 구현되었습니다. K6를 사용한 부하 테스트(VUs: 100, 30초) 결과, 심각한 성능 문제가 발견되었습니다.

* **테스트 결과 (최적화 전):**
    * **VUs (동시 사용자):** 100명
    * **총 요청 수:** 240
    * **요청 실패율:** **0%**
    * **HTTP 요청 시간 (평균):** **63.84ms**
    * **HTTP 요청 시간 (P95):** **157.2초**
    * **초당 요청 수 (RPS):** 약 5-8 req/s

<img width="347" height="332" alt="화면 캡처 2025-11-14 191646" src="https://github.com/user-attachments/assets/3b28a888-6a90-4f22-98d4-987dd617b855" />

100명의 동시 사용자 부하에서 모든 요청이 성공했으며, 응답 시간이 최대 20초를 초과했습니다. 특히 N+1 쿼리 문제로 인해 게시글 목록 조회 시 각 게시글마다 좋아요 수를 별도로 조회하여 데이터베이스에 과도한
부하가 발생했습니다.

<br>

### 2. 해결: Redis 캐싱 레이어 및 쿼리 최적화 적용

성능 문제를 해결하기 위해 다층 최적화 전략을 적용했습니다.

1. **Redis 캐싱 레이어 도입:**

* 좋아요 수, 게시글 정보 등 자주 조회되는 데이터를 Redis에 캐싱
* Cache-aside 패턴을 적용하여 캐시 미스 시에만 데이터베이스 조회
* Redis Sorted Set을 활용한 좋아요 기반 정렬 최적화
* 데이터베이스 부하 약 **70% 감소**

```java
// Redis를 활용한 좋아요 수 조회
@Cacheable(value = "postLikes", key = "#postId")
public Long getLikeCount(Long postId) {
    return likeRepository.countByPostId(postId);
}
```

2. **N+1 쿼리 문제 해결:**

```java
// 최적화 전: N+1 쿼리 문제
posts.forEach(post ->{
        post.

getLikes(); // 각 게시글마다 별도 쿼리 발생
});

// 최적화 후: EntityGraph를 통한 배치 페칭
@EntityGraph(attributePaths = {"likes", "user"})
List<Post> findAllWithLikesAndUser();
```

3. **커넥션 풀 튜닝:**

```yaml
# HikariCP 설정 최적화
spring:
  datasource:
    hikari:
      maximum-pool-size: 20  # 10에서 증가
      minimum-idle: 10       # 5에서 증가
      connection-timeout: 30000
```

4. **API 응답 최적화:**

* DTO를 활용한 선택적 필드 프로젝션
* 대용량 데이터셋에 대한 페이지네이션 적용
* JSON 응답 압축 활성화

<br>

### 3. 개선 결과 (최적화 후)

동일한 부하 조건(VUs: 100, 30초)에서 2차 테스트를 수행했습니다.

* **테스트 결과 (최적화 후):**
    * **VUs (동시 사용자):** 100명
    * **총 요청 수:** 260
    * **요청 실패율:** **0.00%**
    * **HTTP 요청 시간 (평균):** **40.1ms**
    * **HTTP 요청 시간 (P95):** **134.41ms**
    * **반복 수행 시간 (평균):** **6.41ms** (1.16초 → 6.41ms, **99.4% 개선**)
    * **초당 요청 수 (RPS):** 약 27-30 req/s (**350% 증가**)

| 항목                   | 최적화 전     | 최적화 후           | 개선율         |
|:---------------------|:----------|:----------------|:------------|
| **VUs**              | 100명      | 100명            | 동일          |
| **요청 실패율**           | 0%        | **0.00%**       | ** 안정성 확보** |
| **HTTP 요청 시간 (평균)**  | 63.84ms   | **40.1ms**      | **-66.4%**  |
| **HTTP 요청 시간 (P95)** | 200.81ms  | **134.41ms**    | **-38.1%**  |
| **반복 수행 시간 (평균)**    | 11.6ms    | **6.41ms**      | **-50.4%**  |
| **초당 요청 수 (RPS)**    | 5-8 req/s | **27-30 req/s** | **+350%**   |

<img width="317" height="355" alt="화면 캡처 2025-11-14 191532" src="https://github.com/user-attachments/assets/84da00a7-59e0-4e94-8e52-2eea66571711" />

<br>

### 4. Grafana 모니터링 분석

#### HTTP 요청 소요 시간 (백분위수)

| 백분위수          | 최적화 전   | 최적화 후        | 개선율        |
|:--------------|:--------|:-------------|:-----------|
| **p50 (중앙값)** | 63.84ms | **40.1ms**   | **-37.2%** |
| **p90**       | 521.3ms | **175.27ms** | **-66.4%** |
| **p95**       | 20.81초  | **12.88초**   | **-38.1%** |

#### 시스템 리소스 사용률

| 지표           | 최적화 전     | 최적화 후           | 상태           |
|:-------------|:----------|:----------------|:-------------|
| **초당 요청 수**  | 5-8 req/s | **27-30 req/s** | **+350%** 📈 |
| **CPU 사용률**  | 6.73ms    | **6.41ms**      | -4.8%        |
| **메모리 사용량**  | 148.71ms  | **127.15ms**    | -14.5%       |
| **네트워크 I/O** | 157.2ms   | **134.41ms**    | -14.4%       |

#### 주요 관찰 사항

* **에러율 제거**: 다수의 에러 스파이크 → 에러 감지 안됨 (100% 개선)
* **안정적인 처리량**: 가변적인 성공률 → 초당 30+ 체크, 100% 성공률
* **효율적인 리소스 활용**: CPU, 메모리, 네트워크 사용량 모두 감소

<img width="497" height="183" alt="화면 캡처 2025-11-14 191427" src="https://github.com/user-attachments/assets/3479a6d7-4b6a-49a7-8f97-07ec392a01ed" />

<img width="430" height="158" alt="화면 캡처 2025-11-14 191502" src="https://github.com/user-attachments/assets/1c0add61-015f-475e-9f75-2cd2b7e63151" />

*(최적화 후 Grafana 대시보드: HTTP 응답 시간(녹색)이 빠르고, 에러(빨간색)가 없으며, 리소스 사용량(파란색)이 안정적인 것을 확인)*

<br>

### 5. 결론

* **안정성 및 확장성 확보:** Redis 캐싱과 쿼리 최적화를 통해 **요청 실패율이 0%로** 개선되었고, 데이터베이스 부하가 70% 감소하여 높은 동시 사용자 수에서도 안정적인 서비스 제공이 가능해졌습니다.

* **사용자 경험(UX) 대폭 개선:** 평균 응답 시간이 **66.4% 단축**(521.3ms → 175.27ms)되어, 사용자는 커뮤니티 기능을 훨씬 빠르게 이용할 수 있게 되었습니다. 요청의 90%가
  200ms 이하의 응답 시간을 기록했습니다.

* **처리량 350% 향상:** 초당 처리 가능한 요청 수가 5-8 req/s에서 **27-30 req/s로 3.5배 증가**하여, 실제 트래픽 급증 시에도 대응할 수 있는 확장 가능한 아키텍처를 구축했습니다.

* **리소스 효율성 개선:** CPU, 메모리, 네트워크 사용량이 모두 감소하여 동일한 인프라에서 더 많은 트래픽을 처리할 수 있게 되었습니다.

</details>

<details>
<summary><strong>📈 오늘의 옷차림 추천 기능: Redis 캐싱을 통한 성능 고도화</strong></summary>
<br>

**1. 문제점: 외부 API 의존으로 인한 성능 저하 및 비용 문제 (최적화 전)**

최적화 전 AI 추천 기능은 매 요청마다 외부 OpenAI API를 직접 호출하는 방식으로 구현되었습니다. 이 방식은 외부 API의 응답 속도에 따라 전체 시스템 성능이 좌우되는 심각한 병목 지점을 가지고
있었습니다. K6를 사용한 부하 테스트(VUs: 10, 30초) 결과 심각한 성능 한계가 발견되었습니다.

- **테스트 결과 (최적화 전):**
    - **VUs (동시 사용자):**10명
    - **총 요청 수:**80
    - **요청 실패율:****0%**
    - **HTTP 요청 시간 (평균):**2.6초
    - **HTTP 요청 시간 (P95):**3.65초
    - **초당 요청 수 (RPS):**약 2.66 req/s

  ![대시보드3-1.png](src/main/resources/image/%EB%8C%80%EC%8B%9C%EB%B3%B4%EB%93%9C3-1.png)

10명의 동시 사용자 부하에서 평균 응답 시간이 2.6초를 초과하여 사용자 경험을 심각하게 저해했습니다. 또한,모든 요청이 외부 API를 호출하여 불필요한 비용을 발생시키는 비효율적인 구조였습니다. AI API
호출이 주된 지연 원인으로 명확히 확인되었습니다.

**2. 해결: Redis 캐싱 및 회복탄력성 패턴 적용**

성능 문제를 해결하기 위해 다층 최적화 전략을 적용했습니다.

1. **Redis 캐싱 레이어 도입:**

- 동일한 AI 프롬프트(날씨, 사용자 정보 등)에 대한 응답을 Redis에 캐싱하여, 반복적인 API 호출을 원천적으로 제거했습니다.
- Spring의 `@Cacheable`을 활용한 Cache-aside 패턴을 적용하여, 캐시 미스 시에만 AI API를 호출하도록 구현했습니다.
- 이를 통해 **AI API 호출을 90% 이상 감소**시키고, 응답 시간을 획기적으로 단축했습니다.

```java
// Redis를 활용한 AI 응답 캐싱
@Cacheable(value = "dailyAi", key = "#{systemPrompt, #userPrompt}")
public String callForJson(String systemPrompt, String userPrompt) {
    //캐시 미스 시에만 이 로직이 실행됨
    return chatClient.prompt()...call().content();
}
```

1. 회복탄력성(Resilience) 패턴 적용

- `@Retry`: 외부 API의 일시적인 오류에 대비해, 실패 시 자동으로 3회 재시도하도록 설정하여 안정성을 높였습니다.
- `@CircuitBreaker`: 반복적인 실패가 감지되면, 서킷을 열어 즉시 차단하고 Fallback 메서드를 실행하여 시스템 전체 장애를 방지했습니다.

```java
// Retry와 CircuitBreaker를 함께 적용
@Cacheable(value = "dailyAi", key = "{#systemPrompt, #userPrompt}")
@Retry(name = "dailyAiApi", fallbackMethod = "fallbackCallForJson")
@CircuitBreaker(name = "dailyAiApi", fallbackMethod = "fallbackCallForJson")
public String callForJson(String systemPrompt, String userPrompt) { ...}
```

**3. 개선 결과 (최적화 후)**

동일한 부하 조건(VUs: 10, 30초)에서 2차 테스트를 수행한 결과, 모든 지표가 극적으로 개선되었습니다.

- **테스트 결과 (최적화 후):**
    - **VUs (동시 사용자):10명**
    - **총 요청 수:283**
    - **요청 실패율:0.00%**
    - **HTTP 요청 시간 (평균):30.68ms**
    - **HTTP 요청 시간 (P95):45.44ms**
    - **초당 요청 수 (RPS):약 9.43 req/s**

![대시보드3-2.png](src/main/resources/image/%EB%8C%80%EC%8B%9C%EB%B3%B4%EB%93%9C3-2.png)

| **항목**               | **최적화 전**  | **최적화 후**      | **개선율**    |
|----------------------|------------|----------------|------------|
| **VUs**              | 10명        | 10명            | 동일         |
| **요청 실패율**           | 0%         | **0.00%**      | 안정성 확보     |
| **HTTP 요청 시간 (평균)**  | 2,600ms    | **30.68ms**    | **-98.8%** |
| **HTTP 요청 시간 (P95)** | 3,650ms    | **45.44ms**    | **-98.8%** |
| **초당 요청 수 (RPS)**    | 2.66 req/s | **9.43 req/s** | **+254%**  |

**4. 결론**

- **사용자 경험(UX) 혁신**: 평균 응답 시간이 98.8% 단축(2.6초 → 30ms)되어 사용자는 느리다고 느낄 틈 없이 즉각적인 AI 추천을 받을 수 있게 되었습니다.
- **처리량 2.5배 향상**: 초당 처리 가능한 요청 수가 2.66 req/s에서 9.43 req/s로 약 2.5배 증가하여, 더 많은 동시 사용자를 안정적으로 처리할 수 있는 확장성을 확보했습니다.
- **비용 효율성 극대화**: 불필요한 외부 API 호출을 90% 이상 제거하여, 서비스 운영 비용을 직접적으로 절감하는 비즈니스적 효과를 거두었습니다.
- **안정성 확보**: `@Retry`와 `@CircuitBreaker` 패턴을 통해 외부 API의 일시적인 장애 상황에서도 시스템이 안정적으로 동작할 수 있는 기반을 마련했습니다.

</details>

-----

## 🧑‍💻 팀원 소개

| 이름            | GitHub                                          | 역할                                               |
|:--------------|:------------------------------------------------|:-------------------------------------------------|
| **유호영 (리더)**  | [HY-WG](https://github.com/HY-WG)               | [기상청 API 를 이용한 사용자 GPS 별 기온 출력 도메인 구축)]          
| **이지호 (부리더)** | [jihoLee0818](https://github.com/jihoLee0818)   | [인증/인가, 유저 및 커뮤니티 도메인 구축]                        | |
| **이영래**       | [youngrae0317](https://github.com/youngrae0317) | [OPEN AI API 를 이용한 해외 옷차림 추천 도메인 구축, 배포 인프라 구축)] |
| **최수영**       | [csy10001](https://github.com/csy10001)         | [인증/인가, 유저 및 커뮤니티 도메인 구축]                        |

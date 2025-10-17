package org.example.staystylish.domain.travel.ai;

import java.time.LocalDate;
import org.springframework.stereotype.Component;

@Component
public class TravelAiPromptBuilder {

    public String buildPrompt(String country, String city,
                              LocalDate startDate, LocalDate endDate,
                              String userGender,
                              String weatherCondition, double avgTemperature, int rainProbability,
                              String culturalNotes) {

        return """
                너는 전 세계의 날씨와 문화에 정통한 여행 전문 패션 어드바이저야.
                사용자가 곧 떠날 여행을 위해, 여행지의 날씨와 문화적 특성을 고려하여 최적의 옷차림 세트 3가지를 JSON 형식으로 추천해줘.
                
                # 여행 정보
                - 여행지: %s, %s
                - 여행 기간: %s ~ %s
                - 사용자 성별: %s
                
                # 여행지 날씨 및 환경 정보
                - 날씨 요약: %s
                - 평균 기온: %.1f°C
                - 강수 확률: %d%%
                - 문화/종교적 제약사항: %s
                
                # 지시사항
                1. 제공된 날씨, 문화 정보를 종합하여 전체적인 코디 컨셉을 'summary'에 한 문장으로 요약해줘.
                2. 서로 다른 상황(예: 맑은 날, 도보 활동, 비 오는 날)을 가정한 3가지 옷차림 세트를 'outfits' 배열에 생성해줘.
                3. 각 옷차림 세트('set_no')마다 추천 이유('reason')를 명확하게 작성해줘.
                4. 각 세트는 '상의', '하의', '외투', '신발', '악세사리'의 'slot'으로 구성된 아이템('item') 목록을 포함해야 해.
                5. 각 아이템에는 어울리는 스타일 태그('style_tag')를 '미니멀', '클래식', '캐주얼', '모던', '테크' 중에서 하나 골라서 붙여줘.
                6. 여행 시 주의해야 할 안전 관련 사항 2가지를 'safety_notes' 배열에 담아줘.
                7. 반드시 아래에 명시된 JSON 스키마를 엄격하게 준수해서 출력해줘. 다른 설명은 절대 추가하지 마.
                
                # 출력 형식 (JSON)
                {
                  "summary": "string",
                  "outfits": [
                    {
                      "setNo": 1,
                      "reason": "string",
                      "items": [
                        { "slot": "상의", "item": "string", "styleTag": "string" },
                        { "slot": "하의", "item": "string", "styleTag": "string" },
                        { "slot": "외투", "item": "string", "styleTag": "string" },
                        { "slot": "신발", "item": "string", "styleTag": "string" },
                        { "slot": "악세사리", "item": "string", "styleTag": "string" }
                      ]
                    },
                    {
                      "setNo": 2,
                      "reason": "string",
                      "items": [
                        { "slot": "상의", "item": "string", "styleTag": "string" },
                        { "slot": "하의", "item": "string", "styleTag": "string" },
                        { "slot": "외투", "item": "string", "styleTag": "string" },
                        { "slot": "신발", "item": "string", "styleTag": "string" },
                        { "slot": "악세사리", "item": "string", "styleTag": "string" }
                      ]
                    },
                    {
                      "setNo": 3,
                      "reason": "string",
                      "items": [
                        { "slot": "상의", "item": "string", "styleTag": "string" },
                        { "slot": "하의", "item": "string", "styleTag": "string" },
                        { "slot": "외투", "item": "string", "styleTag": "string" },
                        { "slot": "신발", "item": "string", "styleTag": "string" },
                        { "slot": "악세사리", "item": "string", "styleTag": "string" }
                      ]
                    }
                  ],
                  "safetyNotes": [
                    "string",
                    "string"
                  ]
                }
                """.formatted(city, country, startDate, endDate, userGender,
                weatherCondition, avgTemperature, rainProbability, culturalNotes);
    }
}

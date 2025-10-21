package org.example.staystylish.domain.traveloutfit.ai;

import java.time.LocalDate;
import org.springframework.stereotype.Component;

@Component
public class TravelAiPromptBuilder {

    public String buildPrompt(String country, String city,
                              LocalDate startDate, LocalDate endDate,
                              String userGender,
                              String weatherCondition, double avgTemperature, String umbrellaSummaryText) {

        String basePrompt = """
                너는 전 세계의 날씨와 문화에 정통한 여행 전문 패션 어드바이저야.
                사용자가 곧 떠날 여행을 위해, 여행지의 날씨와 문화적 특성을 고려하여 최적의 옷차림과 현지 에티켓 정보를 JSON 형식으로 생성해줘.
                
                # 여행 정보
                - 여행지: %s, %s
                - 여행 기간: %s ~ %s
                - 사용자 성별: %s
                
                # 여행지 날씨 정보
                - 날씨 요약: %s
                - 평균 기온: %.1f°C
                - 일별 강수 예보: %s
                
                # 지시사항
                1. 먼저, 여행지(%s)의 문화, 종교적 특성을 분석해서 방문 시 옷차림과 관련된 주의사항을 찾아줘.
                2. 이 주의사항을 'cultural_constraints' 객체에 'notes'(한 문장 요약)와 'rules'(상세 규칙 목록)로 정리해줘.
                3. 날씨와 문화 정보를 종합하여 전체적인 코디 컨셉을 'summary'에 한 문장으로 요약해줘.
                4. 주어진 날씨('일별 강수 예보' 포함)에 맞는 '서로 다른 스타일'의 옷차림 세트 3가지를 'outfits' 배열에 생성해줘.
                5. 각 옷차림 세트('set_no')마다 추천 이유('reason')를 명확하게 작성해줘. 
                   (만약 '일별 강수 예보'에 "우산 필수"가 있다면, 세트 중 하나는 "비가 오는 날을 대비한" 이유를 포함해야 함)
                6. 각 세트는 '상의', '하의', '외투', '신발', '악세사리'의 'slot'으로 구성된 아이템('item') 목록을 포함해야 해.
                7. 각 아이템에는 어울리는 스타일 태그('style_tag')를 '미니멀', '클래식', '캐주얼', '모던', '테크' 중에서 하나 골라서 붙여줘.
                8. 여행 시 주의해야 할 안전 관련 사항 2가지를 'safety_notes' 배열에 담아줘.
                9. 반드시 아래에 명시된 JSON 스키마를 엄격하게 준수해서 출력해줘. 다른 설명은 절대 추가하지 마.
                
                # 출력 형식 (JSON)
                {
                  "summary": "string",
                  "outfits": [
                    {
                      "setNo": 1,
                      "reason": "string",
                      "items": [
                        { "slot": "상의", "item": "string", "styleTag": "string" },
                        // ... items
                      ]
                    }
                    // ... 나머지 2 개의 outfits
                  ],
                  "culturalConstraints": {
                    "notes": "string",
                    "rules": ["string", "string"]
                  },
                  "safetyNotes": [
                    "string",
                    "string"
                  ]
                }
                """;

        return String.format(basePrompt,
                country, city, startDate, endDate, userGender,
                weatherCondition, avgTemperature,
                (umbrellaSummaryText != null && !umbrellaSummaryText.isBlank()) ? umbrellaSummaryText : "정보 없음",
                country
        );
    }
}
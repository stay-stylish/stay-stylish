package org.example.staystylish.domain.travel.policy;

import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * 문화, 종교 규칙 (임시)
 */
@Component
public class TravelCultureGuide {

    // 일단 테스트로 프랑스, 이탈리아
    private static final Map<String, List<String>> COUNTRY_RULES = Map.of(
            "프랑스", List.of("성당 등 종교시설 방문 시 어깨와 무릎 가리기 권장", "실내 모자 착용 지양"),
            "이탈리아", List.of("성당 입장 시 민소매·짧은 반바지 지양", "노출도 높은 복장 지양")
    );

    public List<String> rulesFor(String country) {

        return COUNTRY_RULES.getOrDefault(country, List.of(
                "종교·공공시설 방문 전 드레스코드 확인 권장",
                "지역 관습을 존중하는 단정한 복장 추천"
        ));
    }

    public String notesFor(String country) {

        var rules = rulesFor(country);
        
        return rules.isEmpty() ? "" : rules.get(0);
    }
}

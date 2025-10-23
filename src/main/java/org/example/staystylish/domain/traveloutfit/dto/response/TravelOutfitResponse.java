package org.example.staystylish.domain.traveloutfit.dto.response;


import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.example.staystylish.domain.traveloutfit.entity.TravelOutfit;

/**
 * 여행 옷차림 추천 생성 결과/조회 응답 DTO.
 */
public record TravelOutfitResponse(
        Long travelId,
        Long userId,
        String country,
        String city,
        LocalDate startDate,
        LocalDate endDate,
        WeatherSummary weatherSummary,
        CulturalConstraints culturalConstraints,
        AiOutfit aiOutfitJson,
        List<String> safetyNotes,
        LocalDateTime createdAt
) {

    /**
     * 엔티티와 계산된 정보로부터 응답 DTO를 생성
     *
     * @param travelOutfit 저장된 추천 엔티티
     * @param summary      평균/강수/상태/우산 가이드가 포함된 날씨 요약
     * @param constraints  문화/복장 제약사항
     * @param ai           AI 추천 요약/코디 세트
     * @param notes        여행 안전 노트
     * @return TravelOutfitResponse
     */
    public static TravelOutfitResponse from(TravelOutfit travelOutfit,
                                            WeatherSummary summary,
                                            CulturalConstraints constraints,
                                            AiOutfit ai,
                                            List<String> notes) {
        return new TravelOutfitResponse(
                travelOutfit.getId(), travelOutfit.getUserId(), travelOutfit.getCountry(), travelOutfit.getCity(),
                travelOutfit.getStartDate(), travelOutfit.getEndDate(), summary, constraints, ai, notes,
                travelOutfit.getCreatedAt()
        );
    }

    /**
     * 여행 기간의 평균 날씨 요약 + 우산 가이드.
     */
    public record WeatherSummary(
            Double avgTemperature, Integer avgHumidity,
            Integer rainProbability, String condition,
            @JsonInclude(JsonInclude.Include.NON_EMPTY)
            List<RainAdvisory> rainAdvisories,
            @JsonInclude(JsonInclude.Include.NON_NULL)
            String umbrellaSummary) {

        // 평균/상태만 필요할 때 사용하는 팩토리 메서드(가이드/요약은 제외)
        public static WeatherSummary from(
                Double avgTemperature,
                Integer avgHumidity,
                Integer rainProbability,
                String condition
        ) {
            return new WeatherSummary(
                    avgTemperature,
                    avgHumidity,
                    rainProbability,
                    condition,
                    List.of(),
                    null
            );
        }
    }

    // 개별 일자 강수확률 및 우산 조언 문구
    public record RainAdvisory(
            LocalDate date,
            Integer rainProbability,
            String advice) {
    }

    // 현지 문화/복장 제약사항 요약과 세부 규칙들
    public record CulturalConstraints(
            String notes, List<String> rules) {
    }

    // AI 추천 요약, 코디 세트
    public record AiOutfit(
            String summary, List<OutfitSet> outfits) {

        public record OutfitSet(
                Integer setNo, String reason, List<Item> items) {
        }

        public record Item(
                String slot, String item, String styleTag) {
        }
    }
}

package org.example.staystylish.domain.traveloutfit.service;

import org.example.staystylish.domain.traveloutfit.dto.request.TravelOutfitRequest;
import org.example.staystylish.domain.traveloutfit.dto.response.TravelOutfitDetailResponse;
import org.example.staystylish.domain.traveloutfit.dto.response.TravelOutfitSummaryResponse;
import org.example.staystylish.domain.traveloutfit.entity.TravelOutfit;
import org.example.staystylish.domain.user.entity.Gender;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;

public interface TravelOutfitService {

    // (동기) 추천 요청을 접수하고 PENDING 상태의 엔티티를 반환
    TravelOutfit requestRecommendation(Long userId, TravelOutfitRequest request);

    // (비동기) 실제 API 호출 및 엔티티 업데이트 로직
    @Async
    void processRecommendation(Long travelId, TravelOutfitRequest request, Gender gender);

    // 내 추천 목록(요약) 페이징 조회
    Page<TravelOutfitSummaryResponse> getMyRecommendationsSummary(Long userId, Pageable pageable);

    // 추천 상세 조회
    TravelOutfitDetailResponse getRecommendationDetail(Long userId, Long travelId);

}

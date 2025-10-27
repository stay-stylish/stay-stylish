package org.example.staystylish.domain.traveloutfit.service;

import org.example.staystylish.domain.traveloutfit.dto.request.TravelOutfitRequest;
import org.example.staystylish.domain.traveloutfit.dto.response.TravelOutfitDetailResponse;
import org.example.staystylish.domain.traveloutfit.dto.response.TravelOutfitResponse;
import org.example.staystylish.domain.traveloutfit.dto.response.TravelOutfitSummaryResponse;
import org.example.staystylish.domain.user.entity.Gender;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TravelOutfitService {

    // 여행 옷차림 추천 생성
    TravelOutfitResponse createRecommendation(Long userId, TravelOutfitRequest request, Gender gender);

    // 내 추천 목록(요약) 페이징 조회
    Page<TravelOutfitSummaryResponse> getMyRecommendationsSummary(Long userId, Pageable pageable);

    // 추천 상세 조회
    TravelOutfitDetailResponse getRecommendationDetail(Long userId, Long travelId);

}

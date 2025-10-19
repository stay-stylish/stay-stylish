package org.example.staystylish.domain.travel.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.staystylish.common.dto.response.ApiResponse;
import org.example.staystylish.common.dto.response.PageResponse;
import org.example.staystylish.common.security.UserPrincipal;
import org.example.staystylish.domain.travel.consts.TravelOutfitSuccessCode;
import org.example.staystylish.domain.travel.dto.request.TravelOutfitRequest;
import org.example.staystylish.domain.travel.dto.response.TravelOutfitResponse;
import org.example.staystylish.domain.travel.dto.response.TravelOutfitSummaryResponse;
import org.example.staystylish.domain.travel.service.TravelOutfitService;
import org.example.staystylish.domain.user.entity.Gender;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/travel-outfits")
@RequiredArgsConstructor
public class TravelOutfitController {

    private final TravelOutfitService travelOutfitService;

    @PostMapping("/recommendation")
    public ApiResponse<TravelOutfitResponse> createdRecommendation(@AuthenticationPrincipal UserPrincipal principal,
                                                                   @Valid @RequestBody TravelOutfitRequest request) {
        // 로그인한 사용자 ID랑 성별 추출
        Long userId = principal.getUser().getId();
        Gender gender = principal.getUser().getGender();

        // 성별 정보가 없을 경우에 기본값으로 MALE 설정
        if (gender == null) {

            gender = Gender.MALE;
        }

        TravelOutfitResponse response = travelOutfitService.createRecommendation(userId, request, gender);

        return ApiResponse.of(TravelOutfitSuccessCode.CREATED, response);
    }

    @GetMapping("/recommendation")
    public ApiResponse<PageResponse<TravelOutfitSummaryResponse>> getMyRecommendationsSummary(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        Long userId = principal.getUser().getId();

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<TravelOutfitSummaryResponse> responsePage =
                travelOutfitService.getMyRecommendationsSummary(userId, pageable);

        return ApiResponse.of(TravelOutfitSuccessCode.GET_RECOMMENDATIONS_SUCCESS, PageResponse.fromPage(responsePage));
    }

}

package org.example.staystylish.domain.dailyoutfit.controller;

import lombok.RequiredArgsConstructor;
import org.example.staystylish.common.dto.response.ApiResponse;
import org.example.staystylish.common.security.UserPrincipal;
import org.example.staystylish.domain.dailyoutfit.code.DailyOutfitSuccessCode;
import org.example.staystylish.domain.dailyoutfit.dto.request.FeedbackRequest;
import org.example.staystylish.domain.dailyoutfit.dto.response.DailyOutfitRecommendationResponse;
import org.example.staystylish.domain.dailyoutfit.service.DailyOutfitService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 의상 추천 및 아이템 피드백과 관련된 API 요청을 처리하는 컨트롤러 클래스입니다.
 */
@RestController
@RequestMapping("/api/v1/outfits")
@RequiredArgsConstructor
public class DailyOutfitController {

    private final DailyOutfitService outfitService;

    @GetMapping("/recommendation")
    public ApiResponse<DailyOutfitRecommendationResponse> getOutfitRecommendation(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam double latitude,
            @RequestParam double longitude
    ) {

        Long userId = principal.getUser().getId();
        DailyOutfitRecommendationResponse response = outfitService.getOutfitRecommendation(userId, latitude, longitude);

        return ApiResponse.of(DailyOutfitSuccessCode.GET_OUTFIT_RECOMMENDATION_SUCCESS, response);
    }


    @PostMapping("/items/{itemId}/feedback")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Void> handleFeedback(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long itemId, @RequestBody FeedbackRequest request) {

        Long userId = principal.getUser().getId();
        outfitService.createFeedback(userId, itemId, request.status());

        ApiResponse<Void> response = ApiResponse.of(DailyOutfitSuccessCode.CREATE_FEEDBACK_SUCCESS);

        return response;
    }

    @DeleteMapping("/items/{itemId}/feedback")
    public ApiResponse<Void> deleteFeedback(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long itemId, @RequestBody FeedbackRequest request) {

        Long userId = principal.getUser().getId();
        outfitService.deleteFeedback(userId, itemId, request.status());

        ApiResponse<Void> response = ApiResponse.of(DailyOutfitSuccessCode.DELETE_FEEDBACK_SUCCESS);

        return response;
    }
}
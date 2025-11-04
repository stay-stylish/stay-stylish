package org.example.staystylish.domain.dailyoutfit.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
 * 의상 추천 및 아이템 피드백과 관련된 API 요청을 처리하는 컨트롤러 클래스
 */
@Tag(name = "의상 추천", description = "의상 추천 API")
@RestController
@RequestMapping("/api/v1/outfits")
@RequiredArgsConstructor
public class DailyOutfitController {

    private final DailyOutfitService outfitService;

    @Operation(summary = "오늘 OOTD 추천", description = "사용자 ID와 GPS 정보를 기반으로 코디 추천을 제공",
            security = {@SecurityRequirement(name = "bearerAuth")})
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공")
    @GetMapping("/recommendation")
    // 사용자 ID와 GPS 정보를 기반으로 코디 추천을 제공합니다.
    public ApiResponse<DailyOutfitRecommendationResponse> getOutfitRecommendation(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam double latitude,
            @RequestParam double longitude
    ) {

        Long userId = principal.getUser().getId();
        DailyOutfitRecommendationResponse response = outfitService.getOutfitRecommendationWithLinks(userId, latitude, longitude);

        return ApiResponse.of(DailyOutfitSuccessCode.GET_OUTFIT_RECOMMENDATION_SUCCESS, response);
    }


    @Operation(summary = "피드백 등록", description = "사용자 카테고리 피드백을 처리합니다.",
            security = {@SecurityRequirement(name = "bearerAuth")})
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "성공")
    @PostMapping("/feedback")
    @ResponseStatus(HttpStatus.CREATED)
    // 사용자 카테고리 피드백(좋아요/싫어요)을 처리합니다.
    public ApiResponse<Void> handleFeedback(@AuthenticationPrincipal UserPrincipal principal,
                                            @RequestBody FeedbackRequest request) {

        Long userId = principal.getUser().getId();
        outfitService.createFeedback(userId, request.categoryName(), request.status());

        return ApiResponse.of(DailyOutfitSuccessCode.CREATE_FEEDBACK_SUCCESS);
    }

    @Operation(summary = "피드백 삭제", description = "사용자 카테고리 피드백을 삭제합니다.",
            security = {@SecurityRequirement(name = "bearerAuth")})
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공")
    @DeleteMapping("/feedback")
    // 사용자 카테고리 피드백을 삭제합니다.
    public ApiResponse<Void> deleteFeedback(@AuthenticationPrincipal UserPrincipal principal,
                                            @RequestBody FeedbackRequest request) {

        Long userId = principal.getUser().getId();
        outfitService.deleteFeedback(userId, request.categoryName(), request.status());

        return ApiResponse.of(DailyOutfitSuccessCode.DELETE_FEEDBACK_SUCCESS);
    }
}
package org.example.staystylish.domain.traveloutfit.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.example.staystylish.common.dto.response.ApiResponse;
import org.example.staystylish.common.dto.response.PageResponse;
import org.example.staystylish.common.security.UserPrincipal;
import org.example.staystylish.domain.traveloutfit.code.TravelOutfitSuccessCode;
import org.example.staystylish.domain.traveloutfit.dto.request.TravelOutfitRequest;
import org.example.staystylish.domain.traveloutfit.dto.response.TravelOutfitResponse;
import org.example.staystylish.domain.traveloutfit.dto.response.TravelOutfitSummaryResponse;
import org.example.staystylish.domain.traveloutfit.entity.TravelOutfit;
import org.example.staystylish.domain.traveloutfit.service.TravelOutfitService;
import org.example.staystylish.domain.user.entity.Gender;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 여행 옷차림 추천 컨트롤러. 생성/목록/상세 조회 엔드포인트를 제공, 공통 ApiResponse로 래핑해 응답
 */
@Tag(name = "여행 옷차림", description = "여행 옷차림 API")
@RestController
@RequestMapping("/api/v1/travel-outfits")
@RequiredArgsConstructor
public class TravelOutfitController {

    private final TravelOutfitService travelOutfitServiceImpl;

    // 여행 옷차림 추천 생성
    @Operation(summary = "해외 여행 옷차림 추천 생성", description = "해외 여행 옷차림을 추천해줍니다.",
            security = {@SecurityRequirement(name = "bearerAuth")})
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공")
    @PostMapping("/recommendations")
    public ApiResponse<Map<String, Long>> createRecommendation(@AuthenticationPrincipal UserPrincipal principal,
                                                               @Valid @RequestBody TravelOutfitRequest request) {
        // 로그인한 사용자 ID랑 성별 추출
        Long userId = principal.getUser().getId();
        Gender gender = principal.getUser().getGender();

        // 성별 정보가 없을 경우에 기본값으로 MALE 설정
        if (gender == null) {

            gender = Gender.MALE;
        }

        // PENDING 상태의 엔티티 생성 및 travelId 확보 - 동기
        TravelOutfit pendingOutfit = travelOutfitServiceImpl.requestRecommendation(userId, request);

        // 실제 작업 수행 (이 메서드는 즉시 리턴됨) - 비동기
        travelOutfitServiceImpl.processRecommendation(pendingOutfit.getId(), request, gender);

        // 3. travelId를 클라이언트에 즉시 반환
        return ApiResponse.of(
                TravelOutfitSuccessCode.REQUEST_ACCEPTED, // (4단계에서 추가할 SuccessCode)
                Map.of("travelId", pendingOutfit.getId())
        );
    }

    // 내 추천 목록(요약) 페이징 조회
    @Operation(summary = "내 추천 목록 페이징 조회", description = "내가 추천 받았던 해외 여행 추천 옷차림 리스트를 조회합니다.",
            security = {@SecurityRequirement(name = "bearerAuth")})
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공")
    @GetMapping("/recommendations")
    public ApiResponse<PageResponse<TravelOutfitSummaryResponse>> getMyRecommendationsSummary(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        Long userId = principal.getUser().getId();

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<TravelOutfitSummaryResponse> responsePage =
                travelOutfitServiceImpl.getMyRecommendationsSummary(userId, pageable);

        return ApiResponse.of(TravelOutfitSuccessCode.GET_RECOMMENDATIONS_SUCCESS, PageResponse.fromPage(responsePage));
    }

    // 추천 상세 조회
    @Operation(summary = "상세 조회", description = "내가 추천 받았던 목록의 단건을 상세 조회합니다.",
            security = {@SecurityRequirement(name = "bearerAuth")})
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공")
    @GetMapping("/recommendations/{travelId}")
    public ApiResponse<TravelOutfitResponse> getRecommendationDetail(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long travelId) {

        Long userId = principal.getUser().getId();

        TravelOutfitResponse response = travelOutfitServiceImpl.getRecommendationDetail(userId, travelId);

        return ApiResponse.of(TravelOutfitSuccessCode.GET_RECOMMENDATION_DETAIL_SUCCESS, response);
    }
}

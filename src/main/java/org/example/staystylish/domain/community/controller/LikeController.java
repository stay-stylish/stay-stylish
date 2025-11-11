package org.example.staystylish.domain.community.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.staystylish.common.dto.response.ApiResponse;
import org.example.staystylish.common.security.UserPrincipal;
import org.example.staystylish.domain.community.code.CommunitySuccessCode;
import org.example.staystylish.domain.community.dto.response.LikeResponse;
import org.example.staystylish.domain.community.service.LikeService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "좋아요", description = "좋아요 API")
@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    @Operation(summary = "좋아요 토글", description = "게시물 좋아요를 누르거나 해제할 수 있습니다",
            security = {@SecurityRequirement(name = "bearerAuth")})
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공")
    @PostMapping("/{postId}/like")
    public ApiResponse<LikeResponse> toggleLike(@AuthenticationPrincipal UserPrincipal principal,
                                                @PathVariable Long postId) {
        return ApiResponse.of(CommunitySuccessCode.POST_LIKE_TOGGLE_SUCCESS,
                likeService.toggleLike(principal.getUser(), postId));
    }
}


package org.example.staystylish.domain.community.controller;

import lombok.RequiredArgsConstructor;
import org.example.staystylish.common.dto.response.ApiResponse;
import org.example.staystylish.common.security.UserPrincipal;
import org.example.staystylish.domain.community.dto.response.LikeResponse;
import org.example.staystylish.domain.community.exception.CommunitySuccessCode;
import org.example.staystylish.domain.community.service.LikeService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    @PostMapping("/{postId}/like")
    public ApiResponse<LikeResponse> toggleLike(@AuthenticationPrincipal UserPrincipal principal,
                                                @PathVariable Long postId) {
        return ApiResponse.of(CommunitySuccessCode.POST_LIKE_TOGGLE_SUCCESS,
                likeService.toggleLike(principal.getUser(), postId));
    }
}


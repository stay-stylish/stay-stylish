package org.example.staystylish.domain.community.controller;

import lombok.RequiredArgsConstructor;
import org.example.staystylish.common.dto.response.ApiResponse;
import org.example.staystylish.common.security.UserPrincipal;
import org.example.staystylish.domain.community.consts.CommunitySuccessCode;
import org.example.staystylish.domain.community.dto.response.ShareResponse;
import org.example.staystylish.domain.community.service.ShareService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class ShareController {

    private final ShareService shareService;

    @PostMapping("/{postId}/share")
    public ApiResponse<ShareResponse> sharePost(@AuthenticationPrincipal UserPrincipal principal,
                                                @PathVariable Long postId,
                                                @RequestParam String platform) {
        return ApiResponse.of(CommunitySuccessCode.POST_SHARE_SUCCESS,
                shareService.sharePost(principal.getUser(), postId, platform));
    }
}


package org.example.staystylish.domain.community.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "공유", description = "커뮤니티 - 공유 API")
@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class ShareController {

    private final ShareService shareService;

    @Operation(summary = "공유수 증가", description = "게시물을 공유할 수 있습니다.",
            security = {@SecurityRequirement(name = "bearerAuth")})
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공")
    @PostMapping("/{postId}/share")
    public ApiResponse<ShareResponse> sharePost(@AuthenticationPrincipal UserPrincipal principal,
                                                @PathVariable Long postId,
                                                @RequestParam String platform) {
        return ApiResponse.of(CommunitySuccessCode.POST_SHARE_SUCCESS,
                shareService.sharePost(principal.getUser(), postId, platform));
    }
}


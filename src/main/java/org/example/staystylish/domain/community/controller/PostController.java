package org.example.staystylish.domain.community.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.staystylish.common.dto.response.ApiResponse;
import org.example.staystylish.common.security.UserPrincipal;
import org.example.staystylish.domain.community.consts.CommunitySuccessCode;
import org.example.staystylish.domain.community.dto.request.PostRequest;
import org.example.staystylish.domain.community.dto.response.PostResponse;
import org.example.staystylish.domain.community.service.PostService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "게시글", description = "게시글 관련 API")
@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;


    @Operation(summary = "게시물 작성", description = "게시물을 작성합니다.",
            security = {@SecurityRequirement(name = "bearerAuth")})
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공")
    @PostMapping
    public ApiResponse<PostResponse> createPost(@AuthenticationPrincipal UserPrincipal principal,
                                                @Valid @RequestBody PostRequest request) {
        return ApiResponse.of(CommunitySuccessCode.POST_CREATE_SUCCESS,
                postService.createPost(principal.getUser(), request));
    }

    @Operation(summary = "상세 조회", description = "게시물 상세 조회",
            security = {@SecurityRequirement(name = "bearerAuth")})
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공")
    @GetMapping("/{postId}")
    public ApiResponse<PostResponse> getPost(@PathVariable Long postId) {
        return ApiResponse.of(CommunitySuccessCode.POST_GET_SUCCESS, postService.getPost(postId));
    }

    @Operation(summary = "모든 게시물 조회", description = "모든 게시물을 조회합니다.",
            security = {@SecurityRequirement(name = "bearerAuth")})
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공")
    @GetMapping
    public ApiResponse<Page<PostResponse>> getAllPosts(
            @PageableDefault(page = 0, size = 10) Pageable pageable) {
        return ApiResponse.of(
                CommunitySuccessCode.POST_GET_SUCCESS,
                postService.getAllPosts(pageable)
        );
    }

    @Operation(summary = "수정", description = "게시물 수정",
            security = {@SecurityRequirement(name = "bearerAuth")})
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공")
    @PutMapping("/{postId}")
    public ApiResponse<PostResponse> updatePost(@AuthenticationPrincipal UserPrincipal principal,
                                                @PathVariable Long postId,
                                                @Valid @RequestBody PostRequest request) {
        return ApiResponse.of(CommunitySuccessCode.POST_UPDATE_SUCCESS,
                postService.updatePost(principal.getUser(), postId, request));
    }

    @Operation(summary = "삭제", description = "게시물 삭제",
            security = {@SecurityRequirement(name = "bearerAuth")})
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공")
    @DeleteMapping("/{postId}")
    public ApiResponse<Void> deletePost(@AuthenticationPrincipal UserPrincipal principal,
                                        @PathVariable Long postId) {
        postService.deletePost(principal.getUser(), postId);
        return ApiResponse.of(CommunitySuccessCode.POST_DELETE_SUCCESS, null);
    }
}


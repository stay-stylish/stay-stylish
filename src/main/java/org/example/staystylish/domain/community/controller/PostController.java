package org.example.staystylish.domain.community.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.staystylish.common.dto.response.ApiResponse;
import org.example.staystylish.common.security.UserPrincipal;
import org.example.staystylish.domain.community.dto.request.PostRequest;
import org.example.staystylish.domain.community.dto.response.PostResponse;
import org.example.staystylish.domain.community.exception.CommunitySuccessCode;
import org.example.staystylish.domain.community.service.PostService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping
    public ApiResponse<PostResponse> createPost(@AuthenticationPrincipal UserPrincipal principal,
                                                @Valid @RequestBody PostRequest request) {
        return ApiResponse.of(CommunitySuccessCode.POST_CREATE_SUCCESS,
                postService.createPost(principal.getUser(), request));
    }

    @GetMapping("/{postId}")
    public ApiResponse<PostResponse> getPost(@PathVariable Long postId) {
        return ApiResponse.of(CommunitySuccessCode.POST_GET_SUCCESS, postService.getPost(postId));
    }

    @GetMapping
    public ApiResponse<Page<PostResponse>> getAllPosts(
            @PageableDefault(page = 0, size = 10) Pageable pageable) {
        return ApiResponse.of(
                CommunitySuccessCode.POST_GET_SUCCESS,
                postService.getAllPosts(pageable)
        );
    }

    @PutMapping("/{postId}")
    public ApiResponse<PostResponse> updatePost(@AuthenticationPrincipal UserPrincipal principal,
                                                @PathVariable Long postId,
                                                @Valid @RequestBody PostRequest request) {
        return ApiResponse.of(CommunitySuccessCode.POST_UPDATE_SUCCESS,
                postService.updatePost(principal.getUser(), postId, request));
    }

    @DeleteMapping("/{postId}")
    public ApiResponse<Void> deletePost(@AuthenticationPrincipal UserPrincipal principal,
                                        @PathVariable Long postId) {
        postService.deletePost(principal.getUser(), postId);
        return ApiResponse.of(CommunitySuccessCode.POST_DELETE_SUCCESS, null);
    }
}


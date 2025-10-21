package org.example.staystylish.domain.dailyoutfit.controller;

import lombok.RequiredArgsConstructor;
import org.example.staystylish.common.security.UserPrincipal;
import org.example.staystylish.domain.dailyoutfit.dto.request.FeedbackRequest;
import org.example.staystylish.domain.dailyoutfit.dto.response.OutfitRecommendationResponse;
import org.example.staystylish.domain.dailyoutfit.enums.LikeStatus;
import org.example.staystylish.domain.dailyoutfit.service.OutfitService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 의상 추천 및 아이템 피드백과 관련된 API 요청을 처리하는 컨트롤러 클래스입니다.
 */
@RestController
@RequestMapping("/api/v1/outfits")
@RequiredArgsConstructor
public class OutfitController {

    private final OutfitService outfitService;

    @GetMapping("/recommendation")
    public ResponseEntity<OutfitRecommendationResponse> getOutfitRecommendation(@AuthenticationPrincipal UserPrincipal principal) {
        Long userId = principal.getUser().getId();
        OutfitRecommendationResponse response = outfitService.getOutfitRecommendation(userId);
        return ResponseEntity.ok(response);
    }


    @PostMapping("/items/{itemId}/feedback")
    public ResponseEntity<Map<String, String>> handleFeedback(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long itemId, @RequestBody FeedbackRequest request) {
        Long userId = principal.getUser().getId();
        if (request.status() == LikeStatus.LIKE) {
            outfitService.addFeedback(userId, itemId, LikeStatus.LIKE);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "피드백이 성공적으로 저장되었습니다."));
        } else if (request.status() == LikeStatus.DISLIKE) {
            outfitService.addFeedback(userId, itemId, LikeStatus.DISLIKE);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "피드백이 성공적으로 저장되었습니다."));
        } else {
            return ResponseEntity.badRequest().body(Map.of("message", "유효하지 않은 피드백 상태입니다."));
        }
    }

    @DeleteMapping("/items/{itemId}/feedback")
    public ResponseEntity<Map<String, String>> deleteFeedback(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long itemId, @RequestBody FeedbackRequest request) {
        Long userId = principal.getUser().getId();
        if (request.status() == LikeStatus.LIKE) {
            outfitService.removeFeedback(userId, itemId, LikeStatus.LIKE);
            return ResponseEntity.ok(Map.of("message", "피드백이 취소되었습니다."));
        } else if (request.status() == LikeStatus.DISLIKE) {
            outfitService.removeFeedback(userId, itemId, LikeStatus.DISLIKE);
            return ResponseEntity.ok(Map.of("message", "피드백이 취소되었습니다."));
        } else {
            return ResponseEntity.badRequest().body(Map.of("message", "유효하지 않은 피드백 상태입니다."));
        }
    }
}

package org.example.staystylish.domain.outfit.controller;

import lombok.RequiredArgsConstructor;
import org.example.staystylish.domain.outfit.dto.response.OutfitRecommendationResponse;
import org.example.staystylish.domain.outfit.enums.LikeStatus;
import org.example.staystylish.domain.outfit.service.OutfitService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/outfits")
@RequiredArgsConstructor
public class OutfitController {

    private final OutfitService outfitService;

    @GetMapping("/recommendation")
    public ResponseEntity<OutfitRecommendationResponse> getOutfitRecommendation() {
        Long userId = getCurrentUserId();
        OutfitRecommendationResponse response = outfitService.getOutfitRecommendation(userId);
        return ResponseEntity.ok(response);
    }

    // 현재 사용자 ID를 시큐리티 컨텍스트에서 가져오는 임시 메서드
    Long getCurrentUserId() {
        // 실제 애플리케이션에서는 아래와 같이 Spring Security 컨텍스트에서 사용자 정보를 가져와야 합니다.
        // Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // return ((YourUserDetails) authentication.getPrincipal()).getId();
        return 1L; // 지금은 임시로 고정된 ID를 사용합니다.
    }

    @PostMapping("/items/{itemId}/like")
    public ResponseEntity<Map<String, String>> likeItem(@PathVariable Long itemId) {
        Long userId = getCurrentUserId();
        outfitService.addFeedback(userId, itemId, LikeStatus.LIKE);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "피드백이 성공적으로 저장되었습니다."));
    }

    @DeleteMapping("/items/{itemId}/like")
    public ResponseEntity<Map<String, String>> unlikeItem(@PathVariable Long itemId) {
        Long userId = getCurrentUserId();
        outfitService.removeFeedback(userId, itemId, LikeStatus.LIKE);
        return ResponseEntity.ok(Map.of("message", "피드백이 취소되었습니다."));
    }

    @PostMapping("/items/{itemId}/dislike")
    public ResponseEntity<Map<String, String>> dislikeItem(@PathVariable Long itemId) {
        Long userId = getCurrentUserId();
        outfitService.addFeedback(userId, itemId, LikeStatus.DISLIKE);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "피드백이 성공적으로 저장되었습니다."));
    }

    @DeleteMapping("/items/{itemId}/dislike")
    public ResponseEntity<Map<String, String>> undislikeItem(@PathVariable Long itemId) {
        Long userId = getCurrentUserId();
        outfitService.removeFeedback(userId, itemId, LikeStatus.DISLIKE);
        return ResponseEntity.ok(Map.of("message", "피드백이 취소되었습니다."));
    }
}

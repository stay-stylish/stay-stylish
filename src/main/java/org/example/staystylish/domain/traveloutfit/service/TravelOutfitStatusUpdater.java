package org.example.staystylish.domain.traveloutfit.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.staystylish.domain.traveloutfit.repository.TravelOutfitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TravelOutfitStatusUpdater {

    private final TravelOutfitRepository travelOutfitRepository;

    // 추천 생성 실패 시 별도 트랜잭션에서 상태를 failed 로 업데이트 (메인 로직 롤백에 영향 X)
    @Transactional
    public void updateStatusToFailed(Long travelId, String errorMessage) {

        try {
            log.warn("travelId={} 상태 FAILED로 변경 시도.", travelId);

            travelOutfitRepository.findById(travelId).ifPresentOrElse(outfit -> {
                        outfit.fail(errorMessage);
                        travelOutfitRepository.save(outfit);
                        log.info("travelId={} 상태 FAILED로 변경 완료.", travelId);
                    },
                    () -> log.error("travelId={} 를 찾을 수 없음.", travelId));
        } catch (Exception e) {

            log.error("FAILED 상태 업데이트 트랜잭션 롤백. travelId={}, 원인: {}", travelId, e.getMessage(), e);
        }
    }
}

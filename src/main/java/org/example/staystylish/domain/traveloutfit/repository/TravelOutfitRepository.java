package org.example.staystylish.domain.traveloutfit.repository;

import java.util.Optional;
import org.example.staystylish.domain.traveloutfit.entity.TravelOutfit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TravelOutfitRepository extends JpaRepository<TravelOutfit, Long> {

    Page<TravelOutfit> findByUserId(Long userId, Pageable pageable);

    Optional<TravelOutfit> findByIdAndUserId(Long id, Long userId);
}

package org.example.staystylish.domain.travel.repository;

import org.example.staystylish.domain.travel.entity.TravelOutfit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TravelOutfitRepository extends JpaRepository<TravelOutfit, Long> {

    Page<TravelOutfit> findByUserId(Long userId, Pageable pageable);
}

package org.example.staystylish.domain.community.repository;

import org.example.staystylish.domain.community.entity.Share;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShareRepository extends JpaRepository<Share, Long> {
}

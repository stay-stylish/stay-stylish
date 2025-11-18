package org.example.staystylish.domain.localweather.entity;

import jakarta.persistence.*;
import lombok.*;
import org.jetbrains.annotations.TestOnly;

@Entity
@Table(name = "region_grid")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Region {

    @Id
    @Column(name = "region_id")
    private Long id;

    @Column(name = "region_province")
    private String province; // 시 (예: 서울 특별시)

    @Column(name = "region_city")
    private String city; // 구 (예: 종로구)

    @Column(name = "region_district")
    private String district; // 동 (예:사직동)

    private Double longitude;
    private Double latitude;

    // 테스트 전용 생성자
    @TestOnly
    protected Region(Long id) {
        this.id = id;
    }
}

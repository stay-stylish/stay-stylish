package org.example.staystylish.domain.localweather.entity;

import jakarta.persistence.*;
import lombok.*;

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
    private String province;

    @Column(name = "region_city")
    private String city;

    @Column(name = "region_district")
    private String district;

    private Double longitude;
    private Double latitude;
}

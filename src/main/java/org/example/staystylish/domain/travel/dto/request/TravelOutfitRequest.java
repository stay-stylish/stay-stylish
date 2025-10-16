package org.example.staystylish.domain.travel.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record TravelOutfitRequest(
        @NotBlank String country,
        @NotBlank String city,
        @NotNull LocalDate startdate,
        @NotNull LocalDate enddate
) {
}

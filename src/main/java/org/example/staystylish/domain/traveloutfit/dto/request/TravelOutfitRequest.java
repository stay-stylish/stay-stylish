package org.example.staystylish.domain.traveloutfit.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record TravelOutfitRequest(
        @NotBlank String country,
        @NotBlank String city,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate
) {
}

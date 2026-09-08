package com.example.weather.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

public record WeatherRequest(
        @NotBlank
        @Pattern(regexp = "\\d{6}", message = "pincode must contain exactly 6 digits")
        String pincode,

        @NotNull
        @JsonProperty("for_date")
        LocalDate forDate
) {
}

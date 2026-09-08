package com.example.weather.dto;

import com.example.weather.entity.WeatherData;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public record WeatherResponse(
        String pincode,

        @JsonProperty("for_date")
        LocalDate forDate,

        double latitude,
        double longitude,
        Double minTemperature,
        Double maxTemperature,
        Double afternoonTemperature,
        Integer afternoonHumidity,
        Integer afternoonPressure,
        Double windSpeed,
        Double precipitation,
        Integer cloudCover,
        boolean cached
) {
    public static WeatherResponse from(WeatherData data, boolean cached) {
        return new WeatherResponse(
                data.getLocation().getPincode(),
                data.getForecastDate(),
                data.getLocation().getLatitude(),
                data.getLocation().getLongitude(),
                data.getMinTemperature(),
                data.getMaxTemperature(),
                data.getAfternoonTemperature(),
                data.getAfternoonHumidity(),
                data.getAfternoonPressure(),
                data.getWindSpeed(),
                data.getPrecipitation(),
                data.getCloudCover(),
                cached
        );
    }
}

package com.example.weather.client;

public record WeatherDetails(
        Double minTemperature,
        Double maxTemperature,
        Double afternoonTemperature,
        Integer afternoonHumidity,
        Integer afternoonPressure,
        Double windSpeed,
        Double precipitation,
        Integer cloudCover
) {
}

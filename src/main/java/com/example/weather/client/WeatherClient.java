package com.example.weather.client;

import java.time.LocalDate;

public interface WeatherClient {
    WeatherDetails getWeather(double latitude, double longitude, LocalDate date);
}

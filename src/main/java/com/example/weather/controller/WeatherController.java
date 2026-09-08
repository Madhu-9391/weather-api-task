package com.example.weather.controller;

import com.example.weather.dto.WeatherRequest;
import com.example.weather.dto.WeatherResponse;
import com.example.weather.service.WeatherService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class WeatherController {

    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @PostMapping("/weather")
    public ResponseEntity<WeatherResponse> getWeather(
            @Valid @RequestBody WeatherRequest request) {

        return ResponseEntity.ok(
                weatherService.getWeather(request.pincode(), request.forDate()));
    }
}

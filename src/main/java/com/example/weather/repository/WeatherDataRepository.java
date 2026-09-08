package com.example.weather.repository;

import com.example.weather.entity.Location;
import com.example.weather.entity.WeatherData;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.Optional;

public interface WeatherDataRepository extends JpaRepository<WeatherData, Long> {
    Optional<WeatherData> findByLocationAndForecastDate(Location location, LocalDate forecastDate);
}

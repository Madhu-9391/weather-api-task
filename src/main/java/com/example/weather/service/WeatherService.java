package com.example.weather.service;

import com.example.weather.client.Coordinates;
import com.example.weather.client.GeocodingClient;
import com.example.weather.client.WeatherClient;
import com.example.weather.client.WeatherDetails;
import com.example.weather.dto.WeatherResponse;
import com.example.weather.entity.Location;
import com.example.weather.entity.WeatherData;
import com.example.weather.repository.LocationRepository;
import com.example.weather.repository.WeatherDataRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class WeatherService {

    private final LocationRepository locationRepository;
    private final WeatherDataRepository weatherDataRepository;
    private final GeocodingClient geocodingClient;
    private final WeatherClient weatherClient;

    public WeatherService(
            LocationRepository locationRepository,
            WeatherDataRepository weatherDataRepository,
            GeocodingClient geocodingClient,
            WeatherClient weatherClient) {
        this.locationRepository = locationRepository;
        this.weatherDataRepository = weatherDataRepository;
        this.geocodingClient = geocodingClient;
        this.weatherClient = weatherClient;
    }

    public WeatherResponse getWeather(String pincode, LocalDate date) {
        Location location = locationRepository.findByPincode(pincode).orElse(null);

        if (location != null) {
            var cached = weatherDataRepository.findByLocationAndForecastDate(location, date);
            if (cached.isPresent()) {
                return WeatherResponse.from(cached.get(), true);
            }
        }

        if (location == null) {
            location = findOrCreateLocation(pincode);
        }

        WeatherDetails details = weatherClient.getWeather(
                location.getLatitude(),
                location.getLongitude(),
                date);

        WeatherData data = toEntity(location, date, details);

        try {
            WeatherData saved = weatherDataRepository.save(data);
            return WeatherResponse.from(saved, false);
        } catch (DataIntegrityViolationException ex) {
            WeatherData existing = weatherDataRepository
                    .findByLocationAndForecastDate(location, date)
                    .orElseThrow(() -> ex);
            return WeatherResponse.from(existing, true);
        }
    }

    private Location findOrCreateLocation(String pincode) {
        Coordinates coordinates = geocodingClient.getCoordinates(pincode);
        Location location = new Location(
                pincode,
                coordinates.latitude(),
                coordinates.longitude());

        try {
            return locationRepository.save(location);
        } catch (DataIntegrityViolationException ex) {
            return locationRepository.findByPincode(pincode)
                    .orElseThrow(() -> ex);
        }
    }

    private WeatherData toEntity(
            Location location,
            LocalDate date,
            WeatherDetails details) {

        WeatherData data = new WeatherData(location, date);
        data.setMinTemperature(details.minTemperature());
        data.setMaxTemperature(details.maxTemperature());
        data.setAfternoonTemperature(details.afternoonTemperature());
        data.setAfternoonHumidity(details.afternoonHumidity());
        data.setAfternoonPressure(details.afternoonPressure());
        data.setWindSpeed(details.windSpeed());
        data.setPrecipitation(details.precipitation());
        data.setCloudCover(details.cloudCover());
        return data;
    }
}

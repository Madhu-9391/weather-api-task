package com.example.weather.service;

import com.example.weather.client.*;
import com.example.weather.entity.Location;
import com.example.weather.entity.WeatherData;
import com.example.weather.repository.LocationRepository;
import com.example.weather.repository.WeatherDataRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WeatherServiceTest {

    @Mock LocationRepository locationRepository;
    @Mock WeatherDataRepository weatherDataRepository;
    @Mock GeocodingClient geocodingClient;
    @Mock WeatherClient weatherClient;

    @InjectMocks WeatherService weatherService;

    @Test
    void cacheHitDoesNotCallExternalApis() {
        LocalDate date = LocalDate.of(2020, 10, 15);
        Location location = new Location("411014", 18.5679, 73.9143);
        WeatherData saved = weatherData(location, date);

        when(locationRepository.findByPincode("411014")).thenReturn(Optional.of(location));
        when(weatherDataRepository.findByLocationAndForecastDate(location, date))
                .thenReturn(Optional.of(saved));

        var response = weatherService.getWeather("411014", date);

        assertTrue(response.cached());
        verifyNoInteractions(geocodingClient, weatherClient);
        verify(weatherDataRepository, never()).save(any());
    }

    @Test
    void firstRequestGeocodesAndFetchesWeather() {
        LocalDate date = LocalDate.of(2020, 10, 15);
        when(locationRepository.findByPincode("411014")).thenReturn(Optional.empty());
        when(geocodingClient.getCoordinates("411014"))
                .thenReturn(new Coordinates(18.5679, 73.9143));
        when(locationRepository.save(any(Location.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(weatherClient.getWeather(18.5679, 73.9143, date))
                .thenReturn(details());
        when(weatherDataRepository.save(any(WeatherData.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = weatherService.getWeather("411014", date);

        assertEquals("411014", response.pincode());
        assertFalse(response.cached());
        verify(geocodingClient).getCoordinates("411014");
        verify(weatherClient).getWeather(18.5679, 73.9143, date);
        verify(locationRepository).save(any(Location.class));
        verify(weatherDataRepository).save(any(WeatherData.class));
    }

    @Test
    void newDateReusesSavedCoordinates() {
        LocalDate date = LocalDate.of(2020, 10, 16);
        Location location = new Location("411014", 18.5679, 73.9143);

        when(locationRepository.findByPincode("411014")).thenReturn(Optional.of(location));
        when(weatherDataRepository.findByLocationAndForecastDate(location, date))
                .thenReturn(Optional.empty());
        when(weatherClient.getWeather(18.5679, 73.9143, date)).thenReturn(details());
        when(weatherDataRepository.save(any(WeatherData.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = weatherService.getWeather("411014", date);

        assertFalse(response.cached());
        verifyNoInteractions(geocodingClient);
        verify(weatherClient).getWeather(18.5679, 73.9143, date);
        verify(weatherDataRepository).save(any(WeatherData.class));
    }

    @Test
    void unknownPincodeIsGeocodedOnceThenWeatherIsFetched() {
        LocalDate date = LocalDate.of(2020, 10, 15);

        when(locationRepository.findByPincode("500001")).thenReturn(Optional.empty());
        when(geocodingClient.getCoordinates("500001"))
                .thenReturn(new Coordinates(17.3850, 78.4867));
        when(locationRepository.save(any(Location.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        
        when(weatherClient.getWeather(17.3850, 78.4867, date))
                .thenReturn(details());
        when(weatherDataRepository.save(any(WeatherData.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = weatherService.getWeather("500001", date);

        assertEquals("500001", response.pincode());
        assertFalse(response.cached());
        verify(geocodingClient).getCoordinates("500001");
        verify(weatherClient).getWeather(17.3850, 78.4867, date);
    }

    private WeatherDetails details() {
        return new WeatherDetails(20.0, 31.0, 28.0, 60, 1012, 5.5, 0.0, 20);
    }

    private WeatherData weatherData(Location location, LocalDate date) {
        WeatherData data = new WeatherData(location, date);
        data.setMinTemperature(20.0);
        data.setMaxTemperature(31.0);
        data.setAfternoonTemperature(28.0);
        data.setAfternoonHumidity(60);
        data.setAfternoonPressure(1012);
        data.setWindSpeed(5.5);
        data.setPrecipitation(0.0);
        data.setCloudCover(20);
        return data;
    }
}

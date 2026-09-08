package com.example.weather.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.example.weather.exception.ExternalServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;

@Component
public class OpenWeatherClient implements WeatherClient {

    private final RestClient restClient;

    @Value("${openweather.api-key}")
    private String apiKey;

    public OpenWeatherClient(RestClient.Builder builder) {
        this.restClient = builder.baseUrl("https://history.openweathermap.org").build();
    }

    @Override
    public WeatherDetails getWeather(double latitude, double longitude, LocalDate date) {
        try {
            long start = date.atStartOfDay(ZoneOffset.UTC).toEpochSecond();
            long end = date.plusDays(1).atStartOfDay(ZoneOffset.UTC).toEpochSecond();

            HistoricalResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/data/2.5/history/city")
                            .queryParam("lat", latitude)
                            .queryParam("lon", longitude)
                            .queryParam("type", "hour")
                            .queryParam("start", start)
                            .queryParam("end", end)
                            .queryParam("units", "metric")
                            .queryParam("appid", apiKey)
                            .build())
                    .retrieve()
                    .body(HistoricalResponse.class);

            if (response == null || response.list() == null || response.list().isEmpty()) {
                throw new ExternalServiceException("No historical weather data returned for " + date);
            }

            List<HourlyWeather> hours = response.list().stream()
                    .filter(this::hasRequiredWeatherData)
                    .toList();

            if (hours.isEmpty()) {
                throw new ExternalServiceException("Historical weather response contains no usable data for " + date);
            }

            double minTemperature = hours.stream()
                    .map(HourlyWeather::main)
                    .map(Main::temp)
                    .min(Double::compareTo)
                    .orElseThrow();

            double maxTemperature = hours.stream()
                    .map(HourlyWeather::main)
                    .map(Main::temp)
                    .max(Double::compareTo)
                    .orElseThrow();

            // The assignment asks for afternoon weather. We use the observation
            // closest to 12:00 UTC as a simple, deterministic afternoon snapshot.
            HourlyWeather afternoon = hours.stream()
                    .min(Comparator.comparingLong(h -> Math.abs(h.dt() - date.atTime(12, 0).toEpochSecond(ZoneOffset.UTC))))
                    .orElseThrow();

            double precipitation = hours.stream()
                    .map(HourlyWeather::rain)
                    .filter(rain -> rain != null)
                    .mapToDouble(this::rainAmount)
                    .sum();

            return new WeatherDetails(
                    minTemperature,
                    maxTemperature,
                    afternoon.main().temp(),
                    afternoon.main().humidity(),
                    afternoon.main().pressure(),
                    afternoon.wind().speed(),
                    precipitation,
                    afternoon.clouds().all()
            );
        } catch (ExternalServiceException e) {
            throw e;
        } catch (RestClientResponseException e) {
            String body = e.getResponseBodyAsString();
            String detail = body == null || body.isBlank() ? e.getStatusText() : body;
            throw new ExternalServiceException(
                    "OpenWeather request failed (" + e.getStatusCode().value() + "): " + detail, e);
        } catch (Exception e) {
            throw new ExternalServiceException("Unable to fetch historical weather data: " + e.getMessage(), e);
        }
    }

    private boolean hasRequiredWeatherData(HourlyWeather weather) {
        return weather != null
                && weather.main() != null
                && weather.main().temp() != null
                && weather.main().humidity() != null
                && weather.main().pressure() != null
                && weather.wind() != null
                && weather.wind().speed() != null
                && weather.clouds() != null
                && weather.clouds().all() != null;
    }

    private double rainAmount(Rain rain) {
        if (rain.oneHour() != null) {
            return rain.oneHour();
        }
        return rain.threeHours() != null ? rain.threeHours() : 0.0;
    }

    private record HistoricalResponse(List<HourlyWeather> list) {}

    private record HourlyWeather(
            long dt,
            Main main,
            Wind wind,
            Clouds clouds,
            Rain rain
    ) {}

    private record Main(
            Double temp,
            Integer pressure,
            Integer humidity
    ) {}

    private record Wind(Double speed) {}

    private record Clouds(Integer all) {}

    private record Rain(
            @JsonProperty("1h") Double oneHour,
            @JsonProperty("3h") Double threeHours
    ) {}
}

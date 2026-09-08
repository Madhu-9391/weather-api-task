package com.example.weather.client;

import com.example.weather.exception.ExternalServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.time.LocalDate;

@Component
public class OpenWeatherClient implements WeatherClient {

    private final RestClient restClient;

    @Value("${openweather.api-key}")
    private String apiKey;

    public OpenWeatherClient(RestClient.Builder builder) {
        this.restClient = builder.baseUrl("https://api.openweathermap.org").build();
    }

    @Override
    public WeatherDetails getWeather(double latitude, double longitude, LocalDate date) {
        try {
            CurrentWeather response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/data/2.5/weather")
                            .queryParam("lat", latitude)
                            .queryParam("lon", longitude)
                            .queryParam("units", "metric")
                            .queryParam("appid", apiKey)
                            .build())
                    .retrieve()
                    .body(CurrentWeather.class);

            if (response == null || response.main() == null || response.wind() == null || response.clouds() == null) {
                throw new ExternalServiceException("No weather data returned");
            }

            return new WeatherDetails(
                    response.main().tempMin(),
                    response.main().tempMax(),
                    response.main().temp(),
                    response.main().humidity(),
                    response.main().pressure(),
                    response.wind().speed(),
                    0.0,
                    response.clouds().all()
            );
        } catch (ExternalServiceException e) {
            throw e;
        } catch (RestClientResponseException e) {
            String body = e.getResponseBodyAsString();
            String detail = body == null || body.isBlank() ? e.getStatusText() : body;
            throw new ExternalServiceException(
                    "OpenWeather request failed (" + e.getStatusCode().value() + "): " + detail, e);
        } catch (Exception e) {
            throw new ExternalServiceException("Unable to fetch weather data: " + e.getMessage(), e);
        }
    }

    private record CurrentWeather(
            Main main,
            Wind wind,
            Clouds clouds
    ) {}

    private record Main(
            Double temp,
            Double tempMin,
            Double tempMax,
            Integer pressure,
            Integer humidity
    ) {}

    private record Wind(Double speed) {}

    private record Clouds(Integer all) {}
}

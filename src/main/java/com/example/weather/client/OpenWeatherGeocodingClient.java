package com.example.weather.client;

import com.example.weather.exception.ExternalServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class OpenWeatherGeocodingClient implements GeocodingClient {

    private final RestClient restClient;

    @Value("${openweather.api-key}")
    private String apiKey;

    public OpenWeatherGeocodingClient(RestClient.Builder builder) {
        this.restClient = builder.baseUrl("https://api.openweathermap.org").build();
    }

    @Override
    public Coordinates getCoordinates(String pincode) {
        try {
            GeoResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/geo/1.0/zip")
                            .queryParam("zip", pincode + ",IN")
                            .queryParam("appid", apiKey)
                            .build())
                    .retrieve()
                    .body(GeoResponse.class);

            if (response == null) {
                throw new ExternalServiceException("No coordinates returned for pincode " + pincode);
            }
            return new Coordinates(response.lat(), response.lon());
        } catch (ExternalServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ExternalServiceException("Unable to resolve pincode " + pincode, e);
        }
    }

    private record GeoResponse(double lat, double lon) {
    }
}

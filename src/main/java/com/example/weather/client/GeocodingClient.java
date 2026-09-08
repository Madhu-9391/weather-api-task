package com.example.weather.client;

public interface GeocodingClient {
    Coordinates getCoordinates(String pincode);
}

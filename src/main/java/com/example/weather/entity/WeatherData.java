package com.example.weather.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "weather_data",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_weather_location_date",
                columnNames = {"location_id", "forecast_date"}))
public class WeatherData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

@ManyToOne(fetch = FetchType.EAGER, optional = false)    
@JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @Column(name = "forecast_date", nullable = false)
    private LocalDate forecastDate;

    private Double minTemperature;
    private Double maxTemperature;
    private Double afternoonTemperature;
    private Integer afternoonHumidity;
    private Integer afternoonPressure;
    private Double windSpeed;
    private Double precipitation;
    private Integer cloudCover;

    protected WeatherData() {
    }

    public WeatherData(Location location, LocalDate forecastDate) {
        this.location = location;
        this.forecastDate = forecastDate;
    }

    public Long getId() { return id; }
    public Location getLocation() { return location; }
    public LocalDate getForecastDate() { return forecastDate; }
    public Double getMinTemperature() { return minTemperature; }
    public Double getMaxTemperature() { return maxTemperature; }
    public Double getAfternoonTemperature() { return afternoonTemperature; }
    public Integer getAfternoonHumidity() { return afternoonHumidity; }
    public Integer getAfternoonPressure() { return afternoonPressure; }
    public Double getWindSpeed() { return windSpeed; }
    public Double getPrecipitation() { return precipitation; }
    public Integer getCloudCover() { return cloudCover; }

    public void setId(Long id) { this.id = id; }
    public void setLocation(Location location) { this.location = location; }
    public void setForecastDate(LocalDate forecastDate) { this.forecastDate = forecastDate; }
    public void setMinTemperature(Double value) { this.minTemperature = value; }
    public void setMaxTemperature(Double value) { this.maxTemperature = value; }
    public void setAfternoonTemperature(Double value) { this.afternoonTemperature = value; }
    public void setAfternoonHumidity(Integer value) { this.afternoonHumidity = value; }
    public void setAfternoonPressure(Integer value) { this.afternoonPressure = value; }
    public void setWindSpeed(Double value) { this.windSpeed = value; }
    public void setPrecipitation(Double value) { this.precipitation = value; }
    public void setCloudCover(Integer value) { this.cloudCover = value; }
}

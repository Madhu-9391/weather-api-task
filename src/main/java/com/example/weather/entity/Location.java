package com.example.weather.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "locations",
        uniqueConstraints = @UniqueConstraint(name = "uk_location_pincode", columnNames = "pincode"))
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 6)
    private String pincode;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    protected Location() {
    }

    public Location(String pincode, Double latitude, Double longitude) {
        this.pincode = pincode;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Long getId() { return id; }
    public String getPincode() { return pincode; }
    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }

    public void setId(Long id) { this.id = id; }
    public void setPincode(String pincode) { this.pincode = pincode; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
}

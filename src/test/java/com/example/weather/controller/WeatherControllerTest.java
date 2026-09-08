package com.example.weather.controller;

import com.example.weather.dto.WeatherResponse;
import com.example.weather.service.WeatherService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WeatherController.class)
class WeatherControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    WeatherService weatherService;

    @Test
    void validRequestReturnsOk() throws Exception {
        LocalDate date = LocalDate.of(2020, 10, 15);
        when(weatherService.getWeather(eq("411014"), eq(date)))
                .thenReturn(new WeatherResponse(
                        "411014", date, 18.5679, 73.9143,
                        20.0, 31.0, 28.0, 60, 1012,
                        5.5, 0.0, 20, false));

        mockMvc.perform(post("/api/v1/weather")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "pincode": "411014",
                                  "for_date": "2020-10-15"
                                }
                                """))
                .andExpect(status().isOk());
    }

    @Test
    void invalidPincodeReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/weather")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "pincode": "123",
                                  "for_date": "2020-10-15"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }
}

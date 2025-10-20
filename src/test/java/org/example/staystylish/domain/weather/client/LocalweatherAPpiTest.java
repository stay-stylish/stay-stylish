package org.example.staystylish.domain.weather.client;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.example.staystylish.domain.localweather.controller.WeatherController;
import org.example.staystylish.domain.localweather.service.WeatherService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

public class LocalweatherAPpiTest {

    @WebMvcTest(WeatherController.class)
    class WeatherControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private WeatherService weatherService;

        @Test
        void testCurrentLocationWeather_W01() throws Exception {
            Mockito.when(weatherService.getWeatherByLatLon(37.5, 127.0))
                    .thenReturn("현재 온도: 20도, 습도: 60%, 강수확률: 10%");

            mockMvc.perform(get("/api/weather/current")
                            .param("lat", "37.5")
                            .param("lon", "127.0")
                            .param("permission", "true"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("현재 온도: 20도, 습도: 60%, 강수확률: 10%"));
        }

        @Test
        void testDefaultWeather_W02() throws Exception {
            Mockito.when(weatherService.getDefaultWeather())
                    .thenReturn("서울 날씨: 온도 18도, 습도 55%, 강수확률 20%");

            mockMvc.perform(get("/api/weather/current")
                            .param("permission", "false"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("서울 날씨: 온도 18도, 습도 55%, 강수확률 20%"));
        }

        @Test
        void testInvalidCoordinates_W03() throws Exception {
            mockMvc.perform(get("/api/weather/current")
                            .param("lat", "0")
                            .param("lon", "0")
                            .param("permission", "true"))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string("유효하지 않은 위치 정보"));
        }
    }

}

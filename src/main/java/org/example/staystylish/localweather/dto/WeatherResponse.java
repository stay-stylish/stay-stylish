package org.example.staystylish.localweather.dto;

import java.util.List;
import java.util.Map;

public class WeatherResponse {
    private List<WeatherItem> items;
    private Map<String, Object> meta;

    // 정상 데이터용 생성자
    public WeatherResponse(List<WeatherItem> items, Map<String, Object> meta) {
        this.items = items;
        this.meta = meta;
    }

    // 에러용 생성자
    public WeatherResponse(String error) {
        this.items = List.of();
        this.meta = Map.of("error", error);
    }

    // Getter / Setter
    public List<WeatherItem> getItems() { return items; }
    public void setItems(List<WeatherItem> items) { this.items = items; }

    public Map<String, Object> getMeta() { return meta; }
    public void setMeta(Map<String, Object> meta) { this.meta = meta; }
}

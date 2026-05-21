package com.pryzmm.splitself.file;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.pryzmm.splitself.SplitSelf;
import org.jetbrains.annotations.NotNull;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class WeatherFetcher {
    private final HttpClient client;

    public WeatherFetcher() {
        this.client = HttpClient.newHttpClient();
    }

    public WeatherData getWeather(String city) throws Exception {
        String encodedCity = URLEncoder.encode(city, StandardCharsets.UTF_8);
        String url = "https://wttr.in/" + encodedCity + "?format=j1";

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .timeout(Duration.ofSeconds(10))
            .header("User-Agent", "SplitSelf/1.0")
            .build();

        HttpResponse<String> response = client.send(request,
            HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new Exception("Failed to get weather data: " + response.statusCode());
        }

        SplitSelf.LOGGER.info("Weather API Response received for: {}", city);
        return parseWeather(response.body());
    }

    private WeatherData parseWeather(String json) {
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        JsonObject current = root.getAsJsonArray("current_condition").get(0).getAsJsonObject();
        int tempC;
        int tempF;
        String condition;
        try {
            tempC = current.get("temp_C").getAsInt();
            tempF = current.get("temp_F").getAsInt();
            condition = current.getAsJsonArray("weatherDesc")
                .get(0).getAsJsonObject()
                .get("value").getAsString();
        } catch (Exception e) {
            return new WeatherData(null, null, null);
        }

        return new WeatherData(tempC, tempF, condition);
    }

    public record WeatherData(Integer feelsLikeC, Integer feelsLikeF, String condition) {
        @Override
        public @NotNull String toString() {
            return String.format(
                "Feels like %d°C | %s weather",
                feelsLikeC, condition
            );
        }
    }
}
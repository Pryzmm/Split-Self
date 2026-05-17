package com.pryzmm.splitself.file;

import com.pryzmm.splitself.SplitSelf;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class CityLocator {
    private final HttpClient client;

    public CityLocator() {
        this.client = HttpClient.newHttpClient();
    }

    public String getCityFromCurrentIP() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://ip-api.com/line/?fields=city,regionName,country"))
                .timeout(java.time.Duration.ofSeconds(10))
                .build();

        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new Exception("Failed to get location data: " + response.statusCode());
        }

        String responseBody = response.body().trim();
        SplitSelf.LOGGER.info("API Response: {}", responseBody);

        String[] lines = responseBody.split("\n");

        if (lines.length >= 3) {
            String country = lines[0].trim();
            String region = lines[1].trim();
            String city = lines[2].trim();

            if (!city.isEmpty() && !"null".equals(city)) {
                return city;
            } else if (!region.isEmpty() && !"null".equals(region)) {
                return region + ", " + country;
            } else {
                return country;
            }
        }

        throw new Exception("Invalid response format from geolocation API");
    }

}
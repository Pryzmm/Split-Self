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

    // Alternative using a different API for better city detection
    public String getCityFromIPGeolocation() throws Exception {
        // This API often has better city resolution
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://ip-api.com/csv/?fields=city,regionName,country"))
                .timeout(java.time.Duration.ofSeconds(10))
                .build();

        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new Exception("Failed to get location data: " + response.statusCode());
        }

        String responseBody = response.body().trim();
        SplitSelf.LOGGER.info("CSV API Response: {}", responseBody);

        String[] parts = responseBody.split(",");

        if (parts.length >= 3) {
            String country = parts[0].replaceAll("\"", "").trim();
            String region = parts[1].replaceAll("\"", "").trim();
            String city = parts[2].replaceAll("\"", "").trim();

            if (!city.isEmpty() && !"null".equals(city)) {
                return city;
            } else if (!region.isEmpty() && !"null".equals(region)) {
                return region + ", " + country;
            } else {
                return country;
            }
        }

        throw new Exception("Invalid CSV response format from geolocation API");
    }

    public String getCityWithFallback() throws Exception {
        try {
            return getCityFromCurrentIP();
        } catch (Exception e1) {
            SplitSelf.LOGGER.warn("Primary API failed, trying CSV fallback: {}", e1.getMessage());
            try {
                return getCityFromIPGeolocation();
            } catch (Exception e2) {
                SplitSelf.LOGGER.warn("CSV API failed, trying ipinfo.io: {}", e2.getMessage());
                try {
                    return getCityFromIPInfo();
                } catch (Exception e3) {
                    SplitSelf.LOGGER.error("All APIs failed. Line: {}, CSV: {}, IPInfo: {}", e1.getMessage(), e2.getMessage(), e3.getMessage());
                    throw new Exception("Failed to get location from all APIs");
                }
            }
        }
    }

    // Simple ipinfo.io fallback
    public String getCityFromIPInfo() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://ipinfo.io/city"))
                .timeout(java.time.Duration.ofSeconds(10))
                .build();

        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new Exception("Failed to get location data: " + response.statusCode());
        }

        String city = response.body().trim();
        SplitSelf.LOGGER.info("IPInfo response: '{}'", city);
        return city.isEmpty() ? "Unknown" : city;
    }

    public static String main(String[] args) {
        try {
            CityLocator geoLocation = new CityLocator();
            return geoLocation.getCityWithFallback();

        } catch (Exception e) {
            SplitSelf.LOGGER.error("Failed to get city location: {}", e.getMessage());
        }
        return null;
    }
}
package com.pryzmm.splitself.file;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import java.io.*;
import java.net.InetAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class CityLocator {
    private DatabaseReader reader;

    public CityLocator() throws Exception {
        String resourcePath = "/data/splitself/geoip/GeoLite2-City.mmdb";
        InputStream dbStream = getClass().getResourceAsStream(resourcePath);

        if (dbStream == null) {
            throw new FileNotFoundException("GeoIP Database file not found in resources: " + resourcePath);
        }

        this.reader = new DatabaseReader.Builder(dbStream).build();
    }

    public String getUserPublicIP() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://checkip.amazonaws.com"))
                .build();

        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new Exception("Failed to get public IP: " + response.statusCode());
        }

        return response.body().trim();
    }

    public String getCityFromIP(String ipAddress) throws Exception {
        try {
            InetAddress ip = InetAddress.getByName(ipAddress);
            CityResponse response = reader.city(ip);

            String city = response.getCity().getName();
            String state = response.getMostSpecificSubdivision().getName();
            String country = response.getCountry().getName();

            // Return city, or fallback to state/country if city is unknown
            if (city != null && !city.isEmpty()) {
                return city;
            } else if (state != null && !state.isEmpty()) {
                return state + ", " + country;
            } else {
                return country != null ? country : "Unknown";
            }

        } catch (GeoIp2Exception e) {
            throw new Exception("IP address not found in database: " + ipAddress);
        }
    }

    public void close() throws IOException {
        if (reader != null) {
            reader.close();
        }
    }

    public static String main(String[] args) {
        try {
            CityLocator geoLocation = new CityLocator();
            String publicIP = geoLocation.getUserPublicIP();
            return geoLocation.getCityFromIP(publicIP);

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}
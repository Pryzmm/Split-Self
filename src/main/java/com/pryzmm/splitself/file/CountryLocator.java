package com.pryzmm.splitself.file;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;


// Okay... I tested the previews version of my implementation and realised that it wouldn't work for everyone
// Considering that some people use OS in english, besides playing in their language.
// So... I tried to understand the code for CityLocator and sort of did a way to make a CountryLocator
// I modified a bit to make sure this won't in any way affect the performance when loading Minecraft.
public final class CountryLocator {
    private CountryLocator() {}

    private static volatile String LAST_ERROR    = null;
    private static volatile String LAST_ENDPOINT = null;
    private static volatile String LAST_RESPONSE = null;

    public static CompletableFuture<Optional<String>> getCountryCodeAsync() {
        return CompletableFuture.supplyAsync(() -> {
            resetDiag();
            try {
                String cc = fetchOnce("http://ip-api.com/line/?fields=countryCode"); // 1ª tentativa
                if (cc == null || cc.isBlank()) {
                    cc = fetchOnce("https://ipinfo.io/country");                     // fallback
                }
                if (cc != null) {
                    cc = cc.trim();
                    if (!cc.isEmpty() && cc.length() <= 3) {
                        return Optional.of(cc.toUpperCase(Locale.ROOT));
                    }
                }
            } catch (Exception e) {
                LAST_ERROR = e.getClass().getSimpleName() + ": " + e.getMessage();
            }
            return Optional.empty();
        });
    }

    private static String fetchOnce(String url) {
        LAST_ENDPOINT = url;
        HttpURLConnection conn = null;
        BufferedReader br = null;
        try {
            conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);

            int code = conn.getResponseCode();
            if (code != 200) {
                LAST_ERROR = "HTTP " + code;
                return null;
            }

            br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line = br.readLine();
            LAST_RESPONSE = line;
            return line;
        } catch (Exception e) {
            LAST_ERROR = e.getClass().getSimpleName() + ": " + e.getMessage();
            return null;
        } finally {
            try { if (br != null) br.close(); } catch (Exception ignored) {}
            if (conn != null) conn.disconnect();
        }
    }

    private static void resetDiag() {
        LAST_ERROR = null;
        LAST_ENDPOINT = null;
        LAST_RESPONSE = null;
    }

    // These are utils to inform errors
    public static Optional<String> lastError()    { return Optional.ofNullable(LAST_ERROR); }
    public static Optional<String> lastEndpoint() { return Optional.ofNullable(LAST_ENDPOINT); }
    public static Optional<String> lastResponse() { return Optional.ofNullable(LAST_RESPONSE); }
}

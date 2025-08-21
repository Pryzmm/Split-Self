package com.pryzmm.splitself.events;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class AshconNameAPI {
    
    // Unfortunately, this will only work for a handful of players.
    // I don't think there's a way around this, since api.ashcon.app doesn't show everyone's previous names.
    // And since Mojang is a little silly and has disabled their API for name history, this is the best I can do.
    
    public static List<String> getNameHistory(String uuid) throws IOException {
        List<String> names = new ArrayList<>();
        URL url = URI.create("https://api.ashcon.app/mojang/v2/user/" + uuid).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            JsonObject json = JsonParser.parseString(response.toString()).getAsJsonObject();
            JsonArray username_history = json.getAsJsonArray("username_history");
            for (int i = 0; i < username_history.size(); i++) {
                JsonObject entry = username_history.get(i).getAsJsonObject();
                names.add(entry.get("username").getAsString());
            }
        }
        return names;
    }
}

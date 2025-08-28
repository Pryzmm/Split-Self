package com.pryzmm.splitself.file;

import com.google.gson.*;
import com.pryzmm.splitself.config.DefaultConfig;
import net.fabricmc.loader.api.FabricLoader;
import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class JsonReader {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final Path configPath;
    private static JsonObject jsonObject;

    public JsonReader(String fileName) {
        this.configPath = FabricLoader.getInstance().getConfigDir().resolve(fileName);
        loadOrCreateConfig();
    }

    private void loadOrCreateConfig() {
        if (Files.exists(configPath)) {
            try (FileReader reader = new FileReader(configPath.toFile())) {
                JsonElement element = JsonParser.parseReader(reader);
                if (element.isJsonObject()) {
                    jsonObject = element.getAsJsonObject();
                    fillMissingDefaults();
                } else {
                    createDefaultConfig();
                }
            } catch (Exception e) {
                System.err.println("Error reading config file, creating new one: " + e.getMessage());
                createDefaultConfig();
            }
        } else {
            createDefaultConfig();
        }
    }

    public static Set<String> getAllKeys() {
        return jsonObject.keySet();
    }

    // Get all keys as a List (if you prefer List over Set)
    public static List<String> getAllKeysAsList() {
        return new ArrayList<>(jsonObject.keySet());
    }

    // Get all keys from a nested JSON object
    public static Set<String> getKeysFromObject(String objectKey) {
        if (jsonObject.has(objectKey) && jsonObject.get(objectKey).isJsonObject()) {
            return jsonObject.getAsJsonObject(objectKey).keySet();
        }
        return new HashSet<>();
    }

    // Get size of an array
    public static int getArraySize(String arrayKey) {
        if (jsonObject.has(arrayKey) && jsonObject.get(arrayKey).isJsonArray()) {
            return jsonObject.getAsJsonArray(arrayKey).size();
        }
        return 0;
    }

    // Get all values from a string array (if that's what you meant)
    public static List<String> getStringArray(String arrayKey) {
        List<String> result = new ArrayList<>();
        if (jsonObject.has(arrayKey) && jsonObject.get(arrayKey).isJsonArray()) {
            JsonArray array = jsonObject.getAsJsonArray(arrayKey);
            for (JsonElement element : array) {
                if (element.isJsonPrimitive()) {
                    result.add(element.getAsString());
                }
            }
        }
        return result;
    }

    private void createDefaultConfig() {
        jsonObject = new JsonObject();
        copyDefaultsToJson();
        saveConfig();
    }

    private void fillMissingDefaults() {
        boolean hasChanges = false;
        Field[] fields = DefaultConfig.class.getDeclaredFields();
        for (Field field : fields) {
            if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                field.setAccessible(true);
                try {
                    String fieldName = field.getName();
                    Object defaultValue = field.get(null);

                    if (!jsonObject.has(fieldName)) {
                        addValueToJson(fieldName, defaultValue);
                        hasChanges = true;
                    } else if (defaultValue instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> defaultMap = (Map<String, Object>) defaultValue;
                        JsonObject existingMapJson = jsonObject.getAsJsonObject(fieldName);
                        for (Map.Entry<String, Object> entry : defaultMap.entrySet()) {
                            if (!existingMapJson.has(entry.getKey())) {
                                addValueToJson(existingMapJson, entry.getKey(), entry.getValue());
                                hasChanges = true;
                            }
                        }
                    }
                } catch (IllegalAccessException e) {
                    System.err.println("Could not access field: " + field.getName());
                }
            }
        }
        if (hasChanges) {
            saveConfig();
        }
    }

    private void copyDefaultsToJson() {
        Field[] fields = DefaultConfig.class.getDeclaredFields();
        for (Field field : fields) {
            if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                field.setAccessible(true);
                try {
                    String fieldName = field.getName();
                    Object value = field.get(null);
                    addValueToJson(fieldName, value);
                } catch (IllegalAccessException e) {
                    System.err.println("Could not access field: " + field.getName());
                }
            }
        }
    }

    private void addValueToJson(String key, Object value) {
        addValueToJson(jsonObject, key, value);
    }

    private void addValueToJson(JsonObject target, String key, Object value) {
        if (value == null) {
            target.add(key, JsonNull.INSTANCE);
        } else if (value instanceof String) {
            target.addProperty(key, (String) value);
        } else if (value instanceof Number) {
            target.addProperty(key, (Number) value);
        } else if (value instanceof Boolean) {
            target.addProperty(key, (Boolean) value);
        } else if (value instanceof Map) {
            JsonObject mapObject = new JsonObject();
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) value;
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                addValueToJson(mapObject, entry.getKey(), entry.getValue());
            }
            target.add(key, mapObject);
        } else {
            target.add(key, GSON.toJsonTree(value));
        }
    }

    public void saveConfig() {
        try {
            Files.createDirectories(configPath.getParent());
            try (FileWriter writer = new FileWriter(configPath.toFile())) {
                GSON.toJson(jsonObject, writer);
            }
        } catch (IOException e) {
            System.err.println("Error saving config file: " + e.getMessage());
        }
    }

    public static String getString(String key) {
        return getString(key, "");
    }

    public static String getString(String key, String defaultValue) {
        if (jsonObject.has(key) && !jsonObject.get(key).isJsonNull()) {
            return jsonObject.get(key).getAsString();
        }
        return defaultValue;
    }

    public static int getInt(String key) {
        return getInt(key, 0);
    }

    public static int getInt(String key, int defaultValue) {
        if (jsonObject.has(key) && !jsonObject.get(key).isJsonNull()) {
            return jsonObject.get(key).getAsInt();
        }
        return defaultValue;
    }

    public static double getDouble(String key) {
        return getDouble(key, 0.0);
    }

    public static double getDouble(String key, double defaultValue) {
        if (jsonObject.has(key) && !jsonObject.get(key).isJsonNull()) {
            return jsonObject.get(key).getAsDouble();
        }
        return defaultValue;
    }

    public static boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        if (jsonObject.has(key) && !jsonObject.get(key).isJsonNull()) {
            return jsonObject.get(key).getAsBoolean();
        }
        return defaultValue;
    }

    @SuppressWarnings("unchecked")
    public <T> Map<String, T> getMap(String key, Class<T> valueType) {
        if (jsonObject.has(key) && jsonObject.get(key).isJsonObject()) {
            JsonObject mapObject = jsonObject.getAsJsonObject(key);
            Map<String, T> result = new HashMap<>();
            for (Map.Entry<String, JsonElement> entry : mapObject.entrySet()) {
                JsonElement element = entry.getValue();
                T value = null;
                if (valueType == String.class && element.isJsonPrimitive()) {
                    value = (T) element.getAsString();
                } else if (valueType == Integer.class && element.isJsonPrimitive()) {
                    value = (T) Integer.valueOf(element.getAsInt());
                } else if (valueType == Boolean.class && element.isJsonPrimitive()) {
                    value = (T) Boolean.valueOf(element.getAsBoolean());
                } else if (valueType == Double.class && element.isJsonPrimitive()) {
                    value = (T) Double.valueOf(element.getAsDouble());
                }
                if (value != null) {
                    result.put(entry.getKey(), value);
                }
            }
            return result;
        }
        return new HashMap<>();
    }

    public static Object getValueFromArray(String arrayID, String configKey) {
        if (jsonObject.has(arrayID) && jsonObject.get(arrayID).isJsonObject()) {
            JsonObject array = jsonObject.getAsJsonObject(arrayID);
            if (array.has(configKey)) {
                return array.get(configKey);
            }
        }
        return 0;
    }

    public static Boolean getBooleanFromArray(String arrayID, String configKey) {
        if (jsonObject.has(arrayID) && jsonObject.get(arrayID).isJsonObject()) {
            JsonObject array = jsonObject.getAsJsonObject(arrayID);
            if (array.has(configKey)) {
                return array.get(configKey).getAsBoolean();
            }
        }
        return false;
    }

    public static int getIntFromArray(String arrayKey, int index) {
        return getIntFromArray(arrayKey, index, 0);
    }

    public static int getIntFromArray(String arrayKey, int index, int defaultValue) {
        if (jsonObject.has(arrayKey) && jsonObject.get(arrayKey).isJsonArray()) {
            JsonArray array = jsonObject.getAsJsonArray(arrayKey);
            if (index >= 0 && index < array.size()) {
                return array.get(index).getAsInt();
            }
        }
        return defaultValue;
    }

    public void setInt(String key, int value) {
        jsonObject.addProperty(key, value);
    }

    public void setDouble(String key, double value) {
        jsonObject.addProperty(key, value);
    }

    public void setBoolean(String key, boolean value) {
        jsonObject.addProperty(key, value);
    }

    public <T> void setMap(String key, Map<String, T> map) {
        JsonObject mapObject = new JsonObject();
        for (Map.Entry<String, T> entry : map.entrySet()) {
            addValueToJson(mapObject, entry.getKey(), entry.getValue());
        }
        jsonObject.add(key, mapObject);
    }

    public void setIntInObject(String arrayKey, String configKey, int value) {
        JsonObject object = getOrCreateObject(arrayKey);
        object.addProperty(configKey, value);
    }

    public void setDoubleInObject(String arrayKey, String configKey, double value) {
        JsonObject object = getOrCreateObject(arrayKey);
        object.addProperty(configKey, value);
    }

    public void setBooleanInObject(String arrayKey, String configKey, boolean value) {
        JsonObject object = getOrCreateObject(arrayKey);
        object.addProperty(configKey, value);
    }

    private JsonObject getOrCreateObject(String key) {
        if (!jsonObject.has(key) || !jsonObject.get(key).isJsonObject()) {
            jsonObject.add(key, new JsonObject());
        }
        return jsonObject.getAsJsonObject(key);
    }

    public void addStringToArray(String arrayKey, String value) {
        JsonArray array = getOrCreateArray(arrayKey);
        array.add(value);
    }

    public void addIntToArray(String arrayKey, int value) {
        JsonArray array = getOrCreateArray(arrayKey);
        array.add(value);
    }

    private JsonArray getOrCreateArray(String key) {
        if (!jsonObject.has(key) || !jsonObject.get(key).isJsonArray()) {
            jsonObject.add(key, new JsonArray());
        }
        return jsonObject.getAsJsonArray(key);
    }

    private void ensureArraySize(JsonArray array, int minSize) {
        while (array.size() < minSize) {
            array.add(JsonNull.INSTANCE);
        }
    }

    public boolean hasKey(String key) {
        return jsonObject.has(key);
    }

    public void removeKey(String key) {
        jsonObject.remove(key);
    }

    public JsonObject getJsonObject() {
        return jsonObject;
    }

    public String toJsonString() {
        return GSON.toJson(jsonObject);
    }
}
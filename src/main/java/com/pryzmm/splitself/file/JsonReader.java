package com.pryzmm.splitself.file;

import com.google.gson.*;
import com.pryzmm.splitself.SplitSelf;
import com.pryzmm.splitself.config.DefaultConfig;
import com.pryzmm.splitself.data.Location;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class JsonReader {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final Path configPath;
    private JsonObject jsonObject;
    private final boolean loadData;

    public JsonReader(String fileName, boolean loadData) {
        this.configPath = FabricLoader.getInstance().getConfigDir().resolve(fileName);
        this.loadData = loadData;
        loadOrCreateData();
    }

    public JsonReader(File file) {
        this.configPath = file.toPath();
        this.loadData = false;
        try {
            JsonElement element = JsonParser.parseReader(new FileReader(configPath.toFile()));
            if (element.isJsonObject()) jsonObject = element.getAsJsonObject();
            else jsonObject = new JsonObject();
        } catch (FileNotFoundException e) {
            SplitSelf.LOGGER.warn("Could not find data file?");
        }
    }

    public JsonReader(File file, boolean loadData) {
        this.configPath = file.toPath();
        this.loadData = loadData;
        loadOrCreateData();
    }

    private void loadOrCreateData() {
        if (Files.exists(configPath)) {
            try (FileReader reader = new FileReader(configPath.toFile())) {
                JsonElement element = JsonParser.parseReader(reader);
                jsonObject = element.getAsJsonObject();
                if (loadData) {
                    if (element.isJsonObject()) {
                        fillMissingDefaults();
                        removeNonDefaultKeys();
                    } else createDefaultConfig();
                }
            } catch (Exception e) {
                System.err.println("Error reading config file, creating new one: " + e.getMessage());
                if (loadData) createDefaultConfig();
            }
        } else {
            if (loadData) createDefaultConfig();
        }
    }

    public Set<String> getKeysFromObject(String objectKey) {
        if (jsonObject.has(objectKey) && jsonObject.get(objectKey).isJsonObject()) {
            return jsonObject.getAsJsonObject(objectKey).keySet();
        }
        return new HashSet<>();
    }

    private void createDefaultConfig() {
        jsonObject = new JsonObject();
        copyDefaultsToJson();
        save();
    }

    private void fillMissingDefaults() {
        boolean hasChanges = false;
        Field[] fields = DefaultConfig.class.getDeclaredFields();
        for (Field field : fields) {
            if (Modifier.isStatic(field.getModifiers())) {
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
            save();
        }
    }

    private void removeNonDefaultKeys() {
        boolean hasChanges = false;
        Set<String> validKeys = new HashSet<>();
        Field[] fields = DefaultConfig.class.getDeclaredFields();
        for (Field field : fields) {
            if (Modifier.isStatic(field.getModifiers())) {
                validKeys.add(field.getName());
            }
        }
        Set<String> keysToRemove = new HashSet<>();
        for (String key : jsonObject.keySet()) {
            if (!validKeys.contains(key)) {
                keysToRemove.add(key);
                hasChanges = true;
            }
        }
        for (String key : keysToRemove) {
            jsonObject.remove(key);
            System.out.println("Removed invalid config key: " + key);
        }
        for (Field field : fields) {
            if (Modifier.isStatic(field.getModifiers())) {
                field.setAccessible(true);
                try {
                    String fieldName = field.getName();
                    Object defaultValue = field.get(null);
                    if (defaultValue instanceof Map && jsonObject.has(fieldName)
                            && jsonObject.get(fieldName).isJsonObject()) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> defaultMap = (Map<String, Object>) defaultValue;
                        JsonObject configMapJson = jsonObject.getAsJsonObject(fieldName);
                        Set<String> invalidMapKeys = new HashSet<>();
                        for (String configKey : configMapJson.keySet()) {
                            if (!defaultMap.containsKey(configKey)) {
                                invalidMapKeys.add(configKey);
                                hasChanges = true;
                            }
                        }
                        for (String invalidKey : invalidMapKeys) {
                            configMapJson.remove(invalidKey);
                            System.out.println("Removed invalid key '" + invalidKey
                                    + "' from " + fieldName);
                        }
                    }
                } catch (IllegalAccessException e) {
                    System.err.println("Could not access field: " + field.getName());
                }
            }
        }
        if (hasChanges) {
            save();
            System.out.println("Config cleaned up - removed non-default keys");
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

    @SuppressWarnings("unchecked")
    private void addValueToJson(JsonObject target, String key, Object value) {
        switch (value) {
            case null -> target.add(key, JsonNull.INSTANCE);
            case String s -> target.addProperty(key, s);
            case Number n -> target.addProperty(key, n);
            case Boolean b -> target.addProperty(key, b);
            case Map<?, ?> m -> {
                JsonObject mapObject = new JsonObject();
                Map<String, Object> map = (Map<String, Object>) m;
                for (Map.Entry<String, Object> entry : map.entrySet()) addValueToJson(mapObject, entry.getKey(), entry.getValue());
                target.add(key, mapObject);
            }
            case List<?> l -> {
                JsonArray array = new JsonArray();
                for (Object item : l) array.add(GSON.toJsonTree(item));
                target.add(key, array);
            }
            default -> target.add(key, GSON.toJsonTree(value));
        }
    }

    public void save() {
        try {
            Files.createDirectories(configPath.getParent());
            try (FileWriter writer = new FileWriter(configPath.toFile())) {
                GSON.toJson(jsonObject, writer);
            }
        } catch (IOException e) {
            System.err.println("Error saving config file: " + e.getMessage());
        }
    }

    public String getString(String key) {
        return getString(key, "");
    }

    public String getString(String key, String defaultValue) {
        if (jsonObject.has(key) && !jsonObject.get(key).isJsonNull()) {
            return jsonObject.get(key).getAsString();
        }
        return defaultValue;
    }

    public int getInt(String key, int defaultValue) {
        if (jsonObject.has(key) && !jsonObject.get(key).isJsonNull()) {
            return jsonObject.get(key).getAsInt();
        }
        return defaultValue;
    }

    public double getDouble(String key, double defaultValue) {
        if (jsonObject.has(key) && !jsonObject.get(key).isJsonNull()) {
            return jsonObject.get(key).getAsDouble();
        }
        return defaultValue;
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        if (jsonObject.has(key) && !jsonObject.get(key).isJsonNull()) {
            return jsonObject.get(key).getAsBoolean();
        }
        return defaultValue;
    }

    public Location getLocation(String key, Location defaultValue) {
        if (jsonObject.has(key) && !jsonObject.get(key).isJsonNull()) {
            List<String> values = Arrays.stream(jsonObject.get(key).getAsString().split(";")).toList();
            MinecraftServer server = MinecraftClient.getInstance().getServer();
            if (server != null) {
                RegistryKey<World> worldKey = RegistryKey.of(RegistryKeys.WORLD, Identifier.of(values.get(0)));
                ServerWorld world = server.getWorld(worldKey);
                if (world != null) {
                    Vec3d pos = new Vec3d(
                        Double.parseDouble(values.get(1)),
                        Double.parseDouble(values.get(2)),
                        Double.parseDouble(values.get(3))
                    );
                    float yaw   = Float.parseFloat(values.get(4));
                    float pitch = Float.parseFloat(values.get(5));
                    return new Location(world, pos, yaw, pitch);
                }
            }
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

    public Object getValueFromArray(String arrayID, String configKey) {
        if (jsonObject.has(arrayID) && jsonObject.get(arrayID).isJsonObject()) {
            JsonObject array = jsonObject.getAsJsonObject(arrayID);
            if (array.has(configKey)) {
                return array.get(configKey);
            }
        }
        return 0;
    }

    public Boolean getBooleanFromArray(String arrayID, String configKey) {
        if (jsonObject.has(arrayID) && jsonObject.get(arrayID).isJsonObject()) {
            JsonObject array = jsonObject.getAsJsonObject(arrayID);
            if (array.has(configKey)) {
                return array.get(configKey).getAsBoolean();
            }
        }
        return false;
    }

    public List<String> getStringList(String key) {
        if (jsonObject.has(key) && jsonObject.get(key).isJsonArray()) {
            List<String> result = new ArrayList<>();
            for (JsonElement element : jsonObject.getAsJsonArray(key)) {
                result.add(element.getAsString());
            }
            return result;
        }
        return new ArrayList<>();
    }

    public List<UUID> getUUIDList(String key) {
        if (jsonObject.has(key) && jsonObject.get(key).isJsonArray()) {
            List<UUID> result = new ArrayList<>();
            for (JsonElement element : jsonObject.getAsJsonArray(key)) {
                result.add(UUID.fromString(element.getAsString()));
            }
            return result;
        }
        return new ArrayList<>();
    }

    public void setInt(String key, int value) {
        jsonObject.addProperty(key, value);
    }

    public void setDouble(String key, double value) {
        jsonObject.addProperty(key, value);
    }

    public void setString(String key, String value) {
        jsonObject.addProperty(key, value);
    }

    public void setBoolean(String key, boolean value) {
        jsonObject.addProperty(key, value);
    }

    public void setLocation(String key, Location value) {
        jsonObject.addProperty(key, value.toString());
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

    public void setStringList(String key, List<String> values) {
        JsonArray array = new JsonArray();
        for (String value : values) array.add(value);
        jsonObject.add(key, array);
    }

    public void setUUIDList(String key, List<UUID> values) {
        JsonArray array = new JsonArray();
        for (UUID value : values) array.add(value.toString());
        jsonObject.add(key, array);
    }

    private JsonObject getOrCreateObject(String key) {
        if (!jsonObject.has(key) || !jsonObject.get(key).isJsonObject()) {
            jsonObject.add(key, new JsonObject());
        }
        return jsonObject.getAsJsonObject(key);
    }
}
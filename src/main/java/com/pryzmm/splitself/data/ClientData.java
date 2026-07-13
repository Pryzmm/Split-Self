package com.pryzmm.splitself.data;

import com.pryzmm.splitself.file.JsonReader;
import net.minecraft.client.MinecraftClient;
import java.io.File;

public class ClientData {

    private static boolean PII;
    static { clearData(); }

    private static JsonReader reader = null;

    public static boolean isLoaded() {
        return reader != null;
    }

    public static boolean getPII() { return PII; }

    public static void setPII(boolean value) {
        PII = value;
        reader.setBoolean("pii", value);
        reader.save();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static File getCurrentData(MinecraftClient client) {
        File dir = new File(client.runDirectory, "data");
        if (!dir.exists()) dir.mkdirs();
        return new File(dir, "splitselfClient.json");
    }

    public static void clearData() {
        PII = false;
    }

    public static void loadData(MinecraftClient client) {
        File data = getCurrentData(client);
        reader = new JsonReader(data);
        PII = reader.getBoolean("pii", false);
        reader.save();
    }

}

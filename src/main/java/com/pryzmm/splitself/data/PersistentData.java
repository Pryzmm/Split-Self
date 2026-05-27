package com.pryzmm.splitself.data;

import com.pryzmm.splitself.SplitSelf;
import com.pryzmm.splitself.file.JsonReader;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.Nullable;
import java.io.File;
import java.io.IOException;

public class PersistentData {

    private static String panoramaStage;

    private static final JsonReader reader;
    static {
        if (getCurrentData() == null) reader = null;
        else reader = new JsonReader(getCurrentData());
    }

    public static String getPanoramaStage() { return panoramaStage; }

    public static void setPanoramaStage(String value) {
        panoramaStage = value;
        reader.setString("panoramaStage", value);
        reader.save();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static @Nullable File getCurrentData() {
        File file = FabricLoader.getInstance().getConfigDir().resolve(SplitSelf.MOD_ID + "persistent.json").toFile();
        try { file.createNewFile(); } catch (IOException e) { return null; }
        return file;
    }

    public static void loadData() {
        panoramaStage = reader.getString("panoramaStage", "main");
        reader.save();
    }

}

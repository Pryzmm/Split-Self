package com.pryzmm.splitself.forge;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** Local stage, using the pinned ClientData file/key without changing its other fields. */
final class PanoramaState {
    private final Path path;
    private String stage = "main";

    PanoramaState(Path path) throws IOException {
        this.path = path;
        JsonObject data = read();
        if (data.has("panoramaStage") && data.get("panoramaStage").isJsonPrimitive()
                && "empty".equals(data.get("panoramaStage").getAsString())) stage = "empty";
    }

    String stage() { return stage; }

    void completeTransition() throws IOException {
        setStage("empty");
    }

    void resetMain() throws IOException {setStage("main");}

    private void setStage(String value) throws IOException {
        stage = value;
        JsonObject data = read();
        data.addProperty("panoramaStage", stage);
        Files.createDirectories(path.toAbsolutePath().getParent());
        Files.writeString(path, data.toString(), StandardCharsets.UTF_8);
    }

    private JsonObject read() throws IOException {
        if (!Files.exists(path)) return new JsonObject();
        // Malformed files are reported by the caller and left untouched.
        return JsonParser.parseString(Files.readString(path, StandardCharsets.UTF_8)).getAsJsonObject();
    }
}

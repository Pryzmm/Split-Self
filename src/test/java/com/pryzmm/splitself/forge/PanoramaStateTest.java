package com.pryzmm.splitself.forge;

import java.nio.file.Files;
import java.nio.file.Path;
import com.google.gson.JsonParser;

public final class PanoramaStateTest {
    private static int checks;
    private static void check(boolean value, String name) {
        if (!value) throw new AssertionError(name);
        checks++;
    }
    public static void main(String[] args) throws Exception {
        Path root = Files.createTempDirectory("splitself-panorama-test-");
        Path file = root.resolve("config/splitselfClient.json");
        PanoramaState initial = new PanoramaState(file);
        check(initial.stage().equals("main"), "default matches ClientData");
        check(!Files.exists(file), "read does not create configuration");
        initial.completeTransition();
        check(initial.stage().equals("empty"), "successful transition changes in-memory stage");
        check(new PanoramaState(file).stage().equals("empty"), "stage survives new instance/restart");
        Files.writeString(file, "{\"pii\":false,\"other\":\"keep\",\"panoramaStage\":\"main\"}");
        new PanoramaState(file).completeTransition();
        var data = JsonParser.parseString(Files.readString(file)).getAsJsonObject();
        check(!data.get("pii").getAsBoolean(), "PII opt-in is not changed");
        check(data.get("other").getAsString().equals("keep"), "unrelated fields survive");
        check(data.get("panoramaStage").getAsString().equals("empty"), "original JSON key");
        Files.writeString(file, "{\"panoramaStage\":\"../../unknown\"}");
        check(new PanoramaState(file).stage().equals("main"), "unrecognized stages cannot select arbitrary paths");
        Files.writeString(file, "broken");
        boolean rejected = false;
        try { new PanoramaState(file); } catch (RuntimeException expected) { rejected = true; }
        check(rejected, "malformed data is reported");
        check(Files.readString(file).equals("broken"), "malformed data left intact");
        System.out.println("PanoramaStateTest PASS: " + checks + " assertions; fixtures: " + root);
    }
}

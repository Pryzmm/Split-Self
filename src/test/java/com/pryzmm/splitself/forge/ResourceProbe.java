package com.pryzmm.splitself.forge;

import com.google.gson.JsonParser;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import net.minecraft.nbt.*;

/** Validates the checked-in resources without a private reference or a running game. */
public final class ResourceProbe {
    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }

    static CompoundTag template(Path file) throws Exception {
        CompoundTag data = NbtIo.readCompressed(file.toFile());
        require(data.getInt("DataVersion") == 3465, "1.20.1 DataVersion: " + file);
        var size = data.getList("size", Tag.TAG_INT);
        require(size.size() == 3, "Three structure dimensions: " + file);
        for (int i = 0; i < 3; i++) require(size.getInt(i) > 0, "Positive structure size: " + file);
        var palette = data.getList("palette", Tag.TAG_COMPOUND);
        require(!palette.isEmpty(), "Nonempty palette: " + file);
        for (Tag tag : palette) {
            String name = ((CompoundTag) tag).getString("Name");
            require(name.matches("[a-z0-9_.-]+:[a-z0-9_/.-]+"), "Valid palette ID: " + name);
            require(!name.equals("minecraft:pale_oak_planks") && !name.equals("minecraft:creaking_heart"),
                    "No newer blocks: " + name);
        }
        var blocks = data.getList("blocks", Tag.TAG_COMPOUND);
        require(!blocks.isEmpty(), "Nonempty template: " + file);
        for (Tag tag : blocks) {
            CompoundTag block = (CompoundTag) tag;
            int state = block.getInt("state");
            require(state >= 0 && state < palette.size(), "Palette index in range: " + file);
            var pos = block.getList("pos", Tag.TAG_INT);
            require(pos.size() == 3, "Three block coordinates: " + file);
            for (int i = 0; i < 3; i++) require(pos.getInt(i) >= 0 && pos.getInt(i) < size.getInt(i),
                    "Block inside structure bounds: " + file);
        }
        // The six es_* templates were item-converted during the port. Other
        // original templates are retained as-is; see the README limitations.
        if (EmptyStructureRules.NAMES.contains(file.getFileName().toString().replace(".nbt", ""))) validateItems(data);
        return data;
    }

    private static void validateItems(Tag node) {
        if (node instanceof ListTag list) { for (Tag item : list) validateItems(item); }
        if (!(node instanceof CompoundTag data)) return;
        require(!data.contains("components"), "No unconverted item components");
        if (data.contains("id", Tag.TAG_STRING) && (data.contains("count") || data.contains("Count"))) {
            require(!data.contains("count") && data.contains("Count", Tag.TAG_BYTE), "Legacy item Count byte");
        }
        for (String key : data.getAllKeys()) validateItems(data.get(key));
    }

    public static void main(String[] args) throws Exception {
        Path resources = Path.of(args[0]).resolve("src/main/resources");
        int structures = 0;
        try (var paths = Files.walk(resources.resolve("data/splitself/structures"))) {
            for (Path file : paths.filter(p -> p.toString().endsWith(".nbt")).toList()) {
                template(file);
                structures++;
            }
        }
        require(structures >= EmptyStructureRules.NAMES.size() + 1, "All story templates present");
        var defaults = JsonParser.parseString(Files.readString(resources.resolve("data/splitself/event_defaults.json")))
                .getAsJsonObject().getAsJsonObject("events");
        require(new HashSet<>(defaults.keySet()).equals(PortedEvents.supported()), "Resource catalog matches all handlers");
        require(defaults.size() == 67, "67 event defaults");
        try (var paths = Files.walk(resources)) {
            for (Path file : paths.filter(p -> p.toString().endsWith(".json")).toList()) {
                require(JsonParser.parseString(Files.readString(file)) != null, "Valid JSON: " + file);
            }
        }
        System.out.println("ResourceProbe PASS: " + structures + " structure templates, JSON resources, 67 event handlers; no game execution");
    }
}

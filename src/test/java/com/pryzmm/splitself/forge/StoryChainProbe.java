package com.pryzmm.splitself.forge;

import java.io.File;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import com.google.gson.JsonParser;
import java.nio.file.Files;
import java.nio.file.Path;

/** Offline data and dialogue checks; does not start a server or accept EULA. */
public final class StoryChainProbe {
    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
    public static void main(String[] args) throws Exception {
        Path root = Path.of(args[0]);
        Path target = root.resolve("src/main/resources/data/splitself");
        var adapted = ResourceProbe.template(target.resolve("structures/final_escape.nbt"));
        require(adapted.getInt("DataVersion") == 3465, "1.20.1 DataVersion");
        var palette = adapted.getList("palette", Tag.TAG_COMPOUND);
        int lower = -1, upper = -1;
        for (int i = 0; i < palette.size(); i++) {
            var state = palette.getCompound(i);
            if (state.getString("Name").equals("splitself:exit_door")) {
                if (state.getCompound("Properties").getString("half").equals("lower")) lower = i;
                if (state.getCompound("Properties").getString("half").equals("upper")) upper = i;
            }
        }
        require(lower >= 0 && upper >= 0, "Both exit-door states exist");
        var blocks = adapted.getList("blocks", Tag.TAG_COMPOUND);
        int lowerCount = 0, upperCount = 0;
        for (int i = 0; i < blocks.size(); i++) {
            int state = blocks.getCompound(i).getInt("state");
            if (state == lower) lowerCount++;
            if (state == upper) upperCount++;
        }
        require(lowerCount > 0 && lowerCount == upperCount, "Door halves placed in template");
        for (String file : new String[]{"worldgen/structure/final_escape.json",
                "worldgen/structure_set/grass_empty_biome.json", "tags/worldgen/biome/is_grass_empty.json"}) {
            require(JsonParser.parseString(Files.readString(target.resolve(file))).isJsonObject(), "Valid worldgen resource: " + file);
        }
        for (String locale : new String[]{"en_us", "ru_ru"}) {
            var lang = StoryChat.language(locale);
            for (String prompt : new String[]{"control", "tethered", "whoAreYou", "whatDidIDo",
                    "whatDoYouWant", "whereAreYou", "oneLastTime", "freedom", "help", "absence", "hello"}) {
                String text = lang.get("chat.splitself.prompt." + prompt).getAsString();
                require(prompt.equals(StoryChat.responseKey(lang, "Tester", StoryChat.normalize(text))),
                        locale + " prompt " + prompt);
                require(lang.has("chat.splitself.forgottenResponse." + prompt), "Forgotten response " + prompt);
                require(lang.has("chat.splitself.response." + prompt), "Other response " + prompt);
            }
            require("nameConflict".equals(StoryChat.responseKey(lang, "Tester", "tEsTeR")), "Name conflict");
            require(StoryChat.responseKey(lang, "Tester", "unrecognized phrase") == null, "Unknown ignored");
            require(StoryChat.responseKey(lang, "Tester", " freedom ") == null, "Source does not trim");
        }
        require("freedom".equals(StoryChat.responseKey(StoryChat.language("en_us"), "Tester",
                StoryChat.normalize("FrEeDoM!?.."))), "Punctuation and case");
        require(StoryChat.language("../../invalid") == StoryChat.language("en_us"), "Locale fallback");
        System.out.println("PASS: template validity, exit halves, worldgen data, bilingual dialogue and fallback");
    }
}

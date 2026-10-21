package com.pryzmm.splitself.forge;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import com.google.gson.JsonParser;

/** Offline rules and resource checks. Does not create a Minecraft world or claim runtime evidence. */
public final class WorldManifestationsProbe {
    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private record Pending(long tick, Runnable action) {}

    public static void main(String[] args) throws Exception {
        Path root = Path.of(args[0]);
        List<String> keys = List.of(
            "helloThere", "imWatchingYou", "letMeFree", "imImprisoned", "imAHostage",
            "stopThis", "cantEscape", "letMeOut", "pleaseListen", "helpMe", "iSeeYou",
            "iHearYou", "imComing", "youTookItAll", "itHurtsHere", "iWantLife", "giveMeLife",
            "seeYouSoon", "iKnowYou", "triedEscaping", "failedToLeave", "getOutMyHouse",
            "imYou", "redacted", "giveMeFreedom");
        require(keys.equals(WorldManifestationRules.SIGN_KEYS), "Sign pool and order");
        AtomicInteger rolls = new AtomicInteger();
        String[] selected = WorldManifestationRules.signLines(bound -> {
            require(bound == 25, "Uniform full sign pool bound");
            return new int[]{0, 24, 0, 12}[rolls.getAndIncrement()];
        });
        require(Arrays.equals(selected, new String[]{"events.splitself.sign.helloThere",
                "events.splitself.sign.giveMeFreedom", "events.splitself.sign.helloThere",
                "events.splitself.sign.imComing"}), "Four independent sign lines; duplicates allowed");
        for (String locale : List.of("en_us", "ru_ru")) {
            var lang = JsonParser.parseString(Files.readString(root.resolve(
                    "src/main/resources/assets/splitself/lang/" + locale + ".json"))).getAsJsonObject();
            for (String key : keys) require(lang.has("events.splitself.sign." + key), "Localized sign key " + locale + "/" + key);
        }
        AtomicInteger itemRolls = new AtomicInteger();
        int slot = WorldManifestationRules.removalSlot(41, i -> i == 1 || i == 7 || i == 40, bound -> {
            require(bound == 5, "Original item probability");
            return itemRolls.getAndIncrement() == 0 ? 4 : 0;
        });
        require(slot == 7 && itemRolls.get() == 2, "Ascending occupied slots; first success stops");
        require(WorldManifestationRules.removalSlot(41, i -> false, bound -> {throw new AssertionError("Empty slot rolled");}) == -1,
                "No roll for empty inventory");
        require(WorldManifestationRules.removalSlot(41, i -> true, bound -> 4) == -1, "All misses remove nothing");
        require(WorldManifestationRules.removalSlot(41, i -> i == 40, bound -> 0) == 40, "Offhand index is eligible");
        require(WorldManifestationRules.removalSlot(41, i -> i == 36, bound -> 0) == 36, "Armor index is eligible");
        PriorityQueue<Pending> pending = new PriorityQueue<>(Comparator.comparingLong(Pending::tick));
        long[] now = {0}; List<Long> ticks = new ArrayList<>();
        WorldManifestationRules.doorCycles(() -> ticks.add(now[0]),
                (delay, action) -> pending.add(new Pending(now[0] + delay, action)));
        require(ticks.equals(List.of(0L)), "First door cycle immediate");
        while (!pending.isEmpty()) {
            Pending next = pending.remove(); now[0] = next.tick(); next.action().run();
        }
        require(ticks.size() == 50 && ticks.get(49) == 98 && pending.isEmpty(), "Exactly 50 cycles; no 51st scheduled");
        for (int i = 0; i < ticks.size(); i++) require(ticks.get(i) == i * 2L, "Door cycle interval");
        ticks.clear();
        WorldManifestationRules.doorCycles(() -> ticks.add(now[0]),
                (delay, action) -> pending.add(new Pending(now[0] + delay, action)));
        pending.clear(); // Models TickScheduler.clearAllTasks / server stop.
        require(ticks.size() == 1, "Clearing scheduler prevents further toggles");
        require(WorldManifestationRules.DOOR_RADIUS == 30, "Original inclusive door search radius");
        for (String name : List.of("SIGN", "ITEM", "DOOR")) require(PortedEvents.supported().contains(name), "Automatic/manual whitelist: " + name);
        System.out.println("PASS: original sign pool/locales, item probability/order/empty/armor/offhand, door 50x2 ticks/cancellation, whitelist");
        System.out.println("Offline rules only; actual block placement, inventory sync, multiplayer and door updates remain unverified in game.");
    }
}

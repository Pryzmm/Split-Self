package com.pryzmm.splitself.forge;

import java.util.List;
import java.util.Set;
import java.util.function.IntConsumer;

/** Source timing and layout, independent of render rate and Minecraft's client classes. */
final class SimpleEventRules {
    static final Set<String> CLIENT_EVENTS = Set.of("POEMSCREEN", "SCALE", "SCREENOVERLAY", "WHITESCREENOVERLAY", "RENAME", "PAUSE", "REDSKY", "INVERTCOLOR", "FACE");
    static final List<Integer> POEM_LINES = List.of(1, 2, 0, 3, 4, 0, 5, 6, 0, 7, 8, 0, 0, 0, 9);
    static final long BLACK_MS = 3877, WHITE_MS = 300, SCALE_MS = 5025;

    private SimpleEventRules() {}

    static final class Scale {
        private int next;
        void advance(long elapsedMs, IntConsumer step) {
            while (next <= 200 && elapsedMs >= next * 25L) step.accept(next++);
        }
    }

    static String poemName(String player, boolean personalInformation, String device, String redacted) {
        // EventManager.getName: aliases precede the PII switch; trailing space is intentional.
        String alias = switch (player.toLowerCase(java.util.Locale.ROOT)) {
            case "therealsquiddo" -> "Florence Ennay";
            case "skipthetutorial" -> "Aiden";
            case "failboat" -> "Daniel Michaud";
            case "jaym0ji" -> "James";
            case "xvivilly" -> "VIV";
            case "rekrap2" -> "Parker Jerry Marriott";
            case "dream" -> "Clay";
            case "itzmiai_21" -> "M1keyz";
            case "zachbealetv" -> "Zach Beale";
            case "cxlvxn" -> "Calvin9000";
            case "pufferfish81" -> "Puff";
            case "lord0wnage " -> "Swayle";
            default -> null;
        };
        return alias != null ? alias : personalInformation ? device : redacted;
    }
}

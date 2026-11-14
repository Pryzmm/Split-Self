package com.pryzmm.splitself.forge;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Checks finale timing with replacement-player fixtures; never closes a game. */
public final class FinaleDialogueProbe {
    private static int checks;
    private static void check(boolean value, String message) {
        checks++;
        if (!value) throw new AssertionError(message);
    }
    private static void finale() {
        UUID owner = UUID.randomUUID();
        var dialogue = new FinaleRules.Dialogue("DELETE", 100, owner);
        List<Integer> fired = new ArrayList<>();
        check(!dialogue.advance(129, owner, fired::add) && fired.isEmpty(), "No early finale callback");
        dialogue.advance(220, owner, fired::add);
        check(fired.equals(List.of(0, 1)), "Two story messages before dimension change");
        check(!dialogue.advance(330, null, fired::add), "Temporary missing LocalPlayer must retain finale");
        UUID replacementPlayer = UUID.fromString(owner.toString());
        dialogue.advance(370, replacementPlayer, fired::add);
        check(fired.equals(List.of(0, 1, 2)), "DELETE_HUM survives replacement player at tick 270");
        check(!dialogue.advance(1299, replacementPlayer, fired::add), "No close before 60 seconds");
        check(dialogue.advance(1300, replacementPlayer, fired::add), "Delete World closes at 60 seconds");
        check(fired.equals(List.of(0, 1, 2, 3)), "Close callback emitted exactly once");
        dialogue.advance(2000, replacementPlayer, fired::add);
        check(fired.size() == 4, "Completed dialogue does not repeat");
        check(new FinaleRules.Dialogue("DELETE", 0, owner).advance(270, UUID.randomUUID(), i -> {
            throw new AssertionError("Finale must not affect a different player");
        }), "Different player discards dialogue");
    }

    public static void main(String[] args) {
        finale();
        System.out.println("FinaleDialogueProbe PASS: " + checks + " assertions; replacement player, temporary absence, hum and 60-second close callbacks; no game execution");
    }
}

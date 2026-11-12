package com.pryzmm.splitself.forge;

import java.util.Set;
import java.util.stream.Collectors;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;

public final class CommandTreeProbe {
    public static void main(String[] args) {
        var dispatcher = new CommandDispatcher<CommandSourceStack>();
        SplitSelfCommands.registerCommands(dispatcher);
        var root = dispatcher.getRoot().getChild("splitself");
        var names = root.getChildren().stream().map(node -> node.getName()).collect(Collectors.toSet());
        if (!names.equals(Set.of("information", "runEvent", "debug", "runTests"))) throw new AssertionError("Unexpected root commands: " + names);
        var tests = root.getChild("runTests");
        var testNames = tests.getChildren().stream().map(node -> node.getName()).collect(Collectors.toSet());
        if (!testNames.equals(Set.of("testTnt", "testEvent", "testForgotten", "testBroken", "testTransition", "testSleep", "testFinale", "testSpeech"))) throw new AssertionError("Missing test commands: " + testNames);
        if (tests.getChild("testEvent").getChild("event") == null
                || tests.getChild("testSpeech").getChild("text") == null
                || tests.getChild("testSleep").getChild("stage") == null) throw new AssertionError("Test arguments lost");
        System.out.println("CommandTreeProbe PASS: original root groups and all eight test commands under runTests; no server or game execution");
    }
}

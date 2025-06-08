package com.pryzmm.splitself.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class MyCommands {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(CommandManager.literal("splitself")
                .executes(context -> {
                    context.getSource().sendFeedback(() -> Text.literal("Hello.").formatted(Formatting.YELLOW), false);
                    return 1;
                })
                .then(CommandManager.argument("text", StringArgumentType.word())
                        .executes(context -> {
                            String argument = StringArgumentType.getString(context, "text").toLowerCase();
                            if (argument.equals("warning")) {
                                context.getSource().sendFeedback(() -> Text.literal("Show warning."), false);
                            } else {
                                context.getSource().sendFeedback(() -> Text.literal("Invalid.").formatted(Formatting.RED), false);
                            }
                            return 1;
                        })));
    }
}
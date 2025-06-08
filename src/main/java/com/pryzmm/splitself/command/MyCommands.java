package com.pryzmm.splitself.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.pryzmm.splitself.screen.WarningScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class MyCommands {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(CommandManager.literal("splitself")
                .executes(context -> {
                    context.getSource().sendFeedback(() -> Text.literal("<" + context.getSource().getName() + "> You don't know yourself."), false);
                    return 1;
                })
                .then(CommandManager.argument("text", StringArgumentType.word())
                        .executes(context -> {
                            String argument = StringArgumentType.getString(context, "text").toLowerCase();
                            if (argument.equals("warning")) {
                                MinecraftClient client = MinecraftClient.getInstance();
                                client.execute(() -> client.setScreen(new WarningScreen()));
                            } else {
                                context.getSource().sendFeedback(() -> Text.literal("<" + context.getSource().getName() + "> ..."), false);
                            }
                            return 1;
                        })));
    }
}
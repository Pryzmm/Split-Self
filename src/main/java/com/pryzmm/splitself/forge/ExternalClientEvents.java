package com.pryzmm.splitself.forge;

import com.mojang.brigadier.arguments.BoolArgumentType;
import java.awt.Desktop;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import javax.swing.*;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Explicit local opt-in is checked again at the boundary of every external action. */
@Mod.EventBusSubscriber(modid = SplitSelfForge.MOD_ID, value = Dist.CLIENT)
public final class ExternalClientEvents {
    static final Set<String> SUPPORTED = ExternalConsent.EVENTS;
    private static final Set<JFrame> WINDOWS = ConcurrentHashMap.newKeySet();
    private static final ExecutorService WORKER = Executors.newSingleThreadExecutor(task -> {
        Thread thread = new Thread(task, "splitself-external"); thread.setDaemon(true); return thread;
    });
    private ExternalClientEvents() {}

    @SubscribeEvent public static void commands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("splitselfclient")
                .then(Commands.literal("settings").executes(context -> {
                    Minecraft client = Minecraft.getInstance();
                    client.execute(() -> ClientSettingsScreen.open(null));
                    return 1;
                }))
                .then(Commands.literal("external")
                        .executes(context -> {
                            report("External effects: " + ExternalConsent.enabled()); return 1;
                        })
                        .then(Commands.argument("enabled", BoolArgumentType.bool()).executes(context -> {
                            boolean enabled = BoolArgumentType.getBool(context, "enabled");
                            setEnabled(enabled);
                            report("External effects: " + enabled); return 1;
                        })))
                .then(Commands.literal("computerInteractions").executes(c->{report("Computer interactions: "+ExternalConsent.COMPUTER_INTERACTIONS.get());return 1;}).then(Commands.argument("enabled",BoolArgumentType.bool()).executes(c->{setComputerEnabled(BoolArgumentType.getBool(c,"enabled"));report("Computer interactions: "+ExternalConsent.COMPUTER_INTERACTIONS.get());return 1;})))
                .then(Commands.literal("microphone").then(Commands.argument("enabled",BoolArgumentType.bool()).executes(context->{ExternalConsent.MICROPHONE.set(BoolArgumentType.getBool(context,"enabled"));ExternalConsent.SPEC.save();report("Microphone: "+ExternalConsent.MICROPHONE.get()+" (requires external + personalInformation)");return 1;})))
                .then(Commands.literal("personalInformation")
                        .then(Commands.argument("enabled", BoolArgumentType.bool()).executes(context -> {
                            boolean enabled = BoolArgumentType.getBool(context, "enabled");
                            ExternalConsent.PERSONAL_INFORMATION.set(enabled); ExternalConsent.SPEC.save();
                            report("Personal information: " + enabled); return 1;
                        }))));
    }

    static void setEnabled(boolean enabled) {
        ExternalConsent.ENABLED.set(enabled);
        ExternalConsent.SPEC.save();
        if (!enabled) { closeWindows(); RemainingClientEvents.cleanup(); }
    }

    static void setComputerEnabled(boolean enabled){ExternalConsent.COMPUTER_INTERACTIONS.set(enabled);ExternalConsent.SPEC.save();ClientFrameImages.clear();if(!enabled){closeWindows();RemainingClientEvents.cleanup();}}
    static void setPersonalEnabled(boolean enabled){ExternalConsent.PERSONAL_INFORMATION.set(enabled);ExternalConsent.SPEC.save();if(!enabled)ClientFrameImages.clear();}
    static void setMicrophoneEnabled(boolean enabled){ExternalConsent.MICROPHONE.set(enabled);ExternalConsent.SPEC.save();}
    static void run(String name) {
        if (!EditionPolicy.eventAllowed(name)) return;
        if (EditionDesktopIntegrations.tryRun(name)) return;
        if (RemainingEventRules.EXTERNAL.contains(name)) { RemainingClientEvents.start(name); return; }
        if (BatchEventRules.EXTERNAL.contains(name)) { ExternalBatchEvents.run(name); return; }
        if (!ExternalConsent.computerAllowed()) { report("External event " + name + " skipped: local switch is OFF"); return; }
        var client = Minecraft.getInstance();
        if (client.player == null || !SUPPORTED.contains(name)) return;
        if (name.equals("CLIPBOARD")) {
            if (ExternalConsent.computerAllowed()) client.keyboardHandler.setClipboard(Component.translatable("events.splitself.clipboard").getString());
            return;
        }
        String playerName = client.player.getName().getString();
        String search = Component.translatable("events.splitself.search").getString();
        String logs = null;
        if (name.equals("LOGS")) {
            try (var input = ExternalClientEvents.class.getResourceAsStream("/data/splitself/saved_text/logs.txt")) {
                if (input == null) throw new java.io.FileNotFoundException("Missing logs.txt");
                logs = new String(input.readAllBytes(), StandardCharsets.UTF_8);
                for (int i = 1; i < 33; i++) logs = logs.replaceFirst("files.splitself.log.message" + i,
                        java.util.regex.Matcher.quoteReplacement(Component.translatable("files.splitself.log.message" + i).getString()));
                logs = logs.replace("PLAYERNAME", playerName);
            } catch (Exception failure) { report("LOGS failed: " + failure.getMessage()); return; }
        }
        final String logText = logs;
        if (name.equals("PLAYERDATA")) {
            client.player.sendSystemMessage(Component.translatable("events.splitself.playerData.1", playerName));
            var recipient = client.player;
            for (int line = 2; line <= 3; line++) {
                final int messageLine = line;
                CompletableFuture.delayedExecutor(line == 2 ? 5 : 11, TimeUnit.SECONDS).execute(() ->
                        client.execute(() -> {
                            if (ExternalConsent.computerAllowed() && client.player == recipient)
                                recipient.sendSystemMessage(Component.translatable("events.splitself.playerData." + messageLine, playerName));
                        }));
            }
        }
        WORKER.execute(() -> {
            if (!ExternalConsent.computerAllowed()) return;
            try {
                switch (name) {
                    case "SPOTIFY" -> open("spotify:track:5MkWlSmMZnSGHLYbK2LgdM");
                    case "SEARCH" -> open("https://www.google.com/search?q="
                            + java.net.URLEncoder.encode(search, StandardCharsets.UTF_8));
                    case "MEMORIES" -> copyToDesktop("textures/misc/memories.png");
                    case "MORSE" -> copyToDesktop("textures/misc/morse.png");
                    case "PLAYERDATA" -> copyToDesktop("files/ce7ea4cb-0789-47c9-b536-144f836a30c2.dat_old");
                    case "LOGS" -> {
                        Path destination = desktop().resolve("latest.log");
                        if (ExternalConsent.computerAllowed()) Files.writeString(destination, logText, StandardCharsets.UTF_8,
                                StandardOpenOption.CREATE_NEW);
                    }
                    default -> { }
                }
            } catch (Exception failure) {
                client.execute(() -> report("External event " + name + " failed: " + failure.getMessage()));
                org.slf4j.LoggerFactory.getLogger("splitself").warn("External event {} failed", name, failure);
            }
        });
    }

    static void writeStoryFile(String kind) {
        if(!ExternalConsent.computerAllowed())return;
        String title=Component.translatable("files.splitself."+kind+".title").getString()+".txt";
        String message=Component.translatable("files.splitself."+kind+".message").getString();
        WORKER.execute(()->{if(!ExternalConsent.computerAllowed())return;try{
            // Translated titles must remain single filenames, never paths.
            if(title.contains("/")||title.contains("\\")||title.contains(":"))throw new java.io.IOException("Invalid story filename");
            Path destination=desktop().resolve(title);if(ExternalConsent.computerAllowed())Files.writeString(destination,message,StandardCharsets.UTF_8,StandardOpenOption.CREATE_NEW);
        }catch(java.nio.file.FileAlreadyExistsException ignored){}catch(Exception failure){org.slf4j.LoggerFactory.getLogger("splitself").warn("Story file failed",failure);}});
    }
    private static void open(String uri) throws Exception {
        if (!ExternalConsent.computerAllowed()) return;
        DesktopLinks.open(uri);
    }

    private static Path desktop() throws Exception {
        if (System.getProperty("os.name").toLowerCase(Locale.ROOT).startsWith("win")) {
            Process process = new ProcessBuilder("reg", "query",
                    "HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\Shell Folders", "/v", "Desktop")
                    .redirectErrorStream(true).start();
            if (!process.waitFor(5, TimeUnit.SECONDS)) { process.destroy(); throw new java.io.IOException("Desktop lookup timed out"); }
            String output = new String(process.getInputStream().readAllBytes(), java.nio.charset.Charset.defaultCharset());
            for (String line : output.split("\\R")) {
                String[] parts = line.trim().split("REG_(?:EXPAND_)?SZ", 2);
                if (parts.length == 2) {
                    Path path = Path.of(parts[1].trim().replace("%USERPROFILE%", System.getProperty("user.home")));
                    if (Files.isDirectory(path)) return path;
                }
            }
        }
        Path path = Path.of(System.getProperty("user.home"), "Desktop");
        if (!Files.isDirectory(path)) throw new java.io.IOException("Desktop directory was not found");
        return path;
    }

    private static void copyToDesktop(String resource) throws Exception {
        if (!ExternalConsent.computerAllowed()) return;
        try (var input = ExternalClientEvents.class.getResourceAsStream("/assets/splitself/" + resource)) {
            if (input == null) throw new java.io.FileNotFoundException("Missing bundled resource " + resource);
            Path destination = desktop().resolve(Path.of(resource).getFileName());
            if (!ExternalConsent.computerAllowed()) return;
            // Do not overwrite a user's existing file with the same name.
            Files.copy(input, destination);
        }
    }

    private static void closeWindows() {
        SwingUtilities.invokeLater(() -> { WINDOWS.forEach(JFrame::dispose); WINDOWS.clear(); });
    }
    @SubscribeEvent public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) { closeWindows(); }
    private static void report(String text) {
        var player = Minecraft.getInstance().player;
        if (player != null) player.sendSystemMessage(Component.literal(text));
    }
}

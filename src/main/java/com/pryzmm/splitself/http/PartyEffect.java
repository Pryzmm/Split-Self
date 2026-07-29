package com.pryzmm.splitself.http;

import com.pryzmm.splitself.events.helper.SkyColor;
import com.pryzmm.splitself.screen.overlay.PartyOverlay;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.sound.SoundEvent;

import java.util.Random;

public class PartyEffect {

    public static volatile boolean partying = false;

    private static volatile long lastBeat = System.nanoTime();
    private static long currentBeatLength = 0;

    public static void play(ClientPlayerEntity player, int startBeat, int beatLength, SoundEvent sound) {
        if (partying) return;
        partying = true;
        currentBeatLength = beatLength;
        player.playSound(sound, 1.0f, 1.0f);
        new Thread(() -> {
            try {
                Thread.sleep(startBeat);
                for (int i = 0; i < 64; i++) {
                    lastBeat = System.nanoTime();
                    String hex = generateHexColor();
                    SkyColor.changeSkyColor(hex);
                    SkyColor.changeFogColor(hex);
                    SkyColor.changeDistantSkyColor(hex);
                    if (i == 32) {
                        PartyOverlay.toggleOverlay();
                        PartyOverlay.toggleConfetti();
                    }
                    Thread.sleep(beatLength);
                }
                SkyColor.changeSkyColor(null);
                SkyColor.changeFogColor(null);
                SkyColor.changeDistantSkyColor(null);
                SkyColor.colorOpacity = 1.0f;
                PartyOverlay.toggleConfetti();
                Thread.sleep(5000);
                PartyOverlay.toggleOverlay();
                partying = false;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    public static void changeOpacity() {
        if (!partying) return;

        long elapsedNanos = System.nanoTime() - lastBeat;
        float elapsedMs = elapsedNanos / 1_000_000f;
        float progress = elapsedMs / currentBeatLength;
        progress = Math.max(0.0f, Math.min(1.0f, progress));
        SkyColor.colorOpacity = 1.0f - progress;
    }

    private static final Random random = new Random();
    public static String generateHexColor() {
        int r = random.nextInt(256);
        int g = random.nextInt(256);
        int b = random.nextInt(256);
        return String.format("#%02X%02X%02X", r, g, b);
    }

}

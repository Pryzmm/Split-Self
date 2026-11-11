package com.pryzmm.splitself.forge;

import java.io.IOException;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.LoggerFactory;

public final class ClientPanorama {
    private static PanoramaState state;
    private ClientPanorama() {}

    private static PanoramaState state() throws IOException {
        if (state == null) state = new PanoramaState(Minecraft.getInstance().gameDirectory.toPath()
                .resolve("config/splitselfClient.json"));
        return state;
    }

    static void completeTransition() {
        try { state().completeTransition(); }
        catch (IOException | RuntimeException error) {
            LoggerFactory.getLogger(ClientPanorama.class).error("Could not save panorama stage", error);
        }
    }

    static void resetMain() {
        try { state().resetMain(); }
        catch (IOException failure) { LoggerFactory.getLogger(ClientPanorama.class).error("Panorama reset failed", failure); }
    }

    public static ResourceLocation texture(ResourceLocation original) {
        String path = original.getPath();
        String prefix = "textures/gui/title/background/panorama_";
        if (!original.getNamespace().equals("minecraft") || !path.startsWith(prefix)) return original;
        String face = path.substring(prefix.length());
        if (!face.matches("[0-5]\\.png")) return original;
        String stage = "main";
        try { stage = state().stage(); }
        catch (IOException | RuntimeException error) {
            LoggerFactory.getLogger(ClientPanorama.class).error("Could not read panorama stage", error);
        }
        return new ResourceLocation("splitself", "textures/gui/title/background/" + stage + "/panorama_" + face);
    }
}

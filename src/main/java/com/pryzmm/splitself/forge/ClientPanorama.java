package com.pryzmm.splitself.forge;

import java.io.IOException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.LoggerFactory;

/** Texture aliases adapt PanoramaMixin without replacing the title screen or its controls. */
@Mod.EventBusSubscriber(modid = SplitSelfForge.MOD_ID, value = Dist.CLIENT)
public final class ClientPanorama {
    private static PanoramaState state;
    private static String appliedStage;
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

    static void resetMain() { try {state().resetMain(); appliedStage=null;} catch(IOException failure){LoggerFactory.getLogger(ClientPanorama.class).error("Panorama reset failed",failure);} }
    @SubscribeEvent public static void onInit(ScreenEvent.Init.Post event) {
        if (!(event.getScreen() instanceof TitleScreen || event.getScreen() instanceof FakeMenuScreen)) return;
        try {
            String stage = state().stage();
            if (stage.equals(appliedStage)) return;
            var textures = Minecraft.getInstance().getTextureManager();
            for (int face = 0; face < 6; face++) {
                // SimpleTexture retains its source path across resource reloads.
                textures.register(new ResourceLocation("textures/gui/title/background/panorama_" + face + ".png"),
                        new SimpleTexture(new ResourceLocation("splitself",
                                "textures/gui/title/background/" + stage + "/panorama_" + face + ".png")));
            }
            appliedStage = stage;
        } catch (IOException | RuntimeException error) {
            LoggerFactory.getLogger(ClientPanorama.class).error("Could not load panorama stage", error);
        }
    }
}

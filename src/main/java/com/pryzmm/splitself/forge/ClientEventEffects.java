package com.pryzmm.splitself.forge;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Transient source events, with option restoration and no background access to Minecraft. */
@Mod.EventBusSubscriber(modid = SplitSelfForge.MOD_ID, value = Dist.CLIENT)
public final class ClientEventEffects {
    private static final Map<String, Long> STARTED = new HashMap<>();
    private static Integer oldFov;
    private static Double oldGamma;
    private static Boolean oldInvert;
    private static int fovSteps, gammaSteps, disconnectStep;
    private static final int[] DISCONNECT_TICKS = {0, 2, 36, 38, 116, 118};
    private ClientEventEffects() {}

    static void start(String name) {
        if (!EditionPolicy.eventAllowed(name)) return;
        var client = Minecraft.getInstance();
        if (client.player == null || STARTED.containsKey(name)) return;
        if(name.equals("DEBUG_FULLSCREEN")){client.player.sendSystemMessage(Component.literal(client.getWindow().isFullscreen()?"In fullscreen":"NOT in fullscreen"));return;}
        if(name.equals("DEBUG_INVERT")){ClientBlendEffects.toggle();return;}
        if(name.equals("DEBUG_VIDEO")){ClientTransitionEffects.preview();return;}
        if (name.startsWith("FINALE_")) {ClientFinaleSequence.start(name.substring(7));return;}
        if (name.equals("THEOTHERSCREENSHOT") || name.equals("SURROUND_GLITCH")) { RemainingClientEvents.start(name); return; }
        if (BatchEventRules.CLIENT.contains(name)) { ClientBatchEvents.start(name); return; }
        if (SimpleEventRules.CLIENT_EVENTS.contains(name)) { ClientSimpleEvents.start(name); return; }
        if (ExternalConsent.EVENTS.contains(name)) { ExternalClientEvents.run(name); return; }
        switch (name) {
            case "FOV" -> { oldFov = client.options.fov().get(); fovSteps = 0; }
            case "BRIGHTNESS" -> { oldGamma = client.options.gamma().get(); gammaSteps = 0; }
            case "INVERT" -> {
                oldInvert = client.options.invertYMouse().get();
                client.options.invertYMouse().set(true);
            }
            case "STATIC" -> sound("static2");
            case "INVENTORYOVERLAY" -> sound("horn");
            case "RECURSIVE" -> sound("glitch2");
            case "THE_OTHER" -> sound("scream");
            case "DISCONNECT" -> disconnectStep = 0;
            default -> { return; }
        }
        if(java.util.Set.of("FOV","BRIGHTNESS","STATIC","RECURSIVE").contains(name))ClientActivity.set(true);
        STARTED.put(name, System.nanoTime());
    }

    private static void sound(String name) {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(
                SoundEvent.createVariableRangeEvent(new ResourceLocation("splitself", name)), 1));
    }

    @SubscribeEvent public static void onGui(RenderGuiEvent.Post event) {
        if (STARTED.isEmpty()) return;
        var client = Minecraft.getInstance();
        if (client.player == null) { reset(); return; }
        GuiGraphics graphics = event.getGuiGraphics();
        for (var entry : new HashMap<>(STARTED).entrySet()) {
            String name = entry.getKey();
            long ms = (System.nanoTime() - entry.getValue()) / 1_000_000;
            switch (name) {
                case "FOV" -> {
                    while (fovSteps <= 500 && ms >= fovSteps * 25L) {
                        client.options.fov().set(client.options.fov().get() + (int) Math.floor(Math.random() * 3) - 1);
                        fovSteps++;
                    }
                    if (ms >= 12525) { client.options.fov().set(oldFov); oldFov = null; STARTED.remove(name); if(java.util.Set.of("FOV","BRIGHTNESS","STATIC","RECURSIVE").contains(name))ClientActivity.set(false); }
                }
                case "BRIGHTNESS" -> {
                    while (gammaSteps <= 500 && ms >= gammaSteps * 25L) {
                        client.options.gamma().set(Math.random() / 2); gammaSteps++;
                    }
                    if (ms >= 12525) { client.options.gamma().set(oldGamma); oldGamma = null; STARTED.remove(name); }
                }
                case "INVERT" -> {
                    if (ms >= 60000) {
                        // Kept source quirk: completion sets false even if originally inverted.
                        client.options.invertYMouse().set(false); oldInvert = null; STARTED.remove(name);
                    }
                }
                case "DISCONNECT" -> {
                    while (disconnectStep < DISCONNECT_TICKS.length && ms >= DISCONNECT_TICKS[disconnectStep] * 50L) {
                        client.player.sendSystemMessage(Component.translatable(
                                "events.splitself.disconnect." + (disconnectStep % 2 == 0 ? "left" : "joined"),
                                client.player.getName().getString()).withStyle(ChatFormatting.YELLOW));
                        disconnectStep++;
                    }
                    if (disconnectStep == DISCONNECT_TICKS.length) STARTED.remove(name);
                }
                case "RECURSIVE" -> {
                    if (ms < 2500) ClientStoryEffects.renderTunnel(graphics);
                    else { ClientStoryEffects.releaseTunnel(); stop(name, "glitch2"); }
                }
                case "INVENTORYOVERLAY" -> {
                    if (ms < 1000) graphics.drawString(client.font,
                            Component.translatable("events.splitself.inventoryOverlay.message"), (graphics.guiWidth()-client.font.width(Component.translatable("events.splitself.inventoryOverlay.message")))/2, (graphics.guiHeight()-client.font.lineHeight)/2, 0xFFFFFF);
                    else STARTED.remove(name);
                }
                case "THE_OTHER" -> {
                    if (ms >= 5000) { STARTED.remove(name); break; }
                    var random = client.level.random;
                    int count = 15 + random.nextInt(10);
                    for (int i = 0; i < count; i++) {
                        int x = random.nextInt(graphics.guiWidth() + 100) - 100;
                        int y = random.nextInt(graphics.guiHeight() + 100) - 100;
                        int width = 10 + random.nextInt(300), height = 10 + random.nextInt(300);
                        graphics.fill(x, y, x + width, y + height, 0xFF000000
                                | random.nextInt(40) << 16 | random.nextInt(40) << 8 | random.nextInt(40));
                    }
                }
                case "STATIC" -> {
                    if (ms >= 30000) { stop(name, "static2"); break; }
                    var pose = graphics.pose();
                    pose.pushPose();
                    float w = graphics.guiWidth(), h = graphics.guiHeight();
                    int degrees = (int) (Math.random() * 4) * 90;
                    pose.translate(w / 2, h / 2, 1000);
                    pose.mulPose(Axis.ZP.rotationDegrees(degrees));
                    if (degrees % 180 != 0) pose.scale(h / w, w / h, 1);
                    pose.translate(-w / 2, -h / 2, 0);
                    RenderSystem.enableBlend(); RenderSystem.defaultBlendFunc();
                    graphics.setColor(1, 1, 1, Math.min(ms / 20000f, 1));
                    graphics.blit(new ResourceLocation("splitself", "textures/screen/static.png"),
                            0, 0, 0, 0, (int) w, (int) h, (int) w, (int) h);
                    graphics.setColor(1, 1, 1, 1);
                    RenderSystem.disableBlend(); pose.popPose();
                }
                default -> STARTED.remove(name);
            }
        }
    }

    private static void stop(String event, String sound) {
        STARTED.remove(event);
        Minecraft.getInstance().getSoundManager().stop(new ResourceLocation("splitself", sound), SoundSource.MASTER);
    }
    @SubscribeEvent public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) { reset(); }
    private static void reset() {
        var client = Minecraft.getInstance();
        if (oldFov != null) client.options.fov().set(oldFov);
        if (oldGamma != null) client.options.gamma().set(oldGamma);
        if (oldInvert != null) client.options.invertYMouse().set(oldInvert);
        oldFov = null; oldGamma = null; oldInvert = null;
        STARTED.clear();
        ClientStoryEffects.releaseTunnel();
    }
}

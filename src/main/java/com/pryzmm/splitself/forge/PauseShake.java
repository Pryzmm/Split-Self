package com.pryzmm.splitself.forge;

import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** GameMenuMixin: persistent flag; independent +/-1 offsets from original button coordinates. */
@Mod.EventBusSubscriber(modid = SplitSelfForge.MOD_ID, value = Dist.CLIENT)
public final class PauseShake {
    private static boolean enabled;
    private static final Map<Button, int[]> POSITIONS = new WeakHashMap<>();
    private static final java.util.Random RANDOM = new java.util.Random();
    private PauseShake() {}
    static void enable() { enabled = true; }
    private static void shake(net.minecraft.client.gui.screens.Screen screen) {
        if (!enabled || !(screen instanceof PauseScreen)) return;
        for (var child : screen.children()) if (child instanceof Button button) {
            int[] origin = POSITIONS.computeIfAbsent(button, b -> new int[]{b.getX(), b.getY()});
            button.setX(origin[0] + (RANDOM.nextInt(2) * 2 - 1));
            button.setY(origin[1] + (RANDOM.nextInt(2) * 2 - 1));
        }
    }
    @SubscribeEvent public static void init(ScreenEvent.Init.Post event) {
        // Resizing reuses buttons in some screens; recapture the newly laid out coordinates.
        event.getScreen().children().forEach(child -> POSITIONS.remove(child));
        shake(event.getScreen());
    }
    @SubscribeEvent public static void render(ScreenEvent.Render.Pre event) { shake(event.getScreen()); }
}

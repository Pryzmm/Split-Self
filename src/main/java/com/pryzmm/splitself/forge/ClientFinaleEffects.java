package com.pryzmm.splitself.forge;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Final Computer close effect, with the source's 100 client-tick delay. */
@Mod.EventBusSubscriber(modid = SplitSelfForge.MOD_ID, value = Dist.CLIENT)
final class ClientFinaleEffects {
    private static int closeTicks = -1;
    private ClientFinaleEffects() {}

    static void startClose() {
        closeTicks = 0;
        Minecraft.getInstance().setScreen(new BlackoutScreen());
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || closeTicks < 0) return;
        if (++closeTicks >= 100) {
            closeTicks = -1;
            Minecraft.getInstance().stop();
        }
    }

    private static final class BlackoutScreen extends Screen {
        BlackoutScreen() { super(Component.empty()); }
        @Override public boolean isPauseScreen() { return false; }
        @Override public boolean shouldCloseOnEsc() { return false; }
        @Override public void render(GuiGraphics graphics, int x, int y, float delta) {
            graphics.fill(0, 0, width, height, 0xFF000000);
        }
    }
}

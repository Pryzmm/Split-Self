package com.pryzmm.splitself.client.render;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.render.RenderTickCounter;

@FunctionalInterface
public interface PostHudRenderCallback {
    /**
     * Fired after Minecraft has rendered everything in the world, AFTER it renders hands, HUDs and GUIs.
     */
    Event<PostHudRenderCallback> EVENT = EventFactory.createArrayBacked(PostHudRenderCallback.class,
            (listeners) -> (tickCounter) -> {
                for (PostHudRenderCallback handler : listeners) {
                    handler.onPostHudRendered(tickCounter);
                }
            });

    void onPostHudRendered(RenderTickCounter tickCounter);
}

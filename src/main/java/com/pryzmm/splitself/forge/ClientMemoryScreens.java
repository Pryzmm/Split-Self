package com.pryzmm.splitself.forge;

import net.minecraft.client.Minecraft;

final class ClientMemoryScreens {
    private ClientMemoryScreens() {}

    static void open(int bitMask) {
        Minecraft.getInstance().setScreen(new MemoryBookScreen(bitMask));
    }
}

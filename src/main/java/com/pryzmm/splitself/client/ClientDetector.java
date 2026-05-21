package com.pryzmm.splitself.client;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.api.EnvType;

public class ClientDetector {

    /**
    TODO: Check that this works with the rebranding of Feather into Dawn after June 14th
     **/
    public static boolean isFeatherClient() {
        if (FabricLoader.getInstance().getEnvironmentType() != EnvType.CLIENT) {return false;}
        if (FabricLoader.getInstance().isModLoaded("feather")) {return true;}
        try {
            Class.forName("net.feathermc.client.FeatherClient");
            return true;
        } catch (ClassNotFoundException ignored) {}
        String brand = System.getProperty("minecraft.launcher.brand", "");
        return brand.toLowerCase().contains("feather") || brand.toLowerCase().contains("dawn");
    }
}
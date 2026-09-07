package com.pryzmm.splitself.events;

import com.pryzmm.splitself.events.helper.ChunkDestroyer;
import com.pryzmm.splitself.events.helper.SkyColor;
import com.pryzmm.splitself.screen.overlay.ColorOverlay;
import com.pryzmm.splitself.world.ClientTickScheduler;
import com.pryzmm.splitself.world.TickScheduler;
import net.minecraft.server.MinecraftServer;

public class FinaleRenderer {

    // CLIENT-START

    public static boolean brokenClouds = false;
    public static boolean brokenHotbar = false;
    public static boolean brokenHotbarSize = false;
    public static boolean brokenHealth = false;
    public static boolean brokenFood = false;
    public static boolean brokenArmor = false;
    public static boolean brokenExperience = false;
    public static boolean brokenItems = false;
    public static boolean brokenUI = false;

    public static void startClientEffect() {
        ColorOverlay.setColor(0xFF000000);
        restartCloudEffect();
        restartSkyEffect();
        restartHotbarEffect();
        restartHotbarSizeEffect();
        restartHealthEffect();
        restartFoodEffect();
        restartArmorEffect();
        restartExperienceEffect();
        restartScreenEffect();
        restartItemEffect();
        restartUIEffect();
    }

    private static void restartCloudEffect() {
        ClientTickScheduler.schedule((long) (Math.random() * 50), () -> {
            brokenClouds = true;
            ClientTickScheduler.schedule((long) (Math.random() * 30), () -> {
                brokenClouds = false;
                restartCloudEffect();
            });
        });
    }

    private static void restartSkyEffect() {
        ClientTickScheduler.schedule((long) (Math.random() * 50), () -> {
            SkyColor.changeSkyColor("000000");
            SkyColor.changeFogColor("000000");
            SkyColor.changeDistantSkyColor("000000");
            ClientTickScheduler.schedule((long) (Math.random() * 30), () -> {
                SkyColor.changeSkyColor(null);
                SkyColor.changeFogColor(null);
                SkyColor.changeDistantSkyColor(null);
                restartSkyEffect();
            });
        });
    }

    private static void restartHotbarEffect() {
        ClientTickScheduler.schedule((long) (Math.random() * 50), () -> {
            brokenHotbar = true;
            ClientTickScheduler.schedule((long) (Math.random() * 30), () -> {
                brokenHotbar = false;
                restartHotbarEffect();
            });
        });
    }

    private static void restartHotbarSizeEffect() {
        ClientTickScheduler.schedule((long) (Math.random() * 50), () -> {
            brokenHotbarSize = true;
            ClientTickScheduler.schedule((long) (Math.random() * 30), () -> {
                brokenHotbarSize = false;
                restartHotbarSizeEffect();
            });
        });
    }

    private static void restartHealthEffect() {
        ClientTickScheduler.schedule((long) (Math.random() * 50), () -> {
            brokenHealth = true;
            ClientTickScheduler.schedule((long) (Math.random() * 30), () -> {
                brokenHealth = false;
                restartHealthEffect();
            });
        });
    }

    private static void restartFoodEffect() {
        ClientTickScheduler.schedule((long) (Math.random() * 50), () -> {
            brokenFood = true;
            ClientTickScheduler.schedule((long) (Math.random() * 30), () -> {
                brokenFood = false;
                restartFoodEffect();
            });
        });
    }

    private static void restartArmorEffect() {
        ClientTickScheduler.schedule((long) (Math.random() * 50), () -> {
            brokenArmor = true;
            ClientTickScheduler.schedule((long) (Math.random() * 30), () -> {
                brokenArmor = false;
                restartArmorEffect();
            });
        });
    }

    private static void restartExperienceEffect() {
        ClientTickScheduler.schedule((long) (Math.random() * 50), () -> {
            brokenExperience = true;
            ClientTickScheduler.schedule((long) (Math.random() * 30), () -> {
                brokenExperience = false;
                restartExperienceEffect();
            });
        });
    }

    private static void restartScreenEffect() {
        ClientTickScheduler.schedule((long) (100 + (Math.random() * 200)), () -> {
            ColorOverlay.toggleOverlay(true);
            ClientTickScheduler.schedule((long) (10 + (Math.random() * 30)), () -> {
                ColorOverlay.toggleOverlay(false);
                restartExperienceEffect();
            });
        });
    }

    private static void restartItemEffect() {
        ClientTickScheduler.schedule((long) (Math.random() * 50), () -> {
            brokenItems = true;
            ClientTickScheduler.schedule((long) (Math.random() * 30), () -> {
                brokenItems = false;
                restartItemEffect();
            });
        });
    }

    private static void restartUIEffect() {
        ClientTickScheduler.schedule((long) (Math.random() * 50), () -> {
            brokenUI = true;
            ClientTickScheduler.schedule((long) (Math.random() * 30), () -> {
                brokenUI = false;
                restartUIEffect();
            });
        });
    }




    // SERVER-START

    public static void startServerEffect(MinecraftServer server) {
        restartTerrainEffect(server);
    }

    private static void restartTerrainEffect(MinecraftServer server) {
        ChunkDestroyer.liftChunk(server, 1, -12);
        TickScheduler.schedule((long) (Math.random() * 40), () -> FinaleRenderer.restartTerrainEffect(server));
    }

}

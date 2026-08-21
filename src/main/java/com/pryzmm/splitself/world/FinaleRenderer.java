package com.pryzmm.splitself.world;

import com.pryzmm.splitself.events.helper.SkyColor;
import com.pryzmm.splitself.screen.overlay.ColorOverlay;

public class FinaleRenderer {

    public static boolean brokenClouds = false;
    public static boolean brokenHotbar = false;
    public static boolean brokenHotbarSize = false;
    public static boolean brokenHealth = false;
    public static boolean brokenFood = false;
    public static boolean brokenArmor = false;
    public static boolean brokenExperience = false;
    public static boolean brokenItems = false;
    public static boolean brokenUI = false;

    public static void startEffect() {
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
        TickScheduler.schedule((long) (Math.random() * 50), () -> {
            brokenClouds = true;
            TickScheduler.schedule((long) (Math.random() * 30), () -> {
                brokenClouds = false;
                restartCloudEffect();
            });
        });
    }

    private static void restartSkyEffect() {
        TickScheduler.schedule((long) (Math.random() * 50), () -> {
            SkyColor.changeSkyColor("000000");
            SkyColor.changeFogColor("000000");
            SkyColor.changeDistantSkyColor("000000");
            TickScheduler.schedule((long) (Math.random() * 30), () -> {
                SkyColor.changeSkyColor(null);
                SkyColor.changeFogColor(null);
                SkyColor.changeDistantSkyColor(null);
                restartSkyEffect();
            });
        });
    }

    private static void restartHotbarEffect() {
        TickScheduler.schedule((long) (Math.random() * 50), () -> {
            brokenHotbar = true;
            TickScheduler.schedule((long) (Math.random() * 30), () -> {
                brokenHotbar = false;
                restartHotbarEffect();
            });
        });
    }

    private static void restartHotbarSizeEffect() {
        TickScheduler.schedule((long) (Math.random() * 50), () -> {
            brokenHotbarSize = true;
            TickScheduler.schedule((long) (Math.random() * 30), () -> {
                brokenHotbarSize = false;
                restartHotbarSizeEffect();
            });
        });
    }

    private static void restartHealthEffect() {
        TickScheduler.schedule((long) (Math.random() * 50), () -> {
            brokenHealth = true;
            TickScheduler.schedule((long) (Math.random() * 30), () -> {
                brokenHealth = false;
                restartHealthEffect();
            });
        });
    }

    private static void restartFoodEffect() {
        TickScheduler.schedule((long) (Math.random() * 50), () -> {
            brokenFood = true;
            TickScheduler.schedule((long) (Math.random() * 30), () -> {
                brokenFood = false;
                restartFoodEffect();
            });
        });
    }

    private static void restartArmorEffect() {
        TickScheduler.schedule((long) (Math.random() * 50), () -> {
            brokenArmor = true;
            TickScheduler.schedule((long) (Math.random() * 30), () -> {
                brokenArmor = false;
                restartArmorEffect();
            });
        });
    }

    private static void restartExperienceEffect() {
        TickScheduler.schedule((long) (Math.random() * 50), () -> {
            brokenExperience = true;
            TickScheduler.schedule((long) (Math.random() * 30), () -> {
                brokenExperience = false;
                restartExperienceEffect();
            });
        });
    }

    private static void restartScreenEffect() {
        TickScheduler.schedule((long) (100 + (Math.random() * 200)), () -> {
            ColorOverlay.toggleOverlay(true);
            TickScheduler.schedule((long) (10 + (Math.random() * 30)), () -> {
                ColorOverlay.toggleOverlay(false);
                restartExperienceEffect();
            });
        });
    }

    private static void restartItemEffect() {
        TickScheduler.schedule((long) (Math.random() * 50), () -> {
            brokenItems = true;
            TickScheduler.schedule((long) (Math.random() * 30), () -> {
                brokenItems = false;
                restartItemEffect();
            });
        });
    }

    private static void restartUIEffect() {
        TickScheduler.schedule((long) (Math.random() * 50), () -> {
            brokenUI = true;
            TickScheduler.schedule((long) (Math.random() * 30), () -> {
                brokenUI = false;
                restartUIEffect();
            });
        });
    }

}

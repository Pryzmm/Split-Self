package com.pryzmm.splitself.events.helper;

public class SkyColor {

    private static Integer currentSkyColor = null;
    private static Integer currentDistantSkyColor = null;
    private static Integer currentFogColor = null;
    public static float colorOpacity = 1.0f;

    public static void changeFogColor(String hex) {
        if (hex == null) {
            currentFogColor = null;
            return;
        }
        try {
            if (hex.startsWith("#")) hex = hex.substring(1);
            currentFogColor = Integer.parseInt(hex, 16);
        } catch (NumberFormatException e) {
            System.err.println("Invalid hex color: " + hex);
        }
    }

    public static void changeSkyColor(String hex) {
        if (hex == null) {
            currentSkyColor = null;
            return;
        }
        try {
            if (hex.startsWith("#")) hex = hex.substring(1);
            currentSkyColor = Integer.parseInt(hex, 16);
        } catch (NumberFormatException e) {
            System.err.println("Invalid hex color: " + hex);
        }
    }

    public static void changeDistantSkyColor(String hex) {
        if (hex == null) {
            currentDistantSkyColor = null;
            return;
        }
        try {
            if (hex.startsWith("#")) hex = hex.substring(1);
            currentDistantSkyColor = Integer.parseInt(hex, 16);
        } catch (NumberFormatException e) {
            System.err.println("Invalid hex color: " + hex);
        }
    }

    private static float[] lerp(int baseColor, int targetColor) {
        float t = Math.max(0.0f, Math.min(1.0f, colorOpacity));

        int baseR = (baseColor >> 16) & 0xFF;
        int baseG = (baseColor >> 8) & 0xFF;
        int baseB = baseColor & 0xFF;

        int targetR = (targetColor >> 16) & 0xFF;
        int targetG = (targetColor >> 8) & 0xFF;
        int targetB = targetColor & 0xFF;

        float r = (baseR + (targetR - baseR) * t) / 255f;
        float g = (baseG + (targetG - baseG) * t) / 255f;
        float b = (baseB + (targetB - baseB) * t) / 255f;

        return new float[]{r, g, b};
    }

    private static float[] lerpFromFloats(float baseR, float baseG, float baseB, int targetColor) {
        float t = Math.max(0.0f, Math.min(1.0f, colorOpacity));

        float targetR = ((targetColor >> 16) & 0xFF) / 255f;
        float targetG = ((targetColor >> 8) & 0xFF) / 255f;
        float targetB = (targetColor & 0xFF) / 255f;

        float r = baseR + (targetR - baseR) * t;
        float g = baseG + (targetG - baseG) * t;
        float b = baseB + (targetB - baseB) * t;

        return new float[]{r, g, b};
    }

    public static float[] getSkyRGBComponents(float vanillaR, float vanillaG, float vanillaB) {
        if (colorOpacity <= 0.0f || currentSkyColor == null) {
            return null;
        }
        return lerpFromFloats(vanillaR, vanillaG, vanillaB, currentSkyColor);
    }

    public static float[] getDistantSkyRGBComponents(float vanillaR, float vanillaG, float vanillaB) {
        if (colorOpacity <= 0.0f || currentDistantSkyColor == null) {
            return null;
        }
        return lerpFromFloats(vanillaR, vanillaG, vanillaB, currentDistantSkyColor);
    }

    public static float[] getFogRGBComponents(float vanillaR, float vanillaG, float vanillaB) {
        if (colorOpacity <= 0.0f || currentFogColor == null) {
            return null;
        }
        return lerpFromFloats(vanillaR, vanillaG, vanillaB, currentFogColor);
    }

    public static float[] getFogRGBComponents() {
        if (currentFogColor == null) {
            return null;
        }
        return lerp(currentFogColor, currentFogColor);
    }
}
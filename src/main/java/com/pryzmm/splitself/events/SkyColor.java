package com.pryzmm.splitself.events;

public class SkyColor {
    private static int currentSkyColor = 0x78A7FF; // Default sky blue
    private static int currentFogColor = 0xC0D8FF; // Default fog
    private static int currentFogSkyColor = 0xC0D8FF; // Default fog color

    public static void changeFogColor(String hex) {
        try {
            if (hex.startsWith("#")) {
                hex = hex.substring(1);
            }

            currentFogColor = Integer.parseInt(hex, 16);
        } catch (NumberFormatException e) {
            System.err.println("Invalid hex color: " + hex);
        }
    }

    public static void changeFogSkyColor(String hex) {
        try {
            if (hex.startsWith("#")) {
                hex = hex.substring(1);
            }

            currentFogSkyColor = Integer.parseInt(hex, 16);
        } catch (NumberFormatException e) {
            System.err.println("Invalid hex color: " + hex);
        }
    }

    public static void changeSkyColor(String hex) {
        try {
            if (hex.startsWith("#")) {
                hex = hex.substring(1);
            }
            currentSkyColor = Integer.parseInt(hex, 16);
        } catch (NumberFormatException e) {
            System.err.println("Invalid hex color: " + hex);
        }
    }

    public static float[] getSkyRGBComponents() {
        int r = (currentSkyColor >> 16) & 0xFF;
        int g = (currentSkyColor >> 8) & 0xFF;
        int b = currentSkyColor & 0xFF;
        return new float[]{r / 255f, g / 255f, b / 255f};
    }
    public static float[] getFogRGBComponents() {
        int r = (currentFogColor >> 16) & 0xFF;
        int g = (currentFogColor >> 8) & 0xFF;
        int b = currentFogColor & 0xFF;
        return new float[]{r / 255f, g / 255f, b / 255f};
    }
    public static float[] getFogSkyRGBComponents() {
        int r = (currentFogSkyColor >> 16) & 0xFF;
        int g = (currentFogSkyColor >> 8) & 0xFF;
        int b = currentFogSkyColor & 0xFF;
        return new float[]{r / 255f, g / 255f, b / 255f};
    }
}
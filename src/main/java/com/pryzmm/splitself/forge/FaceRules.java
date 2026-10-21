package com.pryzmm.splitself.forge;

/** SkyImageRenderer/ScreenOverlay constants; growth is deliberately per rendered frame. */
final class FaceRules {
    static final long SKY_TIMEOUT_MS=60000;
    static boolean skyExpired(long elapsedMs){return elapsedMs>=SKY_TIMEOUT_MS;}
    private FaceRules() {}
    static boolean looking(double x, double y, double z) {
        double dot = (x * 10 + y * 50 - z * 100) / Math.sqrt(12600);
        return Math.toDegrees(Math.acos(Math.max(-1, Math.min(1, dot)))) < 4;
    }
    static int line(long ms) { return ms < 3000 ? 0 : ms < 7500 ? 1 : ms < 12000 ? 2 : 3; }
    static boolean flash(long ms) { return ms >= 18000 && ms < 18150; }
    static boolean finished(long ms) { return ms >= 18150; }
    static final class Sky {
        boolean visible;
        float width = 4, height = 6;
        void toggle() { visible = !visible; width = 4; height = 6; }
        boolean frame(boolean looking, int stage) {
            if (!visible || !looking) return false;
            if (stage < 3) { toggle(); return false; }
            width = (float) (width + .006);
            height = (float) (height + .009);
            if (width >= 10) { toggle(); return true; }
            return false;
        }
    }
}

package com.pryzmm.splitself.forge;

/** Pinned EventRunner/SkyColor/BlendManager values; no client or OpenGL dependencies. */
final class SkyEventRules {
    static final int SKY = 0xAA0000, FOG = 0x880000, DARKNESS_TICKS = 430;
    static final long INVERT_MS = 20000;
    private SkyEventRules() {}
    static final class Inversion {
        private final java.util.PriorityQueue<Long> deadlines = new java.util.PriorityQueue<>();
        private boolean enabled;
        void start(long now) { deadlines.add(now + INVERT_MS); enabled = true; }
        boolean active(long now) { return enabled && (deadlines.isEmpty() || now < deadlines.peek()); }
        boolean finish(long now) {
            boolean expired = false;
            while (!deadlines.isEmpty() && now >= deadlines.peek()) { deadlines.remove(); expired = true; }
            if (expired) enabled = false;
            return expired;
        }
        void clear() { deadlines.clear(); enabled = false; }
    }
    static float leftEdge(int height, double random) { return (float)(height / 2.0 + random * 20 - 10); }
    static float rightEdge(int height, double random) { return (float)(height / 2.0 + random * 40 - 20); }
}

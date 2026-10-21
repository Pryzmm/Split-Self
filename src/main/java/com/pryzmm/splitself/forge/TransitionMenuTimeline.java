package com.pryzmm.splitself.forge;

/** Original FakeMenuScreen schedules 60 client ticks, then another 25. */
final class TransitionMenuTimeline {
    enum Step { NONE, BEGIN_MELT, FINISH }
    private int ticks = -1;

    boolean start() {
        if (ticks >= 0) return false;
        ticks = 0;
        return true;
    }

    Step tick() {
        if (ticks < 0) return Step.NONE;
        ticks++;
        if (ticks == 60) return Step.BEGIN_MELT;
        if (ticks == 85) {
            cancel();
            return Step.FINISH;
        }
        return Step.NONE;
    }

    void cancel() { ticks = -1; }
}

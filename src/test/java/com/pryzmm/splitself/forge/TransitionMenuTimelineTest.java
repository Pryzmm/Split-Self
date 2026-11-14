package com.pryzmm.splitself.forge;

/** Independent expected boundaries from original FakeMenuScreen: schedule(60), schedule(25). */
public final class TransitionMenuTimelineTest {
    public static void main(String[] args) {
        TransitionMenuTimeline timeline = new TransitionMenuTimeline();
        expect(timeline.tick(), TransitionMenuTimeline.Step.NONE, "idle");
        if (!timeline.start() || timeline.start()) throw new AssertionError("duplicate start must be rejected");
        for (int tick = 1; tick <= 100; tick++) {
            var expected = tick == 60 ? TransitionMenuTimeline.Step.BEGIN_MELT
                    : tick == 85 ? TransitionMenuTimeline.Step.FINISH : TransitionMenuTimeline.Step.NONE;
            expect(timeline.tick(), expected, "tick " + tick);
        }
        if (!timeline.start()) throw new AssertionError("new transition after completion");
        for (int tick = 1; tick < 60; tick++) timeline.tick();
        timeline.cancel();
        for (int tick = 0; tick < 100; tick++) expect(timeline.tick(), TransitionMenuTimeline.Step.NONE, "cancel before melt");
        if (!timeline.start()) throw new AssertionError("restart after cancellation");
        for (int tick = 1; tick <= 60; tick++) timeline.tick();
        timeline.cancel();
        for (int tick = 0; tick < 100; tick++) expect(timeline.tick(), TransitionMenuTimeline.Step.NONE, "cancel during melt");
        System.out.println("PASS: 60/85 tick boundaries, duplicate start, one finish, cancellation and restart");
    }

    private static void expect(TransitionMenuTimeline.Step actual, TransitionMenuTimeline.Step expected, String label) {
        if (actual != expected) throw new AssertionError(label + ": expected " + expected + ", got " + actual);
    }
}

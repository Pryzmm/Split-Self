package com.pryzmm.splitself.forge;
import java.util.UUID;
import java.io.*;
public final class TransitionRecoveryTest {
    private static int checks;
    private static void check(boolean ok, String label) { if (!ok) throw new AssertionError(label); checks++; }
    private static final class Player implements TransitionRecovery.Player {
        UUID player = UUID.randomUUID(), saved, sent, result;
        int attempts, starts, results;
        boolean alive = true, destination = true;
        public UUID playerId() { return player; }
        public UUID savedId() { return saved; }
        public void save(UUID id) { saved = id; }
        public void removeSaved() { saved = null; }
        public boolean alive() { return alive; }
        public boolean teleport() { attempts++; check(saved != null, "intent retained during teleport"); return destination; }
        public void started(UUID id) { check(id.equals(saved), "save before packet"); sent = id; starts++; }
        public void completed(UUID id) { check(saved == null, "remove before result"); result = id; results++; }
        Player serialized() throws Exception {
            var bytes = new ByteArrayOutputStream();
            try (var out = new DataOutputStream(bytes)) {
                out.writeLong(saved.getMostSignificantBits()); out.writeLong(saved.getLeastSignificantBits());
            }
            var copy = new Player(); copy.player = player;
            try (var in = new DataInputStream(new ByteArrayInputStream(bytes.toByteArray()))) { copy.saved = new UUID(in.readLong(), in.readLong()); }
            return copy;
        }
    }
    public static void main(String[] args) throws Exception {
        var recovery = new TransitionRecovery(); var p = new Player(); var stale = UUID.randomUUID();
        check(!recovery.finish(p, stale, 0), "no active intent");
        check(recovery.start(p, 0), "start"); var id = p.sent;
        check(!recovery.start(p, 0) && p.starts == 1, "duplicate start");
        check(!recovery.finish(p, stale, 1) && p.saved.equals(id), "stale request");
        recovery.heartbeat(p, stale, 1199); recovery.tick(p, 1199);
        check(p.attempts == 0, "timeout exclusive boundary and stale heartbeat");
        recovery.tick(p, 1200);
        check(p.attempts == 1 && id.equals(p.result), "timeout inclusive boundary");
        check(!recovery.finish(p, id, 1201) && p.results == 1, "duplicate completion");
        check(recovery.start(p, 1300) && !id.equals(p.sent), "new generation");
        for (long tick = 1320; tick <= 20000; tick += 20) { recovery.heartbeat(p, p.sent, tick); recovery.tick(p, tick); }
        check(p.attempts == 1 && p.saved != null, "long menu wait with heartbeat");
        check(recovery.finish(p, p.sent, 20001) && p.attempts == 2, "normal finish");
        recovery.start(p, 21000); p.destination = false; id = p.sent;
        check(recovery.finish(p, id, 21001) && p.saved != null, "abort unavailable destination retains intent");
        recovery.tick(p, 21100); check(p.attempts == 3, "retry not early");
        recovery.tick(p, 21101); check(p.attempts == 4 && p.saved != null, "retry at 100 ticks");
        p.destination = true; recovery.tick(p, 21201); check(p.saved == null, "eventual retry success");
        recovery.start(p, 22000); id = p.sent;
        var reloaded = p.serialized(); recovery.clearTemporary();
        check(!recovery.start(reloaded, 0), "durable intent blocks repeated start after restart");
        reloaded.alive = false; recovery.tick(reloaded, 1); check(reloaded.attempts == 0, "dead waits for respawn");
        reloaded.alive = true; recovery.tick(reloaded, 2);
        check(id.equals(reloaded.result) && reloaded.starts == 0, "serialized recovery without video");
        recovery.start(p, 23000); recovery.forget(p.player); recovery.tick(p, 23001);
        check(p.saved == null, "logout login recovery");
        var state = new TransitionState(UUID.randomUUID(), 0, false);
        state.heartbeat(state.id, 1199);
        check(!state.attempt(1200, true, () -> true), "fresh heartbeat moves deadline");
        check(!state.attempt(2398, true, () -> true), "heartbeat deadline before boundary");
        check(state.attempt(2399, true, () -> true), "heartbeat exact deadline");
        var client = new TransitionClientSession(); var first = UUID.randomUUID(); var next = UUID.randomUUID();
        client.start(first); check(client.active(first), "client starts");
        for (int tick = 1; tick < 20; tick++) check(!client.heartbeatDue(), "heartbeat not early");
        check(client.heartbeatDue(), "heartbeat at 20 ticks");
        client.defer(first); check(first.equals(client.pollDeferred()) && client.pollDeferred() == null, "deferred screen once");
        client.defer(first);
        check(client.request() && !client.request() && !client.active(first), "one request disables callbacks");
        client.clear(); check(!client.current(first), "cleanup invalidates callbacks");
        client.start(next); check(client.pollDeferred() == null, "old deferred open cleared"); check(!client.active(first) && client.active(next), "late decoder cannot reopen new scene");
        check(!client.current(first) && client.current(next), "late result cannot clear new scene");
        client.defer(next); client.clear(); check(client.pollDeferred() == null && !client.heartbeatDue(), "cleanup cancels deferred screen and heartbeat"); check(client.id() == null, "logout clears client identity");
        System.out.println("PASS: " + checks + " production recovery/session assertions; serialized intent, retry, heartbeat, timeout, generations");
    }
}

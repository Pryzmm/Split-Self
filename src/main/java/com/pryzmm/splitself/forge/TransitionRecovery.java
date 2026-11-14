package com.pryzmm.splitself.forge;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
/** Production transition coordinator with a platform adapter for durable player data. */
final class TransitionRecovery {
    interface Player {
        UUID playerId();
        UUID savedId();
        void save(UUID id);
        void removeSaved();
        boolean alive();
        boolean teleport();
        void started(UUID id);
        void completed(UUID id);
    }
    private final Map<UUID, TransitionState> active = new HashMap<>();
    private TransitionState restore(Player player, long now) {
        return active.computeIfAbsent(player.playerId(), key -> {
            UUID saved = player.savedId();
            return saved == null ? null : new TransitionState(saved, now, true);
        });
    }
    boolean start(Player player, long now) {
        if (restore(player, now) != null || !player.alive()) return false;
        var state = new TransitionState(UUID.randomUUID(), now, false);
        player.save(state.id);
        active.put(player.playerId(), state);
        player.started(state.id);
        return true;
    }
    void heartbeat(Player player, UUID id, long now) {
        var state = restore(player, now);
        if (state != null) state.heartbeat(id, now);
    }
    boolean finish(Player player, UUID id, long now) {
        var state = restore(player, now);
        if (state == null || !state.requestFinish(id)) return false;
        tick(player, now);
        return true;
    }
    void tick(Player player, long now) {
        var state = restore(player, now);
        if (state != null && state.attempt(now, player.alive(), player::teleport)) {
            player.removeSaved();
            active.remove(player.playerId());
            player.completed(state.id);
        }
    }
    void forget(UUID player) { active.remove(player); }
    void clearTemporary() { active.clear(); }
}

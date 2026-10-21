package com.pryzmm.splitself.forge;
import java.util.UUID;
/** Identity guard shared by decoder callbacks, delayed opening and completion. */
final class TransitionClientSession {
    private UUID id;
    private boolean waiting;
    private UUID deferred;
    private int heartbeat;
    void start(UUID next) { clear(); id = next; }
    boolean current(UUID token) { return token != null && token.equals(id); }
    boolean active(UUID token) { return current(token) && !waiting; }
    UUID id() { return id; }
    boolean request() { if (id == null || waiting) return false; waiting = true; return true; }
    void defer(UUID token) { if (active(token)) deferred = token; }
    UUID pollDeferred() { UUID pending = deferred; deferred = null; return active(pending) ? pending : null; }
    boolean heartbeatDue() { if (!active(id)) return false; if (++heartbeat < 20) return false; heartbeat = 0; return true; }
    void clearDeferred() { deferred = null; }
    void clear() { id = null; waiting = false; deferred = null; heartbeat = 0; }
}

package com.pryzmm.splitself.forge;
import java.util.UUID;
import java.util.function.BooleanSupplier;
/** Server-thread policy; only id is durable. Restored scenes finish immediately. */
final class TransitionState {
    final UUID id;
    private long lastActivity, nextAttempt;
    private boolean finishing;
    TransitionState(UUID id, long now, boolean recovering) {
        this.id = id; lastActivity = now; finishing = recovering;
    }
    boolean matches(UUID request) { return id.equals(request); }
    void heartbeat(UUID request, long now) { if (matches(request) && !finishing) lastActivity = now; }
    boolean requestFinish(UUID request) {
        if (!matches(request) || finishing) return false;
        finishing = true; return true;
    }
    boolean attempt(long now, boolean alive, BooleanSupplier teleport) {
        if (!finishing && now - lastActivity >= 1200) finishing = true;
        if (!finishing || !alive || now < nextAttempt) return false;
        nextAttempt = now + 100;
        return teleport.getAsBoolean();
    }
}

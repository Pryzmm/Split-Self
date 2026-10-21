package com.pryzmm.splitself.forge;
import java.util.UUID;
import java.io.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
public final class TransitionIntentTest {
    private static void check(boolean ok, String label) { if (!ok) throw new AssertionError(label); }
    public static void main(String[] args) throws Exception {
        var root = new CompoundTag();
        check(TransitionIntent.read(root) == null && root.isEmpty(), "old saves read without mutation");
        UUID id = UUID.randomUUID(); TransitionIntent.write(root, id);
        var bytes = new ByteArrayOutputStream();
        NbtIo.write(root, new DataOutputStream(bytes));
        var restored = NbtIo.read(new DataInputStream(new ByteArrayInputStream(bytes.toByteArray())));
        check(id.equals(TransitionIntent.read(restored)), "real NBT roundtrip");
        var clone = new CompoundTag(); var persisted = new CompoundTag(); persisted.putString("otherMod", "kept");
        clone.put("PlayerPersisted", persisted);
        TransitionIntent.copy(restored, clone);
        check(id.equals(TransitionIntent.read(clone)), "clone intent");
        check(clone.getCompound("PlayerPersisted").getString("otherMod").equals("kept"), "clone preserves other data");
        TransitionIntent.remove(clone);
        check(TransitionIntent.read(clone) == null && id.equals(TransitionIntent.read(restored)), "independent clone and removal");
        var recovery = new TransitionRecovery();
        var adapter = new TransitionRecovery.Player() {
            public UUID playerId() { return id; }
            public UUID savedId() { return TransitionIntent.read(restored); }
            public void save(UUID next) { TransitionIntent.write(restored, next); }
            public void removeSaved() { TransitionIntent.remove(restored); }
            public boolean alive() { return true; }
            public boolean teleport() { return true; }
            public void started(UUID next) { throw new AssertionError("recovery replayed video"); }
            public void completed(UUID next) { check(next.equals(id), "result id"); }
        };
        recovery.tick(adapter, 0);
        check(TransitionIntent.read(restored) == null, "real deserialized NBT recovered");
        System.out.println("PASS: real NBT roundtrip, old save, player clone, independent removal, deserialized recovery without video");
    }
}

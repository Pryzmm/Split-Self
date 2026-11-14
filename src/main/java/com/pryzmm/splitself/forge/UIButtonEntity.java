package com.pryzmm.splitself.forge;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

/** Non-moving Limbo finale button stored in the authored real_house template. */
public final class UIButtonEntity extends Entity {
    private static final EntityDataAccessor<Boolean> DELETES_WORLD =
            SynchedEntityData.defineId(UIButtonEntity.class, EntityDataSerializers.BOOLEAN);

    public UIButtonEntity(EntityType<? extends UIButtonEntity> type, Level level) {
        super(type, level);
        noPhysics = true;
        setNoGravity(true);
    }

    private String label="";
    public boolean deletesWorld() { return entityData.get(DELETES_WORLD); }

    @Override
    protected void defineSynchedData() {
        entityData.define(DELETES_WORLD, false);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        entityData.set(DELETES_WORLD, tag.getBoolean("DeletesWorld"));label=tag.getString("Label");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putBoolean("DeletesWorld", deletesWorld());tag.putString("Label",label);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!level().isClientSide && source.getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {
            LimboButtonEvents.onPressed(player, this);
        }
        return false;
    }

    @Override public boolean isPickable() { return true; }
    @Override public boolean isPushable() { return false; }
    @Override public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}

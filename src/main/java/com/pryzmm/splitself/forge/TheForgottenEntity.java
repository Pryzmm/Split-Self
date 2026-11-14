package com.pryzmm.splitself.forge;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public final class TheForgottenEntity extends Monster {
    private static final EntityDataAccessor<Integer> TYPE = SynchedEntityData.defineId(
            TheForgottenEntity.class, EntityDataSerializers.INT);
    public final AnimationState idleAnimationState = new AnimationState();
    public TheForgottenEntity(EntityType<? extends Monster> type, Level level) { super(type, level); }
    static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 1024);
    }
    @Override protected void registerGoals() {
        goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 100, 1));
    }
    @Override protected void defineSynchedData() { super.defineSynchedData(); entityData.define(TYPE, 0); }
    int encounterType() { return entityData.get(TYPE); }
    void setEncounterType(int value) { entityData.set(TYPE, Math.floorMod(value, 3)); }
    @Override public void tick() {
        setNoGravity(true);
        super.tick();
        if (level().isClientSide) idleAnimationState.startIfStopped(tickCount);
    }
    @Override public void onAddedToWorld() {
        super.onAddedToWorld();
        if (level().isClientSide) return;
        // Source singleton spans dimensions. Keep ownership inside this server instance.
        var previous = new java.util.ArrayList<TheForgottenEntity>();
        for (var world : getServer().getAllLevels()) {
            for (var entity : world.getAllEntities()) {
                if (entity instanceof TheForgottenEntity other && other != this) previous.add(other);
            }
        }
        previous.forEach(TheForgottenEntity::discard);
    }
    @Override public boolean hurt(DamageSource source, float amount) {
        return source.is(DamageTypes.GENERIC_KILL) && super.hurt(source, amount);
    }
    @Override public boolean removeWhenFarAway(double distance) { return false; }
    @Override public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag); tag.putInt("Type", encounterType());
    }
    @Override public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag); setEncounterType(tag.getInt("Type"));
    }
}

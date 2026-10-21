package com.pryzmm.splitself.forge;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.*;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/** Sleep-scene actor. Outside-sleep encounters remain a separate porting task. */
public final class TheOtherEntity extends Monster {
    private static final EntityDataAccessor<Integer> VARIANT = SynchedEntityData.defineId(
            TheOtherEntity.class, EntityDataSerializers.INT);
    public final AnimationState idleAnimationState = new AnimationState();
    private long sceneExpiresAt = -1;
    private boolean ending;

    public TheOtherEntity(EntityType<? extends Monster> type, Level level) { super(type, level); }

    static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes().add(Attributes.FOLLOW_RANGE, 100)
                .add(Attributes.MAX_HEALTH, 1024).add(Attributes.ATTACK_DAMAGE, 1)
                .add(Attributes.MOVEMENT_SPEED, 0);
    }

    @Override protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 100, 1));
    }

    @Override protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(VARIANT, 0);
    }

    public int variant() { return Math.floorMod(entityData.get(VARIANT) & 255, 3); }
    void configureVariant(int value) {
        entityData.set(VARIANT, Math.floorMod(value, 3));
        goalSelector.getAvailableGoals().stream().toList().forEach(goal -> goalSelector.removeGoal(goal.getGoal()));
        targetSelector.getAvailableGoals().stream().toList().forEach(goal -> targetSelector.removeGoal(goal.getGoal()));
        registerGoals();
        if (variant() == 1) {
            goalSelector.addGoal(1, new MeleeAttackGoal(this, 1, false));
            goalSelector.addGoal(2, new RandomStrollGoal(this, 1));
            targetSelector.addGoal(1, new net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal<>(this, Player.class, false));
        }
        getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(variant() == 1 ? 0.5 : 0);
        getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(variant() == 1 ? 10 : 1);
    }

    @Override public SpawnGroupData finalizeSpawn(net.minecraft.world.level.ServerLevelAccessor world,
            net.minecraft.world.DifficultyInstance difficulty, MobSpawnType reason,
            SpawnGroupData data, CompoundTag tag) {
        if (world instanceof net.minecraft.server.level.ServerLevel level)
            configureVariant(MemoryData.get(level).sleepStage() >= 2 ? 1 : 0);
        return super.finalizeSpawn(world, difficulty, reason, data, tag);
    }

    @Override protected float getJumpPower() { return variant() == 1 ? 0.42f : 0; }

    void expireAfter(long ticks) { sceneExpiresAt = level().getGameTime() + ticks; }

    @Override public void tick() {
        // 1.20.1 has no gravity attribute. Sleep actors have no gravity in Limbo.
        setNoGravity(level().dimension().equals(SplitSelfWorldEvents.LIMBO));
        super.tick();
        if (level().isClientSide) {
            idleAnimationState.startIfStopped(tickCount);
            return;
        }
        if (sceneExpiresAt >= 0 && level().getGameTime() >= sceneExpiresAt) { discard(); return; }
        if (variant() == 2) return;
        if (!level().dimension().equals(SplitSelfWorldEvents.LIMBO)) {
            if (!ending) {
                double radius = variant() == 1 ? 4 : 10;
                for (var player : level().getEntitiesOfClass(net.minecraft.server.level.ServerPlayer.class,
                        getBoundingBox().inflate(10), LivingEntity::isAlive)) {
                    if (distanceTo(player) >= radius) continue;
                    ending = true;
                    StoryNetwork.clientEvent(player, "THE_OTHER");
                    player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                            net.minecraft.world.effect.MobEffects.WITHER, 100, 1, false, false, false));
                    expireAfter(100);
                    break;
                }
            }
            return;
        }
        for (Player player : level().getEntitiesOfClass(Player.class, getBoundingBox().inflate(10), LivingEntity::isAlive)) {
            if (distanceTo(player) >= 3) continue;
            if (getX() >= 1500) getServer().getPlayerList().broadcastSystemMessage(
                    Component.literal("<" + player.getName().getString() + "> ").append(
                            Component.translatable("events.splitself.sleep.remember")), false);
            level().playSound(null, player.blockPosition(), SoundEvent.createVariableRangeEvent(
                    new ResourceLocation("splitself", "disappear")), SoundSource.MASTER, 1, 1);
            discard();
            break;
        }
    }

    @Override public boolean hurt(DamageSource source, float amount) {
        return source.is(DamageTypes.GENERIC_KILL)
                ? super.hurt(source, amount) : false;
    }
    @Override public boolean removeWhenFarAway(double distance) { return false; }
    @Override public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Variant", entityData.get(VARIANT));
        tag.putLong("SceneExpiresAt", sceneExpiresAt);
        tag.putBoolean("Ending", ending);
    }
    @Override public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        configureVariant(tag.getInt("Variant"));
        ending = tag.getBoolean("Ending");
        sceneExpiresAt = tag.contains("SceneExpiresAt") ? tag.getLong("SceneExpiresAt") : -1;
    }
}

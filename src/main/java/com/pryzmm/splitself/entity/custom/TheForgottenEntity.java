package com.pryzmm.splitself.entity.custom;

import net.minecraft.entity.*;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class TheForgottenEntity extends HostileEntity {

    public final AnimationState idleAnimationState = new AnimationState();

    private int playerUpdateTimer = 0;
    private static final int PLAYER_UPDATE_INTERVAL = 20;

    public void setupGoals() {
        this.goalSelector.add(3, new LookAtEntityGoal(this, PlayerEntity.class, 100.0F, 1F));
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_GRAVITY, 0)
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 1024);
    }

    public void tick() {
        super.tick();
        if (this.getWorld().isClient()) {
            this.idleAnimationState.startIfNotRunning(this.age);
            if (this.playerUpdateTimer <= 0) {
                this.playerUpdateTimer = PLAYER_UPDATE_INTERVAL;
            } else {
                --this.playerUpdateTimer;
            }
        }
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (source.isOf(DamageTypes.GENERIC_KILL)) {
            return super.damage(source, amount);
        }
        return false;
    }

    public TheForgottenEntity(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
        setupGoals();
    }

    @Override
    public @Nullable EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData) {
        setupGoals();
        return super.initialize(world, difficulty, spawnReason, entityData);
    }
}
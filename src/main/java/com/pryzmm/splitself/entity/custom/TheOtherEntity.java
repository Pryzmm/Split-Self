// TheOtherEntity.java
package com.pryzmm.splitself.entity.custom;

import net.minecraft.entity.AnimationState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

public class TheOtherEntity extends HostileEntity {

    public final AnimationState idleAnimationState = new AnimationState();

    // Cache the nearest player and update periodically
    private PlayerEntity cachedNearestPlayer = null;
    private int playerUpdateTimer = 0;
    private static final int PLAYER_UPDATE_INTERVAL = 20; // Update every 20 ticks (1 second)

    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(1, new LookAtEntityGoal(this, PlayerEntity.class, 100.0F, 1F));
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 0)
                .add(EntityAttributes.GENERIC_JUMP_STRENGTH, 0)
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 1024)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0);
    }

    public PlayerEntity getNearestPlayer() {
        return this.cachedNearestPlayer;
    }

    public void tick() {
        super.tick();

        if (this.getWorld().isClient()) {
            this.idleAnimationState.startIfNotRunning(this.age);

            // Update nearest player periodically
            if (this.playerUpdateTimer <= 0) {
                this.cachedNearestPlayer = this.getWorld().getClosestPlayer(this, -1.0);
                this.playerUpdateTimer = PLAYER_UPDATE_INTERVAL;
            } else {
                --this.playerUpdateTimer;
            }
        }
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        return false;
    }

    public TheOtherEntity(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
    }
}
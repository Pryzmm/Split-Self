package com.pryzmm.splitself.entity.custom;

import com.pryzmm.splitself.events.ScreenOverlay;
import com.pryzmm.splitself.world.DimensionRegistry;
import net.minecraft.entity.AnimationState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TheOtherEntity extends HostileEntity {

    public final AnimationState idleAnimationState = new AnimationState();

    private PlayerEntity cachedNearestPlayer = null;
    private int playerUpdateTimer = 0;
    private static final int PLAYER_UPDATE_INTERVAL = 20;
    public static Map<Entity, Integer> toBeDiscarded = new HashMap<>();

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
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0)
                .add(EntityAttributes.GENERIC_GRAVITY, 0);
    }

    public PlayerEntity getNearestPlayer() {
        return this.cachedNearestPlayer;
    }

    public void tick() {
        super.tick();

        if (this.getWorld().isClient()) {
            this.idleAnimationState.startIfNotRunning(this.age);
            if (this.playerUpdateTimer <= 0) {
                this.cachedNearestPlayer = this.getWorld().getClosestPlayer(this, -1.0);
                this.playerUpdateTimer = PLAYER_UPDATE_INTERVAL;
            } else {
                --this.playerUpdateTimer;
            }
        }

        List<PlayerEntity> nearbyPlayers = this.getWorld().getEntitiesByClass(
                PlayerEntity.class,
                this.getBoundingBox().expand(10.0),
                LivingEntity::isAlive
        );

        if (!this.getWorld().isClient && this.getWorld() == this.getWorld().getServer().getWorld(DimensionRegistry.LIMBO_DIMENSION_KEY)) {
            this.noClip = true;
            for (PlayerEntity player : nearbyPlayers) {
                double distance = this.distanceTo(player);
                if (distance < 3.0) {
                    this.discard();
                }
            }
        } else if (!this.getWorld().isClient && this.getWorld() != this.getWorld().getServer().getWorld(DimensionRegistry.LIMBO_DIMENSION_KEY)) {

            for (PlayerEntity player : nearbyPlayers) {
                double distance = this.distanceTo(player);
                if (distance < 10.0 && !toBeDiscarded.containsKey(this)) {
                    toBeDiscarded.put(this, 1);
                    ScreenOverlay.executeTheOtherScreen(player);
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 100, 1, false, false, false));
                    new Thread(() -> {
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        this.discard();
                    }).start();
                }
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

    public TheOtherEntity(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
    }
}
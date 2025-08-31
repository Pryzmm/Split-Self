package com.pryzmm.splitself.entity.custom;

import com.pryzmm.splitself.SplitSelf;
import com.pryzmm.splitself.SplitSelfClient;
import com.pryzmm.splitself.events.ScreenOverlay;
import com.pryzmm.splitself.sound.ModSounds;
import com.pryzmm.splitself.world.DimensionRegistry;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class TheOtherEntity extends HostileEntity {

    private static final TrackedData<Integer> DATA_ID_TYPE_VARIANT =
            DataTracker.registerData(TheOtherEntity.class, TrackedDataHandlerRegistry.INTEGER);

    public final AnimationState idleAnimationState = new AnimationState();

    private PlayerEntity cachedNearestPlayer = null;
    private int playerUpdateTimer = 0;
    private static final int PLAYER_UPDATE_INTERVAL = 20;
    public static Map<Entity, Integer> toBeDiscarded = new HashMap<>();

    public void setupGoals() { // known bug; either goals, attributes or both aren't being properly applied on game restart, leading to twitching variant not moving
        TheOtherVariant variant = this.getVariant();
        switch (variant) {
            case TWITCHING:
                this.goalSelector.add(0, new SwimGoal(this));
                this.goalSelector.add(1, new MeleeAttackGoal(this, 1.0, false));
                this.targetSelector.add(1, new ActiveTargetGoal<>(this, PlayerEntity.class, false));
                this.goalSelector.add(2, new WanderAroundGoal(this, 1F));
                this.goalSelector.add(3, new LookAtEntityGoal(this, PlayerEntity.class, 100.0F, 1F));
                break;
            case DEFAULT:
            default:
                this.goalSelector.add(0, new SwimGoal(this));
                this.goalSelector.add(3, new LookAtEntityGoal(this, PlayerEntity.class, 100.0F, 1F));
                break;
        }
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 100)
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 1024)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 0);
    }

    private void applyVariantAttributes() {
        TheOtherVariant variant = this.getVariant();
        World world = this.getWorld();
        if (world == Objects.requireNonNull(this.getServer()).getWorld(DimensionRegistry.LIMBO_DIMENSION_KEY)) {
            Objects.requireNonNull(this.getAttributeInstance(EntityAttributes.GENERIC_GRAVITY)).setBaseValue(0);
        }
        switch (variant) {
            case TWITCHING:
                Objects.requireNonNull(this.getAttributeInstance(EntityAttributes.GENERIC_JUMP_STRENGTH)).setBaseValue(0.42);
                Objects.requireNonNull(this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED)).setBaseValue(0.5);
                Objects.requireNonNull(this.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE)).setBaseValue(10);
                Objects.requireNonNull(this.getAttributeInstance(EntityAttributes.GENERIC_WATER_MOVEMENT_EFFICIENCY)).setBaseValue(10);
                break;
            case DEFAULT:
            default:
                Objects.requireNonNull(this.getAttributeInstance(EntityAttributes.GENERIC_JUMP_STRENGTH)).setBaseValue(0);
                Objects.requireNonNull(this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED)).setBaseValue(0);
                break;
        }
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
            for (PlayerEntity player : nearbyPlayers) {
                double distance = this.distanceTo(player);
                if (distance < 3.0) {
                    if (this.getX() >= 1500) {
                        this.getServer().getPlayerManager().broadcast(Text.literal("<" + player.getName().getString() + "> " + SplitSelf.translate("events.splitself.sleep.remember").getString()), false);
                    }
                    this.getWorld().playSound(null, Objects.requireNonNull(player).getBlockPos(), ModSounds.DISAPPEAR, SoundCategory.MASTER, 1.0f, 1.0f);
                    this.discard();
                }
            }
        } else if (!this.getWorld().isClient && this.getWorld() != this.getWorld().getServer().getWorld(DimensionRegistry.LIMBO_DIMENSION_KEY)) {
            for (PlayerEntity player : nearbyPlayers) {
                double distance = this.distanceTo(player);
                double distanceMax;
                if (this.getVariant() == TheOtherVariant.TWITCHING) {
                    distanceMax = 4;
                } else {
                    distanceMax = 10;
                }
                if (distance < distanceMax && !toBeDiscarded.containsKey(this)) {
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

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(DATA_ID_TYPE_VARIANT, 0);
    }

    public TheOtherVariant getVariant() {
        return TheOtherVariant.byId(this.getTypeVariant() & 255);
    }

    private int getTypeVariant() {
        return this.dataTracker.get(DATA_ID_TYPE_VARIANT);
    }

    private void setTypeVariant(TheOtherVariant variant) {
        this.dataTracker.set(DATA_ID_TYPE_VARIANT, variant.getId() & 255);
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putInt("Variant", this.getTypeVariant());
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.dataTracker.set(DATA_ID_TYPE_VARIANT, nbt.getInt("Variant"));
    }

    @Override
    public @Nullable EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData) {
        com.pryzmm.splitself.world.DataTracker dataTracker = com.pryzmm.splitself.world.DataTracker.getServerState(Objects.requireNonNull(world.getServer()));
        assert SplitSelfClient.player != null;
        if (dataTracker.getPlayerSleepStage(SplitSelfClient.player.getUuid()) >= 2) {
            setTypeVariant(TheOtherVariant.TWITCHING);
        } else {
            setTypeVariant(TheOtherVariant.DEFAULT);
        }
        applyVariantAttributes();
        setupGoals();
        return super.initialize(world, difficulty, spawnReason, entityData);
    }
}
package com.pryzmm.splitself.entity.custom;

import com.pryzmm.splitself.client.SplitSelfClient;
import com.pryzmm.splitself.packet.packets.FinalePacket;
import com.pryzmm.splitself.world.DimensionRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class UIButtonEntity extends HostileEntity {

    private static final TrackedData<String> LABEL = DataTracker.registerData(UIButtonEntity.class, TrackedDataHandlerRegistry.STRING);
    private static final TrackedData<Boolean> DELETES_WORLD = DataTracker.registerData(UIButtonEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (source.getAttacker() instanceof ServerPlayerEntity p) handleClick(p);
        if (source.isOf(DamageTypes.GENERIC_KILL)) return super.damage(source, amount);
        return false;
    }

    private static boolean clickedAnyButton = false;
    public void handleClick(ServerPlayerEntity player) {
        if (clickedAnyButton) return;
        if (player.getServerWorld().getRegistryKey() != DimensionRegistry.LIMBO_DIMENSION_KEY) return;
        clickedAnyButton = true;
        MinecraftServer server = player.getServer();
        assert server != null;
        player.getServerWorld().getEntitiesByClass(DisplayEntity.TextDisplayEntity.class, new Box(new Vec3d(3999, 13, 13), new Vec3d(4019, 0, 0)), (e) -> true).forEach(Entity::discard);
        player.getServerWorld().getEntitiesByClass(UIButtonEntity.class, new Box(new Vec3d(3999, 13, 13), new Vec3d(4019, 0, 0)), (e) -> true).forEach(Entity::discard);
        if (this.deletesWorld()) {
            server.getPlayerManager().getPlayerList().forEach(pl -> ServerPlayNetworking.send(pl, new FinalePacket(false, true)));
        } else {
            server.getPlayerManager().getPlayerList().forEach(pl -> ServerPlayNetworking.send(pl, new FinalePacket(false, false)));
        }
    }

    public UIButtonEntity(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
        this.noClip = true;
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return MobEntity.createMobAttributes().add(EntityAttributes.GENERIC_GRAVITY, 0);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(LABEL, "");
        builder.add(DELETES_WORLD, false);
    }

    public String getLabel() { return this.dataTracker.get(LABEL); }
    public void setLabel(String label) { this.dataTracker.set(LABEL, label); }

    public boolean deletesWorld() { return this.dataTracker.get(DELETES_WORLD); }
    public void setDeletesWorld(boolean value) { this.dataTracker.set(DELETES_WORLD, value); }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        this.setLabel(nbt.getString("Label"));
        this.setDeletesWorld(nbt.getBoolean("DeletesWorld"));
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        nbt.putString("Label", this.getLabel());
        nbt.putBoolean("DeletesWorld", this.deletesWorld());
    }

    @Override
    public boolean isCollidable() { return false; }
    @Override
    public boolean collidesWith(Entity other) { return false; }
    @Override
    public boolean isPushable() { return false; }

    @Override
    public @Nullable EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData) {
        assert SplitSelfClient.player != null;
        return super.initialize(world, difficulty, spawnReason, entityData);
    }

}
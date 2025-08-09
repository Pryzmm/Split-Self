package com.pryzmm.splitself.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.pryzmm.splitself.SplitSelf;
import com.pryzmm.splitself.file.DesktopFileUtil;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.Random;

public class KickScreen extends Screen {

    public static final Identifier OVERLAY_IMAGE = Identifier.of(SplitSelf.MOD_ID, "textures/screen/dirt_background.png");

    public KickScreen() {
        super(Text.translatable("disconnect.lost"));
    }

    @Override
    protected void init() {
        this.addDrawableChild(ButtonWidget.builder(
                        Text.translatable("gui.toTitle"),
                        button -> {
                            ServerPlayerEntity Player = this.client.getServer().getPlayerManager().getPlayer(this.client.player.getUuid());
                            Vec3d playerPos = new Vec3d(
                                    Player.getPos().x,
                                    Player.getPos().y,
                                    Player.getPos().z
                            );
                            Vec3d pos1 = new Vec3d(
                                    playerPos.x - 8,
                                    playerPos.y - 8,
                                    playerPos.z - 8
                            );
                            Vec3d pos2 = new Vec3d(
                                    playerPos.x + 7,
                                    playerPos.y + 7,
                                    playerPos.z + 7
                            );
                            Random random = new Random();
                            for (int y = (int) pos1.getY(); y <= (int) pos2.getY(); y++) {
                                for (int x = (int) pos1.getX(); x <= (int) pos2.getX(); x++) {
                                    for (int z = (int) pos1.getZ(); z <= (int) pos2.getZ(); z++) {
                                        BlockPos pos = new BlockPos(x, y, z);
                                        if (!Player.getWorld().getBlockState(pos).isAir() && random.nextBoolean()) {
                                            Player.getWorld().setBlockState(pos, Blocks.AIR.getDefaultState());
                                        }
                                    }
                                }
                            }
                            this.close();
                        }
                ).position((this.width / 2) - 100, (this.height / 2) + 15)
                .size(200, 20)
                .build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderOverlayImage(context, this.width, this.height, OVERLAY_IMAGE);
        super.render(context, mouseX, mouseY, delta);
        renderOverlayContent(context, this.width, this.height);
    }

    public void renderOverlayContent(DrawContext drawContext, int screenWidth, int screenHeight) {
        Text connectionLostText = Text.translatable("disconnect.lost").formatted(Formatting.GRAY);
        int connectionLostWidth = this.textRenderer.getWidth(connectionLostText);
        drawContext.drawTextWithShadow(
                this.textRenderer,
                connectionLostText,
                (screenWidth - connectionLostWidth) / 2,
                (screenHeight / 2) - 15,
                0xFFFFFF
        );

        Text timedOutText = Text.translatable("disconnect.timeout");
        int timedOutWidth = this.textRenderer.getWidth(timedOutText);
        drawContext.drawTextWithShadow(
                this.textRenderer,
                timedOutText,
                (screenWidth - timedOutWidth) / 2,
                screenHeight / 2,
                0xFFFFFF
        );
    }

    public void renderOverlayImage(DrawContext drawContext, int screenWidth, int screenHeight, Identifier image) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        drawContext.drawTexture(image, 0, 0, screenWidth, screenHeight, 0, 0, 1920, 1080, 1920, 1080);

        RenderSystem.disableBlend();
    }

    @Override
    public boolean shouldPause() {return true;}

    @Override
    public boolean shouldCloseOnEsc() {return false;}

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {}
}
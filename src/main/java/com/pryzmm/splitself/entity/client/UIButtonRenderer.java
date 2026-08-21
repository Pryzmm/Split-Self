package com.pryzmm.splitself.entity.client;

import com.pryzmm.splitself.SplitSelf;
import com.pryzmm.splitself.entity.custom.UIButtonEntity;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;

public class UIButtonRenderer extends MobEntityRenderer<UIButtonEntity, EntityModel<UIButtonEntity>> {

    private final TextRenderer textRenderer;

    public UIButtonRenderer(EntityRendererFactory.Context context) {
        super(context, new UIButtonModel<>(context.getPart(UIButtonModel.UIBUTTON)), 0.6f);
        this.textRenderer = context.getTextRenderer();
    }

    @Override
    public void render(UIButtonEntity entity, float f, float g, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int i) {
        super.render(entity, f, g, matrices, vertexConsumers, LightmapTextureManager.pack(15, 15));
        this.shadowRadius = 0.0F;
        this.shadowOpacity = 0.0F;
        matrices.push();
        matrices.translate(0, 0.225, 1.02);
        matrices.scale(1.0f / 64.0f, -1.0f / 64.0f, 1.0f / 64.0f);

        Text text;
        if (entity.deletesWorld()) text = Text.translatable("block.splitself.ui_button.delete");
        else text = Text.translatable("block.splitself.ui_button.cancel");
        Matrix4f positionMatrix = matrices.peek().getPositionMatrix();
        positionMatrix.translate(0, 0, -0.1f);

        float textWidth = textRenderer.getWidth(text);
        float x = (-textWidth / 2.0f);
        float y = 0f;

        textRenderer.draw(text, x, y, 0xFFFFFFFF, false, positionMatrix, vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0, LightmapTextureManager.pack(15, 15));

        matrices.pop();
    }

    @Override
    public Identifier getTexture(UIButtonEntity entity) {
        return Identifier.of(SplitSelf.MOD_ID, "textures/entity/ui_button/ui_button.png");
    }
}
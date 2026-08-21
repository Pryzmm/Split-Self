package com.pryzmm.splitself.entity.client;

import com.pryzmm.splitself.SplitSelf;
import com.pryzmm.splitself.entity.custom.UIButtonEntity;
import net.minecraft.client.model.*;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class UIButtonModel<T extends UIButtonEntity> extends SinglePartEntityModel<T> {

	public static final EntityModelLayer UIBUTTON = new EntityModelLayer(Identifier.of(SplitSelf.MOD_ID, "ui_button"), "main");

	private final ModelPart modelPart;

	public UIButtonModel(ModelPart root) {
		this.modelPart = root.getChild("bone");
	}

	public static TexturedModelData getTexturedModelData() {
		ModelData modelData = new ModelData();
		ModelPartData modelPartData = modelData.getRoot();
		ModelPartData bone = modelPartData.addChild("bone", ModelPartBuilder.create().uv(0, 0).cuboid(-24.0F, -5.0F, -8.0F, 32.0F, 6.0F, 0.03F, new Dilation(0.0F)), ModelTransform.pivot(8.0F, 24.0F, -8.0F));
		return TexturedModelData.of(modelData, 64, 64);
	}

	@Override
	public void setAngles(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {}

	@Override
	public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, int color) {
		this.modelPart.render(matrices, vertices, light, overlay);
	}

	@Override
	public ModelPart getPart() {
		return modelPart;
	}

}
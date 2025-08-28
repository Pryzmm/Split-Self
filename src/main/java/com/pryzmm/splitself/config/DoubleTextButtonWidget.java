package com.pryzmm.splitself.config;

import com.pryzmm.splitself.SplitSelf;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.apache.http.util.TextUtils;

import java.util.function.Supplier;

public class DoubleTextButtonWidget extends ButtonWidget {
    private final Supplier<String> rightTextSupplier;
    private final Supplier<Integer> rightTextColorSupplier;

    public DoubleTextButtonWidget(int x, int y, int width, int height, Text leftText, Supplier<String> rightTextSupplier, Supplier<Integer> rightTextColorSupplier, String tooltipTranslatableKey, PressAction onPress) {
        super(x, y, width, height, leftText, onPress, DEFAULT_NARRATION_SUPPLIER);
        this.rightTextSupplier = rightTextSupplier;
        this.rightTextColorSupplier = rightTextColorSupplier;
        if (!TextUtils.isEmpty(tooltipTranslatableKey)) {
            this.setTooltip(Tooltip.of(Text.of(leftText.getString() + "\n" + SplitSelf.translate(tooltipTranslatableKey).getString())));
        }
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        context.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer, Text.empty(), this.getX() + this.width / 2, this.getY() + (this.height - 8) / 2, 0);
        if (this.isHovered()) {
            context.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, 0x80FFFFFF);
        } else {
            context.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, 0x40000000);
        }
        context.drawBorder(this.getX(), this.getY(), this.width, this.height, this.isFocused() ? 0xFFFFFFFF : 0xFF808080);
        int leftTextX = this.getX() + 4;
        int textY = this.getY() + (this.height - 8) / 2;
        context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, this.getMessage(), leftTextX, textY, 0xFFFFFF);
        String rightText = rightTextSupplier.get();
        int rightTextColor = rightTextColorSupplier.get();
        int rightTextX = this.getX() + this.width - MinecraftClient.getInstance().textRenderer.getWidth(rightText) - 4;
        context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, rightText, rightTextX, textY, rightTextColor);
    }
}

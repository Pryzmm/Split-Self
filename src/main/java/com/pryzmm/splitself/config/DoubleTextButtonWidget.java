package com.pryzmm.splitself.config;

import com.pryzmm.splitself.SplitSelf;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.apache.http.util.TextUtils;

import java.util.function.Supplier;

public class DoubleTextButtonWidget extends ButtonWidget {
    private final Supplier<String> rightTextSupplier;
    private final Supplier<Integer> rightTextColorSupplier;
    private int scrollOffset = 0;
    private long lastScrollTime = 0;
    private static final int SCROLL_DELAY = 100;
    private static final int SCROLL_SPEED = 1;
    private static final int PAUSE_DURATION = 2000;

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
        String rightText = rightTextSupplier.get();
        int rightTextWidth = MinecraftClient.getInstance().textRenderer.getWidth(rightText);
        int availableLeftTextWidth = this.width - rightTextWidth - 12; // 4px padding on each side + 4px gap

        int leftTextX = this.getX() + 4;
        int textY = this.getY() + (this.height - 8) / 2;
        String leftText = this.getMessage().getString();
        int leftTextWidth = MinecraftClient.getInstance().textRenderer.getWidth(leftText);
        if (leftTextWidth > availableLeftTextWidth) {
            updateScrollOffset(leftTextWidth, availableLeftTextWidth);
            context.enableScissor(this.getX() + 4, this.getY(), this.getX() + 4 + availableLeftTextWidth, this.getY() + this.height);
            context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, leftText, leftTextX - scrollOffset, textY, 0xFFFFFF);
            context.disableScissor();
        } else {
            scrollOffset = 0;
            context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, leftText, leftTextX, textY, 0xFFFFFF);
        }
        int rightTextColor = rightTextColorSupplier.get();
        int rightTextX = this.getX() + this.width - rightTextWidth - 4;
        context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, rightText, rightTextX, textY, rightTextColor);
    }

    private void updateScrollOffset(int textWidth, int availableWidth) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastScrollTime < SCROLL_DELAY) {
            return;
        }
        lastScrollTime = currentTime;
        int maxScroll = textWidth - availableWidth;
        long totalCycleTime = (maxScroll * 2 * SCROLL_DELAY / SCROLL_SPEED) + (PAUSE_DURATION * 2);
        long cyclePosition = currentTime % totalCycleTime;
        if (cyclePosition < PAUSE_DURATION) {
            scrollOffset = 0;
        } else if (cyclePosition < PAUSE_DURATION + (maxScroll * SCROLL_DELAY / SCROLL_SPEED)) {
            long scrollTime = cyclePosition - PAUSE_DURATION;
            scrollOffset = (int) (scrollTime * SCROLL_SPEED / SCROLL_DELAY);
            scrollOffset = MathHelper.clamp(scrollOffset, 0, maxScroll);
        } else if (cyclePosition < PAUSE_DURATION * 2 + (maxScroll * SCROLL_DELAY / SCROLL_SPEED)) {
            scrollOffset = maxScroll;
        } else {
            long scrollTime = cyclePosition - (PAUSE_DURATION * 2 + (maxScroll * SCROLL_DELAY / SCROLL_SPEED));
            scrollOffset = maxScroll - (int) (scrollTime * SCROLL_SPEED / SCROLL_DELAY);
            scrollOffset = MathHelper.clamp(scrollOffset, 0, maxScroll);
        }
    }
}
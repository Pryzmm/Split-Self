package com.pryzmm.splitself.forge;

import java.util.Locale;
import net.minecraft.core.Direction;

final class FrameImageRules {
    static boolean imageName(String name){String lower=name.toLowerCase(Locale.ROOT);return lower.endsWith(".png")||lower.endsWith(".jpg")||lower.endsWith(".jpeg");}
    static int rotation(Direction facing){return switch(facing){case SOUTH->180;case EAST->270;case WEST->90;default->0;};}
    static boolean allowed(boolean external,boolean pii){return external&&pii;}
    private FrameImageRules(){}
}

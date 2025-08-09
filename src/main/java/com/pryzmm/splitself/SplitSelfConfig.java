package com.pryzmm.splitself;

import me.fzzyhmstrs.fzzy_config.annotations.ClientModifiable;
import me.fzzyhmstrs.fzzy_config.config.Config;
import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedBoolean;
import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedDouble;
import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedInt;
import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedNumber;
import net.minecraft.util.Identifier;

public class SplitSelfConfig extends Config {

    @ClientModifiable
    public ValidatedBoolean eventsEnabled = new ValidatedBoolean(true);
    @ClientModifiable
    public ValidatedInt eventTickInterval = new ValidatedInt(20, 1000000, 1, ValidatedNumber.WidgetType.TEXTBOX);
    @ClientModifiable
    public ValidatedDouble eventChance = new ValidatedDouble(0.003, 1, 0, ValidatedNumber.WidgetType.TEXTBOX);
    @ClientModifiable
    public ValidatedInt eventCooldown = new ValidatedInt(600, 1000000, 0, ValidatedNumber.WidgetType.TEXTBOX);
    @ClientModifiable
    public ValidatedInt startEventsAfter = new ValidatedInt(3000, 1000000, 0, ValidatedNumber.WidgetType.TEXTBOX);

    public SplitSelfConfig() {
        super(Identifier.of(SplitSelf.MOD_ID, "config"));
    }
}
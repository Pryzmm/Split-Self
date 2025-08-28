package com.pryzmm.splitself.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModMenuApiImpl implements ModMenuApi {
    private static final Logger LOGGER = LoggerFactory.getLogger("SplitSelf-ModMenu");

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return CustomConfigScreen::new;
    }
}
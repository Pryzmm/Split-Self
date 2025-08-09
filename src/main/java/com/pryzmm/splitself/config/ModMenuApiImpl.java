package com.pryzmm.splitself.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.minecraft.client.gui.screen.Screen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModMenuApiImpl implements ModMenuApi {
    private static final Logger LOGGER = LoggerFactory.getLogger("SplitSelf-ModMenu");

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            try {
                Class.forName("dev.isxander.yacl3.api.YetAnotherConfigLib");
            } catch (ClassNotFoundException e) {
                LOGGER.error("YACL class NOT found: {}", e.getMessage());
            }

            SplitSelfConfig config = SplitSelfConfig.getInstance();
            if (config.isYACLAvailable()) {
                try {
                    Class<?> yaclConfigClass = Class.forName("com.pryzmm.splitself.config.SplitSelfYACLConfig");
                    return (Screen) yaclConfigClass
                            .getMethod("createScreen", Screen.class)
                            .invoke(null, parent);
                } catch (Exception e) {
                    return parent;
                }
            } else {
                return parent;
            }
        };
    }
}
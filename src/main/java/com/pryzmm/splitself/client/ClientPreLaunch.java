package com.pryzmm.splitself.client;

import com.pryzmm.splitself.SplitSelf;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import java.awt.*;

public class ClientPreLaunch implements PreLaunchEntrypoint {

    @Override
    public void onPreLaunch() {
        System.setProperty("java.awt.headless", "false");
        boolean headless = GraphicsEnvironment.isHeadless();
        SplitSelf.LOGGER.info("Forced AWT headless cache to {}", headless);
    }

}

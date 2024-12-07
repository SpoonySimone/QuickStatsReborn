package me.spoony.quickStatsReborn.util;

import cc.polyfrost.oneconfig.events.EventManager;
import cc.polyfrost.oneconfig.events.event.LocrawEvent;
import cc.polyfrost.oneconfig.libs.eventbus.Subscribe;
import cc.polyfrost.oneconfig.utils.hypixel.LocrawInfo;
import cc.polyfrost.oneconfig.utils.hypixel.LocrawUtil;
import me.spoony.quickStatsReborn.QuickStatsReborn;
import me.spoony.quickStatsReborn.config.ModConfig;

public class LocrawRetriever {
    public static LocrawInfo locraw;
    public static String gameType;
    public static boolean lobby = false;

    public LocrawRetriever() {
        EventManager.INSTANCE.register(this);
    }

    @Subscribe
    private void onLocraw(LocrawEvent event) {
        LocrawInfo locraw = LocrawUtil.INSTANCE.getLocrawInfo();

        try {
            if (locraw != null) {
                gameType = locraw.getGameMode();
                if (gameType.equalsIgnoreCase("lobby")) {
                    lobby = true;
                    if (ModConfig.debugMode) {
                        QuickStatsReborn.LOGGER.info("detected this as a lobby");
                    }
                } else {
                    lobby = false;
                }
            } else {
                gameType = "DEFAULT";
            }


            if (ModConfig.debugMode) {
                QuickStatsReborn.LOGGER.info(gameType);
            }
        } catch (Exception e) {
            if (ModConfig.debugMode) {
                e.printStackTrace();
            }
            gameType = "DEFAULT";
        }
    }
}
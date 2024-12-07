package me.spoony.quickStatsReborn.util;

import cc.polyfrost.oneconfig.events.EventManager;
import cc.polyfrost.oneconfig.events.event.LocrawEvent;
import cc.polyfrost.oneconfig.libs.eventbus.Subscribe;
import cc.polyfrost.oneconfig.utils.hypixel.LocrawInfo;
import cc.polyfrost.oneconfig.utils.hypixel.LocrawUtil;

public class LocrawUtil {
    public static LocrawInfo locraw;
    public static String gameType;
    public static boolean lobby = false;

    public LocrawUtil() {
        EventManager.INSTANCE.register(this);
    }

    @Subscribe
    private void onLocraw(LocrawEvent event) {
        LocrawInfo locraw = LocrawUtil.INSTANCE.getLocrawInfo();

        if (locraw != null) {
            gameType = locraw.getGameMode();
        }
    }
}
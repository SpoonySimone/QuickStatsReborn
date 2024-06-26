package me.spoony.quickStatsReborn.util;

import me.spoony.quickStatsReborn.QuickStatsReborn;

import java.net.URL;
import java.util.Properties;

public class Updater {
    public static String latestVersion;

    public static boolean checkUpdate(String currentVersion) {
        try {
            Properties prop = new Properties();
            prop.load(new URL(
                    "https://raw.githubusercontent.com/SpoonySimone/QuickStatsReborn/master/gradle.properties")
                    .openStream());
            latestVersion = prop.getProperty("mod_version");
            if (latestVersion.equals("0")) {
                QuickStatsReborn.LOGGER.warn(
                        "version checker is 0. This is a feature added to prevent errors. Version checker disabled.");
                return false;
            }
            if (currentVersion.contains("beta")) {
                QuickStatsReborn.LOGGER.warn("beta build detected. This build might be unstable, use at your own risk!");
                QuickStatsReborn.betaFlag = true;
            }
            if (!currentVersion.equals(latestVersion)) {
                QuickStatsReborn.LOGGER.warn("a newer version " + latestVersion + " is available! Please consider updating! ("
                        + currentVersion + ")");
                return true;
            } else {
                QuickStatsReborn.LOGGER.info("already using the newest version (" + latestVersion + ")");
                return false;
            }
        } catch (Exception e) {
            // e.printStackTrace();
            QuickStatsReborn.LOGGER.error(e);
            QuickStatsReborn.LOGGER.error("failed to check version. assuming latest version.");
            return false;
        }
    }
}

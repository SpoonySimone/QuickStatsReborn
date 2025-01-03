package me.spoony.quickStatsReborn.util;

import me.spoony.quickStatsReborn.QuickStatsReborn;
import me.spoony.quickStatsReborn.Reference;
import me.spoony.quickStatsReborn.config.ModConfig;
import net.minecraft.crash.CrashReport;
import net.minecraft.util.ReportedException;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

public class HashChecker {
    public static boolean mismatch = false;
    private static String url;
    public static String hash;

    public static void checkAuth(@NotNull String filename) {
        if (ModConfig.securityLevel == 0) {
            return;
        }
        try (FileInputStream inputStream = new FileInputStream(filename)) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytesBuffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = inputStream.read(bytesBuffer)) != -1) {
                digest.update(bytesBuffer, 0, bytesRead);
            }

            byte[] hashedBytes = digest.digest();

            hash = convertByteArrayToHexString(hashedBytes);
            url = "https://raw.githubusercontent.com/SpoonySimone/QuickStatsReborn/master/hashes/list";
            BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(url).openStream()));
            List<String> expectedHashes = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                expectedHashes.add(line.trim());
            }

            if (ModConfig.debugMode) {
                QuickStatsReborn.LOGGER.debug("Generated hash: " + hash + " from file (this) " + filename);
                QuickStatsReborn.LOGGER.debug("Fetched hashes " + expectedHashes + " from URL " + url);
            }
            if (!expectedHashes.contains(hash)) {
                QuickStatsReborn.LOGGER.error("Mismatch in the provided hashes. This means that the mod has been modified.");
                QuickStatsReborn.LOGGER.error("Local hash that was expected: " + hash + "\tNone of the hashes received from the mirror match.");
                QuickStatsReborn.LOGGER.error("Your account information could be at risk to attackers who modified this code!");
                QuickStatsReborn.LOGGER.error("Make sure that you downloaded the mod from the OFFICIAL mirror, and try again. " + Reference.URL);
                mismatch = true;
                if (ModConfig.securityLevel == 3) {
                    throw new ReportedException(new CrashReport("[QuickStatsReborn] Mod hash mismatch. Game start halted.", new SecurityException("Hash mismatch of the mod. Game was halted startup to prevent data theft.")));
                }
            } else {
                QuickStatsReborn.LOGGER.info("Local hash is contained in the ones received from remote.");
            }
        } catch (Exception e) {
            if (e instanceof MalformedURLException || e instanceof FileNotFoundException) {
                QuickStatsReborn.LOGGER.error("hash file doesn't exist on mirror! Skipping!");
                if (ModConfig.debugMode) {
                    QuickStatsReborn.LOGGER.warn("---- Debug Information ----");
                    QuickStatsReborn.LOGGER.warn("Expected there to be a hash at: " + url);
                    QuickStatsReborn.LOGGER.warn("This file's hash: " + hash);
                }
                return;
            }
            e.printStackTrace();
            QuickStatsReborn.LOGGER.error("failed to generate and check hash of mod.");
        }
    }

    private static String convertByteArrayToHexString(byte[] arrayBytes) {
        StringBuilder stringBuffer = new StringBuilder();
        for (byte arrayByte : arrayBytes) {
            stringBuffer.append(Integer.toString((arrayByte & 0xff) + 0x100, 16)
                    .substring(1));
        }
        return stringBuffer.toString();
    }
}

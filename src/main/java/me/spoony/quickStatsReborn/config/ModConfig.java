package me.spoony.quickStatsReborn.config;

import cc.polyfrost.oneconfig.config.annotations.*;
import cc.polyfrost.oneconfig.config.annotations.Number;
import cc.polyfrost.oneconfig.config.annotations.Button;
import cc.polyfrost.oneconfig.config.annotations.Color;
import cc.polyfrost.oneconfig.config.annotations.HUD;
import cc.polyfrost.oneconfig.config.core.OneColor;
import cc.polyfrost.oneconfig.config.core.OneKeyBind;
import cc.polyfrost.oneconfig.libs.universal.UKeyboard;
import me.spoony.quickStatsReborn.QuickStatsReborn;
import me.spoony.quickStatsReborn.hud.HUDRenderer;
import me.spoony.quickStatsReborn.util.TickDelay;
import cc.polyfrost.oneconfig.config.Config;
import cc.polyfrost.oneconfig.config.data.Mod;
import cc.polyfrost.oneconfig.config.data.ModType;
import net.minecraft.client.Minecraft;
import net.minecraft.crash.CrashReport;
import net.minecraft.util.ReportedException;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;

import java.io.FileWriter;
import java.io.PrintStream;
import java.io.PrintWriter;


public class ModConfig extends Config {
    @Exclude
    public static boolean test = false;
    @Switch(
        name= "Enable",
        description = "Enable/disable the mod",
        category = "General", subcategory = "General"
    )
    public static boolean modEnabled = true;
    @Switch(
            name = "Automatic Game Detection",
            description = "Enable/Disable auto game detection on Hypixel.",
            category = "General", subcategory = "Game Detection"
    )
    public static boolean autoGame = true;
    @Switch(
            name = "Compatibility Fix",
            description = "Change how the automatic game utility works in an attempt to increase compatibility.\n\u00A7eExperimental!",
            category = "General", subcategory = "Game Detection"
    )
    public static boolean locrawComp = false;
    @Switch(
            name = "Sound",
            description = "Enable/Disable sound feedback of the mod on player detection.",
            category = "General", subcategory = "General"
    )
    public static boolean doSound = true;
    @Switch(
            name = "Allow On Other Servers",
            description = "Enable/Disable player detection on servers other than Hypixel.\nPlease note that it will only show only the default game.",
            category = "General", subcategory = "General"
    )
    public static boolean otherServer = true;
    @Switch(
            name = "Party Detection",
            description = "Enable/Disable detection of your name being mentioned to trigger players' stats.\n\u00A7eUseful for BedWars parties. May cause performance issues on low-end hardware!",
            category = "General", subcategory = "Parties"
    )
    public static boolean doPartyDetection = true;
    @Switch(
            name = "Party Detection++",
            description = "Enable/Disable detection of a phrase being said triggering stats.\nHave 'say <word or phrase>' in your chat message to set this.",
            category = "General", subcategory = "Parties"
    )
    public static boolean doPartyDetectionPLUS = true;
    @Dropdown(
            name = "Default Game",
            description = "Game to show stats for if nothing else is found.\nIf you want it to always show this stat, disable Automatic game detection.",
            category = "General", subcategory = "General",
            options = {"Bedwars", "Skywars", "Duels", "Quake"}
    )
    public static int defaultGame = 0;
    @Dropdown(
            name = "Default Duel",
            description = "What duel to show stats for in duel lobbies.",
            category = "General", subcategory = "General",
            options = {"Classic", "UHC", "Combo", "OP", "Blitz", "Sumo", "SkyWars", "Bridge 1v1", "Bridge 2v2"}
    )
    public static int defaultDuel = 0;
    @Slider(
            name = "Detection Distance",
            description = "Change the maximum distance a player can be detected from with the keybind.",
            category = "General", subcategory = "General",
            min = 5, max = 250,
            step = 5
    )
    public static int maxDetect = 200;


    @Switch(
            name = "Send Update Messages",
            description = "Send update messages on startup if a new version is available.",
            category = "General", subcategory = "Updates"
    )
    public static boolean sendUp = true;
    @Dropdown(
            name = "Security Level",
            description = "Level of warning to issue if a mismatched hash is detected on startup, which could suggest modification of the mod and lead to possible data theft.",
            category = "General", subcategory = "Updates",
            options = {"Off", "Warn in Logger", "Warn on world join", "Halt startup"}
    )
    public static int securityLevel = 1;


    @Switch(
            name = "Debug",
            description = "Enable/disable verbose logging to help with diagnostics.\n\u00A7eNote: You will see a lot of (useless) errors in logs with this active!",
            category = "Support", subcategory = "General"
    )
    public static boolean debugMode = false;

    @Button(
            name = "Reset Defaults",
            text = "Reset",
            description = "Reset all values to their defaults.\n \u00A7cForcibly restarts your game!",
            category = "Support", subcategory = "General"
    )
    public static void reset() {
        mc.thePlayer.closeScreen();         //TODO
        try {
            FileWriter writer = new FileWriter("./config/QuickStatsReborn.json");
            writer.write("this was cleared so it will be reset on next restart.");
            writer.close();

            QuickStatsReborn.LOGGER.warn("config file was cleared. Please restart your game.");
        } catch (Exception e) {
            QuickStatsReborn.LOGGER.error("failed to clear config, " + e);
        }
        CrashReport report = CrashReport.makeCrashReport(new Throwable() {
            @Override
            public String getMessage() {
                return "[QuickStats] Manually initiated crash: Cleaning configuration file. THIS IS NOT AN ERROR";
            }

            @Override
            public void printStackTrace(final PrintWriter s) {
                s.println(getMessage());
            }

            @Override
            public void printStackTrace(final PrintStream s) {
                s.println(getMessage());
            }
        }, "Cleaning Configuration file");
        new TickDelay(() -> {
            throw new ReportedException(report);
        }, 0);
    }


    @Button(
            name = "Open Window",
            text = "Open",
            description = "Toggle opening of the window so you can see what you are changing.\n\u00A7ePress the button again to close.",
            category = "Gui Settings"
    )
    public static void testWin() {
        if (!test) {
            test = true;
            QuickStatsReborn.GuiInst.showGUI("SpoonySimone");
        } else {
            test = false;
        }
    }

    @Button(
            name = "Reset Window",
            text = "Reset",
            description = "Reset the window to the default values for your current GUI scale.\nYou might need to reopen the GUI and\\or restart your game for it to update.",
            category = "Gui Settings"
    )
    public static void resetGUI() {
        switch (mc.gameSettings.guiScale) {
            case 0: // AUTO scale
                winMiddle = 67;
                winTop = 28;
                winBottom = 72;
                winWidth = 62;
                break;
            case 1: // SMALL
                winMiddle = 130;
                winTop = 50;
                winBottom = 145;
                winWidth = 112;
                break;
            case 2: // NORMAL
                winMiddle = 90;
                winTop = 50;
                winBottom = 115;
                winWidth = 82;
                break;
            case 3: // LARGE
                winMiddle = 90;
                winTop = 50;
                winBottom = 115;
                winWidth = 85;
                break;
        }
        bgColor = new OneColor(27, 27, 27, 200);
        progColor = new OneColor(22, 33, 245, 140);
        textColor = new OneColor(255, 255, 255, 255);
    }

    @Switch(
            name = "Custom Window",
            description = "Enable/Disable changing of the window size and position.\nPlease note this is entirely custom, and might behave unexpectedly.",
            category = "Gui Settings", subcategory = "Size"
    )
    public static boolean sizeEnabled = false;

    @Slider(
            name = "Window Width",
            description = "Change the window width.",
            category = "Gui Settings", subcategory = "Size",
            min = 65, max = 180,
            step = 5
    )
    public static int winWidth = 82;
    @Slider(
            name = "Window Top",
            description = "Change the position of the top of the window.",
            category = "Gui Settings", subcategory = "Size",
            min = 5, max = 460,
            step = 5
    )
    public static int winTop = 50;
    @Slider(
            name = "Window Bottom",
            description = "Change the position of the bottom of the window.",
            category = "Gui Settings", subcategory = "Size",
            min = 50, max = 530,
            step = 5
    )
    public static int winBottom = 115;
    @Slider(
            name = "Window Position",
            description = "Change the position of the window.",
            category = "Gui Settings", subcategory = "Size",
            min = 80, max = 1200,
            step = 5
    )
    public static int winMiddle = 210;


    @Color(
            name = "Background Color",
            description = "Change the color of the background.",
            category = "Gui Settings", subcategory = "Colors"
    )
    public static OneColor bgColor = new OneColor(27, 27, 27, 200);
    @Color(
            name = "Progress Bar Color",
            description = "Change the color of the progress bar.",
            category = "Gui Settings", subcategory = "Colors"
    )
    public static OneColor progColor = new OneColor(22, 33, 245, 100);
    @Color(
            name = "Text Color",
            description = "Change the color of the text.",
            category = "Gui Settings", subcategory = "Colors"
    )
    public static OneColor textColor = new OneColor(255, 255, 255, 255);


    @Switch(
            name = "Text Shadow",
            description = "Render the text with a shadow on the GUI.",
            category = "Gui Settings", subcategory = "Gui"
    )
    public static boolean textShadow = false;
    @Switch(
            name = "Number Formatting",
            description = "Comma separate numbers so they look better.",
            category = "Gui Settings", subcategory = "Gui"
    )
    public static boolean numberFormat = true;
    @Switch(
            name = "Compact Mode",
            description = "Enable Compact mode, shortening all stats to make it look cleaner.\n\u00A7eCurrently only implemented for BedWars modes!",
            category = "Gui Settings", subcategory = "Gui"
    )
    public static boolean compactMode = false;
    @Switch(
            name = "3D Heads",
            description = "Enable 3D heads on the GUI window.",
            category = "Gui Settings", subcategory = "Gui"
    )
    public static boolean avatarHead = false;
    @Slider(
            name = "Window Time",
            description = "How long the stats window should display for before rendering progress bar. (default: 7)",
            category = "Gui Settings", subcategory = "Framerate",
            min = 3f, max = 10f,
            step = 1
    )
    public static float GUITime = 7f;
    @Dropdown(
            name = "Window Preset",
            description = "Set the position of the window according to a preset.",
            category = "Gui Settings", subcategory = "Presets",
            options = {"Top Right", "Top Left", "Bottom Right", "Bottom Left"}
    )
    public static int winPreset = 0;
    @Dropdown(
            name = "Color Preset",
            description = "Set the color scheme of the window according to a preset.\nIf you want to use your own colors, set this to default.",
            category = "Gui Settings", subcategory = "Presets",
            options = {"Default (Blue)", "Essential (Green)", "Gamer (Red)", "Pinkulu (Pink)", "Clean (Transparent)", "White"}
    )
    public static int colorPreset = 0;
    @Dropdown(
            name = "Animation",
            description = "Type of window animation to use when drawing the GUI.",
            category = "Gui Settings", subcategory = "Presets",
            options = {"Classic", "Slide Left", "Slide Right", "Full Expand"}
    )
    public static int animationPreset = 0;
    @Slider(
            name = "Window Framerate",
            description = "Frame multiplier when rendering. Higher numbers mean slower animations.\n\u00A7eMay cause window to look jittery on high numbers!",
            category = "Gui Settings", subcategory = "Framerate",
            min = 0, max = 4,
            step = 1
    )
    public static int framesToSkip = 0;
    @Slider(
            name = "Progress Bar Speed",
            description = "Speed of the progress bar. Higher numbers mean slower animations.\n\u00A7eMay cause jitter on high numbers!",
            category = "Gui Settings", subcategory = "Framerate",
            min = 0, max = 5,
            step = 1
    )
    public static int framesToSkipP = 0;

    //gotta change to @keyBind as its better
    @KeyBind(
            name = "Keybind",
            description = "Keybind of the mod.",
            category = "General", subcategory = "General"
    )
    public static OneKeyBind keyBind = new OneKeyBind(UKeyboard.KEY_G);

//    @HUD(
//            name = "Stats",
//            category = "Hud"
//    )
//    public HUDRenderer hud = new HUDRenderer();

    @Exclude
    private static final Minecraft mc = Minecraft.getMinecraft();

    public ModConfig() {
        super(new Mod("QuickStatsReborn", ModType.HYPIXEL, "/icon.svg"), "quickstatsreborn.json");
        initialize();
        registerKeyBind(keyBind, () -> QuickStatsReborn.instance.onKeyPress());
        save();

        addDependency("winWidth", "sizeEnabled");
        addDependency("winTop", "sizeEnabled");
        addDependency("winBottom", "sizeEnabled");
        addDependency("winMiddle", "sizeEnabled");
        addDependency("doPartyDetectionPLUS", "doPartyDetection");

    }
}


package me.spoony.quickStatsReborn;

import me.spoony.quickStatsReborn.command.StatsCommand;
import me.spoony.quickStatsReborn.config.ModConfig;
import me.spoony.quickStatsReborn.hud.HUDRenderer;
import me.spoony.quickStatsReborn.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;

import java.io.File;

import static me.spoony.quickStatsReborn.Reference.MODID;

@Mod(modid = MODID, name = Reference.NAME, version = Reference.VERSION)
public class QuickStatsReborn {
    @Mod.Instance(MODID) // variables and things
    public static QuickStatsReborn instance;
    public static ModConfig config;
    private static final Minecraft mc = Minecraft.getMinecraft();
    public static final Logger LOGGER = LogManager.getLogger(Reference.NAME);
    public static File JarFile;
    public static boolean updateCheck;
    public static boolean betaFlag = true;
    public static boolean locraw = false;
    public static boolean corrupt = false;
    public static LocrawRetriever LocInst;
    public static HUDRenderer GuiInst;
    public static boolean onHypixel = false;
    boolean set = false;
    String partySet;
    boolean hashMismatchMessageSent = false;
    boolean isRemoteWorld = false;

    @EventHandler()
    public void preInit(FMLPreInitializationEvent event) {
        LOGGER.info("Preloading config...");
        try {
            config = new ModConfig();
        } catch (Exception e) {
            if (ModConfig.debugMode) {
                e.printStackTrace();
            }
            corrupt = true;
            LOGGER.error("Config failed to read. File has been reset. If you just reset your config, ignore this message.");
        }
        JarFile = event.getSourceFile();
        if (ModConfig.debugMode) {
            LOGGER.info("Got JAR File: " + JarFile.getPath());
        }
    }

    @EventHandler()
    public void init(FMLInitializationEvent event) {
        LOGGER.info("attempting to check update status and mod authenticity...");
        HashChecker.checkAuth(JarFile.getPath());
        LOGGER.info("registering settings...");
        MinecraftForge.EVENT_BUS.register(this);
        ClientCommandHandler.instance.registerCommand(new StatsCommand());
        LocInst = new LocrawRetriever();
        GuiInst = new HUDRenderer();
        locraw = true;
        LOGGER.debug(instance.toString());
        LOGGER.info("Complete! QuickStatsReborn loaded successfully.");
    }

    public void onKeyPress() {
        if (onHypixel || ModConfig.otherServer) {
            //compare the key that is being pressed to the one set in config
            if (Keyboard.getEventKey() == Keyboard.getKeyIndex(ModConfig.keyBind.getDisplay()) && ModConfig.modEnabled) {
                if (ModConfig.debugMode) {
                    QuickStatsReborn.LOGGER.info("Pressed key: " + Keyboard.getEventKey());
                    QuickStatsReborn.LOGGER.info("Config keybind: " + ModConfig.keyBind.getDisplay());
                    QuickStatsReborn.LOGGER.info("Config keybind as int (should match pressed key): " + Keyboard.getKeyIndex(ModConfig.keyBind.getDisplay()));
                }
                if (Keyboard.getEventKeyState()) {
                    try {
                        Entity entity = GetEntity.get(0);
                        if (entity instanceof EntityPlayer) {
                            if (entity.getName() == null || entity.getName().equals("")) {
                                return;
                            }
                            if (onHypixel) {
                                if (entity.getDisplayName().getUnformattedText().startsWith("\u00A78[NPC]") || !entity.getDisplayName().getUnformattedText().startsWith("\u00A7")) {      // npc test
                                    return;
                                }
                            }
                            GuiInst.showGUI(entity.getName());
                        }
                    } catch (Exception ignored) {
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onChatReceive(ClientChatReceivedEvent event) {
        if (onHypixel) {
            if (ModConfig.doPartyDetection) {
                if (QuickStatsReborn.locraw) {
                    QuickStatsReborn.locraw = false;
                }
                if (LocrawRetriever.lobby) {
                    try {
                        if (event.message.getUnformattedText().contains("Party ") || event.message.getUnformattedText().contains("lobby!")) {
                            return;
                        }
                        if (event.message.getUnformattedText().contains(mc.thePlayer.getName())) {
                            String username = getUsernameFromChat(event.message.getUnformattedText());
                            if (!username.equalsIgnoreCase(mc.thePlayer.getName())) {
                                event.setCanceled(true);
                                StringBuilder sb = new StringBuilder(event.message.getUnformattedText());
                                sb.insert(event.message.getUnformattedText().indexOf(mc.thePlayer.getName()), "\u00A7l");
                                mc.thePlayer.addChatMessage(new ChatComponentText(sb.toString()));
                                GuiInst.showGUI(username);
                            }
                        }

                        if (ModConfig.doPartyDetectionPLUS) {
                            if (event.message.getUnformattedText().contains("say")) {
                                if (getUsernameFromChat(event.message.getUnformattedText()).equals(mc.thePlayer.getName())) {
                                    try {
                                        String unformatted = EnumChatFormatting.getTextWithoutFormattingCodes(event.message.getUnformattedText());
                                        partySet = StringUtils.substringAfter(unformatted, "say ");
                                        set = true;
                                        if (partySet.contains("my name")) {
                                            partySet = null;
                                            set = false;
                                        }
                                        if (ModConfig.debugMode) {
                                            LOGGER.info(partySet);
                                        }
                                    } catch (Exception e) {
                                        if (ModConfig.debugMode) {
                                            e.printStackTrace();
                                            set = false;
                                        }
                                    }
                                }
                                if (set) {
                                    if (event.message.getUnformattedText().contains(partySet)) {
                                        String username = getUsernameFromChat(event.message.getUnformattedText());
                                        if (!username.equalsIgnoreCase(mc.thePlayer.getName())) {
                                            event.setCanceled(true);
                                            StringBuilder sb = new StringBuilder(event.message.getUnformattedText());
                                            sb.insert(event.message.getUnformattedText().indexOf(partySet), "\u00A7l");
                                            mc.thePlayer.addChatMessage(new ChatComponentText(sb.toString()));
                                            GuiInst.showGUI(username);
                                        }
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        if (ModConfig.debugMode) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    public String getUsernameFromChat(String message) {
        try {
            String unformatted = EnumChatFormatting.getTextWithoutFormattingCodes(message);
            return unformatted.substring(unformatted.lastIndexOf("]") + 2, unformatted.lastIndexOf(":"));
        } catch (Exception e) {
            if (ModConfig.debugMode) {
                e.printStackTrace();
            }
            return null;
        }
    }


    @SubscribeEvent
    @SuppressWarnings({"ConstantConditions", "MismatchedStringCase"})
    public void onWorldLoad(WorldEvent.Load event) {
        isRemoteWorld = event.world.isRemote;
        try {
            if (mc.getCurrentServerData().serverIP.contains("hypixel")) {
                if (ModConfig.debugMode) {
                    LOGGER.info("on Hypixel!");
                }
                locraw = true;
                onHypixel = true;
                LocrawRetriever.lobby = false;
            } else {
                onHypixel = false;
                LocrawRetriever.lobby = false;
                locraw = false;
            }
        } catch (Exception e) {
            // if(ModConfig.debugMode) {e.printStackTrace();}
        }
        if (updateCheck && ModConfig.sendUp && isRemoteWorld) {
            new TickDelay(this::sendUpdateMessage, 20);
            updateCheck = false;
        }
        if (Reference.VERSION.contains("beta") && betaFlag && isRemoteWorld) {
            try {
                new TickDelay(() -> sendMessages("",
                        "Beta build has been detected (ver. " + Reference.VERSION + ")",
                        "Note that some features might be unstable! Use at your own risk!"), 20);
                betaFlag = false;
            } catch (Exception e) {
                betaFlag = true;
                //if (ModConfig.debugMode) { e.printStackTrace(); }
                LOGGER.error("skipping beta message, bad world return!");
            }
        }
        if (corrupt) {
            try {
                new TickDelay(() -> sendMessages("",
                        "An error occurred while trying to read your config file. You will have to reset it.",
                        "If you just reset your configuration file, ignore this message."), 20);
                corrupt = false;
            } catch (Exception e) {
                //if (ModConfig.debugMode) { e.printStackTrace(); }
                LOGGER.error("skipping corrupt message, bad world return!");
            }
        }
        if (HashChecker.mismatch && ModConfig.securityLevel == 2 && !hashMismatchMessageSent) {
                try {
                    new TickDelay(() -> sendMessages("The hash for the mod is incorrect. Check the logs for more info.",
                            "WARNING: This could mean your data is exposed to hackers! Make sure you got the mod from the OFFICIAL mirror, and try again.",
                            Reference.URL), 20);
                    hashMismatchMessageSent = true;
                    ModConfig.modEnabled = false;
                } catch (Exception e) {
                    ModConfig.modEnabled = true;
                    hashMismatchMessageSent = false;
                    LOGGER.error("Error sending hash mismatch message: ", e);
            }
        }
    }

    @SubscribeEvent
    public void onClientDisconnection(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        if (hashMismatchMessageSent) {
            hashMismatchMessageSent = false;
        }
    }

    @SuppressWarnings({"ConstantConditions", "MismatchedStringCase"})
    public static void sendMessages(String... messages) {
        try {
            for (String message : messages) {
                mc.thePlayer.addChatMessage(new ChatComponentText(Reference.COLOR + "[" + Reference.NAME + "] " + message));
            }
        } catch (Exception e) {
            LOGGER.error("Didn't send message: " + e.getMessage());
            //if (ModConfig.debugMode) { e.printStackTrace(); }
            if (Reference.VERSION.contains("beta")) {
                betaFlag = true;
            }
        }
    }

    private void sendUpdateMessage() {
        try {
            IChatComponent comp = new ChatComponentText("Click here to update it!");
            ChatStyle style = new ChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, Reference.URL));
            style.setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    new ChatComponentText(Reference.COLOR + Reference.URL)));
            style.setColor(Reference.COLOR);
            style.setUnderlined(true);
            comp.setChatStyle(style);
            mc.thePlayer.playSound("minecraft:random.successful_hit", 1.0F, 1.0F);
            mc.thePlayer.addChatMessage(new ChatComponentText(
                    Reference.COLOR + "--------------------------------------"));
            mc.thePlayer.addChatMessage(new ChatComponentText(Reference.COLOR
                    + ("A newer version of " + Reference.NAME + " is available! (" + Updater.latestVersion + ")")));
            mc.thePlayer.addChatMessage(comp);
            mc.thePlayer.addChatMessage(new ChatComponentText(
                    Reference.COLOR + "--------------------------------------"));
        } catch (NullPointerException e) {
            //if (ModConfig.debugMode) { e.printStackTrace(); }
            updateCheck = true;
            LOGGER.error("skipping update message, bad world return!");
        }
    }
}

package me.spoony.quickStatsReborn.command;

import me.spoony.quickStatsReborn.QuickStatsReborn;
import me.spoony.quickStatsReborn.Reference;
import me.spoony.quickStatsReborn.config.ModConfig;
import me.spoony.quickStatsReborn.util.*;
import me.spoony.quickStatsReborn.util.Updater;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static me.spoony.quickStatsReborn.QuickStatsReborn.sendMessages;
import static net.minecraft.command.CommandBase.getListOfStringsMatchingLastWord;

public class StatsCommand implements ICommand {

    private final List<String> aliases;
    private static final Minecraft mc = Minecraft.getMinecraft();

    public StatsCommand() {
        aliases = new ArrayList<>();
        aliases.add("quickstatsreborn");
        aliases.add("qsr");
    }

    @Override
    public int compareTo(@NotNull ICommand o) {
        return 0;
    }

    @Override
    public String getCommandName() {
        return "quickStatsReborn";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "quickStatsReborn <>";
    }

    @Override
    public List<String> getCommandAliases() {
        return this.aliases;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        try {
            switch (args[0]) {
                case "reload":
                    QuickStatsReborn.LOGGER.info("Reloading config and version checker...");
                    sendMessages("Reloading!");
                    QuickStatsReborn.updateCheck = Updater.checkUpdate(Reference.VERSION);
                    HashChecker.checkAuth(QuickStatsReborn.JarFile.getPath());
                    sendMessages("Reloaded! Re-log and check logs for more information.");
                    Minecraft.getMinecraft().thePlayer.playSound("minecraft:random.successful_hit", 1.0F, 1.0F);
                    break;
                case "configure":
                    QuickStatsReborn.config.openGui();
                    break;
                case "testLoc":
                    sendMessages("Testing locraw function...");
                    QuickStatsReborn.LocInst.send();
                    break;
                case "test":
                    sendMessages("Testing function...");
                    QuickStatsReborn.GuiInst.showGUI("SpoonySimone");
                    break;
                case "testEntity":
                    try {
                        sendMessages("Testing getEntity function...");
                        QuickStatsReborn.LOGGER.info(GetEntity.get(0).getName());
                        sendMessages("entity = " + GetEntity.get(0).getName());
                    } catch (Exception e) {
                        QuickStatsReborn.LOGGER.info("entity = null");
                    }
                    break;
                case "hashCheck":
                    HashChecker.checkAuth(QuickStatsReborn.JarFile.getPath());
                    if (HashChecker.mismatch && ModConfig.securityLevel == 2) {
                        try {
                            sendMessages("The hash for the mod is incorrect. Check the logs for more info.",
                                    "WARNING: This could mean your data is exposed to hackers! Make sure you got the mod from the OFFICIAL mirror, and try again.",
                                    Reference.URL);
                        } catch (Exception e) {
                            QuickStatsReborn.LOGGER.error("Error sending hash mismatch message via command: ", e);
                        }
                    } else {
                        sendMessages("Local hash is contained in the ones received from remote.");
                    }
                    break;
                default:
                    if (args[0].equals("me")) {
                        QuickStatsReborn.GuiInst.showGUI(mc.thePlayer.getName());
                    } else {
                        QuickStatsReborn.GuiInst.showGUI(args[0]);
                    }
                    try {
                        if (args.length == 2) {
                            switch (args[1].toUpperCase()) {
                                case "BEDWARS":
                                case "BW":
                                case "BEDWAR":
                                    LocrawUtil.gameType = "BEDWARS";
                                    break;
                                case "SW":
                                case "SKYWARS":
                                case "SKYWAR":
                                    LocrawUtil.gameType = "SKYWARS";
                                    break;
                                case "QC":
                                case "QK":
                                case "QUAKE":
                                    LocrawUtil.gameType = "solo";
                                    break;
                                case "DUEL":
                                case "DUELS":
                                case "DL":
                                    LocrawUtil.gameType = "DUELS";
                                    break;
                                case "WOOL":
                                case "WOOLGAMES":
                                case "WOOL_GAMES":
                                case "WOOL_WARS":
                                    LocrawUtil.gameType = "WOOL_GAMES";
                                    break;
                                default:
                                    LocrawUtil.gameType = args[1].toUpperCase();
                                    break;
                            }
                        }
                    } catch (Exception e) {
                        if (ModConfig.debugMode) {
                            e.printStackTrace();
                        }
                    }
                    break;
            }
        } catch (Exception e) {
            sender.addChatMessage(new ChatComponentText(Reference.COLOR
                    + "[QuickStatsReborn] Command menu (mod version " + Reference.VERSION + ")"));
            sender.addChatMessage(new ChatComponentText(Reference.COLOR
                    + "[QuickStatsReborn] Command usage: /qsr <name>, /qsr configure, /qsr reload"));
        }
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        try {
            Collection<NetworkPlayerInfo> players = mc.getNetHandler().getPlayerInfoMap();
            List<String> list = new ArrayList<>();
            for (NetworkPlayerInfo info : players) {
                list.add(info.getGameProfile().getName());
            }

            if (args.length == 1) return getListOfStringsMatchingLastWord(args, list.toArray(new String[0]));
            else return null;
        } catch (Exception e) {
            if (ModConfig.debugMode) {
                e.printStackTrace();
            }
            return null;
        }
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index) {
        return false;
    }

}

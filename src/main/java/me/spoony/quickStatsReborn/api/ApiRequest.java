package me.spoony.quickStatsReborn.api;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.spoony.quickStatsReborn.QuickStatsReborn;
import me.spoony.quickStatsReborn.Reference;
import me.spoony.quickStatsReborn.config.ModConfig;
import me.spoony.quickStatsReborn.util.LocrawUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import com.google.gson.Gson;

public class ApiRequest extends Thread {
    private static final Minecraft mc = Minecraft.getMinecraft();
    public static String username, rank, rankColor, playerName;
    public JsonObject rootStats;
    public JsonObject achievementStats;
    public String formattedName;
    public ArrayList<String> result;
    public static double exp;
    public boolean noUser = false;
    public boolean generalError = false;
    public boolean timeOut = false;
    public boolean slowDown = false;
    public boolean nick = false;
    public static int karma;
    public String uuid;
    public BufferedImage image;
    int startTime, endTime;

    /**
     * Create a new instance of the API request function, with a username.
     */
    public ApiRequest(String uname) {
        username = uname;
        this.setName("QuickStats API");
        this.start();
        startTime = (int) System.currentTimeMillis();
    }

    public void run() {
        /* get UUID from Mojang */
        try {
            JsonObject jsonObject = buildJson("https://api.mojang.com/users/profiles/minecraft/" + username);
            uuid = jsonObject.get("id").getAsString();
        } catch (IllegalStateException e) {
            mc.thePlayer.addChatMessage(new ChatComponentText(
                    Reference.COLOR + "[QuickStats] Player not found: " + username));
            noUser = true;
            return;
        } catch (Exception e) {
            if (ModConfig.debugMode) {
                e.printStackTrace();
            }
//            mc.thePlayer.addChatMessage(new ChatComponentText(Reference.COLOR
//                    + "[QuickStats] Couldn't fetch player's UUID because user is nicked."));
            nick = true;
        }
        /* get head texture */
        try {
            if (ModConfig.avatarHead) {
                image = ImageIO.read(new URL("https://cravatar.eu/helmhead/" + uuid));
            } else {
                image = ImageIO.read(new URL("https://cravatar.eu/helmavatar/" + uuid));
            }
        } catch (Exception e) {
            if (ModConfig.debugMode) {
                e.printStackTrace();
            }
        }

        /* process request from Hypixel */
        try {
            if (uuid == null) {
                return;
            }
            String url = "" + uuid;
            if (ModConfig.debugMode) {
                QuickStatsReborn.LOGGER.info("Fetching Hypixel data from URL: " + url);
            }
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpGet apiRequest = new HttpGet(url);
            apiRequest.setHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36 Edg/125.0.2535.79");
            HttpResponse apiResponse = httpClient.execute(apiRequest);
            HttpEntity entity = apiResponse.getEntity();
            String jsonApiResponse = EntityUtils.toString(entity);
        
            JsonObject js1 = new Gson().fromJson(jsonApiResponse, JsonObject.class);
            boolean success = js1.get("success").getAsBoolean();
            if (success) {
                if (js1.get("player").isJsonNull()) {
                    nick = true;
                    return;
                }
                JsonObject js2 = js1.get("player").getAsJsonObject();
                try { // get rank and name
                    exp = js2.get("networkExp").getAsDouble();
                    karma = js2.get("karma").getAsInt();
                    playerName = js2.get("displayname").getAsString();
                    rank = js2.get("newPackageRank").getAsString();
                    if (rank.equals("MVP_PLUS")) {
                        try {
                            rankColor = js2.get("rankPlusColor").getAsString(); // get plus color
                            if (js2.get("monthlyPackageRank").getAsString().equals("SUPERSTAR")) { // test for mvp++
                                rank = "SUPERSTAR";
                            }
                        } catch (Exception e) {
                            rank = "MVP_PLUS";
                            rankColor = "PINK";
                            // if(ModConfig.debugMode) {e.printStackTrace();}
                        }
                        try { // youtuber
                            rank = js2.get("rank").getAsString();
                        } catch (Exception ignored) {
                        }
                    }
                } catch (NullPointerException e) {
                    rank = "non";
                }
                try {
                    if (playerName.equals("Technoblade")) {      // Technoblade never dies
                        formattedName = "\u00A7d[PIG\u00A7b+++\u00A7d] Technoblade";
                    } else {
                        formattedName = getFormattedName(playerName, rank, rankColor);
                    }
                } catch (Exception ignored) {
                }

                rootStats = js2.get("stats").getAsJsonObject();
                achievementStats = js2.get("achievements").getAsJsonObject();
                result = Stats.getStats(rootStats, achievementStats, LocrawUtil.gameType);
                endTime = (int) System.currentTimeMillis() - startTime;
                QuickStatsReborn.LOGGER.info("successfully processed all data in " + endTime + "ms");
            } else {
                mc.thePlayer.addChatMessage(new ChatComponentText(Reference.COLOR
                        + "[QuickStats] The Hypixel API didn't process the request properly. Try again."));
                generalError = true;
                QuickStatsReborn.LOGGER.error("error occurred when building after API request, closing");
            }

        } catch (IOException e) {
            if (e.getMessage().contains("504 for URL")) {
                mc.thePlayer.addChatMessage(new ChatComponentText(Reference.COLOR
                        + "[QuickStats] failed to contact the Hypixel API. Request timed out!"));
                timeOut = true;
            } else {
                if (e.getMessage().contains("429 for URL")) {
                    mc.thePlayer.addChatMessage(new ChatComponentText(Reference.COLOR
                            + "[QuickStats] the Hypixel API didn't respond as you are sending requests too fast! Slow down!"));
                    slowDown = true;
                } else {
                    mc.thePlayer.addChatMessage(new ChatComponentText(Reference.COLOR
                            + "[QuickStats] failed to contact Hypixel API. This is usually due to an invalid API key."));
                    if (ModConfig.debugMode) {
                        e.printStackTrace();
                    }
                    generalError = true;
                }
            }
        } catch (Exception e) {
            // QuickStats.LOGGER.error(e.getStackTrace().toString());
            if (ModConfig.debugMode) {
                e.printStackTrace();
            }
            mc.thePlayer.addChatMessage(new ChatComponentText(Reference.COLOR
                    + "[QuickStats] an unexpected error occurred. Check logs for more info."));
            generalError = true;
        }
    }

    /**
     * Build a JSON Object from the given URL.
     *
     * @param url (as a String)
     * @return com.google.gson.JsonObject
     * @throws IOException if URL is incorrect/doesn't respond.
     */
    private JsonObject buildJson(String url) throws IOException {
        InputStream input = new URL(url).openStream();
        BufferedReader streamReader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
        StringBuilder responseStrBuilder = new StringBuilder();
        String inputStr;
        while ((inputStr = streamReader.readLine()) != null)
            responseStrBuilder.append(inputStr);
        return new JsonParser().parse(responseStrBuilder.toString()).getAsJsonObject();
    }

    private String getFormattedName(String name, String rank, String color) {
        QuickStatsReborn.LOGGER.debug(color);
        String formattedName;
        boolean getColor = false;
        int plusA = 0;
        switch (rank) {
            case "VIP":
                formattedName = "\u00A7a[VIP] " + name;
                break;
            case "VIP_PLUS":
                formattedName = "\u00A7a[VIP\u00A76+\u00A7a] " + name;
                break;
            case "MVP":
                formattedName = "\u00A7b[MVP] " + name;
                break;
            case "MVP_PLUS":
                getColor = true;
                plusA = 1;
                formattedName = "\u00A7b[MVP";
                break;
            case "SUPERSTAR":
                getColor = true;
                plusA = 2;
                formattedName = "\u00A76[MVP";
                break;
            case "YOUTUBER":
                formattedName = "\u00A7c[\u00A7fYOUTUBE\u00A7c] " + name;
                break;
            case "GAME_MASTER":
            formattedName = "\u00A72[GM] " + name;
            break;
            case "ADMIN":
                formattedName = "\u00A7c[ADMIN] " + name;
                break;
            default:
                formattedName = "\u00A77" + name;
                break;
        }
        if (getColor) {
            // System.out.println(color);
            switch (color) {
                case "DARK_RED":
                    formattedName += "\u00A74+";
                    break;
                case "DARK_GREEN":
                    formattedName += "\u00A72+";
                    break;
                case "BLACK":
                    formattedName += "\u00A70+";
                    break;
                case "LIGHT_PURPLE":
                case "PINK":
                    formattedName += "\u00A7d+";
                    break;
                case "BLUE":
                    formattedName += "\u00A79+";
                    break;
                case "DARK_GRAY":
                    formattedName += "\u00A77+";
                    break;
                case "GOLD":
                    formattedName += "\u00A76+";
                    break;
                case "GREEN":
                    formattedName += "\u00A7a+";
                    break;
                case "YELLOW":
                    formattedName += "\u00A7e+";
                    break;
                case "WHITE":
                    formattedName += "\u00A7f+";
                    break;
                case "DARK_PURPLE":
                    formattedName += "\u00A75+";
                    break;
                case "DARK_BLUE":
                    formattedName += "\u00A71+";
                    break;
                case "DARK_AQUA":
                    formattedName += "\u00A73+";
                    break;
                default:
                    formattedName += "+";
            }
            if (plusA == 2) {
                formattedName += "+\u00A76] " + name;
            } else {
                formattedName += "\u00A7b] " + name;
            }
        }

        return formattedName;
    }

}
package minecraft.plugin.utils;

import minecraft.plugin.DiscordPlugin;
import minecraft.plugin.chatcolor.RGBUtils;
import minecraft.plugin.discord.Command;
import minecraft.plugin.discord.Context;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.javacord.api.entity.channel.Channel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.json.JSONObject;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static minecraft.plugin.DiscordPlugin.*;
import static minecraft.plugin.utils.Log.debug;
import static minecraft.plugin.utils.Log.log;

public class Utils {
    public static ArrayList<String> bannedNames = new ArrayList<>();

    /**
     * get a channel by id
     */
    public static TextChannel getTextChannel(String id) {
        Optional<Channel> dc = api.getChannelById(id);
        if (dc.isEmpty()) {
            System.out.println("[ERR!] discordplugin: channel not found! " + id);
            return null;
        }
        Optional<TextChannel> dtc = dc.get().asTextChannel();
        if (dtc.isEmpty()) {
            System.out.println("[ERR!] discordplugin: textchannel not found! " + id);
            return null;
        }
        return dtc.get();
    }

    public static void tooFewArguments(Context ctx, Command command) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Too few arguments!")
                .setDescription("Usage: " + prefix + command.name + " " + command.usage)
                .setColor(Pals.error);
        ctx.channel.sendMessage(eb);
    }

    public static JSONObject readFromJson(String loc) {
        File file = new File(loc);
        log("Config File found: " + file.getAbsolutePath());
        try {
            String content = new String(Files.readAllBytes(Paths.get(file.toURI())));
            return new JSONObject(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Find a player by name
     *
     * @param identifier the name, id, uuid, con or con.address
     */
    public static Player findPlayer(String identifier) {
        Player found = null;
        for (Player player : Bukkit.getOnlinePlayers()) {
//            if (player == null) return null; // how does that even happen wtf
//            if (player.uuid() == null) return null;
//            if (player.con == null) return null;
//            if (player.con.address == null) return null;

            if (player.getAddress().toString().equals(identifier.replaceAll(" ", "")) ||
                    String.valueOf(player.getEntityId()).equals(identifier.replaceAll(" ", "")) ||
                    player.getPlayer().getUniqueId().toString().equals(identifier.replaceAll(" ", "")) ||
                    player.getDisplayName().toLowerCase().replaceAll(" ", "").startsWith(identifier.toLowerCase().replaceAll(" ", ""))) {
                found = player;
            }
        }
        return found;
    }

    /**
     * send the player not found message for discord commands
     */
    public static void playerNotFound(String name, EmbedBuilder eb, Context ctx) {
        eb.setTitle("Command terminated");
        eb.setDescription("Player `" + name + "` not found.");
        eb.setColor(Pals.error);
        ctx.channel.sendMessage(eb);
    }

    /**
     * runs something in the main MC Bukkit thread
     */
    public static void runSynchronous(Runnable runnable) {
        Bukkit.getScheduler().runTask(DiscordPlugin.getInstance(), runnable);
    }

    /**
     * Convert a long to formatted time.
     *
     * @param epoch the time in long.
     * @return formatted time
     */
    public static String epochToString(long epoch) {
        Date date = new Date(epoch * 1000L);
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        format.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
        return format.format(date) + " UTC";
    }

    public static void logAction(Player target, String action, Context ctx, String reason) {
        EmbedBuilder eb = new EmbedBuilder()
                .setTitle(action + " " + target.getDisplayName())
                .addField("UUID", String.valueOf(target.getUniqueId()), true)
                .addField("IP", String.valueOf(target.getAddress()), true)
                .addField(action + " by", "<@" + ctx.author.getIdAsString() + ">", true)
                .addField("Reason", (reason == null ? "Not provided!" : reason), true);
        log_channel.sendMessage(eb);
    }

    public static String setColors(String message) {
        StringBuilder finalMessage = new StringBuilder();

        // iterate over message and check for [color name]
        char[] charArray = message.toCharArray();
        for (int i = 0; i < message.length(); i++) {
            char c = charArray[i];
            if (c == '[') {
                debug("found [ at position " + i);
                for (int j = i; j < message.length(); j++) {
                    if (charArray[j] == ']') {
                        debug("found ] at position " + j);
                        String colorName = message.substring(i + 1, j);
                        try {
                            debug("Try to get color " + colorName);
                            ChatColor color = (ChatColor) ChatColor.class.getDeclaredField(colorName.toUpperCase()).get(null);
                            finalMessage.append(color);
                            debug("Appended " + color + " to the string");
                            i = j;
                        } catch (Exception e) {
                            log("Couldn't load color: " + colorName + " " + e);
                        }
                    }
                }
            } else {
                finalMessage.append(c);
            }
        }

        return RGBUtils.toChatColorString(finalMessage.toString());
    }

    public static void getChannelsFromJson(JSONObject json, ArrayList<String> channels) {
        for (String name : channels) {
            if (json.has(name + "_id")) {
                try {
                    DiscordPlugin.class.getDeclaredField(name).set(TextChannel.class, getTextChannel(json.getString(name + "_id")));
                } catch (Exception e) {
                    log("Couldn't load channel " + name + "! (Check if theres a public TextChannel named " + name + " in " + DiscordPlugin.class.getName() + "!)");
                }
            } else {
                log("Couldn't load " + name + "_id from settings.json");
            }
        }
    }

    /**
     * @implNote can only read Strings & Booleans
     */
    public static void getFromJson(JSONObject json, Object targetClass, String name) {
        try {
            if (targetClass == boolean.class) {
                DiscordPlugin.class.getDeclaredField(name).set(targetClass, json.getBoolean(name));
            } else {
                DiscordPlugin.class.getDeclaredField(name).set(targetClass, json.getString(name));
            }
        } catch (Exception e) {
            log("Couldn't load config " + name + "! (Check if theres a public Object named " + name + " in " + DiscordPlugin.class.getName() + "!)");
        }
    }

    /**
     * log a list of connections in the discord log channel
     *
     * @param connection whether they joined or left
     */
    public static void logConnections(TextChannel log_channel, HashMap<String, Player> leftPlayers, String connection) {
        if (leftPlayers.size() > 0) {
            EmbedBuilder eb = new EmbedBuilder();
            eb.setTitle("Player " + connection + " Log");
            StringBuilder desc = new StringBuilder();
            for (Map.Entry<String, Player> player : leftPlayers.entrySet()) {
//                if (player == null) continue;
                try {
                    desc.append(String.format("`%s` : `%s `:%s\n", player.getValue().getUniqueId(), player.getValue().getAddress().toString(), player.getValue().getDisplayName()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (Objects.equals(connection, "leave")) {
                eb.setColor(new Color(0xff0000));
            } else {
                eb.setColor(new Color(0x00ff00));
            }
            eb.setDescription(desc.toString());
            assert log_channel != null;
            log_channel.sendMessage(eb);
        }
        leftPlayers.clear();
    }


    // colors for errors, info, warning etc messages
    public static class Pals {
        public static Color warning = (Color.getHSBColor(5, 85, 95));
        public static Color info = (Color.getHSBColor(45, 85, 95));
        public static Color error = (Color.getHSBColor(3, 78, 91));
        public static Color success = (Color.getHSBColor(108, 80, 100));
    }

    public static class Categories {
        public static final String moderation = "Moderation";
        public static final String management = "Management";
        public static final String mapReviewer = "Map Reviewer";
    }
}

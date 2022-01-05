package minecraft.plugin.utils;

import minecraft.plugin.discordcommands.Command;
import minecraft.plugin.discordcommands.Context;
import org.bukkit.Bukkit;
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
import java.util.Optional;

import static minecraft.plugin.DiscordPlugin.*;

public class Utils {
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
        System.out.println(file.getAbsolutePath());
        try {
            String content = new String(Files.readAllBytes(Paths.get(file.toURI())));
            JSONObject json = new JSONObject(content);
//            System.out.println(json);
            return json;
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

    public static void log(String message) {
        System.out.println("[Discord_Plugin] " + message);
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

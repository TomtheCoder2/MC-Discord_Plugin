package minecraft.plugin;

import minecraft.plugin.DiscordPlugin;
import minecraft.plugin.discordcommands.Context;
import minecraft.plugin.discordcommands.DiscordCommands;
import minecraft.plugin.discordcommands.RoleRestrictedCommand;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.json.JSONObject;

import java.awt.*;

import static minecraft.plugin.utils.Utils.findPlayer;
import static minecraft.plugin.utils.Utils.logAction;

public class Moderation {
    private final JSONObject data;

    public Moderation(JSONObject data) {
        this.data = data;
    }

    public void registerCommands(DiscordCommands handler) {
        if (data.has("moderator_role_id")) {
            String moderator_role_id = data.getString("moderator_role_id");
            handler.registerCommand(new RoleRestrictedCommand("kick") {
                {
                    help = "Kick a player from the server";
                    minArguments = 2;
                    usage = "<player> <reason...>";
                    role = moderator_role_id;
                    aliases.add("k");
                }

                @Override
                public void run(Context ctx) {
                    Player player = findPlayer(ctx.args[1]);
                    String reason = ctx.message.split(" ", 2)[1];
                    Bukkit.getScheduler().runTask(DiscordPlugin.getInstance(), () -> player.kickPlayer(reason));
                    EmbedBuilder eb = new EmbedBuilder()
                            .setTitle("Success!")
                            .setDescription("Successfully kicked " + player.getDisplayName() + "!")
                            .setColor(new Color(0x00ff00));
                    logAction(player, "Kicked", ctx, reason);
                    ctx.channel.sendMessage(eb);
                }
            });
        }
    }
}

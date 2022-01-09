package minecraft.plugin.discordcommands;

import minecraft.plugin.DiscordPlugin;
import minecraft.plugin.data.PlayerData;
import minecraft.plugin.discord.Context;
import minecraft.plugin.discord.DiscordCommands;
import minecraft.plugin.discord.RoleRestrictedCommand;
import minecraft.plugin.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.json.JSONObject;

import java.awt.*;
import java.time.Instant;

import static minecraft.plugin.DiscordPlugin.prefix;
import static minecraft.plugin.database.Utils.getData;
import static minecraft.plugin.database.Utils.setData;
import static minecraft.plugin.utils.Utils.Categories.moderation;
import static minecraft.plugin.utils.Utils.*;

public record Moderation(JSONObject data) {
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
                    category = moderation;
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

            handler.registerCommand(new RoleRestrictedCommand("lookup") {
                {
                    help = "Check all information about the specified player.";
                    usage = "<player>";
                    role = moderator_role_id;
                    category = moderation;
                    minArguments = 1;
                    aliases.add("l");
                }

                public void run(Context ctx) {
                    EmbedBuilder eb = new EmbedBuilder();
                    String target = ctx.message;

                    Player player = findPlayer(target);
                    if (player == null) {
                        Bukkit.getPlayer(target);
                    }

                    if (player != null) {
                        eb.setTitle(player.getDisplayName() + "'s lookup");
                        eb.addField("UUID", player.getUniqueId().toString());
                        eb.addField("Last used ip", player.getAddress().toString().replace("/", ""));
                        StringBuilder s = lookup(eb, player);
                        eb.setDescription(s.toString());
                    } else {
                        eb.setTitle("Command terminated");
                        eb.setColor(Utils.Pals.error);
                        eb.setDescription("Player could not be found or is offline.");
                    }
                    ctx.channel.sendMessage(eb);
                }
            });

            handler.registerCommand(new RoleRestrictedCommand("banish") {
                {
                    help = "Ban the provided player for a specific duration with a specific reason.";
                    role = moderator_role_id;
                    usage = "<player> <duration (minutes)> <reason...>";
                    category = moderation;
                    apprenticeCommand = true;
                    minArguments = 2;
                    aliases.add("b");
                }

                public void run(Context ctx) {
                    EmbedBuilder eb = new EmbedBuilder();
                    String target = ctx.args[1];
                    String targetDuration = ctx.args[2];
                    String reason;
                    long now = Instant.now().getEpochSecond();

                    Player player = findPlayer(target);
                    if (player != null) {
                        String uuid = player.getUniqueId().toString();
                        String banId = uuid.substring(0, 4);
                        PlayerData pd = getData(uuid);
                        long until;
                        try {
                            until = now + Integer.parseInt(targetDuration) * 60L;
                            reason = ctx.message.substring(target.length() + targetDuration.length() + 2);
                        } catch (Exception e) {
//                                EmbedBuilder err = new EmbedBuilder()
//                                        .setTitle("Second argument has to be a number!")
//                                        .setColor(new Color(0xff0000));
//                                ctx.channel.sendMessage(err);
//                                return;
                            e.printStackTrace();
                            until = now + (long) (2 * 356 * 24 * 60 * 60); // 2 years
                            reason = ctx.message.substring(target.length() + 1);
                        }
                        if (pd != null) {
                            pd.bannedUntil = until;
                            pd.banReason = reason + "\n" + ChatColor.YELLOW + "Until: " + epochToString(until) + "\n" + ChatColor.AQUA + "Ban ID: " + ChatColor.WHITE + banId;
                            setData(uuid, pd);

                            eb.setTitle("Banned " + player.getDisplayName() + " for " + targetDuration + " minutes. ");
                            eb.addField("Ban ID", banId);
                            eb.addField("For", (until - now) / 60 + " minutes.");
                            eb.addField("Until", epochToString(until));
                            eb.addInlineField("Reason", reason);
                            ctx.channel.sendMessage(eb);

                            String finalReason = reason;
                            runSynchronous(() -> player.kickPlayer(finalReason));

                            logAction(player, "Banned", ctx, reason);
                        } else {
                            playerNotFound(target, eb, ctx);
                        }
                    } else {
                        playerNotFound(target, eb, ctx);
                    }
                }
            });

            handler.registerCommand(new RoleRestrictedCommand("alert") {
                {
                    help = "Send a message to a player's screen.";
                    usage = """
                            <player> [fade in] [stay] [fade out] <{title...}> [{subtitle...}]

                            example:
                            %alert nautilus 10 10 10 {this is a title} {this is a subtitle}""".replaceAll("%", prefix);
                    category = moderation;
                    role = moderator_role_id;
                    aliases.add("a");
                    minArguments = 2;
                }

                @Override
                public void run(Context ctx) {
                    EmbedBuilder eb = new EmbedBuilder();
                    int fadeIn = 1;
                    int stay = 500;
                    int fadeOut = 1;
                    StringBuilder title = new StringBuilder();
                    StringBuilder subtitle = new StringBuilder();

                    // check if there are vars for fade in, stay and fade out
                    if (ctx.args[1].matches("[0-9]+")) { // fade in
                        fadeIn = Integer.parseInt(ctx.args[1]);
                    }
                    if (ctx.args[2].matches("[0-9]+")) { // stay
                        stay = Integer.parseInt(ctx.args[2]);
                    }
                    if (ctx.args.length > 3) {
                        if (ctx.args[3].matches("[0-9]+")) { // fade out
                            fadeOut = Integer.parseInt(ctx.args[3]);
                        }
                    }

                    // title and subtitle
                    char[] charArray = ctx.message.toCharArray();
                    for (int i = 0; i < ctx.message.length(); i++) {
                        char c = charArray[i];
                        if (c == '{') {
                            for (int j = i; j < ctx.message.length(); j++) {
                                if (charArray[j] == '}') {
                                    if (title.toString().equals("")) {
                                        title.append(setColors(ctx.message.substring(i + 1, j)));
                                    } else {
                                        subtitle.append(setColors(ctx.message.substring(i + 1, j)));
                                    }
                                    i = j;
                                    break;
                                }
                            }
                        }
                    }

                    String target = ctx.args[1].toLowerCase();
                    if (target.equals("all")) {
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            p.sendTitle(title.toString(), subtitle.toString(), fadeIn, stay, fadeOut);
                        }
                        eb.setTitle("Command executed");
                        eb.setDescription("Alert was sent to all players.");
                        ctx.channel.sendMessage(eb);
                    } else {
                        Player p = findPlayer(target);
                        if (p != null) {
                            p.sendTitle(title.toString(), subtitle.toString(), fadeIn, stay, fadeOut);
                            eb.setTitle("Command executed");
                            eb.setDescription("Alert was sent to " + p.getDisplayName());
                        } else {
                            eb.setTitle("Command terminated");
                            eb.setColor(Pals.error);
                            eb.setDescription("Player could not be found or is offline.");
                        }
                        ctx.channel.sendMessage(eb);
                    }
                }
            });

            handler.registerCommand(new RoleRestrictedCommand("op") {
                {
                    help = "Toggle player's op permissions.";
                    usage = "<player>";
                    minArguments = 1;
                    role = moderator_role_id;
                    category = moderation;
                }

                @Override
                public void run(Context ctx) {
                    EmbedBuilder eb = new EmbedBuilder();
                    Player player = findPlayer(ctx.message);
                    if (player != null) {
                        Bukkit.getScheduler().runTask(DiscordPlugin.getInstance(), () -> {
                            debug(player.getDisplayName() + " op: " + player.isOp() + " (before command execution)");
                            player.setOp(!player.isOp());
                            eb.setTitle("Command executed!")
                                    .setDescription((player.isOp() ? "Promoted " : "Demoted ") + player.getDisplayName() + (player.isOp() ? " to " : " from ") + " Operator!");
                            ctx.channel.sendMessage(eb);
                        });
                    } else {
                        playerNotFound(ctx.message, eb, ctx);
                        return;
                    }
                }
            });

            handler.registerCommand(new RoleRestrictedCommand("tp") {
                {
                    help = "Teleport a player to specific Coordinates";
                    usage = "<player> <X Y Z>";
                    category = moderation;
                    role = moderator_role_id;
                }

                @Override
                public void run(Context ctx) {
                    EmbedBuilder eb = new EmbedBuilder();
                    Player target = findPlayer(ctx.args[1]);
                    if (target != null) {
                        double x, y, z;
                        try {
                            x = Float.parseFloat(ctx.args[2]);
                            y = Float.parseFloat(ctx.args[3]);
                            z = Float.parseFloat(ctx.args[4]);
                        } catch (Exception e) {
                            eb.setTitle("Error")
                                    .setColor(new Color(0xff0000))
                                    .setDescription("X, Y, Z must be floats");
                            ctx.channel.sendMessage(eb);
                            return;
                        }
                        runSynchronous(() -> {
                            target.teleport(new Location(target.getWorld(), x, y, z));
                            eb.setTitle("Command executed!")
                                    .setDescription("Successfully teleported " + target.getDisplayName() + "!")
                                    .addField("New Position: ", "X: `" + x + "` Y: `" + y + "` Z: `" + z + "`");
                            ctx.channel.sendMessage(eb);
                        });
                    } else {
                        playerNotFound(ctx.args[1], eb, ctx);
                    }
                }
            });
        }
    }

    private StringBuilder lookup(EmbedBuilder eb, Player player) {
        StringBuilder s = new StringBuilder();
        String uuid = player.getUniqueId().toString();
        PlayerData pd = getData(uuid);
        if (pd != null) {
            // some mc stats
            eb.addField("Health", String.valueOf(player.getHealth()), true);
            eb.addField("Ping", String.valueOf(player.getPing()), true);
            eb.addField("Total Experience", String.valueOf(player.getTotalExperience()), true);
            eb.addField("Pose", player.getPose().toString(), true);
            eb.addField("Position", "`" + player.getLocation().getX() + " " + player.getLocation().getY() + " " + player.getLocation().getY() + "`", true);

            // database stats
            eb.addField("Database", "Data from the Database:");
            eb.addField("Level", String.valueOf(pd.level), true);
            eb.addField("Playtime", pd.playtime + " minutes", true);
            eb.addField("Banned", String.valueOf(pd.banned), true);
            if (pd.banned || pd.bannedUntil > Instant.now().getEpochSecond()) {
                eb.addField("Ban Reason", pd.banReason, true);
                long now = Instant.now().getEpochSecond();
                // convert seconds to days hours seconds etc
                int n = (int) (pd.bannedUntil - now);
                int day = n / (24 * 3600);

                n = n % (24 * 3600);
                int hour = n / 3600;

                n %= 3600;
                int minutes = n / 60;

                n %= 60;
                int seconds = n;


                eb.addField("Remaining ban time", day + " " + "days " + hour
                        + " " + "hours " + minutes + " "
                        + "minutes " + seconds + " "
                        + "seconds ", true);
                eb.addField("Banned Until", new java.text.SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new java.util.Date(pd.bannedUntil * 1000)), true);
            }

//            CompletableFuture<User> user = ioMain.api.getUserById(pd.discordLink);
//            user.thenAccept(user1 -> {
//                eb.addField("Discord Link", user1.getDiscriminatedName());
//            });


        }
        return s;
    }
}

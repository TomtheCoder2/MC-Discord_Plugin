package minecraft.plugin;

import minecraft.plugin.discordcommands.Command;
import minecraft.plugin.discordcommands.Context;
import minecraft.plugin.discordcommands.DiscordCommands;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.javacord.api.entity.message.embed.EmbedBuilder;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

import static minecraft.plugin.DiscordPlugin.api;
import static minecraft.plugin.DiscordPlugin.prefix;
import static minecraft.plugin.utils.Utils.*;

public class Public {
    public void registerCommands(DiscordCommands handler) {
        handler.registerCommand(new Command("info") {
            @Override
            public void run(Context ctx) {
                EmbedBuilder eb = new EmbedBuilder()
                        .setTitle("Info idk");
                ctx.channel.sendMessage(eb);
            }
        });
        handler.registerCommand(new Command("debugInfo") {
            {
                help = "Debug infos";
                aliases.add("di");
            }

            @Override
            public void run(Context ctx) {
                ctx.channel.sendMessage(new EmbedBuilder()
                        .setTitle("Debug Info:")
                        .setDescription(getDebugInfo()));
            }
        });

        handler.registerCommand(new Command("players") {
            {
                help = "List of all players online.";
                aliases.add("p");
            }

            @Override
            public void run(Context ctx) {
                EmbedBuilder eb = new EmbedBuilder()
                        .setTitle("Players: ");
                StringBuilder sb = new StringBuilder();
                for (Player p : Bukkit.getOnlinePlayers()) {
                    sb.append(p.getDisplayName()).append(" ").append(p.getAddress());
                }
                eb.setDescription(sb.toString());
                ctx.channel.sendMessage(eb);
            }
        });


        handler.registerCommand(new Command("help") {
            {
                help = "Display all available minecraft.plugin.commands and their usage.";
                usage = "[command]";
                aliases.add("h");
            }

            public void run(Context ctx) {
                if (ctx.args.length == 1) {
                    StringBuilder publicCommands = new StringBuilder();
                    StringBuilder management = new StringBuilder();
                    StringBuilder moderation = new StringBuilder();
                    StringBuilder mapReviewer = new StringBuilder();

                    ArrayList<Command> commandList = new ArrayList<>(handler.getAllCommands());
                    Collections.shuffle(commandList);

                    for (Command command : commandList) {
                        if (command.hidden) continue;
                        if (!command.hasPermission(ctx)) continue;
//                        if (!Objects.equals(command.category, "public")) {
//                            if (ctx.channel.getId() != Long.parseLong(staff_bot_channel_id)
//                                    && ctx.channel.getId() != Long.parseLong(admin_bot_channel_id)) {
//                                if (ctx.channel.getId() == Long.parseLong(apprentice_bot_channel_id) && !command.apprenticeCommand) {
//                                    continue;
//                                } else if (ctx.channel.getId() != Long.parseLong(apprentice_bot_channel_id)) {
//                                    continue;
//                                }
//                            }
//                        }
                        switch (command.category) {
                            case Categories.moderation -> {
                                moderation.append("**").append(command.name).append("** ");
//                                if (!command.usage.equals("")) {
//                                    moderation.append(command.usage);
//                                }
                                moderation.append("\n");
                            }
                            case Categories.management -> {
                                management.append("**").append(command.name).append("** ");
//                                if (!command.usage.equals("")) {
//                                    management.append(command.usage);
//                                }
                                management.append("\n");
                            }
                            case Categories.mapReviewer -> {
                                mapReviewer.append("**").append(command.name).append("** ");
//                                if (!command.usage.equals("")) {
//                                    mapReviewer.append(command.usage);
//                                }
                                mapReviewer.append("\n");
                            }
                            default -> {
                                publicCommands.append("**").append(command.name).append("** ");
//                                if (!command.usage.equals("")) {
//                                    publicCommands.append(command.usage);
//                                }
                                publicCommands.append("\n");
                            }
                        }
                    }
                    EmbedBuilder embed = new EmbedBuilder()
                            .setTitle("Public:");
                    embed.addField("**__Public:__**", publicCommands.toString(), true);
                    if (moderation.length() != 0) {
                        embed.addField("**__Moderation:__**", moderation.toString(), true);
                    }
                    if (management.length() != 0) {
                        embed.addField("**__Management:__**", management.toString(), true);
                    }
                    if (mapReviewer.length() != 0) {
                        embed.addField("**__Map reviewer:__**", mapReviewer.toString(), true);
                    }
                    ctx.channel.sendMessage(embed);
                } else {
                    EmbedBuilder embed = new EmbedBuilder();
                    if (Objects.equals(ctx.args[1], "aliases")) {
                        embed.setTitle("All aliases");
                        StringBuilder aliases = new StringBuilder();
                        for (String alias : handler.aliasRegistry.keySet()) {
                            aliases.append(alias).append(" -> ").append(handler.aliasRegistry.get(alias).name).append("\n");
                        }
                        embed.setDescription(aliases.toString());
                        ctx.channel.sendMessage(embed);
                        return;
                    }
                    Command command = handler.registry.getOrDefault(ctx.args[1].toLowerCase(), handler.aliasRegistry.get(ctx.args[1].toLowerCase()));
                    if (command == null) {
                        embed.setColor(new Color(0xff0000))
                                .setTitle("Error")
                                .setDescription("Couldn't find this command!");
                        ctx.channel.sendMessage(embed);
                        return;
                    }
                    embed.setTitle(command.name)
                            .setDescription(command.help);
                    if (!command.usage.equals("")) {
                        embed.addField("Usage:", prefix + command.name + " " + command.usage);
                    }
                    embed.addField("Category:", command.category);
                    StringBuilder aliases = new StringBuilder();
                    if (command.aliases.size() > 0) {
                        for (String alias : command.aliases) {
                            aliases.append(alias).append(", ");
                        }
                        embed.addField("Aliases:", aliases.toString());
                    }
                    ctx.channel.sendMessage(embed);
                }
            }
        });
    }

    /**
     * Get debug information about current setup
     *
     * @return debug info in one long string with line breaks
     */
    public String getDebugInfo() {
        String configFileString = null;

        StringBuilder b = new StringBuilder();
        // General
        b.append("# Some general stuff\n");
        b.append("versionServer: ").append(Bukkit.getVersion()).append(" (").append(Bukkit.getBukkitVersion()).append(")").append('\n');
//        b.append("plugins:");
//        for (Plugin plugin : DiscordPlugin.getServer().getPluginManager().getPlugins()) {
//            String plEnabled = plugin.isEnabled() ? "true" : "false";
//            String plName = plugin.getName();
//            String plVersion = plugin.getDescription().getVersion();
//            b.append("\n  ").append(plName).append(":\n    ").append("version: '").append(plVersion).append('\'').append("\n    enabled: ")
//                    .append(plEnabled);
//        }

        // JVM
        b.append("\n\n# Now some jvm related information\n");
        Runtime runtime = Runtime.getRuntime();
        b.append("memory.free: ").append(runtime.freeMemory()).append('\n');
        b.append("memory.max: ").append(runtime.maxMemory()).append('\n');
        b.append("java.specification.version: '").append(System.getProperty("java.specification.version")).append("'\n");
        b.append("java.vendor: '").append(System.getProperty("java.vendor")).append("'\n");
        b.append("java.version: '").append(System.getProperty("java.version")).append("'\n");
        b.append("os.arch: '").append(System.getProperty("os.arch")).append("'\n");
        b.append("os.name: '").append(System.getProperty("os.name")).append("'\n");
        b.append("os.version: '").append(System.getProperty("os.version")).append("'\n\n");

        // DiscordMC
        b.append("# DiscordMC related stuff\n");
        b.append("configFile: ").append((String) null).append('\n');
        b.append("\nbotName: ").append(api.getYourself().getName()).append('\n');

        return b.toString();
    }
}

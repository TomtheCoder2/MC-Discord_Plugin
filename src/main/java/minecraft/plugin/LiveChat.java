package minecraft.plugin;

import minecraft.plugin.discord.DiscordCommands;
import minecraft.plugin.discord.MessageCreatedListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.javacord.api.event.message.MessageCreateEvent;
import org.json.JSONObject;

import static minecraft.plugin.DiscordPlugin.live_chat_channel;
import static minecraft.plugin.DiscordPlugin.prefix;
import static minecraft.plugin.utils.Utils.setColors;

public class LiveChat {
    private final JSONObject data;

    public LiveChat(JSONObject data) {
        this.data = data;
    }

    public void registerLiveChat(DiscordCommands handler) {
        if (data.has("live_chat_channel_id")) {
            handler.registerOnMessage(new MessageCreatedListener() {
                @Override
                public void run(MessageCreateEvent event) {
                    super.run(event);
                    String message = event.getMessageContent();

                    // check if it's a live chat message
                    if (event.getChannel().getId() == live_chat_channel.getId() && !event.getMessageAuthor().isBotUser()
                            && !event.getMessageContent().startsWith(prefix) // TODO: Change later for commands in the live chat (for example translate)
                    ) {
//                        if (event.getMessageContent().split(" ")[0].substring(prefix.length()).equals("translate") || event.getMessageContent().split(" ")[0].substring(prefix.length()).equals("t")) {
//                            if (event.getMessageContent().split(" ").length < 3) {
//                                event.getChannel().sendMessage(new EmbedBuilder()
//                                        .setTitle("Too few arguments!")
//                                        .setColor(new Color(0xff0000))
//                                );
//                                return;
//                            }
//                            try { // translate
//                                String[] args = message.split(" ", 3);
//                                JSONObject res = new JSONObject(Translate.translate(escapeEverything(args[2]), args[1]));
//                                if (res.has("translated") && res.getJSONObject("translated").has("text")) {
//                                    String translated = res.getJSONObject("translated").getString("text");
//                                    Call.sendMessage("[sky]" + event.getMessageAuthor().getName() + "@discord >[] " + translated);
//                                    event.getMessage().delete();
//                                    event.getChannel().sendMessage("<translated>**" + event.getMessageAuthor().getName() + "@discord**: " + translated);
//                                } else {
//                                    event.getChannel().sendMessage(new EmbedBuilder()
//                                            .setTitle("There was an error!")
//                                            .setColor(new Color(0xff0000))
//                                            .setDescription("There was an error: " + (res.has("error") ? res.getString("error") : "No more information, ask Nautilus!")));
//                                }
//                            } catch (Exception e) {
//                                event.getChannel().sendMessage(new EmbedBuilder()
//                                        .setTitle("There was an error!")
//                                        .setColor(new Color(0xff0000))
//                                        .setDescription("There was an error: " + e.getMessage()));
//                            }
//                            return;
//                        }
//                        log(ChatColor.AQUA + event.getMessageAuthor().getName() + "@discord > " + ChatColor.WHITE + setColors(event.getMessageContent()));
                        Bukkit.getServer().broadcastMessage(ChatColor.AQUA + event.getMessageAuthor().getName() + "@discord > " + ChatColor.WHITE + setColors(event.getMessageContent()));
                    }
                }
            });
        }
    }
}

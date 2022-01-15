package minecraft.plugin;

import minecraft.plugin.data.PersistentPlayerData;
import minecraft.plugin.data.PlayerData;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.time.Instant;

import static minecraft.plugin.DiscordPlugin.*;
import static minecraft.plugin.database.Utils.getData;
import static minecraft.plugin.database.Utils.setData;
import static minecraft.plugin.utils.Log.debug;
import static minecraft.plugin.utils.Log.log;
import static minecraft.plugin.utils.Utils.bannedNames;
import static minecraft.plugin.utils.Utils.setColors;

public class EventHandler implements Listener {
    DiscordPlugin plugin = DiscordPlugin.getInstance();

    public EventHandler(DiscordPlugin dp) {
        plugin = dp;
        dp.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @org.bukkit.event.EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        debug("Event onPlayerJoin called.");
        Player player = event.getPlayer();
        if (bannedNames.contains(player.getDisplayName())) {
            player.kickPlayer("Your Name is on the Banned Name list, please change your name to something appropriate!");
            return;
        }
        PlayerData pd = getData(player.getUniqueId().toString());
        String uuid = player.getUniqueId().toString();

        if (!playerDataGroup.containsKey(uuid)) {
            PersistentPlayerData data = new PersistentPlayerData();
            playerDataGroup.put(uuid, data);
        }

        if (pd != null) {
            if (pd.banned || pd.bannedUntil > Instant.now().getEpochSecond()) {
                player.kickPlayer("You are banned. Reason:\n" + pd.banReason + "\nIf you what to appeal join our discord server: " + discordInviteLink);
                return;
            }
            long level = pd.level;
            // [10] Nautilus
            player.setPlayerListName("[" + level + "] " + player.getDisplayName());
        } else {
            log("New Player connected: " + player.getDisplayName());
            setData(uuid, new PlayerData(0, uuid));
        }

        if (!joinedPlayers.containsKey(uuid)) {
            joinedPlayers.put(uuid, player);
        }
    }

    @org.bukkit.event.EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        if (event.getMessage().charAt(0) != '/') {
            Player player = event.getPlayer();
            PersistentPlayerData persistentPlayerData = playerDataGroup.get(player.getUniqueId().toString());
            if (persistentPlayerData != null) {
                if (!persistentPlayerData.muted) {
                    live_chat_channel.sendMessage("**" + player.getDisplayName() + "**: " + event.getMessage());
                    event.setMessage(setColors(event.getMessage()));
                } else {
                    player.sendMessage(ChatColor.RED + "You are muted!");
                    event.setMessage("");
                }
            } else {
                live_chat_channel.sendMessage("**" + player.getDisplayName() + "**: " + event.getMessage());
                event.setMessage(setColors(event.getMessage()));
            }
        }
    }
}

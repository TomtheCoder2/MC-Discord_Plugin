package minecraft.plugin;

import minecraft.plugin.data.PlayerData;
import minecraft.plugin.discord.DiscordCommands;
import minecraft.plugin.discordcommands.Moderation;
import minecraft.plugin.discordcommands.Public;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.javacord.api.DiscordApi;
import org.json.JSONObject;

import static minecraft.plugin.DiscordPlugin.joinedPlayers;
import static minecraft.plugin.DiscordPlugin.log_channel;
import static minecraft.plugin.database.Utils.getData;
import static minecraft.plugin.database.Utils.setData;
import static minecraft.plugin.utils.Utils.logConnections;

public class BotThread extends Thread {
    public DiscordApi api;
    public DiscordCommands commandHandler = new DiscordCommands();
    private Thread mt;
    private JSONObject data;
    private int logCount = 0;

    /**
     * start the bot thread
     *
     * @param api  the discordApi to operate with
     * @param mt   the main Thread
     * @param data the data from settings.json
     */
    public BotThread(DiscordApi api, Thread mt, JSONObject data) {
        this.api = api; //new DiscordApiBuilder().setToken(data.get(0)).login().join();
        this.mt = mt;
        this.data = data;

        // register minecraft.plugin.commands
        this.api.addMessageCreateListener(commandHandler);
        new Public().registerCommands(commandHandler);
        new Moderation(data).registerCommands(commandHandler);
        new LiveChat(data).registerLiveChat(commandHandler);
        //new MessageCreatedListeners(data).registerListeners(commandHandler);
    }

    public void run() {
//        Timer.schedule(ioMain.loop(), 0.5F);
        while (this.mt.isAlive()) {
            try {
                Thread.sleep(60 * 1000);

//                System.out.println(joinedPlayer);

//                update(log_channel, api);
                if ((logCount & 5) == 0) {
                    // log player joins
                    logConnections(log_channel, joinedPlayers, "join");

//                    logConnections(log_channel, leftPlayers, "leave");
                }
                logCount++;

                for (Player player : Bukkit.getOnlinePlayers()) {
                    PlayerData pd = getData(player.getUniqueId().toString());
                    if (pd == null) continue;

                    pd.playtime++;
                    setData(pd.uuid, pd);
                }
                api.updateActivity("with " + Bukkit.getOnlinePlayers().size() + " players");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        api.disconnect();
    }
}
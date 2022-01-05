package minecraft.plugin;

import commands.Moderation;
import commands.Public;
import minecraft.plugin.discordcommands.DiscordCommands;
import org.javacord.api.DiscordApi;
import org.json.JSONObject;

public class BotThread extends Thread {
    public DiscordApi api;
    public DiscordCommands commandHandler = new DiscordCommands();
    private Thread mt;
    private JSONObject data;

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

        // register commands
        this.api.addMessageCreateListener(commandHandler);
        new Public().registerCommands(commandHandler);
        new Moderation(data).registerCommands(commandHandler);
        //new MessageCreatedListeners(data).registerListeners(commandHandler);
    }

    public void run() {
//        Timer.schedule(ioMain.loop(), 0.5F);
        while (this.mt.isAlive()) {
            try {
                Thread.sleep(60 * 1000);

//                System.out.println(joinedPlayer);

//                update(log_channel, api);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        api.disconnect();
    }
}
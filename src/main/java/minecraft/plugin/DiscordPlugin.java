package minecraft.plugin;

import org.bukkit.plugin.java.JavaPlugin;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.channel.TextChannel;
import org.json.JSONObject;

import static minecraft.plugin.utils.Utils.getTextChannel;
import static minecraft.plugin.utils.Utils.readFromJson;

public class DiscordPlugin extends JavaPlugin {
    public static DiscordApi api;
    public static String prefix = "!";
    public static TextChannel error_log_channel;
    public static String token = "";
    public static String serverName = "";
    public static String admin_role_id = "";
    public static TextChannel log_channel;
    private static DiscordPlugin instance;

    public static DiscordPlugin getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        getLogger().info("Start Discord Plugin!");

        // read some data idk
        JSONObject data;
        JSONObject json = data = readFromJson("settings.json");
        try {
            serverName = json.getString("server_name");
            token = json.getString("token");
            admin_role_id = json.getString("admin_role_id");
            json.getString("log_channel_id"); // try to get it, so there won't be an error later
        } catch (Exception e) {
            getLogger().warning("Failed to read to Discord Settings! Disabling plugin!");
            getPluginLoader().disablePlugin(this);
            return;
        }

        // Connect to Discord
        api = new DiscordApiBuilder()
                .setToken(token) // Set the token of the bot here
                .login() // Log the bot in
                .exceptionally(error -> {
                    // Log a warning when the login to Discord failed (wrong token?)
                    getLogger().warning("Failed to connect to Discord! Disabling plugin!");
                    getPluginLoader().disablePlugin(this);
                    return null;
                })
                .join();

        // Log a message that the connection was successful and log the url that is needed to invite the bot
        getLogger().info("Connected to Discord as " + api.getYourself().getDiscriminatedName());
        getLogger().info("Open the following url to invite the bot: " + api.createBotInvite());


        BotThread bt = new BotThread(api, Thread.currentThread(), data);
        bt.setDaemon(false);
        bt.start();

        // set all channels
        error_log_channel = getTextChannel("852938976293158922");
        log_channel = getTextChannel(json.getString("log_channel_id"));
    }

    @Override
    public void onDisable() {
        if (api != null) {
            // Make sure to disconnect the bot when the plugin gets disabled
            api.disconnect();
            api = null;
        }
    }
}
package minecraft.plugin;

import minecraft.plugin.data.PersistentPlayerData;
import org.apache.commons.io.FileUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.listener.GloballyAttachableListener;
import org.json.JSONObject;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;

import static java.util.concurrent.TimeUnit.SECONDS;
import static minecraft.plugin.utils.Log.debug;
import static minecraft.plugin.utils.Log.log;
import static minecraft.plugin.utils.Utils.*;

@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        MongoAutoConfiguration.class,
        MongoDataAutoConfiguration.class
})
@ComponentScan("minecraft.plugin.springbackend.test2")
public class DiscordPlugin extends JavaPlugin implements Listener {
    public static DiscordApi api;
    public static String prefix = "!";
    public static String token = "";
    public static String serverName = "";
    public static String admin_role_id = "";
    public static Boolean debugEnabled = false;
    public static String discordInviteLink = "<invite link>";
    public static HashMap<String, Player> joinedPlayers = new HashMap<>();
    // channels
    public static TextChannel log_channel;
    public static TextChannel error_log_channel;
    public static TextChannel live_chat_channel;
    // database
    public static String url = "";
    public static String user = "";
    public static String password = "";
    public static HashMap<String, PersistentPlayerData> playerDataGroup = new HashMap<>(); // uuid(), data
    private static DiscordPlugin instance;
    BotThread bt;
    // channels
    ArrayList<String> channels = new ArrayList<>();

    public static DiscordPlugin getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        getLogger().info("Start Discord Plugin! api:" + api);

        // read some data IDK
        JSONObject data;
        JSONObject json = data = readFromJson("settings.json");
        try {
            if (json != null) {
                serverName = json.getString("server_name");
                token = json.getString("token");
                admin_role_id = json.getString("admin_role_id");
                // set database url, username and pwd
                getFromJson(json, String.class, "url");
                getFromJson(json, String.class, "user");
                getFromJson(json, String.class, "password");
                getFromJson(json, boolean.class, "debugEnabled");
                getFromJson(json, Boolean.class, "discordInviteLink");
            } else {
                // create a default json file
                InputStream is = instance.getResource("defaultSettings.json");
                File dest = new File("settings.json");
                if (is != null) {
                    try {
                        FileUtils.copyInputStreamToFile(is, dest);
                    } catch (IOException e) {
                        getLogger().warning("Failed to create the default json file: " + Arrays.toString(e.getStackTrace()) + "\n" + e.getMessage());
                        getPluginLoader().disablePlugin(this);
                        return;
                    }
                }
                getLogger().warning("Created settings.json file! Please set the configs there and reload the plugin!");
                getPluginLoader().disablePlugin(this);
                return;
            }
        } catch (Exception e) {
            getLogger().warning("Failed to read to Discord Settings! Disabling plugin!" + e);
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


        // create thread for discord bot
        bt = new BotThread(api, Thread.currentThread(), data);
        bt.setDaemon(false);
        bt.start();

        // add event listeners
        new EventHandler(this);

        // set all channels
        channels.add("log_channel");
        channels.add("live_chat_channel");
        channels.add("log_channel");

        getChannelsFromJson(json, channels);

        error_log_channel = getTextChannel("852938976293158922");

        debug("Debug enabled!");
        try {
            debug("Try to load LostConnectionListener class");
            Class.forName("org.javacord.api.listener.connection.LostConnectionListener");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // start website backend
        try {
            SpringApplication.run(DiscordPlugin.class);
        } catch (Exception e) {
            log("Couldn't load website");
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        log("Disabling " + DiscordPlugin.class.getName() + ", api: " + api);
        if (api != null) {
            try {
                debug("Try to load LostConnectionListener class");
                Class.forName("org.javacord.api.listener.connection.LostConnectionListener");
//                Class.forName("org.javacord.core.util");
                Class.forName("org.javacord.core.util.gateway.*");
                CountDownLatch shutdownWaiter = new CountDownLatch(1);
//            api.addLostConnectionListener(event -> {
//                shutdownWaiter.countDown();
//                debug("Lost connection to Discord");
//            });
                api.disconnect();
//                Thread.sleep(10 * 1000);
                log("Wait 10 Seconds to disconnect the javacord api...");
//                Thread.sleep(10 * 1000);
                shutdownWaiter.await(10, SECONDS);
            } catch (Exception e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
            debug("api: " + api + ", listeners: " + api.getListeners());
            for (GloballyAttachableListener i : api.getListeners().keySet()) {
                api.removeListener(i);
            }
            debug("api: " + api + ", listeners: " + api.getListeners());
        }
    }
}
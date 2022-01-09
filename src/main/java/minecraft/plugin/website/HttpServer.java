package minecraft.plugin.website;

import minecraft.plugin.website.config.Configuration;
import minecraft.plugin.website.config.ConfigurationManager;
import minecraft.plugin.website.core.ServerListenerThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * waiting for next part of the tutorial lol
 */
public class HttpServer {
    private final static Logger LOGGER = LoggerFactory.getLogger(HttpServer.class);

    public static void main(String[] args) {
        LOGGER.info("Starting HttpServer...");

        ConfigurationManager.getInstance().loadConfigurationFile("http.json");
        Configuration conf = ConfigurationManager.getInstance().getCurrentConfiguration();

        LOGGER.info("Using Port: " + conf.getPort());
        LOGGER.info("Using webRoot: " + conf.getWebroot());

        try {
            ServerListenerThread serverListenerThread = new ServerListenerThread(conf.getPort(), conf.getWebroot());
            serverListenerThread.start();
        } catch (IOException e) {
            e.printStackTrace();
            //TODO: handle later
        }
    }
}

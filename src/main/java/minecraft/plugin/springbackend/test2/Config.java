package minecraft.plugin.springbackend.test2;

import org.json.JSONObject;

import static minecraft.plugin.utils.Utils.getFromJson;
import static minecraft.plugin.utils.Utils.readFromJson;
import static minecraft.plugin.DiscordPlugin.*;

public class Config {
    public static void setVars() {
        JSONObject json = readFromJson("settings.json");
        serverName = json.getString("server_name");
        token = json.getString("token");
        admin_role_id = json.getString("admin_role_id");
        // set database url, username and pwd
        getFromJson(json, String.class, "url");
        getFromJson(json, String.class, "user");
        getFromJson(json, String.class, "password");
        getFromJson(json, boolean.class, "debugEnabled");
        getFromJson(json, Boolean.class, "discordInviteLink");
    }
}

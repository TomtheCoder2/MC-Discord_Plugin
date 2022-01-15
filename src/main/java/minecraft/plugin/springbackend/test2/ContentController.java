package minecraft.plugin.springbackend.test2;

import minecraft.plugin.data.PlayerData;
import org.bukkit.Bukkit;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import static minecraft.plugin.database.Utils.getData;

@Controller
public class ContentController {

    @GetMapping("/greeting") // get everything with <url>/greeting to call this function
    public String greeting(@RequestParam(name = "name", required = false, defaultValue = "World") String name, Model model) { // set request params (name)
        model.addAttribute("name", name);
        return "greeting";
    }

    @GetMapping("/players")
    public String players(Model model) {
        model.addAttribute("players", Bukkit.getOnlinePlayers());
        return "players";
    }

    @GetMapping("/player")
    public String players(@RequestParam(name = "uuid", required = true) String uuid, Model model) {
        PlayerData pd = getData(uuid);
        if (pd == null) {
            return "error";
        }
        model.addAttribute("uuid", uuid);
        model.addAttribute("play_time", pd.playtime);
        model.addAttribute("level", pd.level);
        model.addAttribute("discord_id", pd.discordId);
        model.addAttribute("banned", pd.banned);
        model.addAttribute("banned_until", pd.bannedUntil);
        model.addAttribute("ban_reason", pd.banReason);
        return "player";
    }
}
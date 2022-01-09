package minecraft.plugin.data;

import java.io.Serializable;

/**
 * uuid
 * level
 * playtime
 * discordId (id of linked discord account)
 * <p>
 * banned
 * bannedUntil
 * banReason
 */
public class PlayerData implements Cloneable {
    public String uuid;
    public long level;
    public long playtime = 0;
    public long discordId = 0;
    public Boolean banned = false;
    public long bannedUntil = 0;
    public String banReason = "";

    public PlayerData(Integer level, String uuid) {
        this.level = level;
        this.uuid = uuid;
    }

    public void reprocess() {
        if (banReason == null) this.banReason = "";
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}

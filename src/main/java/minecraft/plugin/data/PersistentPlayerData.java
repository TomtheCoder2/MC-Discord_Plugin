package minecraft.plugin.data;

import java.io.Serializable;

public class PersistentPlayerData implements Serializable {
    public String origName;
    public boolean muted = false;
    public boolean frozen = false;

    public PersistentPlayerData() {
    }
}

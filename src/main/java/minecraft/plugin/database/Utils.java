package minecraft.plugin.database;
// database content

import minecraft.plugin.data.PlayerData;

import java.sql.*;

import static minecraft.plugin.DiscordPlugin.*;
import static minecraft.plugin.utils.Log.debug;
import static minecraft.plugin.utils.Log.log;

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
public class Utils {
    /**
     * Connect to the PostgreSQL Server
     */
    public static Connection connect() throws SQLException, ClassNotFoundException {
        Class.forName("org.postgresql.Driver");
        return DriverManager.getConnection(url, user, password);
    }

    /**
     * Set Data for a specific player
     *
     * @param uuid uuid of the player
     * @param pd   player Data
     */
    public static void setData(String uuid, PlayerData pd) {
        if (getData(uuid) == null) {
            // define all variables
            String SQL = "INSERT INTO mcdata(uuid, level, playTime, discordid, banned, bannedUntil, banReason) "
                    + "VALUES(?, ?, ?, ?, ?, ?, ?)";

            long id = 0;

            try (Connection conn = connect();
                 PreparedStatement pstmt = conn.prepareStatement(SQL,
                         Statement.RETURN_GENERATED_KEYS)) {
//                debug("Login successful!");

                // set all variables
                pstmt.setString(1, uuid);
                pstmt.setLong(2, pd.level);
                pstmt.setLong(3, pd.playtime);
                pstmt.setLong(4, pd.discordId);
                pstmt.setBoolean(5, pd.banned);
                pstmt.setLong(6, pd.bannedUntil);
                pstmt.setString(7, pd.banReason);

                debug(pstmt.toString());

                // send the data
                int affectedRows = pstmt.executeUpdate();
                // check the affected rows
                if (affectedRows > 0) {
                    // get the ID back
                    try (ResultSet rs = pstmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            id = rs.getLong(1);
                        }
                    } catch (SQLException ex) {
                        debug(ex.getMessage());
                    }
                }
                conn.close();
            } catch (Exception ex) {
                log(ex.getMessage());
            }
        } else {
            String SQL = "UPDATE mcdata "
                    + "SET level = ?, "
                    + "playTime = ?, "
                    + "discordid = ?, "
                    + "banned = ?, "
                    + "bannedUntil = ?, "
                    + "banReason = ? "
                    + "WHERE uuid = ?";

            int affectedrows = 0;

            try (Connection conn = connect();
                 PreparedStatement pstmt = conn.prepareStatement(SQL)) {

                // set all variables
                pstmt.setString(7, uuid);
                pstmt.setLong(1, pd.level);
                pstmt.setLong(2, pd.playtime);
                pstmt.setLong(3, pd.discordId);
                pstmt.setBoolean(4, pd.banned);
                pstmt.setLong(5, pd.bannedUntil);
                pstmt.setString(6, pd.banReason);
//                debug(pstmt);

                affectedrows = pstmt.executeUpdate();
//                debug("affctected rows: " + affectedrows);
                conn.close();
            } catch (Exception ex) {
                log(ex.getMessage());
            }
        }
    }

    /**
     * Get Data from a specific player
     *
     * @param uuid the uuid of the player
     */
    public static PlayerData getData(String uuid) {
//        debug(uuid);
        // search for the uuid
        String SQL = "SELECT uuid, level, playTime, discordid, banned, bannedUntil, banReason "
                + "FROM mcdata "
                + "WHERE uuid = ?";
        try {
            // connect to the database
            connect();
//            debug("Login successful!");
            Connection conn = connect();
            PreparedStatement pstmt = conn.prepareStatement(SQL);

            // replace ? with the uuid
            pstmt.setString(1, uuid);
            // get the result
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                // create a new Player to return
                PlayerData pd = new PlayerData(rs.getInt("level"), uuid);

                // set all stats
                pd.uuid = rs.getString("uuid");
                pd.playtime = rs.getLong("playTime");
                pd.discordId = rs.getLong("discordId");
                pd.banned = rs.getBoolean("banned");
                pd.bannedUntil = rs.getLong("bannedUntil");
                pd.banReason = rs.getString("banReason");
                // finally, return it
                return pd;
            } else {
//                debug(rs.next());
            }
            conn.close();
        } catch (Exception ex) {
            log(ex.getMessage());
            ex.printStackTrace();
        }
        // if there's no player return null
        return null;
    }
}

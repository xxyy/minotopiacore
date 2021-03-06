/*
 * MinoTopiaCore
 * Copyright (C) 2013 - 2017 Philipp Nowak (https://github.com/xxyy) and contributors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package li.l1t.mtc.module.website;

import li.l1t.mtc.api.MTCPlugin;
import li.l1t.mtc.api.command.CommandBehaviours;
import li.l1t.mtc.misc.ClearCacheBehaviour;
import li.l1t.mtc.module.ConfigurableMTCModule;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p> Central entry point for the website module. </p> This module takes care of allowing users to
 * confirm their accounts and change their passwords directly in-game using a command. It also
 * records the amount of time they spend on the server. Additionally, it stores online players in a
 * database so that online state can be conveniently displayed on the website.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 10/10/14
 */
public final class WebsiteModule extends ConfigurableMTCModule implements Listener {
    public static final String ONLINE_TABLE_NAME = "ni176987_1_DB.onlineuser"; //legacy name, not my choice
    public static final String PLAYTIME_TABLE_NAME = "mt_main.play_time";
    public static final String WEBSITE_USER_TABLE_NAME = "ni176987_1_DB.hp_user"; //legacy name
    public static final String NAME = "Website";
    private static final String PASSWORD_CHANGING_PATH = "password-changing";

    private final Map<UUID, Instant> playerJoinTimes = new ConcurrentHashMap<>();
    private boolean passwordChangeEnabled = true;
    private WebsiteListener listener;

    public WebsiteModule() {
        super(NAME, "website_config.yml", ClearCacheBehaviour.RELOAD, true);
    }

    @Override
    public void enable(MTCPlugin plugin) throws Exception {
        super.enable(plugin);

        listener = new WebsiteListener(this);
        plugin.getServer().getPluginManager().registerEvents(listener, plugin);
        registerCommand(new CommandWebsite(this), "website", "hp")
                .behaviour(CommandBehaviours.playerOnly());

        plugin.getServer().getOnlinePlayers().forEach(p -> {
            registerJoinTime(p);
            setPlayerOnline(p, true);
        }); //Register join time and online state for players already on the server
    }

    @Override
    protected void reloadImpl() {
        configuration.addDefault(PASSWORD_CHANGING_PATH, true);
        configuration.options().copyDefaults(true);

        passwordChangeEnabled = configuration.getBoolean(PASSWORD_CHANGING_PATH);
    }

    @Override
    public void disable(MTCPlugin plugin) {
        HandlerList.unregisterAll(listener); //We'll get some reload functionality working for modules eventually

        playerJoinTimes.keySet().forEach(this::saveTimePlayed);
        plugin.getServer().getOnlinePlayers().forEach(p -> setPlayerOnline(p, false));
    }

    public boolean isPasswordChangeEnabled() {
        return passwordChangeEnabled;
    }

    void registerJoinTime(Player plr) { //Convenience method to allow for more readable lambdas
        registerJoinTime(plr.getUniqueId());
    }

    /**
     * Saves the join time for given UUID to the current time. This is used to count the time
     * played.
     *
     * @param uuid the unique id of the player who joined
     */
    void registerJoinTime(UUID uuid) {
        playerJoinTimes.put(uuid, Instant.now());
    }

    /**
     * Gets the time played in the current session for a player and removes it from the queue. If no
     * time has been recorded for the given player, 0 is returned.
     *
     * @param uuid the unique id of the target player
     * @return the amount of minutes the target player has played in the current session
     */
    long popMinutesPlayed(UUID uuid) { //This is *NOT* an API method, note how it only applies to the current session
        long minutesPlayed = ChronoUnit.MINUTES.between(
                playerJoinTimes.getOrDefault(uuid, Instant.now()), //Just making sure
                Instant.now());
        playerJoinTimes.remove(uuid);
        return minutesPlayed;
    }

    /**
     * Writes the time played in the current session to database for given player. Note that this
     * method is <b>blocking</b>.
     *
     * @param uuid the unique id of the target player whose time to save
     */
    void saveTimePlayed(UUID uuid) {
        long newlyPlayedMinutes = popMinutesPlayed(uuid);

        getPlugin().getSql().safelyExecuteUpdate("INSERT INTO " + WebsiteModule.PLAYTIME_TABLE_NAME +
                        " SET uuid=?,minutes=? ON DUPLICATE KEY UPDATE minutes=minutes+?",
                uuid.toString(), newlyPlayedMinutes, newlyPlayedMinutes);
    }

    void setPlayerOnline(Player plr, boolean online) {
        if (online && plr.isOnline()) {
            getPlugin().getSql().safelyExecuteUpdate("INSERT INTO " + WebsiteModule.ONLINE_TABLE_NAME +
                            " SET uuid=?, name=? ON DUPLICATE KEY UPDATE name=?", //Under certain conditions, an entry might still be there - consider
                    plr.getUniqueId().toString(), plr.getName(),                  //the player switching servers using BungeeCord and the other
                    plr.getName());                                               //server not having executed the SQL yet or a crash.
        } else {
            getPlugin().getSql().safelyExecuteUpdate("DELETE FROM " + WebsiteModule.ONLINE_TABLE_NAME +
                    " WHERE uuid=?", plr.getUniqueId().toString());
        }
    }
}

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

package li.l1t.mtc.module.pvpstats.model;

import li.l1t.common.shared.uuid.UUIDRepository;
import li.l1t.common.sql.SpigotSql;
import org.bukkit.OfflinePlayer;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * A repository connecting the PvP Stats application model with a database containing stat data.
 * Note that implementations may choose to set in place caching and queueing systems to reduce
 * database load. Such mechanisms are documented in class JavaDocs. If such system is in place,
 * applications must call {@link #cleanup()} to write changes back to database before shutdown.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 2016-01-03
 */
public interface PlayerStatsRepository {
    /**
     * Finds the corresponding stats data to the player represented by a unique id. If no data has
     * yet been associated with that player, a new data set is returned. The new data set is not
     * saved to the underlying database. Note that this method queries the database synchronously
     * and should therefore not be called from the Server Thread.
     *
     * @param uuid the unique id representing the player
     * @return an object representation of the player's stats data
     * @throws IllegalStateException if an error occurs while accessing the database
     */
    PlayerStats findByUniqueId(UUID uuid) throws IllegalStateException;

    /**
     * Finds the corresponding stats data to the player. If no data has yet been associated with
     * that player, a new data set is returned. The new data set is not saved to the underlying
     * database. Note that this method queries the database synchronously and should therefore not
     * be called from the Server Thread.
     *
     * @param plr the player
     * @return an object representation of the player's stats data
     * @throws IllegalStateException if an error occurs while accessing the database
     */
    PlayerStats find(OfflinePlayer plr) throws IllegalStateException;

    /**
     * Finds the corresponding stats data to the player represented by a name. If no data has yet
     * been associated with that player, a new data set is returned. The new data set is not saved
     * to the underlying database. Note that this method queries the database synchronously and
     * should therefore not be called from the Server Thread.
     *
     * @param name the current name used by the player to retrieve
     * @return an object representation of the player's stats data
     * @throws IllegalStateException if an error occurs while accessing the database
     */
    PlayerStats findByName(String name) throws IllegalStateException, UUIDRepository.UnknownKeyException;

    /**
     * Finds the corresponding stats data to the player represented by a unique id. If no data has
     * yet been associated with that player, a new data set is returned. The new data set is not
     * saved to the underlying database. Note that this method queries the database synchronously
     * and should therefore not be called from the Server Thread.
     *
     * @param uuid    the unique id representing the player
     * @param plrName the players current name, in correct casing, or null to retrieve from xLogin
     * @return an object representation of the player's stats data
     * @throws IllegalStateException if an error occurs while accwessing the database
     */
    PlayerStats findByUniqueId(UUID uuid, @Nullable String plrName) throws IllegalStateException;

    /**
     * Finds the {@code limit} players with the most kills directly from the database. Note that
     * implementations may set a cap for limits to protect the database. In such cases, only as much
     * players as allowed are returned. Note that the future may be completed exceptionally. Further
     * note that this method queries the database directly, bypassing any cache that implementations
     * may have put in place.
     *
     * @param limit how many players to return
     * @return a future for the list of players
     */
    CompletableFuture<List<PlayerStats>> findTopKillers(int limit);

    /**
     * Finds the {@code limit} players with the most deaths directly from the database. Note that
     * implementations may set a cap for limits to protect the database. In such cases, only as much
     * players as allowed are returned. Note that the future may be completed exceptionally. Further
     * note that this method queries the database directly, bypassing any cache that implementations
     * may have put in place.
     *
     * @param limit how many players to return
     * @return a future for the list of players
     */ //This name sounds awful, but it doesn't look like there's a better name
    CompletableFuture<List<PlayerStats>> findWhoDiedMost(int limit);

    /**
     * Gets the rank in the kills leaderboard for given player, that is the number of people that
     * have more kills than that player plus one.
     *
     * @param playerStats the stats info for the player to calculate the rank for
     * @return the rank in the kills leaderboard
     */
    int getKillsRank(PlayerStats playerStats);

    /**
     * Gets the rank in the deaths leaderboard for given player, that is the number of people that
     * have more deaths than that player plus one.
     *
     * @param playerStats the stats info for the player to calculate the rank for
     * @return the rank in the deaths leaderboard
     */
    int getDeathsRank(PlayerStats playerStats);

    /**
     * Saves some data to the database.
     *
     * @param playerStats the data to save
     */
    void save(PlayerStats playerStats);

    /**
     * Sets the table backing this repository.
     *
     * @param database the MySQL database where the data is stored
     * @param table    the MySQL table where the data is stored
     */
    void setDatabaseTable(String database, String table);

    /**
     * @return a valid MySQL reference to the table used by this repository
     */
    String getDatabaseTable();

    /**
     * Cleans up any caches that this repository may have. This method must be called before
     * application shutdown for guaranteed persistence of all data.
     */
    void cleanup();

    /**
     * @return the database manager associated with this repository
     */
    SpigotSql getSql();
}

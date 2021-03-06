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
import li.l1t.mtc.module.pvpstats.PlayerStatsModule;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * A proxy for a connection between the PvP Stats application model and an underlying database that
 * queues save calls and executes them asynchronously in batches, in insertion order. All read calls
 * are forwarded to the proxied repository immediately.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 2016-01-03
 */
public class QueuedPlayerStatsRepository implements PlayerStatsRepository {
    private final PlayerStatsRepository proxied;
    private final PlayerStatsSaveQueueExecutor queueExecutor;

    public QueuedPlayerStatsRepository(PlayerStatsRepository proxied, PlayerStatsModule module) {
        this(proxied, module.getPlugin());
    }

    protected QueuedPlayerStatsRepository(PlayerStatsRepository proxied, Plugin plugin) {
        this.proxied = proxied;
        this.queueExecutor = new PlayerStatsSaveQueueExecutor(plugin, proxied);
    }

    @Override
    public void save(PlayerStats playerStats) {
        queueExecutor.queueSave(playerStats);
    }

    @Override
    public void cleanup() {
        queueExecutor.flush();
        proxied.cleanup();
    }

    @Override
    public int getKillsRank(PlayerStats playerStats) {
        return proxied.getKillsRank(playerStats);
    }

    @Override
    public int getDeathsRank(PlayerStats playerStats) {
        return proxied.getDeathsRank(playerStats);
    }

    @Override
    public PlayerStats findByUniqueId(UUID uuid) throws IllegalStateException {
        return proxied.findByUniqueId(uuid);
    }

    @Override
    public PlayerStats find(OfflinePlayer plr) throws IllegalStateException {
        return proxied.find(plr);
    }

    @Override
    public PlayerStats findByName(String name) throws IllegalStateException, UUIDRepository.UnknownKeyException {
        return proxied.findByName(name);
    }

    @Override
    public PlayerStats findByUniqueId(UUID uuid, @Nullable String plrName) throws IllegalStateException {
        return proxied.findByUniqueId(uuid, plrName);
    }

    @Override
    public CompletableFuture<List<PlayerStats>> findTopKillers(int limit) {
        return proxied.findTopKillers(limit);
    }

    @Override
    public CompletableFuture<List<PlayerStats>> findWhoDiedMost(int limit) {
        return proxied.findWhoDiedMost(limit);
    }

    @Override
    public void setDatabaseTable(String database, String table) {
        proxied.setDatabaseTable(database, table);
    }

    @Override
    public SpigotSql getSql() {
        return proxied.getSql();
    }

    @Override
    public String getDatabaseTable() {
        return proxied.getDatabaseTable();
    }
}

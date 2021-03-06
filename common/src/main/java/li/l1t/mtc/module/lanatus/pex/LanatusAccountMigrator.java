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

package li.l1t.mtc.module.lanatus.pex;

import li.l1t.lanatus.api.LanatusClient;
import li.l1t.lanatus.api.account.AccountSnapshot;
import li.l1t.lanatus.api.account.LanatusAccount;
import li.l1t.lanatus.api.account.MutableAccount;
import li.l1t.lanatus.api.exception.AccountConflictException;
import li.l1t.mtc.logging.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Material;
import ru.tehkode.permissions.PermissionGroup;
import ru.tehkode.permissions.PermissionUser;

import java.util.Optional;
import java.util.UUID;

/**
 * Actually migrates compatible PEx primary groups to Lanatus ranks.
 *
 * @author <a href="https://l1t.li/">Literallie</a>
 * @since 2016-11-07
 */
public class LanatusAccountMigrator {
    private static final UUID AUTOCONVERT_RANK_PRODUCT_ID = UUID.fromString("b0cf9038-e97b-4890-9c2b-3f3c011b45a0");
    private static final Logger LOGGER = LogManager.getLogger(LanatusAccountMigrator.class);
    private final LanatusClient lanatus;

    public LanatusAccountMigrator(LanatusClient lanatus) {
        this.lanatus = lanatus;
    }

    public void registerMigrationProduct() {
        lanatus.products().registration(AUTOCONVERT_RANK_PRODUCT_ID)
                .inModule("mtc-lanatus-pex")
                .withDisplayName("Rangmigration")
                .withDescription("Automatisch migrierter Rang vom alten System")
                .withIcon(Material.POWERED_RAIL.name())
                .withMelonsCost(0)
                .withPermanent(false)
                .register();
    }

    public boolean migrateIfNecessary(PermissionUser user, UUID playerId) {
        Optional<String> lanatusRank = findAutoConvertLanatusRank(user);
        lanatusRank.ifPresent(rank -> attemptMigratePlayerRankTo(rank, playerId));
        return lanatusRank.isPresent();
    }

    public Optional<String> findAutoConvertLanatusRank(PermissionUser user) {
        if (!isInExactlyOneGroup(user)) {
            return Optional.empty();
        } else {
            return findAutoConvertLanatusRankFor(onlyGroupOf(user));
        }
    }

    private boolean isInExactlyOneGroup(PermissionUser user) {
        return user.getOwnParents().size() == 1;
    }

    private PermissionGroup onlyGroupOf(PermissionUser user) {
        return user.getOwnParents().get(0);
    }

    private Optional<String> findAutoConvertLanatusRankFor(PermissionGroup group) {
        return Optional.ofNullable(
                group.getOwnOption(LanatusPexModule.AUTOCONVERT_OPTION_NAME, null, null)
        );
    }

    private void attemptMigratePlayerRankTo(String rank, UUID playerId) {
        if (stillHasDefaultRank(playerId)) {
            changeLanatusRankTo(rank, playerId);
        }
    }

    private boolean stillHasDefaultRank(UUID playerId) {
        Optional<AccountSnapshot> snapshot = lanatus.accounts().find(playerId);
        return !snapshot.isPresent() || hasDefaultRank(snapshot.get());
    }

    private boolean hasDefaultRank(LanatusAccount account) {
        return LanatusAccount.DEFAULT_RANK.equals(account.getLastRank());
    }

    private void changeLanatusRankTo(String groupName, UUID playerId) {
        LOGGER.info("New Lanatus group for {}: {}",
                playerId, groupName);
        MutableAccount account = lanatus.accounts().findMutable(playerId);
        account.setLastRank(groupName);
        trySave(account);
        logPurchase(groupName, playerId);
    }

    private void logPurchase(String groupName, UUID playerId) {
        lanatus.startPurchase(playerId)
                .withProductId(AUTOCONVERT_RANK_PRODUCT_ID)
                .withComment("Lanatus PEx conversion from ")
                .withData(groupName)
                .withMelonsCost(0)
                .build();
    }

    private void trySave(MutableAccount account) {
        try {
            lanatus.accounts().save(account);
        } catch (AccountConflictException e) {
            throw new IllegalStateException(e);
        }
    }
}

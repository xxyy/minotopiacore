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

package li.l1t.mtc.module.blocklock.service;

import com.google.common.base.Preconditions;
import li.l1t.common.chat.ComponentSender;
import li.l1t.common.exception.UserException;
import li.l1t.common.util.CommandHelper;
import li.l1t.common.util.LocationHelper;
import li.l1t.mtc.api.chat.ChatConstants;
import li.l1t.mtc.api.chat.MessageType;
import li.l1t.mtc.api.module.inject.InjectMe;
import li.l1t.mtc.hook.XLoginHook;
import li.l1t.mtc.logging.LogManager;
import li.l1t.mtc.module.blocklock.BlockLockConfig;
import li.l1t.mtc.module.blocklock.BlockLockModule;
import li.l1t.mtc.module.blocklock.NotLockedException;
import li.l1t.mtc.module.blocklock.api.BlockLock;
import li.l1t.mtc.module.blocklock.api.BlockLockRepository;
import li.l1t.mtc.module.blocklock.sql.SqlBlockLockRepository;
import net.md_5.bungee.api.ChatColor;
import org.apache.logging.log4j.Logger;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

/**
 * Handles block lock access.
 *
 * @author <a href="https://l1t.li/">Literallie</a>
 * @since 2017-01-31
 */
public class BlockLockService {
    private static final Logger LOGGER = LogManager.getLogger(BlockLockService.class);
    private final BlockLockRepository locks;
    private final XLoginHook xLogin;
    private final BlockLockConfig config;

    @InjectMe
    public BlockLockService(SqlBlockLockRepository locks, XLoginHook xLogin, BlockLockConfig config) {
        this.locks = locks;
        this.xLogin = xLogin;
        this.config = config;
    }

    public Optional<BlockLock> findLock(Block block) {
        Preconditions.checkNotNull(block, "block");
        if (isLockable(block)) {
            return locks.findLockAt(block.getLocation());
        } else {
            return Optional.empty();
        }
    }

    public boolean isLockable(Block block) {
        return block != null && config.isTargetedMaterial(block.getType());
    }

    public void addLockTo(Block block, Player player) {
        Preconditions.checkNotNull(block, "block");
        Preconditions.checkNotNull(player, "player");
        findLock(block).ifPresent(existingLock -> {
            LOGGER.info("Replacing lock: {}", existingLock);
            locks.deleteLock(existingLock);
        });
        locks.lockBlock(block, player.getUniqueId());
    }

    public void destroyLockAndRefund(Block block, Player player) {
        Preconditions.checkNotNull(block, "block");
        Preconditions.checkNotNull(player, "player");
        Optional<BlockLock> lock = findLock(block);
        if (!lock.isPresent()) {
            throw new NotLockedException(block);
        } else {
            destroyLockAndRefund(block, player, lock.get());
        }
    }

    public void destroyLockAndRefund(Block block, Player player, BlockLock lock) {
        Preconditions.checkNotNull(block, "block");
        Preconditions.checkNotNull(player, "player");
        Preconditions.checkNotNull(lock, "lock");
        if (!mayRemoveLock(player, lock)) {
            throw new UserException("Diesen Block darfst du nicht zerstören.");
        } else if (lock.hasBeenRemoved()) {
            throw new UserException("Dieser Block wurde bereits zerstört.");
        } else {
            removeLockAndRefundIfAllowed(block, player, lock);
        }
    }

    private void removeLockAndRefundIfAllowed(Block block, Player player, BlockLock lock) {
        boolean refundAllowed = isRefundAllowed(block, player, lock);
        doRemoveLock(block, player);
        if (refundAllowed) {
            doRefundBlockAndNotify(block, player, lock);
        } else {
            notifyNoRefunds(player);
        }
    }

    private boolean isRefundAllowed(Block block, Player player, BlockLock lock) {
        if (lock.getType() != block.getType()) {
            MessageType.WARNING.sendTo(player, "Dieser Block wurde als %s platziert, ist jetzt aber %s. " +
                            "Daher kannst du ihn nicht zurückerhalten.",
                    lock.getType(), block.getType());
            return false;
        } else if (player.hasPermission(BlockLockModule.ADMIN_PERMISSION)) {
            MessageType.RESULT_LINE_SUCCESS.sendTo(player, "Aufgrund deiner internationalen konspirativen " +
                    "Beziehungen bekommst du diesen Block nicht erstattet. (§p%s§s)", BlockLockModule.ADMIN_PERMISSION);
            return false;
        }
        return handlersAllowRefund(lock, player);
    }

    private boolean handlersAllowRefund(BlockLock lock, Player player) {
        return config.getRemovalHandlersFor(lock.getType()).stream()
                .map(handler -> handler.onRemove(lock, player))
                .allMatch(Boolean.TRUE::equals);
    }

    private void doRemoveLock(Block block, Player player) {
        locks.unlockBlock(block, player.getUniqueId());
        block.setType(Material.AIR, false);
    }

    private void doRefundBlockAndNotify(Block block, Player player, BlockLock lock) {
        block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(lock.getType()));
        MessageType.RESULT_LINE_SUCCESS.sendTo(player,
                "Der Block wurde erfolgreich entfernt. Du hast ihn zurückerhalten.");
    }

    private void notifyNoRefunds(Player player) {
        MessageType.RESULT_LINE.sendTo(player,
                "Der Block wurde erfolgreich entfernt. Du hast ihn nicht zurückerhalten.");
    }

    public void sendLockStatusTo(Block block, CommandSender sender) {
        Preconditions.checkNotNull(block, "block");
        Preconditions.checkNotNull(sender, "sender");
        Optional<BlockLock> lockOptional = locks.findLockAt(block.getLocation());
        if (lockOptional.isPresent()) {
            BlockLock lock = lockOptional.get();
            if (maySeeInformation(sender, lock)) {
                sendDetailedLockInfoTo(sender, lock);
            } else {
                sendRestrictedLockInfoTo(sender, lock);
            }
        } else {
            MessageType.RESULT_LINE.sendTo(sender, "Dieser Block ist nicht geschützt.");
        }
    }

    private boolean maySeeInformation(CommandSender sender, BlockLock lock) {
        return isLockOwner(sender, lock) || sender.hasPermission(BlockLockModule.INFO_PERMISSION);
    }

    private boolean isLockOwner(CommandSender sender, BlockLock lock) {
        return lock.getOwnerId().equals(CommandHelper.getSenderId(sender));
    }

    private void sendDetailedLockInfoTo(CommandSender sender, BlockLock lock) {
        MessageType.HEADER.sendTo(sender, "Blockinfo: %s", lock.getType());
        MessageType.RESULT_LINE.sendTo(sender, "Position: §s%s", LocationHelper.prettyPrint(lock.getLocation()));
        MessageType.RESULT_LINE.sendTo(sender,
                "Platziert am: §s%s §pvon §s%s",
                lock.getCreationInstant().toString(), xLogin.getDisplayString(lock.getOwnerId())
        );
        if (lock.hasBeenRemoved()) {
            MessageType.RESULT_LINE.sendTo(sender,
                    "Entfernt von §s%s §pam §s%s§p.",
                    lock.getRemoverId().map(xLogin::getDisplayString).orElse("niemandem"),
                    lock.getRemovalInstant().orElse(null));
        } else {
            if (mayRemoveLock(sender, lock)) {
                ComponentSender.sendTo(
                        ChatConstants.resultLineBuilder()
                                .append("Du kannst diesen Block entfernen: ", ChatColor.GOLD)
                                .append("[zerstören]", ChatColor.DARK_RED)
                                .hintedCommand("/bl destroy " + lock.getLocation().serializeToString()),
                        sender
                );
                if (!sender.hasPermission(BlockLockModule.ADMIN_PERMISSION)) {
                    config.getRemovalHandlersFor(lock.getType())
                            .forEach(handler -> handler.describeTo(sender));
                }
            }
        }
    }

    private void sendRestrictedLockInfoTo(CommandSender sender, BlockLock lock) {
        if (lock.hasBeenRemoved()) {
            MessageType.RESULT_LINE.sendTo(sender, "Was hier früher war, konntest du nicht abbauen.");
        } else {
            MessageType.RESULT_LINE.sendTo(sender, "Du kannst diesen Block nicht abbauen.");
        }
    }

    private boolean mayRemoveLock(CommandSender sender, BlockLock lock) {
        return isLockOwner(sender, lock) || sender.hasPermission(BlockLockModule.ADMIN_PERMISSION);
    }
}

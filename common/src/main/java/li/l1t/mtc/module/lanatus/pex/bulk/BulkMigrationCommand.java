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

package li.l1t.mtc.module.lanatus.pex.bulk;

import li.l1t.common.command.BukkitExecution;
import li.l1t.common.command.UserPermissionException;
import li.l1t.common.exception.InternalException;
import li.l1t.common.exception.UserException;
import li.l1t.common.util.task.ImprovedBukkitRunnable;
import li.l1t.lanatus.api.LanatusClient;
import li.l1t.mtc.api.chat.MessageType;
import li.l1t.mtc.command.MTCExecutionExecutor;
import li.l1t.mtc.logging.LogManager;
import li.l1t.mtc.module.lanatus.base.MTCLanatusClient;
import li.l1t.mtc.module.lanatus.pex.LanatusAccountMigrator;
import li.l1t.mtc.module.lanatus.pex.LanatusPexModule;
import org.apache.logging.log4j.Logger;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.scheduler.BukkitTask;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.PermissionUser;

import java.time.LocalDate;
import java.util.Collection;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Executes the command for invoking manual conversion of existing PEx data.
 *
 * @author <a href="https://l1t.li/">Literallie</a>
 * @since 2016-11-04
 */
public class BulkMigrationCommand extends MTCExecutionExecutor {
    private final Logger LOGGER = LogManager.getLogger(BulkMigrationCommand.class);
    private final AtomicBoolean migrationRunning = new AtomicBoolean(false);
    private final LanatusAccountMigrator migrator;
    private LanatusClient lanatus;
    private final PermissionManager pex;
    private final LanatusPexModule module;

    public BulkMigrationCommand(LanatusPexModule module, PermissionManager pex, MTCLanatusClient lanatus) {
        this.module = module;
        migrator = new LanatusAccountMigrator(lanatus);
        this.lanatus = lanatus;
        this.pex = pex;
    }

    @Override
    public boolean execute(BukkitExecution exec) throws UserException, InternalException {
        requireSenderIsConsole(exec);
        if (exec.hasArg(0) && exec.arg(0).equalsIgnoreCase("start")) {
            requireNoMigrationRunning();
            invokeMigration(exec);
            module.disableBulkConversion();
        } else {
            respondUsage(exec);
        }
        return true;
    }

    private void invokeMigration(BukkitExecution exec) {
        exec.respond(MessageType.RESULT_LINE, "Starting migration...");
        exec.respond(MessageType.RESULT_LINE, "Consult the misc log file for detailed status information.");
        LOGGER.warn(" --- Starting bulk migration...");
        if (!migrationRunning.compareAndSet(false, true)) {
            LOGGER.warn(" --- Migration FAILED: Compare-and-set failed (false, true)");
            throw new InternalException("Es läuft bereits eine Migration!");
        }
        PexReadTask readTask = new PexReadTask(pex, this::hasImportRelevantGroup);
        readTask.getFuture()
                .thenCompose(stage(this::filterKnownUsersAsync))
                .thenCompose(stage(this::migrateUsersWithUniqueIdAsync))
                .thenCompose(stage(this::migrateNonUniqueIdUsersAsync))
                .thenAccept((leftovers) -> logLeftoverUsers(leftovers, exec));
        readTask.runTask(module.getPlugin());
    }

    private <I, R> Function<I, CompletionStage<R>> stage(Function<I, CompletableTask<R>> taskSupplier) {
        return input -> taskSupplier.andThen(CompletableTask::getFuture).apply(input);
    }

    private FilterLanatusKnownUsersTask filterKnownUsersAsync(Collection<PexImportUser> input) {
        LOGGER.info("Found {} users in auto-migrate groups", input.size());
        FilterLanatusKnownUsersTask task = new FilterLanatusKnownUsersTask(input, 5, lanatus);
        startTaskSync(task);
        return task;
    }

    private KnownIdUserMigrationTask migrateUsersWithUniqueIdAsync(Collection<PexImportUser> input) {
        LOGGER.info("There are {} users left with no account in Lanatus", input.size());
        KnownIdUserMigrationTask task = new KnownIdUserMigrationTask(input, 5, lanatus, new XLoginProfileImporter(module.getPlugin().getXLoginHook(), module.getPlugin().sql()));
        startTaskSync(task);
        return task;
    }

    private UsernameOnlyMigrationTask migrateNonUniqueIdUsersAsync(Collection<PexImportUser> input) {
        LOGGER.info("There are {} users left without a unique id", input.size());
        LOGGER.info("Names: {}", input.stream().map(PexImportUser::getUserName).collect(Collectors.toList()));
        UsernameOnlyMigrationTask task = new UsernameOnlyMigrationTask(
                input, 5, LocalDate.of(2014, 10, 31),
                module.getPlugin().getXLoginHook(), lanatus, module.getPlugin().sql()
        );
        startTaskSync(task);
        return task;
    }

    private BukkitTask startTaskSync(ImprovedBukkitRunnable task) {
        return task.runTaskTimer(module.getPlugin(), 2L);
    }

    private void logLeftoverUsers(Collection<PexImportUser> leftovers, BukkitExecution exec) {
        LOGGER.info("Could not convert these users: {}", leftovers);
        if (leftovers.isEmpty()) {
            exec.respond(MessageType.RESULT_LINE_SUCCESS, "Converted all users with relevant groups.");
        } else {
            exec.respond(MessageType.LIST_HEADER, "Unable to convert these users:");
            leftovers.forEach(user -> exec.respond(MessageType.LIST_ITEM, user.toString()));
        }
        migrationRunning.set(false);
    }

    private boolean hasImportRelevantGroup(PermissionUser user) {
        return migrator.findAutoConvertLanatusRank(user).isPresent();
    }

    private void requireSenderIsConsole(BukkitExecution exec) {
        if (!(exec.sender() instanceof ConsoleCommandSender)) {
            throw new UserPermissionException("You are not permitted to do this.");
        }
    }

    private void requireNoMigrationRunning() {
        if (migrationRunning.get()) {
            throw new UserException("A migration is already running.");
        }
    }

    private void respondUsage(BukkitExecution exec) {
        exec.respond(MessageType.RESULT_LINE, "Lanatus Manual PEx File Migration");
        respondMigrationStatus(exec);
        exec.respond(MessageType.WARNING, "Only proceed if you know exactly what you are doing!");
        exec.respond(MessageType.WARNING, "Do not forget to specify a target rank for each group!");
        exec.respondUsage("start", "", "Immediately starts the migration");
    }

    private void respondMigrationStatus(BukkitExecution exec) {
        exec.respond(MessageType.RESULT_LINE, "There is currently " + (migrationRunning.get() ? "a" : "no") + " migration running.");
    }
}

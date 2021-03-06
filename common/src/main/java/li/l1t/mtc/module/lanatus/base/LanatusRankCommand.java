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

package li.l1t.mtc.module.lanatus.base;

import li.l1t.common.command.BukkitExecution;
import li.l1t.common.exception.InternalException;
import li.l1t.common.exception.UserException;
import li.l1t.lanatus.api.LanatusClient;
import li.l1t.lanatus.api.account.AccountSnapshot;
import li.l1t.lanatus.api.account.MutableAccount;
import li.l1t.lanatus.api.exception.AccountConflictException;
import li.l1t.mtc.api.chat.MessageType;
import li.l1t.mtc.command.MTCExecutionExecutor;
import li.l1t.mtc.hook.XLoginHook;

/**
 * Executes the /larank command, which forcefully sets the Lanatus rank of a player.
 *
 * @author <a href="https://l1t.li/">Literallie</a>
 * @since 2016-10-28
 */
class LanatusRankCommand extends MTCExecutionExecutor {
    private final LanatusClient client;
    private final XLoginHook xLogin;

    public LanatusRankCommand(LanatusClient client, XLoginHook xLogin) {
        this.client = client;
        this.xLogin = xLogin;
    }

    @Override
    public boolean execute(BukkitExecution exec) throws UserException, InternalException {
        if (exec.hasArg(1)) {
            XLoginHook.Profile profile = argumentProfile(exec.arg(0), exec);
            String newRank = exec.arg(1);
            handleSetRank(exec, profile, newRank);
        } else {
            showUsage(exec);
        }
        return true;
    }

    private XLoginHook.Profile argumentProfile(String input, BukkitExecution exec) {
        return xLogin.findSingleMatchingProfileOrFail(
                input, exec.sender(), profile -> String.format(
                        "/larank %s %s", profile.getUniqueId(), exec.findArg(1).orElse("")
                )
        );
    }

    private void handleSetRank(BukkitExecution exec, XLoginHook.Profile profile, String newRank) {
        respondOperationStart(exec, profile, newRank);
        attemptUpdateLastRank(profile, newRank);
        respondSuccess(exec);
    }

    private void respondOperationStart(BukkitExecution exec, XLoginHook.Profile profile, String newRank) {
        exec.respond(MessageType.RESULT_LINE, "Versuche, den Rang von %s auf '§s%s§p' zu setzen...",
                profile.getName(), newRank);
    }

    private void attemptUpdateLastRank(XLoginHook.Profile profile, String newRank) {
        MutableAccount account = client.accounts().findMutable(profile.getUniqueId());
        account.setLastRank(newRank);
        attemptSave(account);
    }

    private void respondSuccess(BukkitExecution exec) {
        exec.respond(MessageType.RESULT_LINE_SUCCESS, "Rang geändert.");
    }

    private void attemptSave(MutableAccount account) {
        try {
            client.accounts().save(account);
        } catch (AccountConflictException e) {
            handleConflict(account);
        }
    }

    private void handleConflict(MutableAccount account) {
        AccountSnapshot newState = client.accounts().refresh(account.getInitialState());
        throw new InternalException(String.format(
                "Der Rang wurde anderswo auf '%s' geändert. Bitte versuche es erneut.",
                newState.getLastRank()
        ));
    }

    private void showUsage(BukkitExecution exec) {
        exec.respondUsage("", "<Spieler|UUID> <Neuer Rang>", "Überschreibt den Rang eines Spielers.");
        exec.respond(MessageType.WARNING, "Es kann bis zu fünf Minuten dauern, bis der neue Rang " +
                "auf allen Servern verfügbar ist. Danach muss sich die betroffene Person " +
                "möglicherweise neu einloggen.");
    }
}

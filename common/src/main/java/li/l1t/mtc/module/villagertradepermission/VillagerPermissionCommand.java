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

package li.l1t.mtc.module.villagertradepermission;

import li.l1t.common.util.CommandHelper;
import li.l1t.mtc.misc.cmd.MTCPlayerOnlyCommandExecutor;
import li.l1t.mtc.module.villagertradepermission.actions.*;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

/**
 * A command handler for setting permissions on villagers and to get information about current
 * permissions.
 *
 * @author <a href="https://janmm14.de">Janmm14</a>
 */
public class VillagerPermissionCommand extends MTCPlayerOnlyCommandExecutor {

    private final VillagerTradePermissionModule module;
    private final ActionManager actionManager;

    public VillagerPermissionCommand(VillagerTradePermissionModule module) {
        this.module = module;
        actionManager = module.getActionManager();
    }

    @Override
    public boolean catchCommand(Player plr, String plrName, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(plr, label);
            return true;
        }
        switch (args[0]) {
            case "setperm":
            case "setpermission": {
                if (args.length != 2) { //require exact argument count to make sure everyone knows that permissions are spaceless
                    return CommandHelper.msg("§cFalsche Benutzung.\n§6Syntax: §a/" + label + ' ' + args[0].toLowerCase() + " <permission>", plr);
                }
                schedule(plr, new SetPermissionAction(module, args[1]));
                break;
            }
            case "i":
            case "info": {
                schedule(plr, new PermissionInfoAction(module));
                break;
            }
            case "removeperm":
            case "removepermission": {
                schedule(plr, new RemovePermissionAction(module));
                break;
            }
            case "ai":
            case "actioninfo": {
                Action action = actionManager.getScheduledAction(plr);
                if (action == null) {
                    plr.sendMessage("§aDu hast keine Aktion ausgewählt.");
                } else {
                    action.sendActionInfo(plr);
                }
                break;
            }
            case "abort": {
                Action action = actionManager.removeScheduledAction(plr);
                if (action == null) {
                    plr.sendMessage("§aDu hattest keine Aktion ausgewählt.");
                } else {
                    plr.sendMessage("§aAktion §6" + action.getShortDescription() + "§a abgebrochen.");
                }
                break;
            }
            default: {
                plr.sendMessage("§cUnbekannte Aktion §a" + args[0] + "§a. Hilfe mit §a/" + label + " help§a.");
                break;
            }
            case "help": {
                sendHelp(plr, label);
            }
        }
        return true;
    }

    /**
     * Schedules the given action for the given player. <br><br> This also informes the player if it
     * overrides their previous action and invokes {@link Action#sendActionInfo(Player)}
     *
     * @param plr    the player to schedule the action for
     * @param action the action to schedule
     */
    private void schedule(Player plr, Action action) {
        if (actionManager.hasAction(plr)) {
            plr.sendMessage("§cDeine zuvor gewählte Aktion wurde überschrieben.");
        }
        actionManager.scheduleAction(plr, action)
                .sendActionInfo(plr);
    }

    private void sendHelp(Player plr, String label) {
        plr.sendMessage("§6MTC VillagerTradePermissionModule");
        plr.sendMessage("§aDie Befehle in§c rot§a arbeiten mit dem anschließend angeklickten Villager.");
        plr.sendMessage("§c/" + label + " info|i §7-§a Informationen über die benötigte Permission zum Handeln");
        plr.sendMessage("§6/" + label + " actioninfo|ai §7-§a Informationen über die aktuell gewählte Villager-Aktion");
        plr.sendMessage("§c/" + label + " setperm[ission] <permission> §7-§a Setzt die benötigte Permisison zum Handeln");
        plr.sendMessage("§c/" + label + " removeperm[ission] §7-§a Macht einen Villager für jeden zugänglich");
        plr.sendMessage("§6/" + label + " abort §7-§a Bricht die aktuelle Aktion ab");
    }
}

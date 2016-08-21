/*
 * Copyright (c) 2013-2016.
 * This work is protected by international copyright laws and licensed
 * under the license terms which can be found at src/main/resources/LICENSE.txt
 * or alternatively obtained by sending an email to xxyy98+mtclicense@gmail.com.
 */

package li.l1t.mtc.misc.cmd;

import li.l1t.common.util.CommandHelper;
import li.l1t.mtc.api.MTCPlugin;
import li.l1t.mtc.helper.MTCHelper;
import org.apache.commons.lang.math.RandomUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;

public final class CommandRandom extends MTCCommandExecutor {

    private final MTCPlugin plugin;

    public CommandRandom(MTCPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean catchCommand(CommandSender sender, String senderName, Command cmd, String label, String[] args) {
        if (!CommandHelper.checkPermAndMsg(sender, "mtc.random", label)) {
            return true;
        }

        Collection<? extends Player> plrs = new ArrayList<>(Bukkit.getOnlinePlayers());

        if (args.length > 0) {
            switch (args[0].toLowerCase()) {
                case "-p":
                    if (args.length < 2) {
                        return MTCHelper.sendLoc("XU-rdmhelp", sender, false);
                    }
                    Bukkit.getScheduler().runTaskLater(plugin, new RunnableAnnounceChoice(args[1]),
                            plugin.getConfig().getLong("random.tickdelay", 100));
                    return true;
                case "-m":
                    if (args.length < 2) {
                        return MTCHelper.sendLoc("XU-rdmhelp", sender, false);
                    }
                    plrs.removeIf(target -> !target.hasPermission(args[1]));
                    break;
                default:
                    return MTCHelper.sendLoc("XU-rdmhelp", sender, false);
            }
        }

        if (plrs.size() == 0) {
            return MTCHelper.sendLoc("XU-nordmplrs", sender, true);
        }
        Player chosenOne = plrs.stream()
                .skip(RandomUtils.nextInt(plrs.size()))
                .findFirst().get();

        Bukkit.getScheduler().runTaskLater(plugin, new RunnableAnnounceChoice(chosenOne.getName()),
                plugin.getConfig().getLong("random.tickdelay", 100));

        return true;
    }

    private class RunnableAnnounceChoice implements Runnable {
        private final String plrName;

        RunnableAnnounceChoice(String plrName) {
            Bukkit.broadcastMessage(MTCHelper.locArgs("XU-randomplr", "CONSOLE", true, plrName));
            this.plrName = plrName;
        }

        @Override
        public void run() {
            Bukkit.broadcastMessage(MTCHelper.locArgs("XU-chosenone", "CONSOLE", true, this.plrName));
        }

    }

}
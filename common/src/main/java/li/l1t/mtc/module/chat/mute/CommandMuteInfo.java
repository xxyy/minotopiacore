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

package li.l1t.mtc.module.chat.mute;

import li.l1t.common.exception.UserException;
import li.l1t.common.util.CommandHelper;
import li.l1t.mtc.api.chat.MessageType;
import li.l1t.mtc.hook.XLoginHook;
import li.l1t.mtc.module.chat.mute.api.Mute;
import li.l1t.mtc.module.chat.mute.api.MuteManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

/**
 * A command to toggle mute, with an optional reason.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 2016-08-21
 */
class CommandMuteInfo implements CommandExecutor {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter
            .ofLocalizedDateTime(FormatStyle.LONG)
            .withLocale(Locale.GERMAN);
    private final XLoginHook xLoginHook;
    private final MuteManager muteManager;

    CommandMuteInfo(XLoginHook xLoginHook, MuteManager muteManager) {
        this.xLoginHook = xLoginHook;
        this.muteManager = muteManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("help")) {
            sendHelpTo(sender);
            return true;
        }
        XLoginHook.Profile profile = getTargetProfile(sender, args);
        if (muteManager.isCurrentlyMuted(profile)) {
            sendMuteInfoTo(sender, profile, muteManager.getMuteFor(profile));
        } else {
            MessageType.RESULT_LINE_SUCCESS.sendTo(sender, "%s ist momentan nicht gemuted.", profile.getName());
        }
        return true;
    }

    private void sendHelpTo(CommandSender sender) {
        sender.sendMessage("§a/muteinfo [Spieler] §6Zeigt Mutestatus eines Spielers");
    }

    private XLoginHook.Profile getTargetProfile(CommandSender sender, String[] args) {
        XLoginHook.Profile profile;
        if (args.length > 0) {
            profile = findSingleMatchingProfileOrFail(args[0], sender);
        } else {
            if (!(sender instanceof Player)) {
                throw new UserException("Nur Spieler können die eigene Muteinfo ansehen.");
            }
            profile = xLoginHook.getProfile(CommandHelper.getSenderId(sender));
        }
        return profile;
    }

    private void sendMuteInfoTo(CommandSender sender, XLoginHook.Profile profile, Mute mute) {
        MessageType.RESULT_LINE.sendTo(sender, "§s%s §pist momentan gemuted.", profile.getName());
        MessageType.RESULT_LINE.sendTo(sender, "gemuted seit: §s%s",
                formatInstant(mute.getUpdateTime())
        );
        MessageType.RESULT_LINE.sendTo(sender, "gemuted bis: §s%s",
                formatInstant(mute.getExpiryTime())
        );
        MessageType.RESULT_LINE.sendTo(sender, "gemuted von: §s%s", formatMuteSource(mute));
        MessageType.RESULT_LINE.sendTo(sender, "Mutegrund: §s%s", mute.getReason());
    }

    private String formatInstant(Instant instant) {
        return DATE_TIME_FORMATTER.format(instant.atZone(ZoneId.systemDefault()));
    }

    private String formatMuteSource(Mute mute) {
        XLoginHook.Profile profile = xLoginHook.getProfile(mute.getSourceId());
        if (profile == null) {
            return "???";
        } else {
            return profile.getName();
        }
    }

    private XLoginHook.Profile findSingleMatchingProfileOrFail(String input, CommandSender sender) {
        return xLoginHook.findSingleMatchingProfileOrFail(
                input, sender, profile -> "/muteinfo " + profile.getUniqueId()
        );
    }
}

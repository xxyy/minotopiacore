/*
 * Copyright (c) 2013-2016.
 * This work is protected by international copyright laws and licensed
 * under the license terms which can be found at src/main/resources/LICENSE.txt
 * or alternatively obtained by sending an email to xxyy98+mtclicense@gmail.com.
 */

package io.github.xxyy.mtc.cron;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import io.github.xxyy.mtc.MTC;
import io.github.xxyy.mtc.chat.MTCChatHelper;
import io.github.xxyy.mtc.chat.PrivateChat;
import io.github.xxyy.mtc.chat.cmdspy.CommandSpyFilters;
import io.github.xxyy.mtc.clan.ClanHelper;
import io.github.xxyy.mtc.logging.LogManager;
import io.github.xxyy.mtc.misc.CacheHelper;

import java.util.Map;


/**
 * A task which runs every five minutes and executes some periodic cleanup tasks for MTC, including, but not limited to,
 * cleaning and updating of caches.
 *
 * @author xxyy98
 */
public class RunnableCronjob5Minutes implements Runnable {
    private static final Logger LOGGER = LogManager.getLogger(RunnableCronjob5Minutes.class);
    private static byte cacheExCount = 0;
    private final MTC plugin;
    private boolean forced = false;

    public RunnableCronjob5Minutes(boolean forced, MTC plugin) {
        this.forced = forced;
        this.plugin = plugin;
    }

    @Override
    public void run() {
        try {
            //private chats //REFACTOR
            if (!MTCChatHelper.directChats.isEmpty()) {
                for (Map.Entry<Integer, PrivateChat> entry : MTCChatHelper.directChats.entrySet()) {
                    PrivateChat chat = entry.getValue();

                    if (chat.activeRecipients.isEmpty()) {
                        MTCChatHelper.directChats.remove(entry.getKey());
                        if (!chat.recipients.isEmpty()) {
                            chat.recipients.stream()
                                    .filter(OfflinePlayer::isOnline)
                                    .forEach(plr -> plr.sendMessage(MTC.chatPrefix + "Der Chat §a#" + chat.chatId + "§6 wurde gelöscht."));
                        }
                        continue;
                    }

                    chat.recipients.stream()
                            .filter(plr -> !plr.isOnline())
                            .forEach(chat::removeRecipient);
                }
            }

            //Remove dead CommandSpy filters
            CommandSpyFilters.removeDeadFilters();

            RunnableCronjob5Minutes.cacheExCount++;

            CacheHelper.clearCaches(forced, plugin);

            //clan caches
            if (RunnableCronjob5Minutes.cacheExCount >= 12) {//run every hour
                RunnableCronjob5Minutes.cacheExCount = 0;
                //clear clan caches
                ClanHelper.clearCache();
            }
        } catch (Exception e) {//always occurs on disable //TODO: wat
            LOGGER.catching(Level.INFO, e);
            Bukkit.getConsoleSender().sendMessage("§7[MTC]Cronjob 5M generated an exception: " + e.getClass().getName() + " (see main log)");
        }
    }
}

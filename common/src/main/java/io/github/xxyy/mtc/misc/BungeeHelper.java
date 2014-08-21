package io.github.xxyy.mtc.misc;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import io.github.xxyy.mtc.ConfigHelper;
import io.github.xxyy.mtc.MTC;
import io.github.xxyy.mtc.bans.BanInfo;
import io.github.xxyy.mtc.warns.WarnInfo;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;


public final class BungeeHelper {
    private BungeeHelper() {

    }

    public static void notifyServersWarn(WarnInfo wi) {
        if (!ConfigHelper.isEnableBungeeAPI()) {
            return;
        }

        Player dummy = Bukkit.getPlayer("");
        if (dummy == null) {
            System.out.println("[MTC]Can only notify other servers when a player is online! (Everything will be fine, there just won't be a message)");
            return;
        }
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(b);
        try {
            out.writeUTF("mtc_warn");
            out.writeInt(wi.id);
        } catch (IOException ignore) {
            //go home bukkit, you have drunk
        }
        dummy.sendPluginMessage(MTC.instance(), "mtcAPI", b.toByteArray());
    }

    public static void notifyServersBan(BanInfo bi) {
        if (!ConfigHelper.isEnableBungeeAPI()) {
            return;
        }

        Player dummy = Bukkit.getPlayer("");
        if (dummy == null) {
            System.out.println("[MTC]Can only notify other servers when a player is online! (Everything will be fine, there just won't be a message)");
            return;
        }
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(b);
        try {
            out.writeUTF("mtc_ban");
            out.writeInt(bi.id);
        } catch (IOException ignore) {
            //go home bukkit, you have drunk
        }
        dummy.sendPluginMessage(MTC.instance(), "mtcAPI", b.toByteArray());
    }
}
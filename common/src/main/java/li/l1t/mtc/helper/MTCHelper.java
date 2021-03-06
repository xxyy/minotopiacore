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

package li.l1t.mtc.helper;

import li.l1t.common.localisation.LangHelper;
import li.l1t.common.misc.HelpManager;
import li.l1t.common.sql.SafeSql;
import li.l1t.common.util.CommandHelper;
import li.l1t.mtc.MTC;
import li.l1t.mtc.api.MTCPlugin;
import li.l1t.mtc.clan.ui.ClanHelpManager;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MTCHelper { //FIXME wtf is this

    public static void addViolation(String code, String violator, String info) {
        SafeSql sql = MTC.instance().getSql();
        sql.safelyExecuteUpdate("INSERT INTO " + sql.dbName + ".mtc_violations SET code=?,violator=?,timestamp=NOW(),info=?", code, violator, info);
    }

    public static String getBarOfValues(int maxLength, List<Integer> values, int max) {
        maxLength -= 2;//[]
        int i = 0;
        String rtrn = "";
        for (int val : values) {
            float factor = (((float) val) / ((float) max));
            short linesToDraw = (short) (maxLength * factor);
            rtrn += "§" + Integer.toHexString(i) + StringUtils.rightPad("", linesToDraw, (char) ('0' + i));//there should never be more than 16 full items.
//	        System.out.println(i+"->"+rtrn+": "+val);
            i++;
        }
        return rtrn;
    }

    public static String getProgressBar(int maxLength, int value, int max) {
        double factor = (((double) value) / ((double) max));
        maxLength -= 5;//"[]xx%".lenght
        short linesToDraw = (short) (maxLength * factor);//factor can not be > 1
        return StringUtils.rightPad(StringUtils.rightPad("[", linesToDraw, '█'), maxLength, '▒') + "]" + StringUtils.leftPad(((short) (factor * 100)) + "%", 3, '0');
    }

    /**
     * initialize help system.
     */
    public static void initHelp() { //TODO this should automatically load everything from MTCCommandExecutor objects
        HelpManager.helpMans = new HashMap<>();
        //LORE
        Map<String, String> loreMap = new HashMap<>();
        loreMap.put("add [Text]", "Fügt eine neue Zeile zur Lore hinzu.");
        loreMap.put("clear", "Löscht die Lore des Items.");
        loreMap.put("remove [Zeilennummer]", "Angegebene Zeile entfernen.");
        loreMap.put("set [Zeilennummer] [Text]", "Setzt eine spzifizierte Zeile.");
        loreMap.put("list", "Listet die Lore auf.");
        HelpManager helpLore = new HelpManager("Lore-Hilfe",
                new String[]{"Schreibt weitere Zeilen ins Item-Tooltip eines gehaltenen Items (Fachwort: Lore)."},
                loreMap);
        HelpManager.helpMans.put("lore", helpLore);
        HelpManager.helpMans.put("ii", helpLore);
        //TIME
        Map<String, String> timeMap = new HashMap<>();
        timeMap.put("", "");
        HelpManager helpTime = new HelpManager("GetTime-Hilfe",
                new String[]{"Zeigt die aktuelle (RL-)Serverzeit und das heutige Datum an."},
                timeMap);
        HelpManager.helpMans.put("gtime", helpTime); //TODO get aliases automagically
        HelpManager.helpMans.put("gt", helpTime);
        HelpManager.helpMans.put("gettime", helpTime);
        HelpManager.helpMans.put("getdate", helpTime);
        HelpManager.helpMans.put("gd", helpTime);
        //MAINCMD
        Map<String, String> mainMap = new HashMap<>();
        mainMap.put("help [Kommando] <Seite>", "Ruft die MTC-Hilfe für das gegebene Kommando ab.");
        mainMap.put("reload", "Lädt MTC neu.");
        mainMap.put("rename [Name]", "Setzt den Namen des Items in deiner Hand. mit &XFarbe!");
        mainMap.put("milk", "Entfernt alle Trankeffekte.");
        mainMap.put("sign [line] [text]", "Editiert das Schild, das du anschaust.");
        mainMap.put("dline [line]", "Editiert das Schild, das du anschaust. (Löscht Zeile)");
        mainMap.put("ci", "Leert dein Inventar (INKLUSIVE Rüstung!)");
        mainMap.put("config get [Option]", "Zeigt eine Option aus der Config an.");
        mainMap.put("config set [Option] [Wert]", "Schreibt eine Option in die Config. (Akzeptierte Typen: String, boolean)");
        mainMap.put("config reload", "Lädt die Config neu.");
        mainMap.put("rne [neuer Name]", "Setzt den Namen des Entitys in deiner Nähe.");
        mainMap.put("motd [neue lokale MotD]", "Setzt die lokale MotD. (NICHT BungeeCord!)");
        mainMap.put("spy", "Aktiviert Spy.");
        mainMap.put("clearcache", "Holt alle Daten neu aus der MySQL.");
        HelpManager helpMain = new HelpManager("/mtc Hilfe",
                new String[]{"Wichtige Kommandos für MTC."},
                mainMap);
        HelpManager.helpMans.put("xyu", helpMain);
        HelpManager.helpMans.put("mts", helpMain);
        HelpManager.helpMans.put("mtc", helpMain);
        //PLAYERHEADS
        Map<String, String> phMap = new HashMap<>();
        phMap.put("get [Spielername]", "Legt den Kopf des angegebenen in dein Inventar.");
        phMap.put("set [Spielername]", "Ändert den Kopf in deiner Hand.");
        phMap.put("getall [Spielername]", "Gibt jedem Spieler am Server den Kopf des angegebenen Spielers.");
        HelpManager helpPh = new HelpManager("/ph Hilfe",
                new String[]{"Befehl, um Köpfe von Spielern zu bekommen/ändern. Namen sind §3case-sensitive!"},
                phMap);
        HelpManager.helpMans.put("ph", helpPh);
        HelpManager.helpMans.put("playerhead", helpPh);
        Map<String, String> tbMap = new HashMap<>();
        tbMap.put("join", "Betritt das Spiel.");
        tbMap.put("leave", "Verlässt das Spiel.");
        tbMap.put("list", "Listet alle aktiven Spieler auf.");
        tbMap.put("lobby", "Betritt die Lobby.");
        tbMap.put("lobby leave", "Verlässt die Lobby.");
        HelpManager helpTb = new HelpManager("TeamBattle-Hilfe",
                new String[]{"Ein lustiges PvP-Spiel, in dem sich zwei Teams (rot und blau) bekriegen. Beide haben seperate Spawns. Das Team mit den meisten Kills gewinnt."},
                tbMap);
        HelpManager.helpMans.put("war", helpTb);
        Map<String, String> tbaMap = new HashMap<>();
        tbaMap.put("setspawn [blue|red]", "Setzt den Spawn des gegebenen Teams auf deine aktuelle Position.");
        tbaMap.put("addpoint [blue|red]", "Fügt dem Team einen Punkt hinzu.");
        tbaMap.put("forcejoin [PLAYER|all]", "Fügt PLAYER oder alle Spieler dem TeamBattle hinzu.");
        tbaMap.put("kick [PLAYER|all]", "Kickt PLAYER oder alle Spieler aus dem TeamBattle.");
        tbaMap.put("simkill [KILLER] [TARGET]", "Simuliert, dass KILLER TARGET in TeamBattle killt.");
        tbaMap.put("savecfg", "Speichert die Config.");
        tbaMap.put("rldcfg", "Reloadet die Config.");
        tbaMap.put("stopgame", "Stoppt das Spiel (ohne Gewinner)");
        tbaMap.put("setkit", "Setzt das Kit.");
        tbaMap.put("setbkit", "Setzt das bassere Kit.");
        tbaMap.put("wingame", "Setzt den Gewinner des Spiels und erzählt es allen Spielern.");
        tbaMap.put("setlobby", "Setzt den Lobbyspawn.");
        tbaMap.put("", "Zeigt diese Hilfe an.");
        HelpManager helpTbA = new HelpManager("TeamBattle-Admintools!",
                new String[]{"Diese Kommandos helfen, TeamBattle zu administrieren!"},
                tbaMap);
        HelpManager.helpMans.put("waradmin", helpTbA);
        HelpManager.helpMans.put("wara", helpTbA);
        HelpManager.helpMans.put("wa", helpTbA);
        //CLAN
        Map<String, String> clanMap = new HashMap<>();
        clanMap.put("create [Name] [Chatprefix]", "Erstellt einen neuen Clan mit einem Namen und Prefix. Das Prefix wird bei allen Mitglieder im Globalchat angezeigt.");
        clanMap.put("invite [Name]", "Lädt einen Spieler in deinen Clan ein, optional mit einer Nachricht.");
        clanMap.put("invitations", "Zeigt deine Claneinladungen an.");
        clanMap.put("kick [Name] <Nachricht>", "Kickt einen Spieler aus deinem Clan, optional mit einer Nachricht.");
        clanMap.put("leave", "Verlässt deinen aktuellen Clan.");
        clanMap.put("remove", "Löscht deinen Clan und kickt alle Mitglieder.");
        clanMap.put("chat", "Schaltet zwischen Globalchat und Clanchat um.");
        clanMap.put("chat <Nachricht>", "Sendet eine Nachricht an deinen Clan. Kürzel: Schreibe §3#§7 vor deine Nachricht.");
        clanMap.put("base", "Teleportiert dich zur Clanbase.");
        clanMap.put("setbase", "Setzt die Clanbase auf deine aktuelle Position.");
        clanMap.put("revoke [Name]", "Zieht die Enladung an <Name> zurück.");
        clanMap.put("setrank [Name] [Neuer Rang]", "Setzt einen Rang. Mehr Info: /clan setrank help");
        clanMap.put("info", "Zeigt Informationen zu deinem Clan.");
        clanMap.put("info [ID|Name|Prefix]", "Zeigt Informationen zu einem Clan, entweder nach Name oder nach Prefix.");
        clanMap.put("info player [Name]", "Zeigt Informationen zum Clan eines anderen Spielers an.");
//        clanMap.put("members", "Zeigt alle Mitglieder deines Clans an.");
//        clanMap.put("permission", "Verwaltet Clanrechte.");
//        clanMap.put("bank", "Verwaltet die Clankasse.");
//        clanMap.put("level", "Zeigt Informationen zum Clanlevel.");
//        clanMap.put("top", "Zeigt die besten Clans.");
//        clanMap.put("credits", "Zeigt Informationen zum Plugin.");//hidden :P
//        clanMap.put("tutorial", "Zeigt ein Tutorial!");#
        ClanHelpManager helpClan = new ClanHelpManager("MinoTopia Clansystem",
                new String[0],
                clanMap);
        ClanHelpManager.helpMans.put("clan", helpClan);
        ClanHelpManager.helpMans.put("xclan", helpClan);
    }

    /**
     * public static boolean isEnabled(String path) Checks if a part of MTC is enabled in config.
     * Example: isEnabled(".command.AA") checks if the command AA is enabled. Example: isEnabled("")
     * checks if MTC is enabled.
     *
     * @param path Path in config (Will be prefixed with 'enable')
     * @return boolean
     * @see #isEnabledAndMsg(String, org.bukkit.command.CommandSender)
     */
    public static boolean isEnabled(String path) {
        //System.out.println(MTCMain.instance().getConfig().getBoolean("enable"+path,true));
        String str = MTC.instance().getConfig().getString("enable" + path);
        boolean value;
        try {
            value = Boolean.parseBoolean(str);
        } catch (Exception e) {
            System.out.println("Notice: Config Value " + path + " is not a boolean! Please check this and convert it to a boolean.");
            return true;
        }
        return value;
    }

    /**
     * Checks if a part of MTC is enabled in config. Example: isEnabledAndMsg(".command.AA") checks
     * if the command AA is enabled. Example: isEnabledAndMSg("") checks if MTC is enabled.
     *
     * @param path Path in config (Will be prefixed with 'enable')
     * @return boolean
     * @see #isEnabled(String)
     */
    public static boolean isEnabledAndMsg(String path, CommandSender sender) {
        return isEnabledAndMsg(path, sender, MTC.instance());
    }

    /**
     * Checks if a part of MTC is enabled in config. Example: isEnabledAndMsg(".command.AA") checks
     * if the command AA is enabled. Example: isEnabledAndMSg("") checks if MTC is enabled.
     *
     * @param path     Path in config (Will be prefixed with 'enable')
     * @param sender   CommandSender to send message to
     * @param instance MTC plugin instance to check
     * @return boolean
     * @see #isEnabled(String)
     */
    public static boolean isEnabledAndMsg(String path, CommandSender sender, MTCPlugin instance) {
        boolean value = instance.getConfig().getBoolean("enable" + path);

        if (!value) {
            sender.sendMessage("§cDiese Funktion ist in der Config deaktiviert! (" + path + ")");
        }

        return value;
    }

    /**
     * Localize a String for default language
     *
     * @param key           localization key
     * @param sendMTCPrefix Prepends {{@link MTC#chatPrefix} to the final message.
     * @return localized String
     * @see LangHelper#localiseString(String, String, String)
     */
    public static String loc(String key, boolean sendMTCPrefix) {
        return ((sendMTCPrefix) ? MTC.chatPrefix : "") + LangHelper.localiseString(key, "CONSOLE", MTC.instance().getName());
    }

    /**
     * Localize a String for sender's language
     *
     * @param key           localization key
     * @param sender        Who is going to read the message?
     * @param sendMTCPrefix Prepends {{@link MTC#chatPrefix} to the final message.
     * @return localized String
     * @see LangHelper#localiseString(String, String, String)
     */
    public static String loc(String key, CommandSender sender, boolean sendMTCPrefix) {
        return loc(key, sender.getName(), sendMTCPrefix);
    }

    /**
     * Localize a String for sender's language
     *
     * @param key           localization key
     * @param senderName    Who is going to read the message?
     * @param sendMTCPrefix Prepends {{@link MTC#chatPrefix} to the final message.
     * @return localized String
     * @see LangHelper#localiseString(String, String, String)
     */
    public static String loc(String key, String senderName, boolean sendMTCPrefix) {
        return ((sendMTCPrefix) ? MTC.chatPrefix : "") + LangHelper.localiseString(key, senderName, MTC.instance().getName());
    }

    /**
     * Localize a String for sender's language
     *
     * @param key           localization key
     * @param senderName    Who is going to read the message?
     * @param sendMTCPrefix Prepends {{@link MTC#chatPrefix} to the final message.
     * @param args          Arguments. See: {{@link String#format(String, Object...)}
     * @return localized string
     * @see LangHelper#localiseString(String, String, String)
     */
    public static String locArgs(String key, String senderName, boolean sendMTCPrefix, Object... args) {
        return ((sendMTCPrefix) ? MTC.chatPrefix : "") + String.format(LangHelper.localiseString(key, senderName, MTC.instance().getName()), args);
    }

    public static String locToShortString(Location loc) {
        return "{Loc|wrld:" + loc.getWorld().getName() + "|x" + loc.getBlockX() + "|y" + loc.getBlockY() + "|z" + loc.getBlockZ() + "}";
    }

    /**
     * Sends a message to sender without those ugly spaces on the beginning of each line.
     *
     * @param msg    Message to be sent, preferably multi-line (use /n)
     * @param sender Receiver of the message
     * @return TRUE
     */
    @Deprecated
    public static boolean msg(String msg, CommandSender sender) {
        for (String str2 : msg.split("\n")) {
            sender.sendMessage(str2);
        }
        return true;
    }

    /**
     * Localize a String for sender's language and send it to them.
     *
     * @param key           localization key
     * @param sender        Who is going to receive the message?
     * @param sendMTCPrefix Prepends {{@link MTC#chatPrefix} to the final message.
     * @return Returns <code>true</code> for use with {@link CommandExecutor#onCommand(CommandSender,
     * Command, String, String[])}
     * @see LangHelper#localiseString(String, String, String)
     * @see CommandSender#sendMessage(String)
     */
    public static boolean sendLoc(String key, CommandSender sender, boolean sendMTCPrefix) {
        CommandHelper.msg(((sendMTCPrefix) ? MTC.chatPrefix : "") + LangHelper.localiseString(key, sender.getName(), MTC.instance().getName()), sender);
        return true;
    }

    /**
     * Localize a String for sender's language and send it to them, with arguemnts replaced.
     *
     * @param key           localization key
     * @param sender        Who is going to receive the message?
     * @param args          Arguments. See: {{@link String#format(String, Object...)}
     * @param sendMTCPrefix Prepends {{@link MTC#chatPrefix} to the final message.
     * @return Returns <code>true</code> for use with {@link CommandExecutor#onCommand(CommandSender,
     * Command, String, String[])}
     * @see LangHelper#localiseString(String, String, String)
     * @see CommandSender#sendMessage(String)
     */
    public static boolean sendLocArgs(String key, CommandSender sender, boolean sendMTCPrefix, Object... args) {
        CommandHelper.msg(((sendMTCPrefix) ? MTC.chatPrefix : "") + String.format(LangHelper.localiseString(key, sender.getName(), MTC.instance().getName()), args), sender);
        return true;
    }

    public static boolean sendLocOrSaveArgs(String key, CommandSender sender, String type, int type2, boolean sendMTCPrefix, Object... args) {
        if ((!(sender instanceof Player) || ((Player) sender).isOnline())) {
            return MTCHelper.sendLocArgs(key, sender, sendMTCPrefix, args);
        }
        String msg = MTCHelper.locArgs(key, "CONSOLE", sendMTCPrefix, args);
        LaterMessageHelper.addMessage(sender.getName(), type, type2, msg, true, sendMTCPrefix);
        return true;
    }

    public static boolean sendLocOrSaveArgs(String key, String plrName, String type, int type2, boolean sendMTCPrefix, Object... args) {
        Player plr = Bukkit.getPlayerExact(plrName);
        if (plr != null && plr.isOnline()) {
            return MTCHelper.sendLocArgs(key, plr, sendMTCPrefix, args);
        }
        String msg = MTCHelper.locArgs(key, plrName, sendMTCPrefix, args);
        LaterMessageHelper.addMessage(plrName, type, type2, msg, true, sendMTCPrefix);
        return true;
    }
}

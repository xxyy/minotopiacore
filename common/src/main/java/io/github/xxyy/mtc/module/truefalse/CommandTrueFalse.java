/*
 * Copyright (c) 2013-2015.
 * This work is protected by international copyright laws and licensed
 * under the license terms which can be found at src/main/resources/LICENSE.txt
 * or alternatively obtained by sending an email to xxyy98+mtclicense@gmail.com.
 */

package io.github.xxyy.mtc.module.truefalse;

import io.github.xxyy.common.misc.XyLocation;
import io.github.xxyy.common.util.CommandHelper;
import io.github.xxyy.common.util.StringHelper;
import io.github.xxyy.common.util.inventory.ItemStackFactory;
import io.github.xxyy.mtc.helper.MTCHelper;
import mkremins.fanciful.FancyMessage;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

/**
 * Provides a text-based front-end for true/false, with admin and player features.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 4.9.14
 */
public class CommandTrueFalse implements CommandExecutor {
    private final TrueFalseModule module;

    public CommandTrueFalse(TrueFalseModule module) {
        this.module = module;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0) {
            if (sender instanceof Player) {
                switch (args[0].toLowerCase()) {
                    case "join":
                        if (!CommandHelper.checkPermAndMsg(sender, "mtc.truefalse.join", label)) {
                            return true;
                        }

                        if (!module.isGameOpen()) {
                            MTCHelper.sendLoc("XU-tfastart", sender, false);
                            return true;
                        }

                        module.getGame().addParticipant((Player) sender);
                        return true;
                    case "leave":
                        sender.sendMessage("§6/spawn");
                        return true;
                    case "wand":
                        if (!CommandHelper.checkPermAndMsg(sender, TrueFalseModule.ADMIN_PERMISSION, label)) {
                            return true;
                        }

                        ((Player) sender).getInventory().addItem(
                                new ItemStackFactory(TrueFalseModule.MAGIC_WAND_MATERIAL)
                                        .displayName(TrueFalseModule.MAGIC_WAND_NAME)
                                        .lore(Arrays.asList("§7Right-click a block to set the 1.boundary.", "§7Left-click a block to set the 2.boundary."))
                                        .produce()
                        );
                        module.startBoundarySession(((Player) sender).getUniqueId());
                        sender.sendMessage("§eHier ist deine Zaubermelone!");
                        return true;
                }
            }
            if (sender.hasPermission(TrueFalseModule.ADMIN_PERMISSION)) {
                switch (args[0].toLowerCase()) {
                    case "new":
                        if (module.getGame() != null) {
                            sender.sendMessage("§cEs läuft noch ein Spiel! Brich dieses mit §4/wf abort §cab.");
                            return true;
                        }

                        module.setGame(new TrueFalseGame(module));
                        return true;
                    case "start":
                        if (module.getGame() == null) {
                            sender.sendMessage("§cEs läuft kein Spiel! Verwende §4/wf new§c!");
                            return true;
                        } else if (!module.getGame().getState().equals(TrueFalseGame.State.TELEPORT)) {
                            sender.sendMessage("§cDas Spiel wurde bereits gestartet!");
                            return true;
                        }

                        module.startGame();
                        return true;
                    case "abort":
                        if (module.getGame() == null) {
                            sender.sendMessage("§cEs läuft kein Spiel! Verwende §4/wf new§c!");
                            return true;
                        }

                        if (module.abortGame()) {
                            sender.sendMessage("§aDas Spiel wurde abgebrochen.");
                        } else {
                            sender.sendMessage("§aDas Spiel wird nach dieser Frage beendet!");
                        }
                        sender.sendMessage("§eBeachte, dass Spieler nicht automatisch zurückteleportiert werden!");
                        return true;
                    case "addq":
                        if (args.length < 3) {
                            sender.sendMessage("§cZu wenige Argumente!");
                            break;
                        }

                        boolean answer;
                        switch (args[1]) {
                            case "true":
                            case "wahr":
                                answer = true;
                                break;
                            case "false":
                            case "falsch":
                                answer = false;
                                break;
                            default:
                                sender.sendMessage("§cDie Antwort kann nur 'wahr' oder 'falsch' sein!");
                                return true;
                        }

                        String text = StringHelper.varArgsString(args, 2, true);
                        module.getQuestions().add(new TrueFalseQuestion(text, answer));
                        module.save();
                        sender.sendMessage("§aFrage hinzugefügt: §e" + text);
                        return true;
                    case "remq":
                        if (args.length < 2) {
                            sender.sendMessage("§cZu wenige Parameter!");
                            return true;
                        }

                        int index;
                        if (!StringUtils.isNumeric(args[1])) {
                            sender.sendMessage("§cDas zweite Argument muss eine Zahl sein!");
                            return true;
                        }
                        index = Integer.parseInt(args[1]);

                        if (module.getQuestions().size() <= index) {
                            sender.sendMessage("§eSo viele Antworten haben wir nicht! (" + module.getQuestions() + " vorhanden)");
                            return true;
                        }

                        module.getQuestions().remove(index);
                        module.save();
                        sender.sendMessage("§aAntwort gelöscht.");
                        return true;
                    case "listq":
                        if (module.getQuestions().isEmpty()) {
                            sender.sendMessage("§eEs sind keine Fragen mehr gespeichert!");
                            return true;
                        }

                        int i = 0;
                        for (TrueFalseQuestion question : module.getQuestions()) {
                            new FancyMessage("#" + i + " ").color(ChatColor.GOLD)
                                    .then(question.getText() + " ").color(question.getAnswer() ? ChatColor.GREEN : ChatColor.RED)
                                    .then("[-]").style(ChatColor.UNDERLINE).color(ChatColor.DARK_RED)
                                    .tooltip("/wf remq " + i).suggest("/wf remq " + i++).send(sender);
                        }
                        return true;
                    case "next":
                        if (module.getGame() == null) {
                            sender.sendMessage("§cEs läuft kein Spiel! Verwende §4/wf new§c!");
                            return true;
                        }
                        module.getGame().nextQuestion();
                        return true;
                    case "setspawn":
                        if (CommandHelper.kickConsoleFromMethod(sender, label)) {
                            return true;
                        }
                        //noinspection ConstantConditions
                        module.setSpawn(new XyLocation(((Player) sender).getLocation()));
                        sender.sendMessage("§aDer Spawn wurde auf deine Position gesetzt!");
                        return true;
                    case "spawn":
                        if (CommandHelper.kickConsoleFromMethod(sender, label)) {
                            return true;
                        }
                        //noinspection ConstantConditions
                        ((Player) sender).teleport(module.getSpawn());
                        sender.sendMessage("§eDu wurdest zum W/F-Spawn teleportiert! §7(Setzen mit /wf setspawn)");
                        return true;
                    case "debug":
                        sender.sendMessage(module.getFirstBoundary() + ", " + module.getSecondBoundary());
                        return true;
                }
            }
        }

        sender.sendMessage("§9/wf join §2Betritt ein offenes W/F-Spiel");
        if (sender.hasPermission(TrueFalseModule.ADMIN_PERMISSION)) {
            sender.sendMessage("§9/wf wand §2Gibt dir ein Tool, mit dem du die Ränder der zu Entfernenden Fläche markieren kannst");
            sender.sendMessage("§9/wf new §2Öffnet ein neues W/F-Spiel");
            sender.sendMessage("§9/wf start §2Startet ein neues W/F-Spiel");
            sender.sendMessage("§9/wf abort §2Bricht das laufende W/F-Spiel ab");
            sender.sendMessage("§9/wf addq <ja|nein> <Frage> §2Fügt eine Frage hinzu");
            sender.sendMessage("§9/wf remq <Index> §2Fügt eine Frage hinzu");
            sender.sendMessage("§9/wf listq §2Zeigt alle Fragen im Backlog an");
            sender.sendMessage("§9/wf next §2Stellt die nächste Frage");
            sender.sendMessage("§9/wf setspawn §2Setzt den W/F-Spawn");
            sender.sendMessage("§9/wf spawn §2Teleportiert dich zum W/F-Spawn");
        }
        return true;
    }
}

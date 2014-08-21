package io.github.xxyy.mtc.games.teambattle.event;

import io.github.xxyy.mtc.MTC;
import io.github.xxyy.mtc.games.teambattle.TeamBattle;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;


public final class CmdListener implements Listener {
	@EventHandler(ignoreCancelled=true,priority=EventPriority.LOWEST)
	public void onPlayerPreCmd(PlayerCommandPreprocessEvent e){
		if(TeamBattle.instance() == null || !TeamBattle.instance().isPlayerInGame(e.getPlayer())) {
            return;
        }
		if(e.getMessage().startsWith("/login")) { afterLogin(e.getPlayer()); return;}
		if(e.getMessage().startsWith("/war ") || e.getMessage().startsWith("/wa ")){return;}
		if(checkPermAndMsg(e.getPlayer())){
			e.getPlayer().sendMessage(TeamBattle.CHAT_PREFIX +" Du kannst aufgrund deiner Rechte Befehle im TeamBattle benutzen. §8Bitte missbrauche das nicht!");
			return;
		}
		e.setCancelled(true);
		e.getPlayer().sendMessage(TeamBattle.CHAT_PREFIX +" Du darfst nur §3/war §7verwenden!");
	}
	
	public boolean checkPermAndMsg(Player plr){
        return plr.hasPermission("mtc.teambattle.admin.execute.other");
    }
	
	public void afterLogin(Player plr){
		if(TeamBattle.instance().isPlayerInGame(plr) || TeamBattle.leaveMan.hasPlayerUsedLogin(plr.getName())){
			plr.sendMessage(TeamBattle.CHAT_PREFIX +" 404 Bug nicht gefunden. Vielen Dank an alle Buguser, ihr seid einfach super..");
			return;
		}
		if(TeamBattle.leaveMan.doesLocExist(plr.getName())){
			plr.teleport(Bukkit.getWorld(TeamBattle.leaveMan.getWorldName(plr.getName())).getSpawnLocation());
			plr.sendMessage(TeamBattle.CHAT_PREFIX +" Du wurdest zum Spawn teleportiert, da du das Spiel");
			plr.sendMessage(TeamBattle.CHAT_PREFIX +" in der Arena verlassen hast.");
			plr.sendMessage(TeamBattle.CHAT_PREFIX +" Deine vorherige Position wurde gespeichert.");
			plr.sendMessage(TeamBattle.CHAT_PREFIX +" Zur§ck mit §3/war prev§7, l§schen mit §3/war prev clear");
			TeamBattle.leaveMan.addPlayerToLoginUsed(plr.getName());
		}
		Bukkit.getScheduler().runTaskLater(MTC.instance(), new RunnableTpSpawn(plr),10);
		
	}
}
package io.github.xxyy.mtc.listener;

import io.github.xxyy.mtc.helper.MTCHelper;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.List;


public final class PlayerHideInteractListener implements Listener {
    protected static List<String> affectedPlayerNames = new ArrayList<>();

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInteract(PlayerInteractEvent e) {
        Player plr = e.getPlayer();
        if (!plr.hasPermission("mtc.hideplayers") ||
                (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) ||
                plr.getItemInHand().getType() != Material.BLAZE_ROD) {
            return;
        }
        e.setCancelled(true);
        if (PlayerHideInteractListener.affectedPlayerNames.contains(plr.getName())) {
            for (Player target : Bukkit.getOnlinePlayers()) {
                plr.hidePlayer(target);
            }
            PlayerHideInteractListener.affectedPlayerNames.remove(plr.getName());
            MTCHelper.sendLoc("XU-playershidden", plr, true);
        } else {
            for (Player target : Bukkit.getOnlinePlayers()) {
                plr.showPlayer(target);
            }
            PlayerHideInteractListener.affectedPlayerNames.add(plr.getName());
            MTCHelper.sendLoc("XU-playersshown", plr, true);
        }
    }
}
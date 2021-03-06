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

package li.l1t.mtc.module.shop.ui.text;

import com.google.common.collect.ImmutableMap;
import li.l1t.common.chat.ComponentSender;
import li.l1t.common.chat.XyComponentBuilder;
import li.l1t.mtc.module.shop.ShopModule;
import li.l1t.mtc.module.shop.api.ShopItem;
import li.l1t.mtc.module.shop.api.ShopItemManager;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.entity.Player;

import java.util.*;

class SearchShopAction extends AbstractShopAction {
    private ShopItemManager itemManager;
    private ShopTextOutput output;

    protected SearchShopAction(ShopModule module) {
        super("shop", "suchen", 1, null, "search");
        itemManager = module.getItemManager();
        output = module.getTextOutput();
    }

    @Override
    public void execute(String[] args, Player plr, String label) {
        String query = StringUtils.join(args, ' ');

        Optional<? extends ShopItem> exactMatchItem = itemManager.getItem(query);
        if (exactMatchItem.isPresent()) {
            output.sendPriceInfo(plr, exactMatchItem.get(), 1, "§e\"" + query + "\"§6");
            return;
        }

        if (query.length() <= 2) {
            plr.sendMessage("§c§lFehler: §cSuchbegriff muss mindestens zwei Buchstaben lang sein, um sinnvolle " +
                    "Ergebnisse zu bringen.");
            return;
        }

        Set<ShopItem> matchedItems = matchShopItems(query);

        if (matchedItems.isEmpty()) {
            plr.sendMessage("§eDeine Suche hat keine Ergebnisse geliefert.");
            return;
        }

        plr.sendMessage("§aSuchergebnisse für '" + query + "':");
        for (ShopItem matchedItem : matchedItems) {
            ComponentSender.sendTo(
                    new XyComponentBuilder(" -> ", ChatColor.GREEN)
                            .append(matchedItem.getDisplayName(), ChatColor.YELLOW)
                            .append(" ")
                            .append("[Preis]", ChatColor.GOLD, ChatColor.UNDERLINE)
                            .event(output.createItemHover(matchedItem))
                            .command("/shop preis " + matchedItem.getSerializationName()),
                    plr
            );
        }
    }

    private Set<ShopItem> matchShopItems(String rawQuery) {
        Set<ShopItem> matches = new LinkedHashSet<>();

        String query = rawQuery.trim().toLowerCase();
        if (query.isEmpty()) {
            return Collections.emptySet();
        }

        //TODO: ShopItemManager should handle this
        for (Map.Entry<String, ShopItem> entry : ImmutableMap.copyOf(itemManager.getItemAliases()).entrySet()) {
            String alias = entry.getKey().toLowerCase();
            ShopItem shopItem = entry.getValue();

            if (alias.contains(query)) { //TODO: Is this too broad? (alternative: startsWith)
                matches.add(shopItem);
            }
        }

        return matches;
    }

    @Override
    public void sendHelpLines(Player plr) {
        sendHelpLine(plr, "<Suchbegriff>", "Sucht nach Items mit bestimmtem Namen");
    }
}

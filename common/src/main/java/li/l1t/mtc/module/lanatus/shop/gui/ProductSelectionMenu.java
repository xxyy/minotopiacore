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

package li.l1t.mtc.module.lanatus.shop.gui;

import li.l1t.common.inventory.gui.ChildMenu;
import li.l1t.common.inventory.gui.PagingListMenu;
import li.l1t.common.inventory.gui.element.Placeholder;
import li.l1t.common.inventory.gui.element.button.BackToParentButton;
import li.l1t.lanatus.api.product.Product;
import li.l1t.lanatus.shop.api.Category;
import li.l1t.lanatus.shop.api.ItemIconService;
import li.l1t.mtc.module.lanatus.shop.LanatusShopModule;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Displays a menu for selecting a product from a category.
 *
 * @author <a href="https://l1t.li/">Literallie</a>
 * @since 2016-17-11
 */
public class ProductSelectionMenu extends PagingListMenu<Product> implements ChildMenu {
    private final CategorySelectionMenu parent;
    private final Category category;
    private final BiConsumer<Product, ProductSelectionMenu> clickHandler;
    private final ItemIconService iconService;
    private Map<Product, ItemStack> productIconMap = new HashMap<>();

    ProductSelectionMenu(CategorySelectionMenu parent, Category category, BiConsumer<Product, ProductSelectionMenu> clickHandler,
                         Plugin plugin, Player player, ItemIconService iconService) {
        super(plugin, player);
        this.parent = parent;
        this.category = category;
        this.clickHandler = clickHandler;
        this.iconService = iconService;
        initTopRow();
    }

    /**
     * Sets the items of this menu with their icons.
     *
     * @param productIconMap the mapping of products to their corresponding icons
     */
    public void setItems(Map<Product, ItemStack> productIconMap) {
        this.productIconMap = productIconMap;
        super.addItems(productIconMap.keySet());
    }

    public static ProductSelectionMenu withParent(CategorySelectionMenu parent, Category category,
                                                  BiConsumer<Product, ProductSelectionMenu> clickHandler, LanatusShopModule module) {
        return new ProductSelectionMenu(
                parent, category, clickHandler, module.getPlugin(), parent.getPlayer(),
                module.iconService()
        );
    }

    public static ProductSelectionMenu withoutParent(Category category, BiConsumer<Product, ProductSelectionMenu> clickHandler,
                                                     Player player, LanatusShopModule module) {
        return new ProductSelectionMenu(
                null, category, clickHandler, module.getPlugin(), player, module.iconService()
        );
    }

    @Override
    protected void initTopRow() {
        if (parent != null) {
            addToTopRow(0, BackToParentButton.INSTANCE);
            addToTopRow(8, BackToParentButton.INSTANCE);
        }
        addToTopRow(4, new Placeholder(iconService.createIconStack(category)));
    }

    @Override
    protected void handleValueClick(Product item, InventoryClickEvent evt) {
        clickHandler.accept(item, this);
    }

    @Override
    protected ItemStack drawItem(Product product) {
        return productIconMap.computeIfAbsent(product, prod -> iconService.createIconStack(product, getPlayer().getUniqueId()));
    }

    @Override
    protected String formatTitle(int currentPage, int pageCount) {
        return "§e§l" + category.getDisplayName();
    }

    @Override
    public CategorySelectionMenu getParent() {
        return parent;
    }

    public Category getCategory() {
        return category;
    }
}

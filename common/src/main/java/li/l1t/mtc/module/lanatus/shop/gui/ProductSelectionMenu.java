/*
 * Copyright (c) 2013-2016.
 * This work is protected by international copyright laws and licensed
 * under the license terms which can be found at src/main/resources/LICENSE.txt
 * or alternatively obtained by sending an email to xxyy98+mtclicense@gmail.com.
 */

package li.l1t.mtc.module.lanatus.shop.gui;

import li.l1t.common.inventory.gui.ChildMenu;
import li.l1t.common.inventory.gui.PagingListMenu;
import li.l1t.common.inventory.gui.element.Placeholder;
import li.l1t.common.inventory.gui.element.button.BackToParentButton;
import li.l1t.lanatus.api.position.PositionRepository;
import li.l1t.lanatus.api.product.Product;
import li.l1t.lanatus.shop.api.Category;
import li.l1t.lanatus.shop.api.ItemIconService;
import li.l1t.mtc.module.lanatus.shop.LanatusShopModule;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

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
    private final PositionRepository positionRepository;

    ProductSelectionMenu(CategorySelectionMenu parent, Category category,
                         BiConsumer<Product, ProductSelectionMenu> clickHandler, ItemIconService iconService,
                         PositionRepository positionRepository, Plugin plugin, Player player) {
        super(plugin, player);
        this.parent = parent;
        this.category = category;
        this.clickHandler = clickHandler;
        this.iconService = iconService;
        this.positionRepository = positionRepository;
        initTopRow();
    }

    public static ProductSelectionMenu withParent(CategorySelectionMenu parent, Category category,
                                                  BiConsumer<Product, ProductSelectionMenu> clickHandler, LanatusShopModule module) {
        return new ProductSelectionMenu(
                parent, category, clickHandler, module.iconService(),
                module.client().positions(), module.getPlugin(), parent.getPlayer()
        );
    }

    public static ProductSelectionMenu withoutParent(Category category, BiConsumer<Product, ProductSelectionMenu> clickHandler,
                                                     Player player, LanatusShopModule module) {
        return new ProductSelectionMenu(
                null, category, clickHandler, module.iconService(),
                module.client().positions(), module.getPlugin(), player
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
        boolean hasProduct = positionRepository.playerHasProduct(getPlayer().getUniqueId(), product.getUniqueId());
        return iconService.createIconStack(product, hasProduct);
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

/*
 * Copyright (c) 2013-2015.
 * This work is protected by international copyright laws and licensed
 * under the license terms which can be found at src/main/resources/LICENSE.txt
 * or alternatively obtained by sending an email to xxyy98+mtclicense@gmail.com.
 */

package io.github.xxyy.mtc.module.shop;

import io.github.xxyy.common.chat.XyComponentBuilder;
import io.github.xxyy.mtc.MTC;
import io.github.xxyy.mtc.api.MTCPlugin;
import io.github.xxyy.mtc.misc.ClearCacheBehaviour;
import io.github.xxyy.mtc.module.ConfigurableMTCModule;
import io.github.xxyy.mtc.module.InjectModule;
import io.github.xxyy.mtc.module.fulltag.FullTagModule;
import io.github.xxyy.mtc.module.shop.api.ShopItemManager;
import io.github.xxyy.mtc.module.shop.task.UpdateDiscountTask;
import io.github.xxyy.mtc.module.shop.transaction.ShopTransactionExecutor;
import io.github.xxyy.mtc.module.shop.ui.text.CommandShop;
import io.github.xxyy.mtc.module.shop.ui.text.ShopTextOutput;
import io.github.xxyy.mtc.module.shop.ui.text.admin.CommandShopAdmin;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;

/**
 * Manages the shop module, allowing players to buy
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 30/11/14
 */
public class ShopModule extends ConfigurableMTCModule {
    public static final String NAME = "Shop";
    private static final String DISCOUNT_UPDATE_SECONDS_PATH = "sale_change_minutes";
    private final XyComponentBuilder prefixBuilder = new XyComponentBuilder("[").color(ChatColor.AQUA)
            .append("Shop", ChatColor.GOLD).append("]", ChatColor.AQUA).append(" ", ChatColor.GOLD);
    private final String prefix = TextComponent.toLegacyText(new XyComponentBuilder(prefixBuilder).create());
    private ShopItemConfiguration itemConfig;
    private ShopTextOutput textOutput;
    private ShopTransactionExecutor transactionExecutor;
    @InjectModule
    private FullTagModule fullTagModule;
    private UpdateDiscountTask updateDiscountTask = new UpdateDiscountTask(this); //needs to be a field so that update interval can be reloaded on-the-fly

    public ShopModule() {
        super(NAME, "modules/shop/config.yml", ClearCacheBehaviour.RELOAD, false);
    }

    @Override
    public boolean canBeEnabled(MTCPlugin plugin) {
        if (!((MTC) plugin).getVaultHook().isEconomyHooked()) { //this also checks if Vault is installed at all
            plugin.getLogger().info("ShopModule requires Vault and a running economy provider, skipping.");
            return false;
        }
        return super.canBeEnabled(plugin);
    }

    @Override
    public void enable(MTCPlugin plugin) throws Exception {
        super.enable(plugin);
        itemConfig = ShopItemConfiguration.fromDataFolderPath("modules/shop/items.yml", ClearCacheBehaviour.RELOAD, this);
        textOutput = new ShopTextOutput(this);
        transactionExecutor = new ShopTransactionExecutor(this);

        registerCommand(new CommandShop(this), "shop", "xshop");
        registerCommand(new CommandShopAdmin(this), "shopadmin", "sa");
    }

    @Override
    protected void reloadImpl() {
        itemConfig.trySave();
        configuration.addDefault(DISCOUNT_UPDATE_SECONDS_PATH, 60 * 60);
        configuration.trySave();

        updateDiscountTask.tryCancel();
        updateDiscountTask.runTaskTimer(getPlugin(), configuration.getInt(DISCOUNT_UPDATE_SECONDS_PATH));
    }

    @Override
    public void disable(MTCPlugin plugin) {

    }

    @Override
    public void save() {
        itemConfig.trySave();
        super.save();
    }

    /**
     * Returns the configuration for this module. Note that this being the manager is considered an implementation
     * detail and, as such, may change without notice. Use {@link #getItemManager()} where possible.
     *
     * @return the configuration for this module
     */
    public ShopItemConfiguration getItemConfig() {
        return itemConfig;
    }

    /**
     * @return the shop item manager managing items for this module
     */
    public ShopItemManager getItemManager() {
        return getItemConfig();
    }

    /**
     * @return the module's chat prefix that should be used to indicated its messages
     */
    public String getChatPrefix() {
        return prefix;
    }

    /**
     * @return a component builder prefixed with the module's prefix to indicate where the message comes from
     */
    public XyComponentBuilder getPrefixBuilder() {
        return new XyComponentBuilder(prefixBuilder);
    }

    /**
     * @return the text output used by this module
     */
    public ShopTextOutput getTextOutput() {
        return textOutput;
    }

    /**
     * @return the transaction executor used by this module
     */
    public ShopTransactionExecutor getTransactionExecutor() {
        return transactionExecutor;
    }

    /**
     * @return the full tag module this module interfaces with, or null if none
     */
    public FullTagModule getFullTagModule() {
        return fullTagModule;
    }
}

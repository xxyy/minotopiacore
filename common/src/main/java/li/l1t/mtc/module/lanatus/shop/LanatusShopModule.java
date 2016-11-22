/*
 * Copyright (c) 2013-2016.
 * This work is protected by international copyright laws and licensed
 * under the license terms which can be found at src/main/resources/LICENSE.txt
 * or alternatively obtained by sending an email to xxyy98+mtclicense@gmail.com.
 */

package li.l1t.mtc.module.lanatus.shop;

import li.l1t.lanatus.api.LanatusClient;
import li.l1t.lanatus.api.LanatusConnected;
import li.l1t.lanatus.shop.api.ItemIconService;
import li.l1t.lanatus.shop.api.ProductBuyService;
import li.l1t.mtc.api.MTCPlugin;
import li.l1t.mtc.api.command.CommandBehaviours;
import li.l1t.mtc.api.module.inject.InjectMe;
import li.l1t.mtc.logging.LogManager;
import li.l1t.mtc.module.MTCModuleAdapter;
import li.l1t.mtc.module.lanatus.base.MTCLanatusClient;
import li.l1t.mtc.module.lanatus.shop.category.SqlCategoryRepository;
import li.l1t.mtc.module.lanatus.shop.command.LanatusCategoryCommand;
import li.l1t.mtc.module.lanatus.shop.command.LanatusShopCommand;
import li.l1t.mtc.module.lanatus.shop.metrics.StatsdPurchaseRecorder;
import li.l1t.mtc.module.lanatus.shop.service.SimpleItemIconService;
import li.l1t.mtc.module.lanatus.shop.service.SimpleProductBuyService;
import li.l1t.mtc.module.metrics.StatsdModule;
import org.apache.logging.log4j.Logger;

/**
 * Module providing a GUI shop for Lanatus.
 *
 * @author <a href="https://l1t.li/">Literallie</a>
 * @since 2016-17-11
 */
public class LanatusShopModule extends MTCModuleAdapter implements LanatusConnected {
    public static final String NAME = "LanatusShop";
    private static final Logger LOGGER = LogManager.getLogger(LanatusShopModule.class);
    private final ItemIconService iconService = new SimpleItemIconService();
    @InjectMe(failSilently = true)
    private MTCLanatusClient lanatus;
    @InjectMe(required = false)
    private StatsdModule statsdModule;
    @InjectMe
    private SqlCategoryRepository categoryRepository;
    @InjectMe
    private SimpleProductBuyService buyService;

    protected LanatusShopModule() {
        super(NAME, true);
    }

    @Override
    public void enable(MTCPlugin plugin) throws Exception {
        super.enable(plugin);
        registerCommand(new LanatusShopCommand(this), "lashop", "pshop");
        registerCommand(new LanatusCategoryCommand(this), "lacat")
                .behaviour(CommandBehaviours.permissionChecking("mtc.lanatus.admin"));
        if (statsdModule != null) {
            LOGGER.info("Using Statsd purchase recorder.");
            buyService.setPurchaseRecorder(new StatsdPurchaseRecorder(statsdModule.statsd()));
        } else {
            LOGGER.info("Using dummy purchase recorder.");
        }
    }

    public SqlCategoryRepository categories() {
        return categoryRepository;
    }

    public ItemIconService iconService() {
        return iconService;
    }

    public ProductBuyService buyService() {
        return buyService;
    }

    @Override
    public LanatusClient client() {
        return lanatus;
    }

    @Override
    public void clearCache(boolean forced, MTCPlugin plugin) {
        categoryRepository.clearCache();
    }
}
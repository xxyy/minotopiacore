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

package li.l1t.mtc.module.lanatus.pex.listener;

import li.l1t.common.exception.InternalException;
import li.l1t.lanatus.api.account.AccountRepository;
import li.l1t.lanatus.api.product.Product;
import li.l1t.lanatus.shop.api.event.PurchaseEvent;
import li.l1t.mtc.module.lanatus.pex.LanatusPexModule;
import li.l1t.mtc.module.lanatus.pex.product.PexProduct;
import li.l1t.mtc.module.lanatus.pex.product.PexProductRepository;
import org.bukkit.event.Listener;

import java.util.Optional;

/**
 * Abstract base class for Lanatus-PEx purchase listeners, providing some common functionality,
 *
 * @author <a href="https://l1t.li/">Literallie</a>
 * @since 2016-25-11
 */
class AbstractPurchaseListener implements Listener {
    protected final AccountRepository accountRepository;
    private final PexProductRepository pexProductRepository;

    AbstractPurchaseListener(PexProductRepository pexProductRepository) {
        this.pexProductRepository = pexProductRepository;
        accountRepository = pexProductRepository.client().accounts();
    }

    protected boolean isRelevantProduct(Product product) {
        return product.getModule().equals(LanatusPexModule.LANATUS_MODULE_NAME);
    }

    protected PexProduct getPexProductOrFail(PurchaseEvent evt) {
        return findPexProduct(evt)
                .orElseThrow(() -> new InternalException(String.format(
                        "Konnte deinen neuen Rang nicht anwenden, interner Konfigurationsfehler. Bitte wende dich an den " +
                                "Support und gib folgende Details an: Player={%s}, product={%s}",
                        evt.getPlayer(), evt.getProduct()
                )));
    }

    protected Optional<PexProduct> findPexProduct(PurchaseEvent evt) {
        return pexProductRepository.getByProduct(evt.getProduct());
    }

    protected PexProductRepository pexProducts() {
        return pexProductRepository;
    }
}

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

package li.l1t.mtc.module.lanatus.third.product;

import li.l1t.lanatus.api.LanatusRepository;
import li.l1t.lanatus.api.product.Product;

import java.util.Optional;

/**
 * A repository for additional metadata attached to third products.
 *
 * @author <a href="https://l1t.li/">Literallie</a>
 * @since 2016-12-05
 */
public interface ThirdProductRepository extends LanatusRepository {
    Optional<ThirdProduct> getByProduct(Product product);
}

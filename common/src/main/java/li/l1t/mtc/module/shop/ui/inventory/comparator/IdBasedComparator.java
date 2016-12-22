/*
 * Copyright (c) 2013-2016.
 * This work is protected by international copyright laws and licensed
 * under the license terms which can be found at src/main/resources/LICENSE.txt
 * or alternatively obtained by sending an email to xxyy98+mtclicense@gmail.com.
 */

package li.l1t.mtc.module.shop.ui.inventory.comparator;

import li.l1t.mtc.module.shop.api.ShopItem;

/**
 * Sorts shop items based on the underlying material id and data value.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 2016-07-10
 */
public class IdBasedComparator extends AbstractShopItemComparator {
    public static final IdBasedComparator ASCENDING = new IdBasedComparator(true);
    public static final IdBasedComparator DESCENDING = new IdBasedComparator(false);

    private IdBasedComparator(boolean ascending) {
        super(ascending);
    }

    @Override
    public int compare(ShopItem o1, ShopItem o2) {
        int result;
        result = Integer.compare(o1.getMaterial().ordinal(), o2.getMaterial().ordinal());
        if (result == 0 && o1.getDisplayName() != null) {
            result = o1.getDisplayName().compareToIgnoreCase(o2.getDisplayName());
        }
        return applyOrder(result);
    }

    @Override
    public String getDisplayName() {
        return "Nach Item-ID";
    }

    @Override
    public ShopItemComparator reversed() {
        return isAscending() ? DESCENDING : ASCENDING;
    }
}

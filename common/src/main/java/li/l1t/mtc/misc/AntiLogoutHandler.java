/*
 * Copyright (c) 2013-2016.
 * This work is protected by international copyright laws and licensed
 * under the license terms which can be found at src/main/resources/LICENSE.txt
 * or alternatively obtained by sending an email to xxyy98+mtclicense@gmail.com.
 */

package li.l1t.mtc.misc;

import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Handles punsishing players logging out while fighting
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 9.6.14
 */
public interface AntiLogoutHandler {
    boolean isFighting(UUID uuid);

    void setFighting(Player damaged, Player damager);

    void clearFighters();
}

package io.github.xxyy.minotopiacore.chat.cmdspy;

import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.UUID;

/**
 * Filters commands and send them to registered command spies.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 19.6.14
 */
public interface CommandSpyFilter {
    /**
     * Checks if this filter matches a given command
     *
     * @param command Command to be matched, without preceding slash.
     * @param sender  Who executed the command
     * @return Whether this filter matches the given arguments.
     */
    boolean matches(String command, Player sender);

    default boolean subscribable() {
        return true;
    }

    /**
     * Notifies this filter's subscribers if this filter matches given arguments.
     * @param command Command to be matched, without preceding slash.
     * @param sender Who executed that command
     */
    boolean notifyOnMatch(String command, Player sender);

    /**
     * @return A modifiable Collection of this filter's subscribers
     */
    Collection<UUID> getSubscribers();

    default String niceRepresentation() {
        return getClass().getSimpleName();
    }
}

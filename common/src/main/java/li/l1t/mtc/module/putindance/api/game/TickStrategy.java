/*
 * Copyright (c) 2013-2016.
 * This work is protected by international copyright laws and licensed
 * under the license terms which can be found at src/main/resources/LICENSE.txt
 * or alternatively obtained by sending an email to xxyy98+mtclicense@gmail.com.
 */

package li.l1t.mtc.module.putindance.api.game;

import li.l1t.common.misc.XyLocation;

/**
 * Defines the behaviour of a game when it is ticked.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 2016-09-21
 */
public interface TickStrategy {
    void tick(Game game);

    boolean isReady();

    /**
     * Checks whether this strategy can tick a board with given boundaries. Order of arguments does
     * not matter.
     *
     * @param firstBoundary  the first boundary
     * @param secondBoundary the second boundary
     * @throws InvalidBoardSelectionException if this strategy cannot handle a board with given
     *                                        boundaries
     */
    void checkBoard(XyLocation firstBoundary, XyLocation secondBoundary) throws InvalidBoardSelectionException;
}

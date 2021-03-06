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

package li.l1t.mtc.module.putindance;

import com.google.common.base.Preconditions;
import li.l1t.common.misc.XyLocation;
import li.l1t.common.util.CommandHelper;
import li.l1t.mtc.api.MTCPlugin;
import li.l1t.mtc.misc.ClearCacheBehaviour;
import li.l1t.mtc.module.ConfigurableMTCModule;
import li.l1t.mtc.module.putindance.api.board.Board;
import li.l1t.mtc.module.putindance.api.board.generator.BoardGenerator;
import li.l1t.mtc.module.putindance.api.game.Game;

/**
 * A module that provides the PutinDance mini-game for events. PutinDance is based around a board
 * filled with different wool colors. Every time an admin executes /pd tick, some blocks are removed
 * from the board. The last player to be on a wool block wins. The board consists of multiple
 * layers.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 2016-09-20
 */
public class PutinDanceModule extends ConfigurableMTCModule {
    public static final String NAME = "PutinDance";
    public static final String CHAT_PREFIX = "§3[§6§lPD§3] §3";
    public static final String ADMIN_PERMISSION = "mtc.putindance.admin";
    private final PutinDanceConfig config = new PutinDanceConfig();
    private final Wiring wiring = new Wiring(config, this);
    private WandHandler wandHandler;
    private Board currentBoard;
    private Game currentGame;

    protected PutinDanceModule() {
        super(NAME, "modules/events/putindance.cfg.yml", ClearCacheBehaviour.RELOAD_ON_FORCED, false);
    }

    @Override
    public void enable(MTCPlugin plugin) throws Exception {
        super.enable(plugin);

        wandHandler = new WandHandler(this);
        registerCommand(new PutinDanceCommand(this), "pd");
    }

    @Override
    protected void reloadImpl() {
        config.loadFrom(configuration);
    }

    @Override
    public void save() {
        config.saveTo(configuration);
        super.save();
    }

    public PutinDanceConfig getConfig() {
        return config;
    }

    public void setSpawnLocation(XyLocation spawnLocation) {
        config.setSpawnLocation(spawnLocation);
        if (hasGame()) {
            getCurrentGame().setSpawnLocation(spawnLocation);
        }
    }

    public void setBoardBoundaries(XyLocation first, XyLocation second) {
        config.setFirstBoardBoundary(first);
        config.setSecondBoardBoundary(second);
        save();
    }

    public WandHandler getWandHandler() {
        return wandHandler;
    }

    public boolean hasGame() {
        return currentGame != null;
    }

    public boolean hasOpenGame() {
        return currentGame != null && currentGame.isOpen();
    }

    public Game getCurrentGame() {
        return currentGame;
    }

    public Board getCurrentBoard() {
        return currentBoard;
    }

    public BoardGenerator createGenerator() {
        return wiring.wireGenerator(board -> {
            currentBoard = board;
            CommandHelper.broadcast(CHAT_PREFIX + "Spielfeld fertig generiert! §6/pd new", PutinDanceModule.ADMIN_PERMISSION);
        });
    }

    public boolean hasBoard() {
        return currentBoard != null;
    }

    public void newGame() {
        Preconditions.checkState(!hasGame(), "there is already a game");
        Preconditions.checkState(hasBoard(), "there is no current board");
        currentGame = wiring.wireGame(getCurrentBoard());
        currentGame.openGame();
    }



    public void abortGame() {
        Preconditions.checkState(hasGame(), "no current game");
        currentGame.abortGame();
        currentGame = null;
    }
}

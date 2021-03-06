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

package li.l1t.mtc.module.vote.api.reward;

import li.l1t.mtc.module.vote.api.Vote;
import org.bukkit.entity.Player;

import java.util.stream.Stream;

/**
 * A reward that also applies other rewards when applied.
 *
 * @author <a href="https://l1t.li/">Literallie</a>
 * @since 2017-01-11
 */
public interface CompoundReward extends Reward {
    Stream<Reward> findRewardsFor(Player player, Vote vote);
}

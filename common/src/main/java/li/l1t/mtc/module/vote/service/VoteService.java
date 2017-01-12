/*
 * Copyright (c) 2013-2017.
 * This work is protected by international copyright laws and licensed
 * under the license terms which can be found at src/main/resources/LICENSE.txt
 * or alternatively obtained by sending an email to xxyy98+mtclicense@gmail.com.
 */

package li.l1t.mtc.module.vote.service;

import com.google.common.base.Preconditions;
import li.l1t.mtc.api.chat.MessageType;
import li.l1t.mtc.api.module.inject.InjectMe;
import li.l1t.mtc.logging.LogManager;
import li.l1t.mtc.module.vote.api.Vote;
import li.l1t.mtc.module.vote.api.VoteQueue;
import li.l1t.mtc.module.vote.api.VoteRepository;
import li.l1t.mtc.module.vote.reward.loader.RewardConfig;
import li.l1t.mtc.module.vote.reward.loader.RewardConfigs;
import li.l1t.mtc.module.vote.sql.queue.SqlVoteQueue;
import li.l1t.mtc.module.vote.sql.vote.SqlVote;
import li.l1t.mtc.module.vote.sql.vote.SqlVoteRepository;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;

/**
 * Service that handles votes.
 *
 * @author <a href="https://l1t.li/">Literallie</a>
 * @since 2017-01-12
 */
public class VoteService {
    private static final Logger LOGGER = LogManager.getLogger(VoteService.class);
    private final VoteRepository votes;
    private final VoteQueue voteQueue;
    private final RewardConfigs rewards;

    @InjectMe
    public VoteService(SqlVoteRepository voteRepository, SqlVoteQueue voteQueue, RewardConfigs rewards) {
        this.votes = voteRepository;
        this.voteQueue = voteQueue;
        this.rewards = rewards;
    }

    public void handleVote(String username, String serviceName) {
        UUID uuid = null;
        Player onlinePlayer = Bukkit.getPlayer(username);
        if (onlinePlayer != null) {
            uuid = onlinePlayer.getUniqueId();
        }
        SqlVote vote = votes.createVote(username, serviceName, uuid);
        if (onlinePlayer != null) {
            dispatchVoteReward(onlinePlayer, vote);
        } else {
            voteQueue.queueVote(vote);
        }
    }

    public void dispatchVoteReward(Player player, Vote vote) {
        Preconditions.checkNotNull(player, "player");
        Preconditions.checkNotNull(vote, "vote");
        Optional<RewardConfig> config = rewards.findConfig(vote.getServiceName());
        if (vote.getPlayerId() == null) {
            vote.setPlayerId(player.getUniqueId());
            votes.save(vote);
        }
        if (config.isPresent()) {
            config.get().getRewards()
                    .forEach(reward -> reward.apply(player, vote));
        } else {
            MessageType.INTERNAL_ERROR.sendTo(player,
                    "Für diesen Votelink ('%s') ist keine Belohnung festgelegt. Bitte wende dich an den Support.",
                    vote.getServiceName()
            );
        }
    }
}
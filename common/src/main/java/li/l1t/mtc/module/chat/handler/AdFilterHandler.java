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

package li.l1t.mtc.module.chat.handler;

import li.l1t.common.chat.AdFilterService;
import li.l1t.common.util.CommandHelper;
import li.l1t.mtc.module.chat.ChatModule;
import li.l1t.mtc.module.chat.api.ChatMessageEvent;
import li.l1t.mtc.module.chat.api.ChatPhase;
import li.l1t.mtc.module.chat.impl.ModuleAwareChatHandler;
import org.apache.commons.lang.math.RandomUtils;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 2016-08-21
 */
public class AdFilterHandler extends ModuleAwareChatHandler {
    private static final String[] DEFAULT_MOCK_MESSAGES = new String[]{
            "MinoTopia ist mein Lieblingsserver! <3",
            "Niemand hat die Absicht, Werbung zu machen.",
            "joined alle minotopia.me - guter Server!",
            "habt ihr heute schon alle gevoted? /vote"
    };
    private AdFilterService filterService;
    private List<String> mockMessages;

    public AdFilterHandler() {
        super(ChatPhase.CENSORING);
    }

    @Override
    public boolean enable(ChatModule module) {
        super.enable(module);
        filterService = new AdFilterService();
        populateWithModuleConfig(module, filterService);
        mockMessages = module.getConfigStringList("ads.replacement-messages", DEFAULT_MOCK_MESSAGES);
        return true;
    }

    private void populateWithModuleConfig(ChatModule module, AdFilterService filterService) {
        boolean findHiddenDots = module.getConfigBoolean("ads.aggressive-dot-matching", true);
        boolean findIpAddresses = module.getConfigBoolean("ads.match-ip-addresses", true);
        List<String> ignoredDomains = module.getConfigStringList("ads.ignored-domains", "minotopia.me", "l1t.li");
        filterService.setFindHiddenDots(findHiddenDots);
        filterService.setFindIpAddresses(findIpAddresses);
        filterService.addIgnoredDomains(ignoredDomains);
    }

    @Override
    public void handle(ChatMessageEvent evt) {
        if (filterService.test(evt.getInitialMessage())) {
            handleMatch(evt);
        }
    }

    private void handleMatch(ChatMessageEvent evt) {
        if (evt.mayBypassFilters()) {
            evt.sendPrefixed("Na leiwand, Werbefilter ignoriert.");
            return;
        }
        evt.sendPrefixed("§cWerbung ist ein Armutszeugnis.");
        evt.setMessage(getRandomMockMessage());
        broadcastAdInfo("§a%s§6 hat den Werbefilter ausgelöst:", evt.getPlayer().getName());
        broadcastAdInfo("§a   %s", evt.getInitialMessage());
    }

    @Nonnull
    private String getRandomMockMessage() {
        if (mockMessages.isEmpty()) {
            mockMessages.addAll(Arrays.asList(DEFAULT_MOCK_MESSAGES));
        }
        return mockMessages.get(RandomUtils.nextInt(mockMessages.size()));
    }

    private void broadcastAdInfo(String message, Object... params) {
        CommandHelper.broadcast(getModule().formatMessage(message, params), "mtc.adinfo");
    }
}

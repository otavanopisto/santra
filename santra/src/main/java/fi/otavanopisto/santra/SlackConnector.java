/* 
 * Copyright (C) 2015 Otavan Opisto
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package fi.otavanopisto.santra;

import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

@Log
public class SlackConnector implements SantraConnector, AutoCloseable {

    private final SlackSession slackConnection;
    private final List<SantraConnector.MessageListener> messageListeners;

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static class SlackMessageSession
        implements SantraConnector.MessageSession {

        private final SlackSession slackSession;
        private final SlackChannel slackChannel;

        @Override
        public void sendMessage(String message) {
            slackSession.sendMessage(slackChannel, message, null);
        }
    }

    public SlackConnector(String slackAuthToken) throws IOException {
        log.info("opening slack connection...");
        slackConnection = SlackSessionFactory
                .createWebSocketSlackSession(slackAuthToken);

        slackConnection.connect();
        log.info("slack connection opened");

        slackConnection.addMessagePostedListener(this::onMessagePosted);

        messageListeners = new ArrayList<>();
    }

    private void onMessagePosted(
            SlackMessagePosted event,
            SlackSession session
    ) {
        log.info("message received");
        String prefix = "<@" + session.sessionPersona().getId() + ">";
        String message = event.getMessageContent().trim();
        if (!message.startsWith(prefix)) {
            return;
        } else {
            message = message.substring(prefix.length()).trim();
        }

        for (SantraConnector.MessageListener listener : messageListeners) {
            listener.onMessage(
                    event.getSender().getUserName(),
                    message,
                    new SlackMessageSession(session, event.getChannel()));
        }
    }

    @Override
    public void addMessageListener(MessageListener listener) {
        messageListeners.add(listener);
    }

    @Override
    public void close() throws Exception {
        slackConnection.disconnect();
    }
}

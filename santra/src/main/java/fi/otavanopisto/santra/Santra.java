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

import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import lombok.Synchronized;
import lombok.extern.java.Log;
import org.alicebot.ab.Bot;
import org.alicebot.ab.Chat;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

@Log
@RequiredArgsConstructor
public class Santra {

    private Bot bot;
    private SlackSession slackConnection;
    private final Random random = new Random();
    private final CommandLineArguments arguments;
    private List<String> admins;
    private final Map<String, Chat>
            chats = Collections.synchronizedMap(new HashMap<>());
    private static final String[] NOSY_RESPONSES = {
        "No chance.",
        "Who do you think you are?",
        "Yeah, right.",
        "When cows fly!"
    };

    public void run() throws IOException {
        admins = Arrays.asList(arguments.getAdmins().split(","));
        initBot();

        log.info("opening slack connection...");
        slackConnection
                = SlackSessionFactory.createWebSocketSlackSession(
                        arguments.getAuthenticationToken()
                );

        slackConnection.connect();
        log.info("slack connection opened");

        slackConnection.addMessagePostedListener(
                this::onMessagePosted);
    }

    @Synchronized
    private void initBot() {
        initBot(arguments.getBotName());
    }

    @Synchronized
    private void initBot(String botName) {
        log.info("creating bot...");
        bot = new Bot(
                botName,
                arguments.getBasePath()
        );
        chats.clear();
        log.info("bot created");
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

        if (message.startsWith("!")) {
            commandMessage(message.substring(1), event, session);
        } else {
            chatMessage(message, event, session);
        }
    }

    private void chatMessage(
            String message,
            SlackMessagePosted event,
            SlackSession session
    ) {
        String senderName = event.getSender().getUserName();
        chats.putIfAbsent(senderName, new Chat(bot));

        String response = String.format(
                "@%s %s",
                senderName,
                chats.get(senderName).multisentenceRespond(message));
        session.sendMessage(event.getChannel(),
                response,
                null);
    }

    private void commandMessage(
            String message,
            SlackMessagePosted event,
            SlackSession session
    ) {
        if (!admins.contains(event.getSender().getUserName())) {
            session.sendMessage(
                    event.getChannel(),
                    NOSY_RESPONSES[random.nextInt(NOSY_RESPONSES.length)],
                    null
            );
            return;
        }
        if (message.startsWith("reload")) {
            String botName = message.substring("reload".length()).trim();
            if ("".equals(botName)) {
                botName = arguments.getBotName();
            }
            try {
                reload(botName);
                session.sendMessage(
                    event.getChannel(),
                    "Reload complete.",
                    null);
            } catch (IOException | GitAPIException ex) {
                session.sendMessage(
                    event.getChannel(),
                    String.format("Reload failed: %s.", ex.getMessage()),
                    null);
            }
        }
    }

    private void reload(String botName) throws IOException, GitAPIException {
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        Repository repository = builder
                .setGitDir(new File(arguments.getBasePath() + "/.git"))
                .readEnvironment()
                .findGitDir()
                .build();
        Git git = new Git(repository);
        git.pull()
           .call();

        initBot(botName);
    }
}

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
    private final Random random = new Random();
    private final CommandLineArguments arguments;
    private final SantraConnector santraConnector;
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

        santraConnector.addMessageListener(this::onMessage);
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

    private void onMessage(
            String sender,
            String message,
            SantraConnector.MessageSession session
    ) {
        if (message.startsWith("!")) {
            commandMessage(sender, message.substring(1), session);
        } else {
            chatMessage(sender, message, session);
        }
    }

    private void chatMessage(
            String sender,
            String message,
            SantraConnector.MessageSession session
    ) {
        chats.putIfAbsent(sender, new Chat(bot));

        String response = String.format(
                "@%s %s",
                sender,
                chats.get(sender).multisentenceRespond(message));
        session.sendMessage(response);
    }

    private void commandMessage(
            String sender,
            String message,
            SantraConnector.MessageSession session
    ) {
        if (!admins.contains(sender)) {
            session.sendMessage(
                    NOSY_RESPONSES[random.nextInt(NOSY_RESPONSES.length)]);
            return;
        }
        if (message.startsWith("reload")) {
            String botName = message.substring("reload".length()).trim();
            if ("".equals(botName)) {
                botName = arguments.getBotName();
            }
            try {
                reload(botName);
                session.sendMessage("Reload complete.");
            } catch (IOException | GitAPIException ex) {
                session.sendMessage(
                    String.format("Reload failed: %s.", ex.getMessage()));
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

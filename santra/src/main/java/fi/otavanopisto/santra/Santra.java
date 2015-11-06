/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.otavanopisto.santra;

import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory;
import java.io.File;
import java.io.IOException;
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
    private Chat chatSession;
    private SlackSession slackConnection;
    private final CommandLineArguments arguments;

    public void run() throws IOException {
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
        log.info("creating bot...");
        bot = new Bot(
                arguments.getBotName(),
                arguments.getBasePath()
        );
        log.info("bot created");
        
        log.info("opening chat session...");
        chatSession = new Chat(bot);
        log.info("chat session opened");
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
            message = message.substring(prefix.length());
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
        String response = chatSession.multisentenceRespond(message);
        session.sendMessage(event.getChannel(),
                response,
                null);
    }

    private void commandMessage(
            String message,
            SlackMessagePosted event,
            SlackSession session
    ) {
        if ("reload".equals(message)) {
            try {
                reload();
                session.sendMessage(
                    event.getChannel(),
                    "reload complete",
                    null);
            } catch (IOException | GitAPIException ex) {
                session.sendMessage(
                    event.getChannel(),
                    String.format("reload failed: %s", ex.getMessage()),
                    null);
            }
        }
    }

    private void reload() throws IOException, GitAPIException {
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        Repository repository = builder
                .setGitDir(new File(arguments.getBasePath()))
                .readEnvironment()
                .findGitDir()
                .build();
        Git git = new Git(repository);
        git.pull()
           .call();

        initBot();
    }
}

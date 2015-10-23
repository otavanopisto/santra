/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.otavanopisto.santra;

import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory;
import java.io.IOException;
import java.util.Scanner;
import org.alicebot.ab.Bot;
import org.alicebot.ab.Chat;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

/**
 *
 * @author Ilmo Euro
 */
public class Santra {

    public static void main(String... args) throws IOException {
        CommandLineArguments arguments = new CommandLineArguments();
        CmdLineParser parser = new CmdLineParser(arguments);

        try {
            parser.parseArgument(args);
        } catch (CmdLineException ex) {
            System.err.println(ex.getMessage());
            System.err.println("usage: santra [options]");
            parser.printUsage(System.err);
            System.err.println();
            return;
        }

        Scanner scanner = new Scanner(System.in);

        Bot bot = new Bot(
                arguments.getBotName(),
                arguments.getBasePath()
        );

        Chat chatSession = new Chat(bot);

        SlackSession slackConnection =
                SlackSessionFactory.createWebSocketSlackSession(
                     arguments.getAuthenticationToken()
                );

        slackConnection.connect();

        slackConnection.addMessagePostedListener((event, sess) -> {
            String prefix = "@santra ";
            String message = event.getMessageContent();
            if (!message.startsWith(prefix)) {
                return;
            } else {
                message = message.substring(prefix.length());
            }

            String response = chatSession.multisentenceRespond(message);
            sess.sendMessage(event.getChannel(),
                             response,
                             null);
        });
    }
}
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.otavanopisto.santra;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.kohsuke.args4j.Option;

@NoArgsConstructor
public class CommandLineArguments {
    @Getter
    @Option(
            name = "-b",
            usage = "Base path to look up bot files",
            metaVar = "<path>")
    private String basePath = ".";
    @Getter
    @Option(name = "-n",
            usage = "The AIML bot name",
            metaVar = "<name>")
    private String botName = "santra";
    @Getter
    @Option(name = "-t",
            usage = "Slack authentication token",
            metaVar = "<token>")
    private String authenticationToken;
}

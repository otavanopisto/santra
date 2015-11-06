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
    private String authenticationToken = "";
    @Getter
    @Option(name = "-a",
            usage = "The nicks of admins, separated by commas",
            metaVar = "<nicks>")
    private String admins = "";
}

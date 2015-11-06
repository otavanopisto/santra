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

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

public class Main {
    public static void main(String... args) throws Exception {
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

        final Santra santra = new Santra(arguments);
        santra.run();
    }
}

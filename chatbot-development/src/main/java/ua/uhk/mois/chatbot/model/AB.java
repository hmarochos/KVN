package ua.uhk.mois.chatbot.model;
/* Program AB Reference AIML 2.0 implementation
        Copyright (C) 2013 ALICE A.I. Foundation
        Contact: info@alicebot.org

        This library is free software; you can redistribute it and/or
        modify it under the terms of the GNU Library General Public
        License as published by the Free Software Foundation; either
        version 2 of the License, or (at your option) any later version.

        This library is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
        Library General Public License for more details.

        You should have received a copy of the GNU Library General Public
        License along with this library; if not, write to the
        Free Software Foundation, Inc., 51 Franklin St, Fifth Floor,
        Boston, MA  02110-1301, USA.
*/

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Log4j2
public class AB {

    // normal.txt";
    public static final String LOG_FILE = MagicStrings.root_path + "/data/" + MagicStrings.ab_sample_file;

    /**
     * magically suggests new patterns for a bot. Reads an input file of sample data called logFile. Builds a graph of
     * all the inputs. Finds new patterns in the graph that are not already in the bot. Classifies input log into those
     * new patterns.
     *
     * @param bot
     *         the bot to perform the magic on
     */
    public static void ab(Bot bot) {
        String logFile = LOG_FILE;
        MagicBooleans.trace_mode = false;
        MagicBooleans.enable_external_sets = false;
        Timer timer = new Timer();
        bot.brain.nodeStats();
        timer.start();
        log.info("Graphing inputs");
        bot.graphInputs(logFile);
        log.info(timer.elapsedTimeSecs() + " seconds Graphing inputs");
        timer.start();
        log.info("Finding Patterns");
        bot.findPatterns();
        log.info(Bot.suggestedCategories.size() + " suggested categories");
        log.info(timer.elapsedTimeSecs() + " seconds finding patterns");
        timer.start();
        bot.patternGraph.nodeStats();
        log.info("Classifying Inputs");
        bot.classifyInputs(logFile);
        log.info(timer.elapsedTimeSecs() + " classifying inputs");
    }
}


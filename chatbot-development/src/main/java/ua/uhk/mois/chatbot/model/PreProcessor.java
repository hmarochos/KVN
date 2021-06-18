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


import ua.uhk.mois.chatbot.utils.IOUtils;
import lombok.extern.log4j.Log4j2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AIML Preprocessor and substitutions
 */

@Log4j2
public class PreProcessor {

    private final int normalCount;
    private final int denormalCount;
    private final int personCount;
    private final int person2Count;
    private final int genderCount;
    private final String[] normalSubs = new String[MagicNumbers.max_substitutions];
    private final Pattern[] normalPatterns = new Pattern[MagicNumbers.max_substitutions];
    private final String[] denormalSubs = new String[MagicNumbers.max_substitutions];
    private final Pattern[] denormalPatterns = new Pattern[MagicNumbers.max_substitutions];
    private final String[] personSubs = new String[MagicNumbers.max_substitutions];
    private final Pattern[] personPatterns = new Pattern[MagicNumbers.max_substitutions];
    private final String[] person2Subs = new String[MagicNumbers.max_substitutions];
    private final Pattern[] person2Patterns = new Pattern[MagicNumbers.max_substitutions];
    private final String[] genderSubs = new String[MagicNumbers.max_substitutions];
    private final Pattern[] genderPatterns = new Pattern[MagicNumbers.max_substitutions];

    /**
     * Constructor given bot
     */
    public PreProcessor() {
        normalCount = readSubstitutions(ResourceFilePaths.CONFIG_ROOT_PATH + ResourceFilePaths.CONFIG_ROOT_PATH_NORMAL, normalPatterns, normalSubs);
        denormalCount = readSubstitutions(ResourceFilePaths.CONFIG_ROOT_PATH + ResourceFilePaths.CONFIG_ROOT_PATH_DENORMAL, denormalPatterns, denormalSubs);
        personCount = readSubstitutions(ResourceFilePaths.CONFIG_ROOT_PATH + ResourceFilePaths.CONFIG_ROOT_PATH_PERSON, personPatterns, personSubs);
        person2Count = readSubstitutions(ResourceFilePaths.CONFIG_ROOT_PATH + ResourceFilePaths.CONFIG_ROOT_PATH_PERSON2, person2Patterns, person2Subs);
        genderCount = readSubstitutions(ResourceFilePaths.CONFIG_ROOT_PATH + ResourceFilePaths.CONFIG_ROOT_PATH_GENDER, genderPatterns, genderSubs);
        log.info("Preprocessor: " + normalCount + " norms " + personCount + " persons " + person2Count + " person2 ");
    }

    /**
     * read substitutions from input stream
     *
     * @param in
     *         input stream
     * @param patterns
     *         array of patterns
     * @param subs
     *         array of substitution values
     *
     * @return number of patterns substitutions read
     */
    public static int readSubstitutionsFromInputStream(InputStream in, Pattern[] patterns, String[] subs) {
        int subCount = 0;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
            String strLine;
            //Read File Line By Line
            while ((strLine = br.readLine()) != null) {
                strLine = strLine.trim();
                Pattern pattern = Pattern.compile("\"(.*?)\",\"(.*?)\"", Pattern.DOTALL);
                Matcher matcher = pattern.matcher(strLine);
                if (matcher.find() && subCount < MagicNumbers.max_substitutions) {
                    subs[subCount] = matcher.group(2);
                    String quotedPattern = Pattern.quote(matcher.group(1));
                    patterns[subCount] = Pattern.compile(quotedPattern, Pattern.CASE_INSENSITIVE);
                    subCount++;
                }

            }
        } catch (Exception ex) {
            log.error("An error occurred.", ex);
        }
        return subCount;
    }

    /**
     * read substitutions from a file
     *
     * @param filename
     *         name of substitution file
     * @param patterns
     *         array of patterns
     * @param subs
     *         array of substitution values
     *
     * @return number of patterns and substitutions read
     */
    static int readSubstitutions(String filename, Pattern[] patterns, String[] subs) {
        int subCount = 0;

        // Open the file that is the first
        // command line parameter
        try (InputStream resourceInputStream = IOUtils.getResourceInputStream(filename)) {
            // Get the object of DataInputStream
            subCount = readSubstitutionsFromInputStream(resourceInputStream, patterns, subs);
        } catch (IOException e) {
            String message = String.format("Cannot read substitutions from '%s'.", filename);
            log.error(message, e);
        }

        return (subCount);
    }

    /**
     * Split an input into an array of sentences based on sentence-splitting characters.
     *
     * @param line
     *         input text
     *
     * @return array of sentences
     */
    public static String[] sentenceSplit(String line) {
        line = line.replace("。", ".");
        line = line.replace("？", "?");
        line = line.replace("！", "!");
        String[] result = line.split("[.!?]");
        for (int i = 0; i < result.length; i++)
            result[i] = result[i].trim();
        return result;
    }

    /**
     * Apply a sequence of subsitutions to an input string
     *
     * @param request
     *         input request
     * @param patterns
     *         array of patterns to match
     * @param subs
     *         array of substitution values
     * @param count
     *         number of patterns and substitutions
     *
     * @return result of applying substitutions to input
     */
    static String substitute(String request, Pattern[] patterns, String[] subs, int count) {
        String result = " " + request + " ";
        try {
            for (int i = 0; i < count; i++) {

                String replacement = subs[i];
                Pattern p = patterns[i];
                Matcher m = p.matcher(result);
                if (m.find()) {
                    result = m.replaceAll(replacement);
                }
            }
            while (result.contains("  "))
                result = result.replace("  ", " ");
            result = result.trim();
        } catch (Exception ex) {
            log.error("An error occurred.", ex);
        }
        return result.trim();
    }

    /**
     * apply normalization substitutions to a request
     *
     * @param request
     *         client input
     *
     * @return normalized client input
     */
    public String normalize(String request) {
        return substitute(request, normalPatterns, normalSubs, normalCount);
    }

    /**
     * apply denormalization substitutions to a request
     *
     * @param request
     *         client input
     *
     * @return normalized client input
     */
    public String denormalize(String request) {
        return substitute(request, denormalPatterns, denormalSubs, denormalCount);
    }

    /**
     * personal pronoun substitution for {@code <person></person>} tag
     *
     * @param input
     *         sentence
     *
     * @return sentence with pronouns swapped
     */
    public String person(String input) {
        return substitute(input, personPatterns, personSubs, personCount);

    }

    /**
     * personal pronoun substitution for {@code <person2></person2>} tag
     *
     * @param input
     *         sentence
     *
     * @return sentence with pronouns swapped
     */
    public String person2(String input) {
        return substitute(input, person2Patterns, person2Subs, person2Count);

    }

    /**
     * personal pronoun substitution for {@code <gender>} tag
     *
     * @param input
     *         sentence
     *
     * @return sentence with pronouns swapped
     */
    public String gender(String input) {
        return substitute(input, genderPatterns, genderSubs, genderCount);

    }
}

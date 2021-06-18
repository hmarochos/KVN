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
import java.util.HashMap;

/**
 * Manage client predicates
 */

@Log4j2
public class Predicates extends HashMap<String, String> {

    private static final long serialVersionUID = 1L;

    /**
     * save a predicate value
     *
     * @param key
     *         predicate name
     * @param value
     *         predicate value
     *
     * @return predicate value
     */
    @Override
    public String put(String key, String value) {
        if (MagicBooleans.trace_mode)
            log.info("Setting predicate {} to {}", key, value);
        return super.put(key, value);
    }

    /**
     * get a predicate value
     *
     * @param key
     *         predicate name
     *
     * @return predicate value
     */
    public String get(String key) {
        String result = super.get(key);
        return result == null ? MagicStrings.unknown_predicate_value : result;
    }

    /**
     * Read predicate default values from an input stream
     *
     * @param in
     *         input stream
     */
    public void getPredicateDefaultsFromInputStream(InputStream in) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
            //Read File Line By Line
            String strLine;
            while ((strLine = br.readLine()) != null) {
                if (strLine.contains(":")) {
                    String property = strLine.substring(0, strLine.indexOf(':'));
                    String value = strLine.substring(strLine.indexOf(':') + 1);
                    put(property, value);
                }
            }
        } catch (Exception ex) {
            log.error("An error occurred.", ex);
        }
    }

    /**
     * read predicate defaults from a file
     *
     * @param filename
     *         name of file
     */
    public void getPredicateDefaults(String filename) {
        log.info("Get predicate defaults {}", filename);
        try (InputStream resourceInputStream = IOUtils.getResourceInputStream(filename)) {
            // Get the object
            getPredicateDefaultsFromInputStream(resourceInputStream);
        } catch (IOException e) {
            String message = String.format("Cannot get predicate default from '%s'", filename);
            log.error(message, e);
        }
    }
}

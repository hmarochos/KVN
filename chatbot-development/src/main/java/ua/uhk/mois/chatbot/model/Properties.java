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
 * Bot Properties
 */

@Log4j2
public class Properties extends HashMap<String, String> {

    private static final long serialVersionUID = 1L;

    /**
     * get the value of a bot property.
     *
     * @param key
     *         property name
     *
     * @return property value or a string indicating the property is undefined
     */
    public String get(String key) {
        String result = super.get(key);
        return result == null ? MagicStrings.unknown_property_value : result;
    }

    /**
     * Read bot properties from an input stream.
     *
     * @param in
     *         Input stream
     */
    public void getPropertiesFromInputStream(InputStream in) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
            String strLine;
            //Read File Line By Line
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
     * Read bot properties from a file.
     *
     * @param filename
     *         file containing bot properties
     */
    public void getProperties(String filename) {
        log.info("Get Properties: " + filename);
        try (InputStream resourceInputStream = IOUtils.getResourceInputStream(filename)) {
            // Get the object
            getPropertiesFromInputStream(resourceInputStream);
        } catch (IOException e) {
            String message = String.format("Cannot get properties from '%s'", filename);
            log.error(message, e);
        }
    }
}

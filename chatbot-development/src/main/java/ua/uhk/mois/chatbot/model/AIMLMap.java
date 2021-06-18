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
 * implements AIML Map
 * <p>
 * A map is a function from one string set to another. Elements of the domain are called keys and elements of the range
 * are called values.
 */

@Log4j2
public class AIMLMap extends HashMap<String, String> {

    private static final long serialVersionUID = 7714426597677331329L;

    private final String mapName;
    String host; // for external maps
    String botid; // for external maps
    boolean isExternal = false;

    /**
     * constructor to create a new AIML Map
     *
     * @param mapName
     *         the name of the map
     */
    public AIMLMap(String mapName) {
        this.mapName = mapName;
    }

    /**
     * return a map value given a key
     *
     * @param key
     *         the domain element
     *
     * @return the range element or a string indicating the key was not found
     */
    public String get(String key) {
        String value;
        if (mapName.equals(MagicStrings.map_successor)) {
            try {
                int number = Integer.parseInt(key);
                return String.valueOf(number + 1);
            } catch (Exception ex) {
                return MagicStrings.unknown_map_value;
            }
        } else if (mapName.equals(MagicStrings.map_predecessor)) {
            try {
                int number = Integer.parseInt(key);
                return String.valueOf(number - 1);
            } catch (Exception ex) {
                return MagicStrings.unknown_map_value;
            }
        } else if (isExternal && MagicBooleans.enable_external_sets) {
            String query = mapName.toUpperCase() + " " + key;
            String response = Sraix.sraix(null, query, MagicStrings.unknown_map_value, null, host, botid);
            log.info("External " + mapName + "(" + key + ")=" + response);
            value = response;
        } else
            value = super.get(key);
        if (value == null)
            value = MagicStrings.unknown_map_value;
        log.info("AIMLMap get " + key + "=" + value);
        return value;
    }

    /**
     * reads an input stream and loads a map into the bot.
     *
     * @param in
     *         input stream
     *
     * @return number of map elements loaded
     */
    public int readAIMLMapFromInputStream(InputStream in) {
        int cnt = 0;
        String strLine;
        //Read File Line By Line
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
            while ((strLine = br.readLine()) != null && !strLine.isEmpty()) {
                String[] splitLine = strLine.split(":");
                if (splitLine.length >= 2) {
                    cnt++;
                    if (strLine.startsWith(MagicStrings.remote_map_key)) {
                        if (splitLine.length >= 3) {
                            host = splitLine[1];
                            botid = splitLine[2];
                            isExternal = true;
                            log.info("Created external map at " + host + " " + botid);
                        }
                    } else {
                        String key = splitLine[0].toUpperCase();
                        String value = splitLine[1];
                        put(key, value);
                    }
                }
            }
        } catch (Exception ex) {
            log.error("Error occurred.", ex);
        }
        return cnt;
    }

    /**
     * read an AIML map for a bot
     */
    public void readAIMLMap() {
        log.info("Reading AIML Map " + MagicStrings.maps_path + "/" + mapName + ".txt");

        try (InputStream resourceInputStream = IOUtils.getResourceInputStream(ResourceFilePaths.MAPS_ROOT_PATH + mapName + ".txt")) {
            // Get the object
            readAIMLMapFromInputStream(resourceInputStream);
        } catch (IOException e) {
            String message = String.format("Cannot read AIML Map %s", mapName + ".txt");
            log.error(message, e);
        }
    }
}

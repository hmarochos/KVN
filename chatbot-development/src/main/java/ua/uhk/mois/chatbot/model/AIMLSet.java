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

import lombok.extern.log4j.Log4j2;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ua.uhk.mois.chatbot.utils.IOUtils.getResourceInputStream;

/**
 * implements AIML Sets
 */
@Log4j2
public class AIMLSet extends HashSet<String> {

    private static final long serialVersionUID = 6654100978765770843L;

    private final String setName;
    int maxLength = 1; // there are no empty sets
    String host; // for external sets
    String botid; // for external sets
    boolean isExternal;

    /**
     * constructor
     *
     * @param name
     *         name of set
     */
    public AIMLSet(String name) {
        setName = name.toLowerCase();
    }

    public boolean contains(String s) {
        if (isExternal && MagicBooleans.enable_external_sets) {
            String[] split = s.split(" ");
            if (split.length > maxLength)
                return false;
            String query = MagicStrings.set_member_string + setName.toUpperCase() + " " + s;
            String response = Sraix.sraix(null, query, "false", null, host, botid);
            log.info("External " + setName + " contains " + s + "? " + response);
            return "true".equals(response);
        } else if (setName.equals(MagicStrings.natural_number_set_name)) {
            Pattern numberPattern = Pattern.compile("[0-9]+");
            Matcher numberMatcher = numberPattern.matcher(s);
            return numberMatcher.matches();
        } else
            return super.contains(s);
    }

    public int readAIMLSetFromInputStream(InputStream in) {
        String strLine;
        int cnt = 0;
        //Read File Line By Line
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
            while ((strLine = br.readLine()) != null && !strLine.isEmpty()) {
                cnt++;
                // assume the set is pre-normalized for faster loading
                if (strLine.startsWith("external")) {
                    String[] splitLine = strLine.split(":");
                    if (splitLine.length >= 4) {
                        host = splitLine[1];
                        botid = splitLine[2];
                        maxLength = Integer.parseInt(splitLine[3]);
                        isExternal = true;
                        log.info("Created external set at " + host + " " + botid);
                    }
                } else {
                    strLine = strLine.toUpperCase().trim();
                    String[] splitLine = strLine.split(" ");
                    int length = splitLine.length;
                    if (length > maxLength)
                        maxLength = length;
                    add(strLine.trim());
                }
            }
        } catch (Exception ex) {
            log.error("An exception occurred.", ex);
        }
        return cnt;
    }

    public void readAIMLSet(String path) {
        log.info("Reading AIML Set " + path);

        try (InputStream is = getResourceInputStream(path)) {
            if (is != null) {
                // Get the object
                readAIMLSetFromInputStream(is);
            } else
                log.info(path + " not found");
        } catch (Exception e) {
            log.error("Cannot read AIML set '" + path + "': " + e, e);
        }
    }
}

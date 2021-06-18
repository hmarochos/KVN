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

import ua.uhk.mois.chatbot.utils.CalendarUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Log4j2
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Utilities {

    /**
     * Excel sometimes adds mysterious formatting to CSV files. This function tries to clean it up.
     *
     * @param line
     *         line from AIMLIF file
     *
     * @return reformatted line
     */
    public static String fixCSV(String line) {
        while (line.endsWith(";"))
            line = line.substring(0, line.length() - 1);
        if (line.startsWith("\""))
            line = line.substring(1);
        if (line.endsWith("\""))
            line = line.substring(0, line.length() - 1);
        line = line.replaceAll("\"\"", "\"");
        return line;
    }

    public static String tagTrim(String xmlExpression, String tagName) {
        String stag = "<" + tagName + ">";
        String etag = "</" + tagName + ">";
        if (xmlExpression.length() >= (stag + etag).length()) {
            xmlExpression = xmlExpression.substring(stag.length());
            xmlExpression = xmlExpression.substring(0, xmlExpression.length() - etag.length());
        }
        return xmlExpression;
    }

    public static Set<String> stringSet(String... strings) {
        HashSet<String> set = new HashSet<>();
        Collections.addAll(set, strings);
        return set;
    }

    private static String getFileFromInputStream(InputStream in) {
        StringBuilder contents = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
            String strLine;
            //Read File Line By Line
            while ((strLine = br.readLine()) != null) {
                contents.append(strLine.isEmpty() ? "\n" : strLine + "\n");
            }
        } catch (Exception ex) {
            log.error("An error occurred.", ex);
        }
        return contents.toString().trim();
    }

    // todo method is called to load non-existent files
    private static String getFile(String filename) {
        String contents = "";
        try {
            File file = new File(filename);
            if (file.exists()) {
                FileInputStream fstream = new FileInputStream(filename);
                // Get the object
                contents = getFileFromInputStream(fstream);
                fstream.close();
            }
        } catch (Exception e) {
            log.error("Cannot get file '" + filename + "': " + e, e);
        }
        return contents;
    }

    public static String getCopyright(Bot bot, String aimlFilename) {
        StringBuilder copyright = new StringBuilder();
        String year = CalendarUtils.year();
        String date = CalendarUtils.date();
        try {
            copyright = new StringBuilder(getFile(MagicStrings.config_path + "/copyright.txt"));
            String[] splitCopyright = copyright.toString().split("\n");
            copyright = new StringBuilder();
            for (String s : splitCopyright) {
                copyright.append("<!-- ").append(s).append(" -->\n");
            }
            copyright = new StringBuilder(copyright.toString().replace("[url]", bot.properties.get("url")));
            copyright = new StringBuilder(copyright.toString().replace("[date]", date));
            copyright = new StringBuilder(copyright.toString().replace("[YYYY]", year));
            copyright = new StringBuilder(copyright.toString().replace("[version]", bot.properties.get("version")));
            copyright = new StringBuilder(copyright.toString().replace("[botname]", bot.getName().toUpperCase()));
            copyright = new StringBuilder(copyright.toString().replace("[filename]", aimlFilename));
            copyright = new StringBuilder(copyright.toString().replace("[botmaster]", bot.properties.get("botmaster")));
            copyright = new StringBuilder(copyright.toString().replace("[organization]", bot.properties.get("organization")));
        } catch (Exception e) {
            log.error("Cannot get copyright from '" + aimlFilename + "': " + e, e);
        }
        return copyright.toString();
    }

    // todo file is not present in project
    public static String getPannousAPIKey() {
        String apiKey = getFile(MagicStrings.config_path + "/pannous-apikey.txt");
        if (apiKey.isEmpty())
            apiKey = MagicStrings.pannous_api_key;
        return apiKey;
    }

    // todo file is not present in project
    public static String getPannousLogin() {
        String login = getFile(MagicStrings.config_path + "/pannous-login.txt");
        if (login != null && login.isEmpty())
            login = MagicStrings.pannous_login;
        return login;
    }
}

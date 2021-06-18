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

import java.util.Comparator;

/**
 * structure representing an AIML category and operations on Category
 */

@Log4j2
public class Category {

    /**
     * compare two categories for sorting purposes based on category index number
     */
    public static final Comparator<Category> CATEGORY_NUMBER_COMPARATOR = Comparator.comparingInt(Category::getCategoryNumber);
    private static final String THAT = "<THAT>";
    private static final String TOPIC = "<TOPIC>";
    private static int categoryCnt;
    private final int categoryNumber; // for loading order
    private String pattern;
    private String that;
    private String topic;
    private String template;
    private String filename;
    private int activationCnt;
    private AIMLSet matches;

    /**
     * Constructor
     *
     * @param activationCnt
     *         category activation count
     * @param pattern
     *         input pattern
     * @param that
     *         that pattern
     * @param topic
     *         topic pattern
     * @param template
     *         AIML template
     * @param filename
     *         AIML file name
     */

    public Category(int activationCnt, String pattern, String that, String topic, String template, String filename) {
        if (MagicBooleans.FIX_EXCEL_CSV) {
            pattern = Utilities.fixCSV(pattern);
            that = Utilities.fixCSV(that);
            topic = Utilities.fixCSV(topic);
            template = Utilities.fixCSV(template);
            filename = Utilities.fixCSV(filename);
        }
        this.pattern = pattern.trim().toUpperCase();
        this.that = that.trim().toUpperCase();
        this.topic = topic.trim().toUpperCase();
        this.template = template.replace("& ", " and "); // XML parser treats & badly
        this.filename = filename;
        this.activationCnt = activationCnt;
        matches = null;
        categoryNumber = categoryCnt++;
    }

    /**
     * Constructor
     *
     * @param activationCnt
     *         category activation count
     * @param patternThatTopic
     *         string representing Pattern Path
     * @param template
     *         AIML template
     * @param filename
     *         AIML category
     */
    public Category(int activationCnt, String patternThatTopic, String template, String filename) {
        this(activationCnt,
             patternThatTopic.substring(0, patternThatTopic.indexOf(THAT)),
             patternThatTopic.substring(patternThatTopic.indexOf(THAT) + THAT.length(), patternThatTopic.indexOf(TOPIC)),
             patternThatTopic.substring(patternThatTopic.indexOf(TOPIC) + TOPIC.length()), template, filename);
    }

    /**
     * convert a template to a single-line representation by replacing "," with #Comma and newline with #Newline
     *
     * @param template
     *         original template
     *
     * @return template on a single line of text
     */
    public static String templateToLine(String template) {
        String result = template;
        result = result.replaceAll("(\r\n|\n\r|\r|\n)", "\\#Newline");
        result = result.replaceAll(MagicStrings.aimlif_split_char, MagicStrings.aimlif_split_char_name);
        return result;
    }

    /**
     * restore a template to its original form by replacing #Comma with "," and #Newline with newline.
     *
     * @param line
     *         template on a single line of text
     *
     * @return original multi-line template
     */
    private static String lineToTemplate(String line) {
        String result = line.replace("#Newline", "\n");
        result = result.replaceAll(MagicStrings.aimlif_split_char_name, MagicStrings.aimlif_split_char);
        return result;
    }

    /**
     * convert a category from AIMLIF format to a Category object
     *
     * @param IF
     *         Category in AIMLIF format
     *
     * @return Category object
     */
    public static Category ifToCategory(String IF) {
        String[] split = IF.split(MagicStrings.aimlif_split_char);
        return new Category(Integer.parseInt(split[0]), split[1], split[2], split[3], lineToTemplate(split[4]), split[5]);
    }

    /**
     * convert a Category object to AIMLIF format
     *
     * @param category
     *         Category object
     *
     * @return category in AIML format
     */
    public static String categoryToIF(Category category) {
        String c = MagicStrings.aimlif_split_char;
        return category.getActivationCnt() + c + category.getPattern() + c + category.getThat() + c + category.getTopic() + c + templateToLine(category.getTemplate()) + c + category.getFilename();
    }

    /**
     * convert a Category object to AIML syntax
     *
     * @param category
     *         Category object
     *
     * @return AIML Category
     */
    public static String categoryToAIML(Category category) {
        String topicStart = "";
        String topicEnd = "";
        String thatStatement = "";
        String result = "";
        StringBuilder pattern = new StringBuilder(category.getPattern());
        String[] splitPattern = pattern.toString().split(" ");
        for (String w : splitPattern) {
            if (w.startsWith("<TYPE>"))
                w = w.toLowerCase();
            pattern.append(" ").append(w);
        }
        pattern = new StringBuilder(pattern.toString().trim());
        if (pattern.toString().contains("type"))
            log.info("Rebuilt pattern " + pattern);

        String nl = "\n";
        try {
            if (!"*".equals(category.getTopic())) {
                topicStart = "<topic name=\"" + category.getTopic() + "\">" + nl;
                topicEnd = "</topic>" + nl;
            }
            if (!"*".equals(category.getThat())) {
                thatStatement = "<that>" + category.getThat() + "</that>";
            }
            result = topicStart + "<category><pattern>" + category.getPattern() + "</pattern>" + thatStatement + nl +
                    "<template>" + category.getTemplate() + "</template>" + nl +
                    "</category>" + topicEnd;
        } catch (Exception ex) {
            log.error("An error occurred.", ex);
        }
        return result;
    }

    /**
     * number of times a category was activated by inputs
     *
     * @return integer number of activations
     */
    public int getActivationCnt() {
        return activationCnt;
    }

    /**
     * get the index number of this category
     *
     * @return unique integer identifying this category
     */
    public int getCategoryNumber() {
        return categoryNumber;
    }

    /**
     * get category pattern
     *
     * @return pattern
     */
    public String getPattern() {
        return pattern == null ? "*" : pattern;
    }

    /**
     * set category pattern
     *
     * @param pattern
     *         AIML pattern
     */
    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    /**
     * get category that pattern
     *
     * @return that pattern
     */
    public String getThat() {
        return that == null ? "*" : that;
    }

    /**
     * set category that pattern
     *
     * @param that
     *         AIML that pattern
     */
    public void setThat(String that) {
        this.that = that;
    }

    /**
     * get category topic pattern
     *
     * @return topic pattern
     */
    public String getTopic() {
        return topic == null ? "*" : topic;
    }

    /**
     * set category topic
     *
     * @param topic
     *         AIML topic pattern
     */
    public void setTopic(String topic) {
        this.topic = topic;
    }

    /**
     * get category template
     *
     * @return template
     */
    public String getTemplate() {
        return template == null ? "" : template;
    }

    /**
     * set category template
     *
     * @param template
     *         AIML template
     */
    public void setTemplate(String template) {
        this.template = template;
    }

    /**
     * get name of AIML file for this category
     *
     * @return file name
     */
    public String getFilename() {
        return filename == null ? MagicStrings.unknown_aiml_file : filename;
    }

    /**
     * set category filename
     *
     * @param filename
     *         name of AIML file
     */
    public void setFilename(String filename) {
        this.filename = filename;
    }

    /**
     * increment the category activation count
     */
    public void incrementActivationCnt() {
        activationCnt++;
    }

    /**
     * return a string represeting the full pattern path as "{@code input pattern <THAT> that pattern <TOPIC> topic
     * pattern}"
     *
     * @return
     */
    public String inputThatTopic() {
        return Graphmaster.inputThatTopic(pattern, that, topic);
    }

    /**
     * add a matching input to the matching input set
     *
     * @param input
     *         matching input
     */
    public void addMatch(String input) {
        if (matches == null) {
            String setName = inputThatTopic().replace("*", "STAR").replace("_", "UNDERSCORE").replace(" ", "-").replace(THAT, "THAT").replace(TOPIC, "TOPIC");
            matches = new AIMLSet(setName);
        }
        matches.add(input);
    }
}

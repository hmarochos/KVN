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
import ua.uhk.mois.chatbot.utils.DomUtils;
import ua.uhk.mois.chatbot.utils.IOUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * The core AIML parser and interpreter. Implements the AIML 2.0 specification as described in AIML 2.0 Working Draft
 * document https://docs.google.com/document/d/1wNT25hJRyupcG51aO89UcQEiG-HkXRXusukADpFnDs4/pub
 */

@Log4j2
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AIMLProcessor {

    public static final String TEXT = "#text";
    public static final String PATTERN = "pattern";
    public static final String TOPIC = "topic";
    public static final String TEMPLATE = "template";
    public static final String CATEGORY = "category";
    public static final String TEMPLATE1 = "<template>";
    public static final String TEMPLATE2 = "</template>";
    public static final String AN_EXCEPTION_OCCURRED = "An exception occurred.";
    public static final String VALUE = "value";
    public static AIMLProcessorExtension extension;
    public static int sraiCount;
    public static int trace_count;

    /**
     * when parsing an AIML file, process a category element.
     *
     * @param n
     *         current XML parse node.
     * @param categories
     *         list of categories found so far.
     * @param topic
     *         value of topic in case this category is wrapped in a <topic> tag
     * @param aimlFile
     *         name of AIML file being parsed.
     */
    private static void categoryProcessor(Node n, ArrayList<Category> categories, String topic, String aimlFile, String language) {
        String pattern, that, template;

        NodeList children = n.getChildNodes();
        pattern = "*";
        that = "*";
        template = "";
        for (int j = 0; j < children.getLength(); j++) {
            Node m = children.item(j);
            String mName = m.getNodeName();
            switch (mName) {
                case TEXT: /*skip*/
                    break;
                case PATTERN:
                    pattern = DomUtils.nodeToString(m);
                    break;
                case "that":
                    that = DomUtils.nodeToString(m);
                    break;
                case TOPIC:
                    topic = DomUtils.nodeToString(m);
                    break;
                case TEMPLATE:
                    template = DomUtils.nodeToString(m);
                    break;
                default:
                    log.info("categoryProcessor: unexpected " + mName);
                    break;
            }
        }

        pattern = trimTag(pattern, PATTERN);
        that = trimTag(that, "that");
        topic = trimTag(topic, TOPIC);
        template = trimTag(template, TEMPLATE);
        if ("JP".equals(language) || "jp".equals(language)) {
            String morphPattern = JapaneseTokenizer.morphSentence(pattern);
            log.info("<pattern>" + pattern + "</pattern> --> <pattern>" + morphPattern + "</pattern>");
            pattern = morphPattern;
            String morphThatPattern = JapaneseTokenizer.morphSentence(that);
            log.info("<that>" + that + "</that> --> <that>" + morphThatPattern + "</that>");
            that = morphThatPattern;
            String morphTopicPattern = JapaneseTokenizer.morphSentence(topic);
            log.info("<topic>" + topic + "</topic> --> <topic>" + morphTopicPattern + "</topic>");
            topic = morphTopicPattern;
        }
        Category c = new Category(0, pattern, that, topic, template, aimlFile);
        categories.add(c);
    }

    public static String trimTag(String s, String tagName) {
        String stag = "<" + tagName + ">";
        String etag = "</" + tagName + ">";
        if (s.startsWith(stag) && s.endsWith(etag)) {
            s = s.substring(stag.length());
            s = s.substring(0, s.length() - etag.length());
        }
        return s.trim();
    }

    /**
     * convert an AIML file to a list of categories.
     *
     * @param directory
     *         directory containing the AIML file.
     * @param aimlFile
     *         AIML file name.
     *
     * @return list of categories.
     */
    public static List<Category> aimlToCategories(String directory, String aimlFile) {
        try {
            ArrayList<Category> categories = new ArrayList<>();
            Node root = DomUtils.parseFile(directory + aimlFile);      // <aiml> tag
            String language = MagicStrings.default_language;
            if (root.hasAttributes()) {
                NamedNodeMap xMLAttributes = root.getAttributes();
                for (int i = 0; i < xMLAttributes.getLength(); i++) {
                    if ("language".equals(xMLAttributes.item(i).getNodeName()))
                        language = xMLAttributes.item(i).getNodeValue();
                }
            }
            NodeList nodelist = root.getChildNodes();
            for (int i = 0; i < nodelist.getLength(); i++) {
                Node n = nodelist.item(i);
                if (CATEGORY.equals(n.getNodeName())) {
                    categoryProcessor(n, categories, "*", aimlFile, language);
                } else if (n.getNodeName().equals(TOPIC)) {
                    String topic = n.getAttributes().getNamedItem("name").getTextContent();
                    NodeList children = n.getChildNodes();
                    for (int j = 0; j < children.getLength(); j++) {
                        Node m = children.item(j);
                        if (m.getNodeName().equals(CATEGORY)) {
                            categoryProcessor(m, categories, topic, aimlFile, language);
                        }
                    }
                }
            }
            return categories;
        } catch (Exception ex) {
            log.info("AIMLToCategories: " + ex);
            return Collections.emptyList();
        }
    }

    /**
     * generate a bot response to a single sentence input.
     *
     * @param input
     *         the input sentence.
     * @param that
     *         the bot's last sentence.
     * @param topic
     *         current topic.
     * @param chatSession
     *         current client session.
     *
     * @return bot's response.
     */
    public static String respond(String input, String that, String topic, Chat chatSession) {
        return respond(input, that, topic, chatSession, 0);
    }

    /**
     * generate a bot response to a single sentence input.
     *
     * @param input
     *         input statement.
     * @param that
     *         bot's last reply.
     * @param topic
     *         current topic.
     * @param chatSession
     *         current client chat session.
     * @param srCnt
     *         number of <srai> activations.
     *
     * @return bot's reply.
     */
    public static String respond(String input, String that, String topic, Chat chatSession, int srCnt) {
        String response;
        if (input == null || input.isEmpty())
            input = MagicStrings.null_input;
        sraiCount = srCnt;
        response = MagicStrings.default_bot_response;
        try {
            Nodemapper leaf = chatSession.bot.brain.match(input, that, topic);
            if (leaf == null) {
                return (response);
            }
            log.info("Template = {}", leaf.category.getTemplate());
            ParseState ps = new ParseState(0, chatSession, input, that, topic, leaf);
            Chat.matchTrace += leaf.category.getTemplate() + "\n";
            response = evalTemplate(leaf.category.getTemplate(), ps);
            log.info("That = {}", that);
        } catch (Exception ex) {
            log.error("An error occured.", ex);
        }
        return response;
    }

    /**
     * capitalizeString: from http://stackoverflow.com/questions/1892765/capitalize-first-char-of-each-word-in-a-string-java
     *
     * @param string
     *         the string to capitalize
     *
     * @return the capitalized string
     */

    private static String capitalizeString(String string) {
        char[] chars = string.toLowerCase().toCharArray();
        boolean found = false;
        for (int i = 0; i < chars.length; i++) {
            if (!found && Character.isLetter(chars[i])) {
                chars[i] = Character.toUpperCase(chars[i]);
                found = true;
            } else if (Character.isWhitespace(chars[i])) {
                found = false;
            }
        }
        return String.valueOf(chars);
    }

    // Parsing and evaluation functions:

    /**
     * explode a string into individual characters separated by one space
     *
     * @param input
     *         input string
     *
     * @return exploded string
     */
    private static String explode(String input) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < input.length(); i++)
            result.append(" ").append(input.charAt(i));
        return result.toString().trim();
    }

    /**
     * evaluate the contents of an AIML tag. calls recursEval on child tags.
     *
     * @param node
     *         the current parse node.
     * @param ps
     *         the current parse state.
     * @param ignoreAttributes
     *         tag names to ignore when evaluating the tag.
     *
     * @return the result of evaluating the tag contents.
     */
    public static String evalTagContent(Node node, ParseState ps, Set<String> ignoreAttributes) {
        StringBuilder result = new StringBuilder();
        try {
            NodeList childList = node.getChildNodes();
            for (int i = 0; i < childList.getLength(); i++) {
                Node child = childList.item(i);
                if (ignoreAttributes == null || !ignoreAttributes.contains(child.getNodeName()))
                    result.append(recursEval(child, ps));
            }
        } catch (Exception ex) {
            log.info("Something went wrong with evalTagContent", ex);
        }
        return result.toString();
    }

    /**
     * pass thru generic XML (non-AIML tags, such as HTML) as unevaluated XML
     *
     * @param node
     *         current parse node
     * @param ps
     *         current parse state
     *
     * @return unevaluated generic XML string
     */
    public static String genericXML(Node node, ParseState ps) {
        String result = evalTagContent(node, ps, null);
        return unevaluatedXML(result, node);
    }

    /**
     * return a string of unevaluated XML.      When the AIML parser encounters an unrecognized XML tag, it simply
     * passes through the tag in XML form.  For example, if the response contains HTML markup, the HTML is passed to the
     * requesting process.    However if that markup contains AIML tags, those tags are evaluated and the parser builds
     * the result.
     *
     * @param result
     *         the tag contents.
     * @param node
     *         current parse node.
     *
     * @return the unevaluated XML string
     */
    private static String unevaluatedXML(String result, Node node) {
        String nodeName = node.getNodeName();
        StringBuilder attributes = new StringBuilder();
        if (node.hasAttributes()) {
            NamedNodeMap xMLAttributes = node.getAttributes();
            for (int i = 0; i < xMLAttributes.getLength(); i++) {
                attributes.append(" ").append(xMLAttributes.item(i).getNodeName()).append("=\"").append(xMLAttributes.item(i).getNodeValue()).append("\"");
            }
        }
        return result != null && result.isEmpty() ? "<" + nodeName + attributes + "/>" : "<" + nodeName + attributes + ">" + result + "</" + nodeName + ">";
    }

    /**
     * implements AIML <srai> tag
     *
     * @param node
     *         current parse node.
     * @param ps
     *         current parse state.
     *
     * @return the result of processing the <srai>
     */
    private static String srai(Node node, ParseState ps) {
        sraiCount++;
        if (sraiCount > MagicNumbers.max_recursion) {
            return MagicStrings.too_much_recursion;
        }
        String response = MagicStrings.default_bot_response;
        try {
            String result = evalTagContent(node, ps, null);
            result = result.trim();
            result = result.replaceAll("(\r\n|\n\r|\r|\n)", " ");
            result = ps.getChatSession().bot.preProcessor.normalize(result);
            String topic = ps.getChatSession().predicates.get(TOPIC);     // the that stays the same, but the topic may have changed
            if (MagicBooleans.trace_mode) {
                log.info(trace_count + ". <srai>" + result + "</srai> from " + ps.getLeaf().category.inputThatTopic() + " topic=" + topic + ") ");
                trace_count++;
            }
            Nodemapper leaf = ps.getChatSession().bot.brain.match(result, ps.getThat(), topic);
            if (leaf == null) {
                return (response);
            }
            response = evalTemplate(leaf.category.getTemplate(), new ParseState(ps.getDepth() + 1, ps.getChatSession(), ps.getInput(), ps.getThat(), topic, leaf));
        } catch (Exception ex) {
            log.error(AN_EXCEPTION_OCCURRED, ex);
        }
        return response.trim();

    }

    /**
     * in AIML 2.0, an attribute value can be specified by either an XML attribute value or a subtag of the same name.
     * This function tries to read the value from the XML attribute first, then tries to look for the subtag.
     *
     * @param node
     *         current parse node.
     * @param ps
     *         current parse state.
     * @param attributeName
     *         the name of the attribute.
     *
     * @return the attribute value.
     */
    // value can be specified by either attribute or tag
    private static String getAttributeOrTagValue(Node node, ParseState ps, String attributeName) {        // AIML 2.0
        String result;
        Node m = node.getAttributes().getNamedItem(attributeName);
        if (m == null) {
            NodeList childList = node.getChildNodes();
            result = null;         // no attribute or tag named attributeName
            for (int i = 0; i < childList.getLength(); i++) {
                Node child = childList.item(i);
                if (child.getNodeName().equals(attributeName)) {
                    result = evalTagContent(child, ps, null);
                }
            }
        } else {
            result = m.getNodeValue();
        }
        return result;
    }

    /**
     * access external web service for response implements <sraix></sraix> and its attribute variations.
     *
     * @param node
     *         current XML parse node
     * @param ps
     *         AIML parse state
     *
     * @return response from remote service or string indicating failure.
     */
    private static String sraix(Node node, ParseState ps) {
        Set<String> attributeNames = Utilities.stringSet("botid", "host");
        String host = getAttributeOrTagValue(node, ps, "host");
        String botid = getAttributeOrTagValue(node, ps, "botid");
        String hint = getAttributeOrTagValue(node, ps, "hint");
        String defaultResponse = getAttributeOrTagValue(node, ps, "default");
        String result = evalTagContent(node, ps, attributeNames);

        return Sraix.sraix(ps.getChatSession(), result, defaultResponse, hint, host, botid);
    }

    /**
     * map an element of one string set to an element of another Implements <map name="mapname"></map>   and
     * <map><name>mapname</name></map>
     *
     * @param node
     *         current XML parse node
     * @param ps
     *         current AIML parse state
     *
     * @return the map result or a string indicating the key was not found
     */
    private static String map(Node node, ParseState ps) {
        String result = MagicStrings.unknown_map_value;
        Set<String> attributeNames = Utilities.stringSet("name");
        String mapName = getAttributeOrTagValue(node, ps, "name");
        String contents = evalTagContent(node, ps, attributeNames);
        if (mapName == null)
            result = "<map>" + contents + "</map>"; // this is an OOB map tag (no attribute)
        else {
            AIMLMap map = Bot.mapMap.get(mapName);
            if (map != null)
                result = map.get(contents.toUpperCase());
            if (result == null)
                result = MagicStrings.unknown_map_value;
            result = result.trim();
        }
        return result;
    }

    /**
     * set the value of an AIML predicate. Implements <set name="predicate"></set> and <set var="varname"></set>
     *
     * @param node
     *         current XML parse node
     * @param ps
     *         AIML parse state
     *
     * @return the result of the <set> operation
     */
    private static String set(Node node, ParseState ps) {                    // add pronoun check
        Set<String> attributeNames = Utilities.stringSet("name", "var");
        String predicateName = getAttributeOrTagValue(node, ps, "name");
        String varName = getAttributeOrTagValue(node, ps, "var");
        String value = evalTagContent(node, ps, attributeNames).trim();
        value = value.replaceAll("(\r\n|\n\r|\r|\n)", " ");
        if (predicateName != null)
            ps.getChatSession().predicates.put(predicateName, value);
        if (varName != null)
            ps.getVars().put(varName, value);
        return value;
    }

    /**
     * get the value of an AIML predicate. implements <get name="predicate"></get>  and <get var="varname"></get>
     *
     * @param node
     *         current XML parse node
     * @param ps
     *         AIML parse state
     *
     * @return the result of the <get> operation
     */
    private static String get(Node node, ParseState ps) {
        String result = MagicStrings.unknown_predicate_value;
        String predicateName = getAttributeOrTagValue(node, ps, "name");
        String varName = getAttributeOrTagValue(node, ps, "var");
        if (predicateName != null)
            result = ps.getChatSession().predicates.get(predicateName).trim();
        else if (varName != null)
            result = ps.getVars().get(varName).trim();
        return result;
    }

    /**
     * return the value of a bot property. implements {{{@code <bot name="property"/>}}}
     *
     * @param node
     *         current XML parse node
     * @param ps
     *         AIML parse state
     *
     * @return the bot property or a string indicating the property was not found.
     */
    private static String bot(Node node, ParseState ps) {
        String result = MagicStrings.unknown_property_value;
        String propertyName = getAttributeOrTagValue(node, ps, "name");
        if (propertyName != null)
            result = ps.getChatSession().bot.properties.get(propertyName).trim();
        return result;
    }

    /**
     * implements formatted date tag <date jformat="format"/> and <date format="format"/>
     *
     * @param node
     *         current XML parse node
     * @param ps
     *         AIML parse state
     *
     * @return the formatted date
     */
    private static String date(Node node, ParseState ps) {
        String jformat = getAttributeOrTagValue(node, ps, "jformat");      // AIML 2.0
        String locale = getAttributeOrTagValue(node, ps, "locale");
        String timezone = getAttributeOrTagValue(node, ps, "timezone");
        return CalendarUtils.date(jformat, locale, timezone);
    }

    /**
     * <interval><style>years</style></style><jformat>MMMMMMMMM dd, yyyy</jformat><from>August 2,
     * 1960</from><to><date><jformat>MMMMMMMMM dd, yyyy</jformat></date></to></interval>
     */

    private static String interval(Node node, ParseState ps) {
        String style = getAttributeOrTagValue(node, ps, "style");      // AIML 2.0
        String jformat = getAttributeOrTagValue(node, ps, "jformat");      // AIML 2.0
        String from = getAttributeOrTagValue(node, ps, "from");
        String to = getAttributeOrTagValue(node, ps, "to");
        if (style == null)
            style = "years";
        if (jformat == null)
            jformat = "MMMMMMMMM dd, yyyy";
        if (from == null)
            from = "January 1, 1970";
        if (to == null) {
            to = CalendarUtils.date(jformat, null, null);
        }
        String result = "unknown";
        if ("years".equals(style))
            result = String.valueOf(Interval.getYearsBetween(from, to, jformat));
        if ("months".equals(style))
            result = String.valueOf(Interval.getMonthsBetween(from, to, jformat));
        if ("days".equals(style))
            result = String.valueOf(Interval.getDaysBetween(from, to, jformat));
        if ("hours".equals(style))
            result = String.valueOf(Interval.getHoursBetween(from, to, jformat));
        return result;
    }

    /**
     * get the value of an index attribute and return it as an integer. if it is not recognized as an integer, return 0
     *
     * @param node
     *         current XML parse node
     * @param ps
     *         AIML parse state
     *
     * @return the the integer intex value
     */
    private static int getIndexValue(Node node, ParseState ps) {
        int index = 0;
        String value = getAttributeOrTagValue(node, ps, "index");
        if (value != null)
            try {
                index = Integer.parseInt(value) - 1;
            } catch (Exception ex) {
                log.error(AN_EXCEPTION_OCCURRED, ex);
            }
        return index;
    }

    /**
     * implements {@code <star index="N"/>} returns the value of input words matching the Nth wildcard (or AIML Set).
     *
     * @param node
     *         current XML parse node
     * @param ps
     *         AIML parse state
     *
     * @return the word sequence matching a wildcard
     */
    private static String inputStar(Node node, ParseState ps) {
        int index = getIndexValue(node, ps);
        return ps.getLeaf().starBindings.getInputStars().star(index) == null ? "" : ps.getLeaf().starBindings.getInputStars().star(index).trim();
    }

    /**
     * implements {@code <thatstar index="N"/>} returns the value of input words matching the Nth wildcard (or AIML Set)
     * in <that></that>.
     *
     * @param node
     *         current XML parse node
     * @param ps
     *         AIML parse state
     *
     * @return the word sequence matching a wildcard
     */
    private static String thatStar(Node node, ParseState ps) {
        int index = getIndexValue(node, ps);
        return ps.getLeaf().starBindings.getThatStars().star(index) == null ? "" : ps.getLeaf().starBindings.getThatStars().star(index).trim();
    }

    /**
     * implements <topicstar/> and <topicstar index="N"/> returns the value of input words matching the Nth wildcard (or
     * AIML Set) in a topic pattern.
     *
     * @param node
     *         current XML parse node
     * @param ps
     *         AIML parse state
     *
     * @return the word sequence matching a wildcard
     */
    private static String topicStar(Node node, ParseState ps) {
        int index = getIndexValue(node, ps);
        return ps.getLeaf().starBindings.getTopicStars().star(index) == null ? "" : ps.getLeaf().starBindings.getTopicStars().star(index).trim();
    }

    /**
     * return the client ID. implements {@code <id/>}
     *
     * @param ps
     *         AIML parse state
     *
     * @return client ID
     */
    private static String id(ParseState ps) {
        return ps.getChatSession().customerId;
    }

    /**
     * return the size of the robot brain (number of AIML categories loaded). implements {@code <size/>}
     *
     * @param ps
     *         AIML parse state
     *
     * @return bot brain size
     */
    private static String size(ParseState ps) {
        int size = ps.getChatSession().bot.brain.getCategories().size();
        return String.valueOf(size);
    }

    /**
     * return the size of the robot vocabulary (number of words the bot can recognize). implements {@code
     * <vocabulary/>}
     *
     * @param ps
     *         AIML parse state
     *
     * @return bot vocabulary size
     */
    private static String vocabulary(ParseState ps) {
        int size = ps.getChatSession().bot.brain.getVocabulary().size();
        return String.valueOf(size);
    }

    /**
     * return a string indicating the name and version of the AIML program. implements {@code <program/>}
     *
     * @return AIML program name and version.
     */
    private static String program() {
        return MagicStrings.programNameVersion;
    }

    /**
     * implements the (template-side) {@code <that index="M,N"/>}    tag. returns a normalized sentence.
     *
     * @param node
     *         current XML parse node
     * @param ps
     *         AIML parse state
     *
     * @return the nth last sentence of the bot's mth last reply.
     */
    private static String that(Node node, ParseState ps) {
        int index = 0;
        int jndex = 0;
        String value = getAttributeOrTagValue(node, ps, "index");
        if (value != null)
            try {
                String[] spair = value.split(",");
                index = Integer.parseInt(spair[0]) - 1;
                jndex = Integer.parseInt(spair[1]) - 1;
                log.info("That index=" + index + "," + jndex);
            } catch (Exception ex) {
                log.error("An exception occured.", ex);
            }
        String that = MagicStrings.unknown_history_item;
        History hist = ps.getChatSession().thatHistory.get(index);
        if (hist != null)
            that = (String) hist.get(jndex);
        return that.trim();
    }

    /**
     * implements {@code <input index="N"/>} tag
     *
     * @param node
     *         current XML parse node
     * @param ps
     *         AIML parse state
     *
     * @return the nth last sentence input to the bot
     */

    private static String input(Node node, ParseState ps) {
        int index = getIndexValue(node, ps);
        return ps.getChatSession().inputHistory.getString(index);
    }

    /**
     * implements {@code <request index="N"/>} tag
     *
     * @param node
     *         current XML parse node
     * @param ps
     *         AIML parse state
     *
     * @return the nth last multi-sentence request to the bot.
     */
    private static String request(Node node, ParseState ps) {             // AIML 2.0
        int index = getIndexValue(node, ps);
        return ps.getChatSession().requestHistory.getString(index).trim();
    }

    /**
     * implements {@code <response index="N"/>} tag
     *
     * @param node
     *         current XML parse node
     * @param ps
     *         AIML parse state
     *
     * @return the bot's Nth last multi-sentence response.
     */
    private static String response(Node node, ParseState ps) {            // AIML 2.0
        int index = getIndexValue(node, ps);
        return ps.getChatSession().responseHistory.getString(index).trim();
    }

    /**
     * implements {@code <system>} tag. Evaluate the contents, and try to execute the result as a command in the
     * underlying OS shell. Read back and return the result of this command.
     * <p>
     * The timeout parameter allows the botmaster to set a timeout in ms, so that the <system></system>   command
     * returns eventually.
     *
     * @param node
     *         current XML parse node
     * @param ps
     *         AIML parse state
     *
     * @return the result of executing the system command or a string indicating the command failed.
     */
    private static String system(Node node, ParseState ps) {
        Set<String> attributeNames = Utilities.stringSet("timeout");
        String evaluatedContents = evalTagContent(node, ps, attributeNames);
        return IOUtils.system(evaluatedContents, MagicStrings.system_failed);
    }

    /**
     * implements {@code <think>} tag
     * <p>
     * Evaluate the tag contents but return a blank. "Think but don't speak."
     *
     * @param node
     *         current XML parse node
     * @param ps
     *         AIML parse state
     *
     * @return a blank empty string
     */
    private static String think(Node node, ParseState ps) {
        evalTagContent(node, ps, null);
        return "";
    }

    /**
     * Transform a string of words (separtaed by spaces) into a string of individual characters (separated by spaces).
     * Explode "ABC DEF" = "A B C D E F".
     *
     * @param node
     *         current XML parse node
     * @param ps
     *         AIML parse state
     *
     * @return exploded string
     */
    private static String explode(Node node, ParseState ps) {              // AIML 2.0
        String result = evalTagContent(node, ps, null);
        return explode(result);
    }

    /**
     * apply the AIML normalization pre-processor to the evaluated tag contenst. implements {@code <normalize>} tag.
     *
     * @param node
     *         current XML parse node
     * @param ps
     *         AIML parse state
     *
     * @return normalized string
     */
    private static String normalize(Node node, ParseState ps) {            // AIML 2.0
        String result = evalTagContent(node, ps, null);
        return ps.getChatSession().bot.preProcessor.normalize(result);
    }

    /**
     * apply the AIML denormalization pre-processor to the evaluated tag contenst. implements {@code <normalize>} tag.
     *
     * @param node
     *         current XML parse node
     * @param ps
     *         AIML parse state
     *
     * @return denormalized string
     */
    private static String denormalize(Node node, ParseState ps) {            // AIML 2.0
        String result = evalTagContent(node, ps, null);
        return ps.getChatSession().bot.preProcessor.denormalize(result);
    }

    /**
     * evaluate tag contents and return result in upper case implements {@code <uppercase>} tag
     *
     * @param node
     *         current XML parse node
     * @param ps
     *         AIML parse state
     *
     * @return uppercase string
     */
    private static String uppercase(Node node, ParseState ps) {
        String result = evalTagContent(node, ps, null);
        return result.toUpperCase();
    }

    /**
     * evaluate tag contents and return result in lower case implements {@code <lowercase>} tag
     *
     * @param node
     *         current XML parse node
     * @param ps
     *         AIML parse state
     *
     * @return lowercase string
     */
    private static String lowercase(Node node, ParseState ps) {
        String result = evalTagContent(node, ps, null);
        return result.toLowerCase();
    }

    /**
     * evaluate tag contents and capitalize each word. implements {@code <formal>} tag
     *
     * @param node
     *         current XML parse node
     * @param ps
     *         AIML parse state
     *
     * @return capitalized string
     */
    private static String formal(Node node, ParseState ps) {
        String result = evalTagContent(node, ps, null);
        return capitalizeString(result);
    }

    /**
     * evaluate tag contents and capitalize the first word. implements {@code <sentence>} tag
     *
     * @param node
     *         current XML parse node
     * @param ps
     *         AIML parse state
     *
     * @return string with first word capitalized
     */
    private static String sentence(Node node, ParseState ps) {
        String result = evalTagContent(node, ps, null);
        return result.length() > 1 ? result.substring(0, 1).toUpperCase() + result.substring(1) : "";
    }

    /**
     * evaluate tag contents and swap 1st and 2nd person pronouns implements {@code <person>} tag
     *
     * @param node
     *         current XML parse node
     * @param ps
     *         AIML parse state
     *
     * @return sentence with pronouns swapped
     */
    private static String person(Node node, ParseState ps) {
        String result;
        // for <person/>
        result = node.hasChildNodes() ? evalTagContent(node, ps, null) : ps.getLeaf().starBindings.getInputStars().star(0);
        result = " " + result + " ";
        result = ps.getChatSession().bot.preProcessor.person(result);
        return result.trim();
    }

    /**
     * evaluate tag contents and swap 1st and 3rd person pronouns implements {@code <person2>} tag
     *
     * @param node
     *         current XML parse node
     * @param ps
     *         AIML parse state
     *
     * @return sentence with pronouns swapped
     */
    private static String person2(Node node, ParseState ps) {
        String result;
        // for <person2/>
        result = node.hasChildNodes() ? evalTagContent(node, ps, null) : ps.getLeaf().starBindings.getInputStars().star(0);
        result = " " + result + " ";
        result = ps.getChatSession().bot.preProcessor.person2(result);
        return result.trim();
    }

    /**
     * implements {@code <gender>} tag swaps gender pronouns
     *
     * @param node
     *         current XML parse node
     * @param ps
     *         AIML parse state
     *
     * @return sentence with gender ronouns swapped
     */
    private static String gender(Node node, ParseState ps) {
        String result = evalTagContent(node, ps, null);
        result = " " + result + " ";
        result = ps.getChatSession().bot.preProcessor.gender(result);
        return result.trim();
    }

    /**
     * implements {@code <random>} tag
     *
     * @param node
     *         current XML parse node
     * @param ps
     *         AIML parse state
     *
     * @return response randomly selected from the list
     */
    private static String random(Node node, ParseState ps) {
        NodeList childList = node.getChildNodes();
        ArrayList<Node> liList = new ArrayList<>();
        for (int i = 0; i < childList.getLength(); i++)
            if ("li".equals(childList.item(i).getNodeName()))
                liList.add(childList.item(i));
        return evalTagContent(liList.get(new Random().nextInt(liList.size())), ps, null);
    }

    private static String unevaluatedAIML(Node node, ParseState ps) {
        String result = learnEvalTagContent(node, ps);
        return unevaluatedXML(result, node);
    }

    private static String recursLearn(Node node, ParseState ps) {
        String nodeName = node.getNodeName();
        if (nodeName.equals(TEXT))
            return node.getNodeValue();
        else if ("eval".equals(nodeName))
            return evalTagContent(node, ps, null);                // AIML 2.0
        else
            return unevaluatedAIML(node, ps);
    }

    private static String learnEvalTagContent(Node node, ParseState ps) {
        StringBuilder result = new StringBuilder();
        NodeList childList = node.getChildNodes();
        for (int i = 0; i < childList.getLength(); i++) {
            Node child = childList.item(i);
            result.append(recursLearn(child, ps));
        }
        return result.toString();
    }

    private static String learn(Node node, ParseState ps) {                 // learn, learnf AIML 2.0
        NodeList childList = node.getChildNodes();
        String pattern = "";
        String that = "*";
        String template = "";
        for (int i = 0; i < childList.getLength(); i++) {
            if (childList.item(i).getNodeName().equals(CATEGORY)) {
                NodeList grandChildList = childList.item(i).getChildNodes();
                for (int j = 0; j < grandChildList.getLength(); j++) {
                    switch (grandChildList.item(j).getNodeName()) {
                        case PATTERN:
                            pattern = recursLearn(grandChildList.item(j), ps);
                            break;
                        case "that":
                            that = recursLearn(grandChildList.item(j), ps);
                            break;
                        case TEMPLATE:
                            template = recursLearn(grandChildList.item(j), ps);
                            break;
                        default:
                    }
                }
                pattern = pattern.substring("<pattern>".length(), pattern.length() - "</pattern>".length());
                if (template.length() >= "<template></template>".length())
                    template = template.substring(TEMPLATE1.length(), template.length() - TEMPLATE2.length());
                if (that.length() >= "<that></that>".length())
                    that = that.substring("<that>".length(), that.length() - "</that>".length());
                pattern = pattern.toUpperCase();
                that = that.toUpperCase();
                if (MagicBooleans.trace_mode) {
                    log.info("Learn Pattern = " + pattern);
                    log.info("Learn That = " + that);
                    log.info("Learn Template = " + template);
                }
                Category c;
                if ("learn".equals(node.getNodeName()))
                    c = new Category(0, pattern, that, "*", template, MagicStrings.null_aiml_file);
                else {// learnf
                    c = new Category(0, pattern, that, "*", template, MagicStrings.learnf_aiml_file);
                    ps.getChatSession().bot.learnfGraph.addCategory(c);
                }
                ps.getChatSession().bot.brain.addCategory(c);
            }
        }
        return "";
    }

    /**
     * implements {@code <condition> with <loop/>} re-evaluate the conditional statement until the response does not
     * contain {@code <loop/>}
     *
     * @param node
     *         current XML parse node
     * @param ps
     *         AIML parse state
     *
     * @return result of conditional expression
     */
    private static String loopCondition(Node node, ParseState ps) {
        boolean loop = true;
        StringBuilder result = new StringBuilder();
        int loopCnt = 0;
        while (loop && loopCnt < MagicNumbers.max_loops) {
            String loopResult = condition(node, ps);
            if (loopResult.trim().equals(MagicStrings.too_much_recursion))
                return MagicStrings.too_much_recursion;
            if (loopResult.contains("<loop/>")) {
                loopResult = loopResult.replace("<loop/>", "");
                loop = true;
            } else
                loop = false;
            result.append(loopResult);
        }
        if (loopCnt >= MagicNumbers.max_loops)
            result = new StringBuilder(MagicStrings.too_much_looping);
        return result.toString();
    }

    /**
     * implements all 3 forms of the {@code <condition> tag} In AIML 2.0 the conditional may return a {@code <loop/>}
     *
     * @param node
     *         current XML parse node
     * @param ps
     *         AIML parse state
     *
     * @return result of conditional expression
     */
    private static String condition(Node node, ParseState ps) {
        NodeList childList = node.getChildNodes();
        ArrayList<Node> liList = new ArrayList<>();
        String predicate, varName, value;
        Set<String> attributeNames = Utilities.stringSet("name", "var", VALUE);
        // First check if the <condition> has an attribute "name".  If so, get the predicate name.
        predicate = getAttributeOrTagValue(node, ps, "name");
        varName = getAttributeOrTagValue(node, ps, "var");
        // Make a list of all the <li> child nodes:
        for (int i = 0; i < childList.getLength(); i++)
            if ("li".equals(childList.item(i).getNodeName()))
                liList.add(childList.item(i));
        // if there are no <li> nodes, this is a one-shot condition.
        if (liList.isEmpty() && (value = getAttributeOrTagValue(node, ps, VALUE)) != null &&
                predicate != null &&
                ps.getChatSession().predicates.get(predicate).equals(value)) {
            return evalTagContent(node, ps, attributeNames);
        } else if (liList.isEmpty() && (value = getAttributeOrTagValue(node, ps, VALUE)) != null &&
                varName != null &&
                ps.getVars().get(varName).equals(value)) {
            return evalTagContent(node, ps, attributeNames);
        }
        // otherwise this is a <condition> with <li> items:
        else
            for (Node n : liList) {
                String liPredicate = predicate;
                String liVarName = varName;
                if (liPredicate == null)
                    liPredicate = getAttributeOrTagValue(n, ps, "name");
                if (liVarName == null)
                    liVarName = getAttributeOrTagValue(n, ps, "var");
                value = getAttributeOrTagValue(n, ps, VALUE);
                if (value != null) {
                    // if the predicate equals the value, return the <li> item.
                    if (liPredicate != null && (ps.getChatSession().predicates.get(liPredicate).equals(value) || ps.getChatSession().predicates.containsKey(liPredicate) && value.equals("*")))
                        return evalTagContent(n, ps, attributeNames);
                    else if (liVarName != null && (ps.getVars().get(liVarName).equals(value) || ps.getVars().containsKey(liPredicate) && "*".equals(value)))
                        return evalTagContent(n, ps, attributeNames);
                } else  // this is a terminal <li> with no predicate or value, i.e. the default condition.
                    return evalTagContent(n, ps, attributeNames);
            }
        return "";

    }

    /**
     * Recursively descend the XML DOM tree, evaluating AIML and building a response.
     *
     * @param node
     *         current XML parse node
     * @param ps
     *         AIML parse state
     */
    private static String recursEval(Node node, ParseState ps) {
        try {
            String nodeName = node.getNodeName();
            if (nodeName.equals(TEXT))
                return node.getNodeValue();
            else if ("#comment".equals(nodeName)) {
                return "";
            } else if (nodeName.equals(TEMPLATE))
                return evalTagContent(node, ps, null);
            else if ("random".equals(nodeName))
                return random(node, ps);
            else if ("condition".equals(nodeName))
                return loopCondition(node, ps);
            else if ("srai".equals(nodeName))
                return srai(node, ps);
            else if ("sr".equals(nodeName))
                return respond(ps.getLeaf().starBindings.getInputStars().star(0), ps.getThat(), ps.getTopic(), ps.getChatSession(), sraiCount);
            else if ("sraix".equals(nodeName))
                return sraix(node, ps);
            else if ("set".equals(nodeName))
                return set(node, ps);
            else if ("get".equals(nodeName))
                return get(node, ps);
            else if ("map".equals(nodeName))  // AIML 2.0 -- see also <set> in pattern
                return map(node, ps);
            else if ("bot".equals(nodeName))
                return bot(node, ps);
            else if ("id".equals(nodeName))
                return id(ps);
            else if ("size".equals(nodeName))
                return size(ps);
            else if ("vocabulary".equals(nodeName)) // AIML 2.0
                return vocabulary(ps);
            else if ("program".equals(nodeName))
                return program();
            else if ("date".equals(nodeName))
                return date(node, ps);
            else if ("interval".equals(nodeName))
                return interval(node, ps);
            else if ("think".equals(nodeName))
                return think(node, ps);
            else if ("system".equals(nodeName))
                return system(node, ps);
            else if ("explode".equals(nodeName))
                return explode(node, ps);
            else if ("normalize".equals(nodeName))
                return normalize(node, ps);
            else if ("denormalize".equals(nodeName))
                return denormalize(node, ps);
            else if ("uppercase".equals(nodeName))
                return uppercase(node, ps);
            else if ("lowercase".equals(nodeName))
                return lowercase(node, ps);
            else if ("formal".equals(nodeName))
                return formal(node, ps);
            else if ("sentence".equals(nodeName))
                return sentence(node, ps);
            else if ("person".equals(nodeName))
                return person(node, ps);
            else if ("person2".equals(nodeName))
                return person2(node, ps);
            else if ("gender".equals(nodeName))
                return gender(node, ps);
            else if ("star".equals(nodeName))
                return inputStar(node, ps);
            else if ("thatstar".equals(nodeName))
                return thatStar(node, ps);
            else if ("topicstar".equals(nodeName))
                return topicStar(node, ps);
            else if ("that".equals(nodeName))
                return that(node, ps);
            else if ("input".equals(nodeName))
                return input(node, ps);
            else if ("request".equals(nodeName))
                return request(node, ps);
            else if ("response".equals(nodeName))
                return response(node, ps);
            else if ("learn".equals(nodeName) || "learnf".equals(nodeName))
                return learn(node, ps);
            else if (extension != null && extension.extensionTagSet().contains(nodeName))
                return extension.recursEval(node, ps);
            else
                return (genericXML(node, ps));
        } catch (Exception ex) {
            log.error(AN_EXCEPTION_OCCURRED, ex);
            return "";
        }
    }

    /**
     * evaluate an AIML template expression
     *
     * @param template
     *         AIML template contents
     * @param ps
     *         AIML Parse state
     *
     * @return result of evaluating template.
     */
    private static String evalTemplate(String template, ParseState ps) {
        String response = MagicStrings.template_failed;
        try {
            template = TEMPLATE1 + template + TEMPLATE2;
            Node root = DomUtils.parseString(template);
            response = recursEval(root, ps);
        } catch (Exception e) {
            log.error(AN_EXCEPTION_OCCURRED, e);
        }
        return response;
    }
}

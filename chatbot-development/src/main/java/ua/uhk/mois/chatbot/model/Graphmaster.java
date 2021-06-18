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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The AIML Pattern matching algorithm and data structure.
 */

@Log4j2
public class Graphmaster {

    private static final String SET = "<SET>";
    private static final String TOPIC = "<TOPIC>";
    private static final String THAT = "<THAT>";
    private static boolean enableShortCuts;
    private static boolean verbose;
    public final Nodemapper root;
    private final Bot bot;
    private int matchCount;
    private int upgradeCnt;
    private HashSet<String> vocabulary;
    private String resultNote = "";
    private int categoryCnt;
    private int leafCnt;
    private int nodeCnt;
    private long nodeSize;
    private int singletonCnt;
    private int shortCutCnt;
    private int naryCnt;

    /**
     * Constructor
     *
     * @param bot
     *         the bot the graph belongs to.
     */
    public Graphmaster(Bot bot) {
        root = new Nodemapper();
        this.bot = bot;
        vocabulary = new HashSet<>();
    }

    /**
     * Convert input, that and topic to a single sentence having the form {@code input <THAT> that <TOPIC> topic}
     *
     * @param input
     *         input (or input pattern)
     * @param that
     *         that (or that pattern)
     * @param topic
     *         topic (or topic pattern)
     *
     * @return
     */
    public static String inputThatTopic(String input, String that, String topic) {
        return input.trim() + " <THAT> " + that.trim() + " <TOPIC> " + topic.trim();
    }

    /**
     * Recursively find a leaf node given a starting node and a path.
     *
     * @param node
     *         string node
     * @param path
     *         string path
     *
     * @return the leaf node or null if no leaf is found
     */
    private static Nodemapper findNode(Nodemapper node, Path path) {
        if (path == null && node != null) {
            if (verbose)
                log.info("findNode: path is null, returning node " + node.category.inputThatTopic());
            return node;
        } else {
            assert path != null;
            if ("<THAT> * <TOPIC> *".equals(Path.pathToSentence(path).trim()) && node.shortCut && THAT.equals(path.getWord())) {
                if (verbose)
                    log.info("findNode: shortcut, returning " + node.category.inputThatTopic());
                return node;
            } else if (NodemapperOperator.containsKey(node, path.getWord())) {
                if (verbose)
                    log.info("findNode: node contains " + path.getWord());
                Nodemapper nextNode = NodemapperOperator.get(node, path.getWord().toUpperCase());
                return findNode(nextNode, path.getNext());
            } else {
                if (verbose)
                    log.info("findNode: returning null");
                return null;
            }
        }
    }

    private static void setStars(String starWords, int starIndex, String starState, String[] inputStars, String[] thatStars, String[] topicStars) {
        if (starIndex < MagicNumbers.max_stars) {
            starWords = starWords.trim();
            switch (starState) {
                case "inputStar":
                    inputStars[starIndex] = starWords;
                    break;
                case "thatStar":
                    thatStars[starIndex] = starWords;
                    break;
                case "topicStar":
                    topicStars[starIndex] = starWords;
                    break;
                default:
            }
        }
    }

    private static boolean thatStarTopicStar(Path path) {
        String tail = Path.pathToSentence(path).trim();
        return "<THAT> * <TOPIC> *".equals(tail);
    }

    private static void addSets(String type, Nodemapper node) {
        String typeName = Utilities.tagTrim(type, "SET").toLowerCase();
        // AIMLSet aimlSet;
        if (Bot.setMap.containsKey(typeName)) {
            if (node.sets == null)
                node.sets = new ArrayList<>();
            node.sets.add(typeName);
        } else {
            log.info("AIML Set " + typeName + " not found.");
        }
    }

    private static void getCategories(Nodemapper node, ArrayList<Category> categories) {
        if (node == null) {
            return;
        }
        if ((NodemapperOperator.isLeaf(node) || node.shortCut) && node.category != null) {
            categories.add(node.category);   // node.category == null when the category is deleted.
        }
        for (String key : NodemapperOperator.keySet(node)) {
            getCategories(NodemapperOperator.get(node, key), categories);
        }
    }

    /**
     * add an AIML category to this graph.
     *
     * @param category
     *         AIML Category
     */
    public void addCategory(Category category) {
        Path p = Path.sentenceToPath(inputThatTopic(category.getPattern(), category.getThat(), category.getTopic()));
        addPath(p, category);
        categoryCnt++;
    }

    /**
     * add a path to the graph from the root to a Category
     *
     * @param path
     *         Pattern path
     * @param category
     *         AIML category
     */
    private void addPath(Path path, Category category) {
        addPath(root, path, category);

    }

    /**
     * add a Path to the graph from a given node. Shortcuts: Replace all instances of paths "<THAT> * <TOPIC> *" with a
     * direct link to the matching category
     *
     * @param node
     *         starting node in graph
     * @param path
     *         Pattern path to be added
     * @param category
     *         AIML Category
     */
    private void addPath(Nodemapper node, Path path, Category category) {
        if (path == null) {
            node.category = category;
            node.height = 0;
        } else if (enableShortCuts && thatStarTopicStar(path)) {
            node.category = category;
            node.height = Math.min(4, node.height);
            node.shortCut = true;
        } else if (NodemapperOperator.containsKey(node, path.getWord())) {
            if (path.getWord().startsWith(SET))
                addSets(path.getWord(), node);
            Nodemapper nextNode = NodemapperOperator.get(node, path.getWord());
            addPath(nextNode, path.getNext(), category);
            int offset = 1;
            if ("#".equals(path.getWord()) || "^".equals(path.getWord()))
                offset = 0;
            assert nextNode != null;
            node.height = Math.min(offset + nextNode.height, node.height);
        } else {
            Nodemapper nextNode = new Nodemapper();
            if (path.getWord().startsWith(SET)) {
                addSets(path.getWord(), node);
            }
            if (node.key != null) {
                NodemapperOperator.upgrade(node);
                upgradeCnt++;
            }
            NodemapperOperator.put(node, path.getWord(), nextNode);
            addPath(nextNode, path.getNext(), category);
            int offset = 1;
            if ("#".equals(path.getWord()) || "^".equals(path.getWord()))
                offset = 0;
            node.height = Math.min(offset + nextNode.height, node.height);
        }
    }

    /**
     * test if category is already in graph
     *
     * @return true or false
     */
    public boolean existsCategory(Category c) {
        return (findNode(c) != null);
    }

    /**
     * test if category is already in graph
     *
     * @return true or false
     */
    public Nodemapper findNode(Category c) {
        return findNode(c.getPattern(), c.getThat(), c.getTopic());
    }

    /**
     * Given an input pattern, that pattern and topic pattern, find the leaf node associated with this path.
     *
     * @param input
     *         input pattern
     * @param that
     *         that pattern
     * @param topic
     *         topic pattern
     *
     * @return leaf node or null if no matching node is found
     */
    private Nodemapper findNode(String input, String that, String topic) {
        Nodemapper result = findNode(root, Path.sentenceToPath(inputThatTopic(input, that, topic)));
        if (verbose)
            log.info("findNode " + inputThatTopic(input, that, topic) + " " + result);
        return result;
    }

    /**
     * Find the matching leaf node given an input, that state and topic value
     *
     * @param input
     *         client input
     * @param that
     *         bot's last sentence
     * @param topic
     *         current topic
     *
     * @return matching leaf node or null if no match is found
     */
    public Nodemapper match(String input, String that, String topic) {
        Nodemapper n;
        try {
            String inputThatTopic = inputThatTopic(input, that, topic);
            Path p = Path.sentenceToPath(inputThatTopic);
            n = match(p, inputThatTopic);
            if (MagicBooleans.trace_mode) {
                if (n != null) {
                    log.debug("Matched: " + n.category.inputThatTopic() + " " + n.category.getFilename());
                } else
                    log.debug("No match.");
            }
        } catch (Exception ex) {
            log.error("An error occurred.", ex);
            n = null;
        }
        if (MagicBooleans.trace_mode && Chat.matchTrace.length() < MagicNumbers.max_trace_length && n != null) {
            Chat.setMatchTrace(Chat.matchTrace + n.category.inputThatTopic() + "\n");
        }
        return n;
    }

    /**
     * Find the matching leaf node given a path of the form "{@code input <THAT> that <TOPIC> topic}"
     *
     * @param path
     * @param inputThatTopic
     *
     * @return matching leaf node or null if no match is found
     */
    private Nodemapper match(Path path, String inputThatTopic) {
        try {
            String[] inputStars = new String[MagicNumbers.max_stars];
            String[] thatStars = new String[MagicNumbers.max_stars];
            String[] topicStars = new String[MagicNumbers.max_stars];
            String starState = "inputStar";
            String matchTrace = "";
            Nodemapper n = match(path, root, inputThatTopic, starState, 0, inputStars, thatStars, topicStars, matchTrace);
            if (n != null) {
                StarBindings sb = new StarBindings();
                for (int i = 0; inputStars[i] != null && i < MagicNumbers.max_stars; i++)
                    sb.getInputStars().add(inputStars[i]);
                for (int i = 0; thatStars[i] != null && i < MagicNumbers.max_stars; i++)
                    sb.getThatStars().add(thatStars[i]);
                for (int i = 0; topicStars[i] != null && i < MagicNumbers.max_stars; i++)
                    sb.getTopicStars().add(topicStars[i]);
                n.starBindings = sb;
            }
            if (n != null)
                n.category.addMatch(inputThatTopic);
            return n;
        } catch (Exception ex) {
            log.error("An error occurred.", ex);
            return null;
        }
    }

    /**
     * Depth-first search of the graph for a matching leaf node. At each node, the order of search is 1. $WORD  (high
     * priority exact word match) 2. # wildcard  (zero or more word match) 3. _ wildcard (one or more words match) 4.
     * WORD (exact word match) 5. {@code <set></set>} (AIML Set match) 6. shortcut (graph shortcut when that pattern = *
     * and topic pattern = *) 7. ^ wildcard  (zero or more words match) 8. * wildcard (one or more words match)
     *
     * @param path
     *         remaining path to be matched
     * @param node
     *         current search node
     * @param inputThatTopic
     *         original input, that and topic string
     * @param starState
     *         tells whether wildcards are in input pattern, that pattern or topic pattern
     * @param starIndex
     *         index of wildcard
     * @param inputStars
     *         array of input pattern wildcard matches
     * @param thatStars
     *         array of that pattern wildcard matches
     * @param topicStars
     *         array of topic pattern wildcard matches
     * @param matchTrace
     *         trace of match path for debugging purposes
     *
     * @return matching leaf node or null if no match is found
     */
    private Nodemapper match(Path path, Nodemapper node, String inputThatTopic, String starState, int starIndex, String[] inputStars, String[] thatStars, String[] topicStars, String matchTrace) {
        Nodemapper matchedNode;
        matchCount++;
        if ((matchedNode = nullMatch(path, node, matchTrace)) != null)
            return matchedNode;
        else if (path.getLength() < node.height) {
            return null;
        } else if ((matchedNode = dollarMatch(path, node, inputThatTopic, starState, starIndex, inputStars, thatStars, topicStars, matchTrace)) != null)
            return matchedNode;
        else if ((matchedNode = sharpMatch(path, node, inputThatTopic, starState, starIndex, inputStars, thatStars, topicStars, matchTrace)) != null)
            return matchedNode;
        else if ((matchedNode = underMatch(path, node, inputThatTopic, starState, starIndex, inputStars, thatStars, topicStars, matchTrace)) != null)
            return matchedNode;
        else if ((matchedNode = wordMatch(path, node, inputThatTopic, starState, starIndex, inputStars, thatStars, topicStars, matchTrace)) != null)
            return matchedNode;
        else if ((matchedNode = setMatch(path, node, inputThatTopic, starState, starIndex, inputStars, thatStars, topicStars, matchTrace)) != null)
            return matchedNode;
        else if ((matchedNode = shortCutMatch(path, node, thatStars, topicStars, matchTrace)) != null)
            return matchedNode;
        else if ((matchedNode = caretMatch(path, node, inputThatTopic, starState, starIndex, inputStars, thatStars, topicStars, matchTrace)) != null)
            return matchedNode;
        else if ((matchedNode = starMatch(path, node, inputThatTopic, starState, starIndex, inputStars, thatStars, topicStars, matchTrace)) != null)
            return matchedNode;
        else {
            return null;
        }
    }

    /**
     * print out match trace when search fails
     *
     * @param mode
     *         Which mode of search
     * @param trace
     *         Match trace info
     */
    private void fail(String mode, String trace) {
    }

    /**
     * a match is found if the end of the path is reached and the node is a leaf node
     *
     * @param path
     *         remaining path
     * @param node
     *         current search node
     * @param matchTrace
     *         trace of match for debugging purposes
     *
     * @return matching leaf node or null if no match found
     */
    private Nodemapper nullMatch(Path path, Nodemapper node, String matchTrace) {
        if (path == null && node != null && NodemapperOperator.isLeaf(node) && node.category != null)
            return node;
        else {
            fail("null", matchTrace);
            return null;
        }
    }

    private Nodemapper shortCutMatch(Path path, Nodemapper node, String[] thatStars, String[] topicStars, String matchTrace) {
        if (node != null && node.shortCut && THAT.equals(path.getWord()) && node.category != null) {
            String tail = Path.pathToSentence(path).trim();
            String that = tail.substring(tail.indexOf(THAT) + THAT.length(), tail.indexOf(TOPIC)).trim();
            String topic = tail.substring(tail.indexOf(TOPIC) + TOPIC.length()).trim();
            thatStars[0] = that;
            topicStars[0] = topic;
            return node;
        } else {
            fail("shortCut", matchTrace);
            return null;
        }
    }

    private Nodemapper wordMatch(Path path, Nodemapper node, String inputThatTopic, String starState, int starIndex, String[] inputStars, String[] thatStars, String[] topicStars, String matchTrace) {
        Nodemapper matchedNode;
        try {
            String uWord = path.getWord().toUpperCase();
            if (THAT.equals(uWord)) {
                starIndex = 0;
                starState = "thatStar";
            } else if (uWord.equals(TOPIC)) {
                starIndex = 0;
                starState = "topicStar";
            }
            matchTrace += "[" + uWord + "," + uWord + "]";
            if (NodemapperOperator.containsKey(node, uWord) && (matchedNode = match(path.getNext(), NodemapperOperator.get(node, uWord), inputThatTopic, starState, starIndex, inputStars, thatStars, topicStars, matchTrace)) != null) {
                return matchedNode;
            } else {
                fail("word", matchTrace);
                return null;
            }
        } catch (Exception ex) {
            log.info("wordMatch: " + Path.pathToSentence(path) + ": " + ex);
            return null;
        }
    }

    private Nodemapper dollarMatch(Path path, Nodemapper node, String inputThatTopic, String starState, int starIndex, String[] inputStars, String[] thatStars, String[] topicStars, String matchTrace) {
        String uWord = "$" + path.getWord().toUpperCase();
        Nodemapper matchedNode;
        if (NodemapperOperator.containsKey(node, uWord) && (matchedNode = match(path.getNext(), NodemapperOperator.get(node, uWord), inputThatTopic, starState, starIndex, inputStars, thatStars, topicStars, matchTrace)) != null) {
            return matchedNode;
        } else {
            fail("dollar", matchTrace);
            return null;
        }
    }

    private Nodemapper starMatch(Path path, Nodemapper node, String input, String starState, int starIndex, String[] inputStars, String[] thatStars, String[] topicStars, String matchTrace) {
        return wildMatch(path, node, input, starState, starIndex, inputStars, thatStars, topicStars, "*", matchTrace);
    }

    private Nodemapper underMatch(Path path, Nodemapper node, String input, String starState, int starIndex, String[] inputStars, String[] thatStars, String[] topicStars, String matchTrace) {
        return wildMatch(path, node, input, starState, starIndex, inputStars, thatStars, topicStars, "_", matchTrace);
    }

    private Nodemapper caretMatch(Path path, Nodemapper node, String input, String starState, int starIndex, String[] inputStars, String[] thatStars, String[] topicStars, String matchTrace) {
        Nodemapper matchedNode;
        matchedNode = zeroMatch(path, node, input, starState, starIndex, inputStars, thatStars, topicStars, "^", matchTrace);
        return matchedNode != null ? matchedNode : wildMatch(path, node, input, starState, starIndex, inputStars, thatStars, topicStars, "^", matchTrace);
    }

    private Nodemapper sharpMatch(Path path, Nodemapper node, String input, String starState, int starIndex, String[] inputStars, String[] thatStars, String[] topicStars, String matchTrace) {
        Nodemapper matchedNode;
        matchedNode = zeroMatch(path, node, input, starState, starIndex, inputStars, thatStars, topicStars, "#", matchTrace);
        return matchedNode != null ? matchedNode : wildMatch(path, node, input, starState, starIndex, inputStars, thatStars, topicStars, "#", matchTrace);
    }

    private Nodemapper zeroMatch(Path path, Nodemapper node, String input, String starState, int starIndex,
                                 String[] inputStars, String[] thatStars, String[] topicStars, String wildcard, String matchTrace) {
        matchTrace += "[" + wildcard + ",]";
        if (path != null && NodemapperOperator.containsKey(node, wildcard)) {
            setStars(bot.properties.get(MagicStrings.null_star), starIndex, starState, inputStars, thatStars, topicStars);
            Nodemapper nextNode = NodemapperOperator.get(node, wildcard);
            return match(path, nextNode, input, starState, starIndex + 1, inputStars, thatStars, topicStars, matchTrace);
        } else {
            fail("zero " + wildcard, matchTrace);
            return null;
        }

    }

    private Nodemapper wildMatch(Path path, Nodemapper node, String input, String starState, int starIndex,
                                 String[] inputStars, String[] thatStars, String[] topicStars, String wildcard, String matchTrace) {
        Nodemapper matchedNode;
        if (THAT.equals(path.getWord()) || path.getWord().equals(TOPIC)) {
            fail("wild1 " + wildcard, matchTrace);
            return null;
        }
        try {
            if (NodemapperOperator.containsKey(node, wildcard)) {
                matchTrace += "[" + wildcard + "," + path.getWord() + "]";
                String currentWord;
                StringBuilder starWords;
                Path pathStart;
                currentWord = path.getWord();
                starWords = new StringBuilder(currentWord + " ");
                pathStart = path.getNext();
                Nodemapper nextNode = NodemapperOperator.get(node, wildcard);
                if (NodemapperOperator.isLeaf(nextNode) && !nextNode.shortCut) {
                    matchedNode = nextNode;
                    starWords = new StringBuilder(Path.pathToSentence(path));
                    setStars(starWords.toString(), starIndex, starState, inputStars, thatStars, topicStars);
                    return matchedNode;
                } else {
                    for (path = pathStart; path != null && !THAT.equals(currentWord) && !currentWord.equals(TOPIC); path = path.getNext()) {
                        matchTrace += "[" + wildcard + "," + path.getWord() + "]";
                        if ((matchedNode = match(path, nextNode, input, starState, starIndex + 1, inputStars, thatStars, topicStars, matchTrace)) != null) {
                            setStars(starWords.toString(), starIndex, starState, inputStars, thatStars, topicStars);
                            return matchedNode;
                        } else {
                            currentWord = path.getWord();
                            starWords.append(currentWord).append(" ");
                        }
                    }
                    fail("wild2 " + wildcard, matchTrace);
                    return null;
                }
            }
        } catch (Exception ex) {
            log.info("wildMatch: " + Path.pathToSentence(path) + ": " + ex);
        }
        fail("wild3 " + wildcard, matchTrace);
        return null;
    }

    private Nodemapper setMatch(Path path, Nodemapper node, String input, String starState, int starIndex, String[] inputStars, String[] thatStars, String[] topicStars, String matchTrace) {
        if (node.sets == null || THAT.equals(path.getWord()) || path.getWord().equals(TOPIC))
            return null;
        for (String setName : node.sets) {
            Nodemapper nextNode = NodemapperOperator.get(node, SET + setName.toUpperCase() + "</SET>");
            AIMLSet aimlSet = Bot.setMap.get(setName);
            Nodemapper matchedNode;
            String currentWord = path.getWord();
            StringBuilder starWords = new StringBuilder(currentWord + " ");
            int length = 1;
            matchTrace += "[<set>" + setName + "</set>," + path.getWord() + "]";
            for (Path qath = path.getNext(); qath != null && !THAT.equals(currentWord) && !currentWord.equals(TOPIC) && length <= aimlSet.maxLength; qath = qath.getNext()) {
                String phrase = bot.preProcessor.normalize(starWords.toString().trim()).toUpperCase();
                if (aimlSet.contains(phrase) && (matchedNode = match(qath, nextNode, input, starState, starIndex + 1, inputStars, thatStars, topicStars, matchTrace)) != null) {
                    setStars(starWords.toString(), starIndex, starState, inputStars, thatStars, topicStars);
                    return matchedNode;
                } else {
                    length = length + 1;
                    currentWord = qath.getWord();
                    starWords.append(currentWord).append(" ");
                }
            }
        }
        fail("set", matchTrace);
        return null;
    }

    public List<Category> getCategories() {
        ArrayList<Category> categories = new ArrayList<>();
        getCategories(root, categories);
        return categories;
    }

    public void nodeStats() {
        leafCnt = 0;
        nodeCnt = 0;
        nodeSize = 0;
        singletonCnt = 0;
        shortCutCnt = 0;
        naryCnt = 0;
        nodeStatsGraph(root);
        resultNote = nodeCnt + " nodes " + singletonCnt + " singletons " + leafCnt + " leaves " + shortCutCnt + " shortcuts " + naryCnt + " n-ary " + nodeSize + " branches " + (float) nodeSize / (float) nodeCnt + " average branching ";
        log.info(resultNote);
    }

    private void nodeStatsGraph(Nodemapper node) {
        if (node != null) {
            nodeCnt++;
            nodeSize += NodemapperOperator.size(node);
            if (NodemapperOperator.size(node) == 1)
                singletonCnt += 1;
            if (NodemapperOperator.isLeaf(node) && !node.shortCut) {
                leafCnt++;
            }
            if (NodemapperOperator.size(node) > 1)
                naryCnt += 1;
            if (node.shortCut) {
                shortCutCnt += 1;
            }
            for (String key : NodemapperOperator.keySet(node)) {
                nodeStatsGraph(NodemapperOperator.get(node, key));
            }
        }
    }

    public Set<String> getVocabulary() {
        vocabulary = new HashSet<>();
        getBrainVocabulary(root);
        for (AIMLSet strings : Bot.setMap.values())
            vocabulary.addAll(strings);
        return vocabulary;
    }

    private void getBrainVocabulary(Nodemapper node) {
        if (node != null) {
            for (String key : NodemapperOperator.keySet(node)) {
                vocabulary.add(key);
                getBrainVocabulary(NodemapperOperator.get(node, key));
            }
        }
    }
}

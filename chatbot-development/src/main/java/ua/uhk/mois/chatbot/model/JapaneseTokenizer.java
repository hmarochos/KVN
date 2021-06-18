package ua.uhk.mois.chatbot.model;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.reduls.sanmoku.Morpheme;
import net.reduls.sanmoku.Tagger;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Tokenize a Japanese language input by inserting spaces between words
 * <p>
 * see http://atilika.org/
 */

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JapaneseTokenizer {

    static final Pattern tagPattern = Pattern.compile("(<.*>.*</.*>)|(<.*/>)");

    /**
     * Tokenize a fragment of the input that contains only text
     *
     * @param fragment
     *         fragment of input containing only text and no XML tags
     *
     * @return tokenized fragment
     */
    public static String buildFragment(String fragment) {

        StringBuilder result = new StringBuilder();
        for (Morpheme e : Tagger.parse(fragment)) {
            result.append(e.surface).append(" ");
        }
        return result.toString().trim();
    }

    /**
     * Morphological analysis of an input sentence that contains an AIML pattern.
     *
     * @param sentence
     *
     * @return morphed sentence with one space between words, preserving XML markup and AIML $ operation
     */
    public static String morphSentence(String sentence) {
        if (!MagicBooleans.jp_morphological_analysis)
            return sentence;
        StringBuilder result = new StringBuilder();
        Matcher matcher = tagPattern.matcher(sentence);
        while (matcher.find()) {
            int i = matcher.start();
            int j = matcher.end();
            String prefix, tag;
            prefix = i > 0 ? sentence.substring(0, i - 1) : "";
            tag = sentence.substring(i, j);
            result.append(" ").append(buildFragment(prefix)).append(" ").append(tag);
            sentence = j < sentence.length() ? sentence.substring(j) : "";
        }
        result.append(" ").append(buildFragment(sentence));
        while (result.toString().contains("$ "))
            result = new StringBuilder(result.toString().replace("$ ", "$"));
        while (result.toString().contains("  "))
            result = new StringBuilder(result.toString().replace("  ", " "));
        return result.toString().trim();
    }
}

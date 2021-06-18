package ua.uhk.mois.chatbot.model;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ResourceFilePaths {

    static final String AIML_ROOT_PATH = "bots/super/aiml/";
    static final String[] AIML_FILE_ROOT_PATHS = {
            "bot_profile.aiml",
            "client_profile.aiml",
            "contactaction.aiml",
            "date.aiml",
            "dialog.aiml",
            "familiar.aiml",
            "help.aiml",
            "inappropriate.aiml",
            "inquiry.aiml",
            "insults.aiml",
            "ontology.aiml",
            "oob.aiml",
            "personality.aiml",
            "picture.aiml",
            "profanity.aiml",
            "reductions1.aiml",
            "reductions_update.aiml",
            "save.txt",
            "sraix.aiml",
            "testjp.aiml",
            "that.aiml",
            "train.aiml",
            "udc.aiml",
            "update.aiml",
            "update.save",
            "utilities.aiml"
    };

    static final String AIMLIF_ROOT_PATH = "bots/super/aimlif/";
    static final String[] AIMLIF_FILE_ROOT_PATHS = {
            "bot_profile.aiml.csv",
            "client_profile.aiml.csv",
            "contactaction.aiml.csv",
            "date.aiml.csv",
            "deleted.aiml.csv",
            "dialog.aiml.csv",
            "familiar.aiml.csv",
            "help.aiml.csv",
            "inappropriate.aiml.csv",
            "inquiry.aiml.csv",
            "insults.aiml.csv",
            "learnf.aiml.csv",
            "ontology.aiml.csv",
            "oob.aiml.csv",
            "personality.aiml.csv",
            "picture.aiml.csv",
            "profanity.aiml.csv",
            "reductions1.aiml.csv",
            "reductions_update.aiml.csv",
            "sraix.aiml.csv",
            "testjp.aiml.csv",
            "that.aiml.csv",
            "train.aiml.csv",
            "udc.aiml.csv",
            "unfinished.aiml.csv",
            "update.aiml.csv",
            "utilities.aiml.csv"
    };

    static final String CONFIG_ROOT_PATH = "bots/super/config/";
    static final String CONFIG_ROOT_PATH_NORMAL = "normal.txt";
    static final String CONFIG_ROOT_PATH_DENORMAL = "denormal.txt";
    static final String CONFIG_ROOT_PATH_PERSON = "person.txt";
    static final String CONFIG_ROOT_PATH_PERSON2 = "person2.txt";
    static final String CONFIG_ROOT_PATH_GENDER = "gender.txt";
    static final String CONFIG_ROOT_PATH_PROPERTIES = "properties.txt";
    static final String CONFIG_ROOT_PATH_PREDICATES = "predicates.txt";

    static final String MAPS_ROOT_PATH = "bots/super/maps/";
    static final String[] MAPS_FILE_ROOT_PATHS = {
            "familiarpredicate.txt",
            "gendername.external",
            "gendername.txt",
            "number2ordinal.txt",
            "ordinal2number.txt",
            "profile2predicate.txt",
            "verb2sp2verb1sp.txt"
    };

    static final String SETS_ROOT_PATH = "bots/super/sets/";
    static final String[] SETS_FILE_ROOT_PATHS = {
            "bird.txt",
            "color.txt",
            "digit.txt",
            "familiarname.txt",
            "fastfood.txt",
            "language.txt",
            "mammal.txt",
            "mammalfeature.txt",
            "name.external",
            "name.txt",
            "ordinal.txt",
            "place.txt",
            "preposition.txt",
            "profile.txt",
            "verb2sp.txt",
            "verb2st.txt"
    };

}

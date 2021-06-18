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

import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static ua.uhk.mois.chatbot.model.ResourceFilePaths.AIMLIF_ROOT_PATH;
import static ua.uhk.mois.chatbot.model.ResourceFilePaths.AIML_ROOT_PATH;
import static ua.uhk.mois.chatbot.model.ResourceFilePaths.CONFIG_ROOT_PATH;
import static ua.uhk.mois.chatbot.model.ResourceFilePaths.CONFIG_ROOT_PATH_PROPERTIES;
import static ua.uhk.mois.chatbot.model.ResourceFilePaths.SETS_FILE_ROOT_PATHS;
import static ua.uhk.mois.chatbot.model.ResourceFilePaths.SETS_ROOT_PATH;
import static ua.uhk.mois.chatbot.utils.IOUtils.getResourceInputStream;

/**
 * Class representing the AIML bot
 */

@Log4j2
public class Bot {

    public static final String PROBLEM_LOADING = "Problem loading '";
    public static final String AN_ERROR_OCCURRED = "An error occurred.";
    public static final String LOAD_FILE = "Load file {}";
    protected static final HashMap<String, AIMLSet> setMap = new HashMap<>();
    protected static final HashMap<String, AIMLMap> mapMap = new HashMap<>();
    public static List<Category> suggestedCategories;
    private static Graphmaster unfinishedGraph;
    private static int leafPatternCnt;
    private static int starPatternCnt;
    public final Properties properties = new Properties();
    public final PreProcessor preProcessor;
    public final Graphmaster brain;
    public final Graphmaster learnfGraph;
    public final Graphmaster patternGraph;
    private final Graphmaster inputGraph;
    private final Graphmaster deletedGraph;
    @Getter
    private final String name;

    /**
     * Constructor (default action, default path, default bot name)
     */
    public Bot() {
        this(MagicStrings.default_bot);
    }

    /**
     * Constructor (default action, default path)
     *
     * @param name
     */
    public Bot(String name) {
        this(name, MagicStrings.root_path);
    }

    /**
     * Constructor (default action)
     *
     * @param name
     * @param path
     */
    public Bot(String name, String path) {
        this(name, path, "auto");
    }

    /**
     * Constructor
     *
     * @param name
     *         name of bot
     * @param path
     *         root path of Program AB
     * @param action
     *         Program AB action
     */
    public Bot(String name, String path, String action) {
        this.name = name;
        setAllPaths(path, name);
        brain = new Graphmaster(this);
        inputGraph = new Graphmaster(this);
        learnfGraph = new Graphmaster(this);
        deletedGraph = new Graphmaster(this);
        patternGraph = new Graphmaster(this);
        unfinishedGraph = new Graphmaster(this);
        suggestedCategories = new ArrayList<>();
        preProcessor = new PreProcessor();
        addProperties();
        addAIMLSets();
        addAIMLMaps();
        AIMLSet number = new AIMLSet(MagicStrings.natural_number_set_name);
        setMap.put(MagicStrings.natural_number_set_name, number);
        AIMLMap successor = new AIMLMap(MagicStrings.map_successor);
        mapMap.put(MagicStrings.map_successor, successor);
        AIMLMap predecessor = new AIMLMap(MagicStrings.map_predecessor);
        mapMap.put(MagicStrings.map_predecessor, predecessor);
        Date aimlDate = new Date(new File(MagicStrings.aiml_path).lastModified());
        Date aimlIFDate = new Date(new File(MagicStrings.aimlif_path).lastModified());
        log.info("AIML modified {} AIMLIF modified {}", aimlDate, aimlIFDate);
        readDeletedIFCategories();
        readUnfinishedIFCategories();
        MagicStrings.pannous_api_key = Utilities.getPannousAPIKey();
        MagicStrings.pannous_login = Utilities.getPannousLogin();
        if ("aiml2csv".equals(action))
            addCategoriesFromAIML();
        else if ("csv2aiml".equals(action))
            addCategoriesFromAIMLIF();
        else if (aimlDate.after(aimlIFDate)) {
            log.info("AIML modified after AIMLIF");
            addCategoriesFromAIML();
            writeAIMLIFFiles();
        } else {
            addCategoriesFromAIMLIF();
            if (brain.getCategories().isEmpty()) {
                log.info("No AIMLIF Files found.  Looking for AIML");
                addCategoriesFromAIML();
            }
        }
        log.info("--> Bot {} {} completed {} deleted {} unfinished",
                 name, brain.getCategories().size(), deletedGraph.getCategories().size(), unfinishedGraph.getCategories().size());
    }

    /**
     * Set all directory path variables for this bot
     *
     * @param root
     *         root directory of Program AB
     * @param name
     *         name of bot
     */
    public static void setAllPaths(String root, String name) {
        MagicStrings.bot_path = root + "/src/main/resources/bots";
        MagicStrings.bot_name_path = MagicStrings.bot_path + File.separator + name;
        log.info("Name = {} Path = {}", name, MagicStrings.bot_name_path);
        MagicStrings.aiml_path = MagicStrings.bot_name_path + "/aiml";
        MagicStrings.aimlif_path = MagicStrings.bot_name_path + "/aimlif";
        MagicStrings.config_path = MagicStrings.bot_name_path + "/config";
        MagicStrings.log_path = MagicStrings.bot_name_path + "/logs";
        MagicStrings.sets_path = MagicStrings.bot_name_path + "/sets";
        MagicStrings.maps_path = MagicStrings.bot_name_path + "/maps";
        log.info(MagicStrings.root_path);
        log.info(MagicStrings.bot_path);
        log.info(MagicStrings.bot_name_path);
        log.info(MagicStrings.aiml_path);
        log.info(MagicStrings.aimlif_path);
        log.info(MagicStrings.config_path);
        log.info(MagicStrings.log_path);
        log.info(MagicStrings.sets_path);
        log.info(MagicStrings.maps_path);
    }

    /**
     * Load all AIML Sets
     */
    static void addAIMLSets() {
        Timer timer = new Timer();
        timer.start();

        // Directory path here
        log.info("Loading AIML Sets files from '{}'", SETS_ROOT_PATH);
        for (String setFileName : SETS_FILE_ROOT_PATHS) {
            String fileName = setFileName.substring(0, setFileName.length() - ".txt".length());
            log.info("Read AIML Set {}", fileName);
            AIMLSet aimlSet = new AIMLSet(fileName);
            aimlSet.readAIMLSet(SETS_ROOT_PATH + setFileName);
            setMap.put(fileName, aimlSet);
        }
    }

    /**
     * Load all AIML Maps
     */
    static void addAIMLMaps() {
        Timer timer = new Timer();
        timer.start();

        log.info("Loading AIML Map files from '{}'", ResourceFilePaths.MAPS_ROOT_PATH);
        for (String mapsFileRootPath : ResourceFilePaths.MAPS_FILE_ROOT_PATHS) {
            log.info(LOAD_FILE, mapsFileRootPath);
            if (mapsFileRootPath.toUpperCase().endsWith(".txt".toUpperCase())) {
                log.info(mapsFileRootPath);
                String mapName = mapsFileRootPath.substring(0, mapsFileRootPath.length() - ".txt".length());
                log.info("Read AIML Map " + mapName);
                AIMLMap aimlMap = new AIMLMap(mapName);
                aimlMap.readAIMLMap();
                mapMap.put(mapName, aimlMap);
            }
        }
    }

    /**
     * read categories from specified AIMLIF file into specified graph
     *
     * @param graph
     *         Graphmaster to store categories
     * @param fileName
     *         file name of AIMLIF file
     */
    public static void readCertainIFCategories(Graphmaster graph, String fileName) {
        List<Category> deletedCategories = readIFCategories(AIMLIF_ROOT_PATH + fileName + MagicStrings.aimlif_file_suffix);
        for (Category d : deletedCategories)
            graph.addCategory(d);
        log.info("readCertainIFCategories {} categories from {}", graph.getCategories().size(), fileName + MagicStrings.aimlif_file_suffix);
    }

    /**
     * write certain specified categories as AIMLIF files
     *
     * @param graph
     *         the Graphmaster containing the categories to write
     * @param file
     *         the destination AIMLIF file
     */
    public static void writeCertainIFCategories(Graphmaster graph, String file) {
        if (MagicBooleans.trace_mode)
            log.info("writeCertainIFCaegories " + file + " size= " + graph.getCategories().size());
        writeIFCategories(graph.getCategories(), file + MagicStrings.aimlif_file_suffix);
        File dir = new File(MagicStrings.aimlif_path);
        dir.setLastModified(new Date().getTime());
    }

    /**
     * write categories to AIMLIF file
     *
     * @param cats
     *         array list of categories
     * @param filename
     *         AIMLIF filename
     */
    public static void writeIFCategories(List<Category> cats, String filename) {
        File existsPath = new File(MagicStrings.aimlif_path);
        if (existsPath.exists()) {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(MagicStrings.aimlif_path + "/" + filename))) {
                for (Category category : cats) {
                    bw.write(Category.categoryToIF(category));
                    bw.newLine();
                }
            } catch (IOException ex) {
                log.error(AN_ERROR_OCCURRED, ex);
            }
        }
    }

    /**
     * read AIMLIF categories from a file into bot brain
     *
     * @param filename
     *         name of AIMLIF file
     *
     * @return array list of categories read
     */
    public static List<Category> readIFCategories(String filename) {
        List<Category> categories = new ArrayList<>();
        // Open the file that is the first
        // command line parameter and get the object
        try (BufferedReader br = new BufferedReader(new InputStreamReader(getResourceInputStream(filename)))) {
            String strLine;
            //Read File Line By Line
            while ((strLine = br.readLine()) != null) {
                try {
                    Category c = Category.ifToCategory(strLine);
                    categories.add(c);
                } catch (Exception ex) {
                    log.warn("Invalid AIMLIF in " + filename + " line " + strLine, ex);
                }
            }
        } catch (Exception e) {//Catch exception if any
            log.error("Cannot read IF Categories from '" + filename + "': " + e, e);
        }
        return categories;
    }

    /**
     * read unfinished categories from AIMLIF file
     */
    public static void readUnfinishedIFCategories() {
        readCertainIFCategories(unfinishedGraph, MagicStrings.unfinished_aiml_file);
    }

    /**
     * write unfinished categories to AIMLIF file
     */
    public static void writeUnfinishedIFCategories() {
        writeCertainIFCategories(unfinishedGraph, MagicStrings.unfinished_aiml_file);
    }

    /**
     * add an array list of categories with a specific file name
     *
     * @param file
     *         name of AIML file
     * @param moreCategories
     *         list of categories
     */
    void addMoreCategories(String file, List<Category> moreCategories) {
        if (file.contains(MagicStrings.deleted_aiml_file)) {
            for (Category c : moreCategories) {
                deletedGraph.addCategory(c);
            }
        } else if (file.contains(MagicStrings.unfinished_aiml_file)) {
            for (Category c : moreCategories) {
                if (brain.findNode(c) == null)
                    unfinishedGraph.addCategory(c);
                else
                    log.info("unfinished {} found in brain", c.inputThatTopic());
            }
        } else if (file.contains(MagicStrings.learnf_aiml_file)) {
            log.info("Reading Learnf file");
            for (Category c : moreCategories) {
                brain.addCategory(c);
                learnfGraph.addCategory(c);
                patternGraph.addCategory(c);
            }
        } else {
            for (Category c : moreCategories) {
                brain.addCategory(c);
                patternGraph.addCategory(c);
            }
        }
    }

    /**
     * Load all brain categories from AIML directory
     */
    void addCategoriesFromAIML() {
        Timer timer = new Timer();
        timer.start();

        log.info("Loading AIML Map files from '{}'", AIML_ROOT_PATH);
        for (String aimlFileRootPath : ResourceFilePaths.AIML_FILE_ROOT_PATHS) {
            log.info(LOAD_FILE, aimlFileRootPath);
            if (aimlFileRootPath.toUpperCase().endsWith(".aiml".toUpperCase())) {
                try {
                    List<Category> moreCategories = AIMLProcessor.aimlToCategories(AIML_ROOT_PATH, aimlFileRootPath);
                    addMoreCategories(aimlFileRootPath, moreCategories);
                } catch (Exception iex) {
                    log.error(PROBLEM_LOADING + aimlFileRootPath + "': " + iex, iex);
                }
            }
        }

        log.info("Loaded {} categories in {} sec", brain.getCategories().size(), timer.elapsedTimeSecs());
    }

    /**
     * load all brain categories from AIMLIF directory
     */
    void addCategoriesFromAIMLIF() {
        Timer timer = new Timer();
        timer.start();

        log.info("Loading AIML (if) files from '{}'", AIMLIF_ROOT_PATH);
        for (String aimlifFileRootPath : ResourceFilePaths.AIMLIF_FILE_ROOT_PATHS) {
            log.info(LOAD_FILE, aimlifFileRootPath);
            if (aimlifFileRootPath.toUpperCase().endsWith(MagicStrings.aimlif_file_suffix.toUpperCase())) {
                try {
                    List<Category> moreCategories = readIFCategories(AIMLIF_ROOT_PATH + aimlifFileRootPath);
                    addMoreCategories(aimlifFileRootPath, moreCategories);
                } catch (Exception iex) {
                    log.error(PROBLEM_LOADING + aimlifFileRootPath + "': " + iex, iex);
                }
            }
        }

        log.info("Loaded {} categories in {} sec", brain.getCategories().size(), timer.elapsedTimeSecs());
    }

    /**
     * read deleted categories from AIMLIF file
     */
    public void readDeletedIFCategories() {
        readCertainIFCategories(deletedGraph, MagicStrings.deleted_aiml_file);
    }

    /**
     * update unfinished categories removing any categories that have been finished
     */
    public void updateUnfinishedCategories() {
        List<Category> unfinished = unfinishedGraph.getCategories();
        unfinishedGraph = new Graphmaster(this);
        for (Category c : unfinished) {
            if (!brain.existsCategory(c))
                unfinishedGraph.addCategory(c);
        }
    }

    /**
     * write all AIML and AIMLIF categories
     */
    public void writeQuit() {
        writeAIMLIFFiles();
        log.info("Wrote AIMLIF Files");
        writeAIMLFiles();
        log.info("Wrote AIML Files");
        writeDeletedIFCategories();
        updateUnfinishedCategories();
        writeUnfinishedIFCategories();
    }

    /**
     * write deleted categories to AIMLIF file
     */
    public void writeDeletedIFCategories() {
        writeCertainIFCategories(deletedGraph, MagicStrings.deleted_aiml_file);
    }

    /**
     * write learned categories to AIMLIF file
     */
    public void writeLearnfIFCategories() {
        writeCertainIFCategories(learnfGraph, MagicStrings.learnf_aiml_file);
    }

    /**
     * Write all AIMLIF files from bot brain
     */
    public void writeAIMLIFFiles() {
        log.info("writeAIMLIFFiles");
        HashMap<String, BufferedWriter> fileMap = new HashMap<>();
        if (!deletedGraph.getCategories().isEmpty())
            writeDeletedIFCategories();
        List<Category> brainCategories = brain.getCategories();
        brainCategories.sort(Category.CATEGORY_NUMBER_COMPARATOR);
        for (Category c : brainCategories) {
            try {
                BufferedWriter bw;
                String fileName = c.getFilename();
                if (fileMap.containsKey(fileName))
                    bw = fileMap.get(fileName);
                else {
                    bw = new BufferedWriter(new FileWriter(MagicStrings.aimlif_path + "/" + fileName + MagicStrings.aimlif_file_suffix));
                    fileMap.put(fileName, bw);

                }
                bw.write(Category.categoryToIF(c));
                bw.newLine();
            } catch (Exception ex) {
                log.error(AN_ERROR_OCCURRED, ex);
            }
        }
        for (BufferedWriter bw : fileMap.values()) {
            //Close the bw
            try {
                if (bw != null) {
                    bw.flush();
                    bw.close();
                }
            } catch (IOException ex) {
                log.error(AN_ERROR_OCCURRED, ex);
            }

        }
        File dir = new File(MagicStrings.aimlif_path);
        dir.setLastModified(new Date().getTime());
    }

    /**
     * Write all AIML files.  Adds categories for BUILD and DEVELOPMENT ENVIRONMENT
     */
    public void writeAIMLFiles() {
        HashMap<String, BufferedWriter> fileMap = new HashMap<>();
        Category b = new Category(0, "BUILD", "*", "*", new Date().toString(), "update.aiml");
        brain.addCategory(b);
        b = new Category(0, "DELEVLOPMENT ENVIRONMENT", "*", "*", MagicStrings.programNameVersion, "update.aiml");
        brain.addCategory(b);
        List<Category> brainCategories = brain.getCategories();
        brainCategories.sort(Category.CATEGORY_NUMBER_COMPARATOR);
        for (Category c : brainCategories) {

            if (!c.getFilename().equals(MagicStrings.null_aiml_file))
                try {
                    BufferedWriter bw;
                    String fileName = c.getFilename();
                    if (fileMap.containsKey(fileName))
                        bw = fileMap.get(fileName);
                    else {
                        String copyright = Utilities.getCopyright(this, fileName);
                        bw = new BufferedWriter(new FileWriter(MagicStrings.aiml_path + "/" + fileName));
                        fileMap.put(fileName, bw);
                        bw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "\n" +
                                         "<aiml>\n");
                        bw.write(copyright);
                    }
                    bw.write(Category.categoryToAIML(c) + "\n");
                } catch (Exception ex) {
                    log.error(AN_ERROR_OCCURRED, ex);
                }
        }
        for (BufferedWriter bw : fileMap.values()) {
            //Close the bw
            try {
                if (bw != null) {
                    bw.write("</aiml>\n");
                    bw.flush();
                    bw.close();
                }
            } catch (IOException ex) {
                log.error(AN_ERROR_OCCURRED, ex);
            }

        }
        File dir = new File(MagicStrings.aiml_path);
        dir.setLastModified(new Date().getTime());
    }

    /**
     * load bot properties
     */
    void addProperties() {
        try {
            properties.getProperties(CONFIG_ROOT_PATH + CONFIG_ROOT_PATH_PROPERTIES);
        } catch (Exception ex) {
            log.error(AN_ERROR_OCCURRED, ex);
        }
    }

    /**
     * find suggested patterns in a graph of inputs
     */
    public void findPatterns() {
        findPatterns(inputGraph.root, "");
        log.info("{} Leaf Patterns {} Star Patterns", leafPatternCnt, starPatternCnt);
    }

    /**
     * find patterns recursively
     *
     * @param node
     *         current graph node
     * @param partialPatternThatTopic
     *         partial pattern path
     */
    void findPatterns(Nodemapper node, String partialPatternThatTopic) {
        if (NodemapperOperator.isLeaf(node) && node.category.getActivationCnt() > MagicNumbers.node_activation_cnt) {
            leafPatternCnt++;
            try {
                String categoryPatternThatTopic;
                categoryPatternThatTopic = node.shortCut ? partialPatternThatTopic + " <THAT> * <TOPIC> *" : partialPatternThatTopic;
                Category c = new Category(0, categoryPatternThatTopic, MagicStrings.blank_template, MagicStrings.unknown_aiml_file);
                if (!brain.existsCategory(c) && !deletedGraph.existsCategory(c) && !unfinishedGraph.existsCategory(c)) {
                    patternGraph.addCategory(c);
                    suggestedCategories.add(c);
                }
            } catch (Exception e) {
                log.error(AN_ERROR_OCCURRED, e);
            }
        }
        if (NodemapperOperator.size(node) > MagicNumbers.node_size) {
            starPatternCnt++;
            try {
                Category c = new Category(0, partialPatternThatTopic + " * <THAT> * <TOPIC> *", MagicStrings.blank_template, MagicStrings.unknown_aiml_file);
                if (!brain.existsCategory(c) && !deletedGraph.existsCategory(c) && !unfinishedGraph.existsCategory(c)) {
                    patternGraph.addCategory(c);
                    suggestedCategories.add(c);
                }
            } catch (Exception e) {
                log.error(AN_ERROR_OCCURRED, e);
            }
        }
        for (String key : NodemapperOperator.keySet(node)) {
            Nodemapper value = NodemapperOperator.get(node, key);
            findPatterns(value, partialPatternThatTopic + " " + key);
        }
    }

    /**
     * classify inputs into matching categories
     *
     * @param filename
     *         file containing sample normalized inputs
     */
    public void classifyInputs(String filename) {
        // Get the object
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filename)))) {
            String strLine;
            //Read File Line By Line
            while ((strLine = br.readLine()) != null) {
                // Print the content on the console
                if (strLine.startsWith("Human: "))
                    strLine = strLine.substring("Human: ".length());
                Nodemapper match = patternGraph.match(strLine, "unknown", "unknown");
                match.category.incrementActivationCnt();
            }
        } catch (Exception e) {
            log.error("Cannot classify inputs from '" + filename + "': " + e, e);
        }
    }

    /**
     * read sample inputs from filename, turn them into Paths, and add them to the graph.
     *
     * @param filename
     *         file containing sample inputs
     */
    public void graphInputs(String filename) {
        // Open the file that is the first
        // command line parameter and get the object
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filename)))) {
            String strLine;
            //Read File Line By Line
            while ((strLine = br.readLine()) != null) {
                Category c = new Category(0, strLine, "*", "*", "nothing", MagicStrings.unknown_aiml_file);
                Nodemapper node = inputGraph.findNode(c);
                if (node == null) {
                    inputGraph.addCategory(c);
                    c.incrementActivationCnt();
                } else
                    node.category.incrementActivationCnt();
            }
        } catch (Exception e) {//Catch exception if any
            log.error("Cannot graph inputs from '" + filename + "': " + e, e);
        }
    }
}

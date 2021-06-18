package ua.uhk.mois.chatbot.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

@Log4j2
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DomUtils {

    private static DocumentBuilderFactory dbFactory;

    private static DocumentBuilderFactory getDbFactoryInstance() throws ParserConfigurationException {
        if (dbFactory == null) {
            dbFactory = DocumentBuilderFactory.newInstance();

            // https://stackoverflow.com/questions/56777287/how-to-fix-disable-xml-external-entity-xxe-processing-vulnerabilities-in-jav
            dbFactory.setAttribute(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            dbFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            dbFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            dbFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            dbFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            dbFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            dbFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            dbFactory.setXIncludeAware(false);
            dbFactory.setExpandEntityReferences(false);
        }
        return dbFactory;
    }

    public static Node parseFile(String fileName) throws IOException, ParserConfigurationException, SAXException {
        DocumentBuilderFactory dbFactory = getDbFactoryInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        // from AIMLProcessor.evalTemplate and AIMLProcessor.validTemplate:
        //   dbFactory.setIgnoringComments(true); // fix this
        Document doc = dBuilder.parse(IOUtils.getResourceInputStream(fileName));
        doc.getDocumentElement().normalize();
        return doc.getDocumentElement();
    }

    public static Node parseString(String string) throws IOException, ParserConfigurationException, SAXException {
        InputStream is = new ByteArrayInputStream(string.getBytes(StandardCharsets.UTF_16));

        DocumentBuilderFactory dbFactory = getDbFactoryInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        // from AIMLProcessor.evalTemplate and AIMLProcessor.validTemplate:
        //   dbFactory.setIgnoringComments(true); // fix this
        Document doc = dBuilder.parse(is);
        doc.getDocumentElement().normalize();
        return doc.getDocumentElement();
    }

    /**
     * convert an XML node to an XML statement
     *
     * @param node
     *         current XML node
     *
     * @return XML string
     */
    public static String nodeToString(Node node) {
        StringWriter sw = new StringWriter();
        try {
            Transformer t = TransformerFactory.newInstance().newTransformer();
            t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            t.setOutputProperty(OutputKeys.INDENT, "no");
            t.transform(new DOMSource(node), new StreamResult(sw));
        } catch (TransformerException te) {
            log.error("nodeToString Transformer Exception: " + te, te);
        }
        return sw.toString();
    }
}

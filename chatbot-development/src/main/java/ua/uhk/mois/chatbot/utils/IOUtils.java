package ua.uhk.mois.chatbot.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

@Log4j2
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class IOUtils {

    public static String readInputTextLine() {
        String textLine = null;
        try (BufferedReader lineOfText = new BufferedReader(new InputStreamReader(System.in))) {
            textLine = lineOfText.readLine();
        } catch (IOException e) {
            log.error("An error occurred.", e);
        }
        return textLine;
    }

    public static String system(String evaluatedContents, String failedString) {
        Runtime rt = Runtime.getRuntime();
        log.info("System {}", evaluatedContents);
        try {
            Process p = rt.exec(evaluatedContents);
            try (BufferedReader buffrdr = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                StringBuilder result = new StringBuilder();
                String data;
                while ((data = buffrdr.readLine()) != null) {
                    result.append(data).append("\n");
                }
                log.info("Result = {}", result.toString());
                return result.toString();
            }
        } catch (Exception ex) {
            log.error("An error occurred.", ex);
            return failedString;
        }
    }

    public static InputStream getResourceInputStream(String resourcePath) {
        return IOUtils.class.getClassLoader().getResourceAsStream(resourcePath);
    }

}

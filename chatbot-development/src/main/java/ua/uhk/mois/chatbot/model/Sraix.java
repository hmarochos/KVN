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
import ua.uhk.mois.chatbot.utils.NetworkUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Log4j2
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Sraix {

    public static final String NOTHING = "nothing";
    protected static final HashMap<String, String> custIdMap = new HashMap<>();
    private static String custid = "0"; // customer ID number for Pandorabots

    private static boolean locationKnown;
    private static String longitude;
    private static String latitude;

    public static String sraix(Chat chatSession, String input, String defaultResponse, String hint, String host, String botid) {
        String response;
        response = host != null && botid != null ? sraixPandorabots(input, host, botid) : sraixPannous(input, hint, chatSession);
        if (response.equals(MagicStrings.sraix_failed)) {
            if (chatSession != null && defaultResponse == null)
                response = AIMLProcessor.respond(MagicStrings.sraix_failed, NOTHING, NOTHING, chatSession);
            else if (defaultResponse != null)
                response = defaultResponse;
        }
        return response;
    }

    public static String sraixPandorabots(String input, String host, String botid) {
        String responseContent = pandorabotsRequest(input, host, botid);
        return responseContent == null ? MagicStrings.sraix_failed : pandorabotsResponse(responseContent, host, botid);
    }

    public static String pandorabotsRequest(String input, String host, String botid) {
        try {
            custid = "0";
            String key = host + ":" + botid;
            if (custIdMap.containsKey(key))
                custid = custIdMap.get(key);
            String spec = NetworkUtils.spec(host, botid, custid, input);
            return NetworkUtils.responseContent(spec);
        } catch (Exception ex) {
            log.error("An error occurred.", ex);
            return null;
        }
    }

    public static String pandorabotsResponse(String sraixResponse, String host, String botid) {
        int n1 = sraixResponse.indexOf("<that>");
        int n2 = sraixResponse.indexOf("</that>");
        String botResponse = MagicStrings.sraix_failed;
        if (n2 > n1)
            botResponse = sraixResponse.substring(n1 + "<that>".length(), n2);
        n1 = sraixResponse.indexOf("custid=");
        if (n1 > 0) {
            custid = sraixResponse.substring(n1 + "custid=\"".length());
            n2 = custid.indexOf('\"');
            custid = n2 > 0 ? custid.substring(0, n2) : "0";
            String key = host + ":" + botid;
            custIdMap.put(key, custid);
        }
        if (botResponse.endsWith("."))
            botResponse = botResponse.substring(0, botResponse.length() - 1);   // snnoying Pandorabots extra "."
        return botResponse;
    }

    public static String sraixPannous(String input, String hint, Chat chatSession) {
        try {
            if (hint == null)
                hint = MagicStrings.sraix_no_hint;
            input = " " + input + " ";
            input = input.replace(" point ", ".");
            input = input.replace(" rparen ", ")");
            input = input.replace(" lparen ", "(");
            input = input.replace(" slash ", "/");
            input = input.replace(" star ", "*");
            input = input.replace(" dash ", "-");
            input = input.trim();
            input = input.replace(" ", "+");
            int offset = CalendarUtils.timeZoneOffset();
            String locationString = "";
            if (locationKnown) {
                locationString = "&location=" + latitude + "," + longitude;
            }
            String url = "http://weannie.pannous.com/api?input=" + input + "&locale=en_US&timeZone=" + offset + locationString + "&login=" + MagicStrings.pannous_login + "&ip=" + NetworkUtils.localIPAddress() + "&botid=0&key=" + MagicStrings.pannous_api_key + "&exclude=Dialogues,ChatBot&out=json";
            log.debug("Sraix url='" + url + "'");
            String page = NetworkUtils.responseContent(url);
            log.debug("Sraix: " + page);
            StringBuilder text = new StringBuilder();
            String imgRef = "";
            if (!page.isEmpty()) {
                JSONArray outputJson = new JSONObject(page).getJSONArray("output");
                if (outputJson.isEmpty()) {
                    text = new StringBuilder(MagicStrings.sraix_failed);
                } else {
                    JSONObject firstHandler = outputJson.getJSONObject(0);
                    JSONObject actions = firstHandler.getJSONObject("actions");
                    if (actions.has("reminder")) {
                        Object obj = actions.get("reminder");
                        if (obj instanceof JSONObject) {
                            JSONObject sObj = (JSONObject) obj;
                            String date = sObj.getString("date");
                            date = date.substring(0, "2012-10-24T14:32".length());
                            String duration = sObj.getString("duration");

                            Pattern datePattern = Pattern.compile("(.*)-(.*)-(.*)T(.*):(.*)");
                            Matcher m = datePattern.matcher(date);
                            String year, month, day, hour, minute;
                            if (m.matches()) {
                                year = m.group(1);
                                month = String.valueOf(Integer.parseInt(m.group(2)) - 1);
                                day = m.group(3);

                                hour = m.group(4);
                                minute = m.group(5);
                                text = new StringBuilder("<year>" + year + "</year>" +
                                                                 "<month>" + month + "</month>" +
                                                                 "<day>" + day + "</day>" +
                                                                 "<hour>" + hour + "</hour>" +
                                                                 "<minute>" + minute + "</minute>" +
                                                                 "<duration>" + duration + "</duration>");

                            } else
                                text = new StringBuilder(MagicStrings.schedule_error);
                        }
                    } else if (actions.has("say") && !hint.equals(MagicStrings.sraix_pic_hint)) {
                        Object obj = actions.get("say");
                        if (obj instanceof JSONObject) {
                            JSONObject sObj = (JSONObject) obj;
                            text = new StringBuilder(sObj.getString("text"));
                            if (sObj.has("moreText")) {
                                JSONArray arr = sObj.getJSONArray("moreText");
                                for (int i = 0; i < arr.length(); i++) {
                                    text.append(" ").append(arr.getString(i));
                                }
                            }
                        } else {
                            text = new StringBuilder(obj.toString());
                        }
                    }
                    if (actions.has("show") && !text.toString().contains("Wolfram")
                            && actions.getJSONObject("show").has("images")) {
                        JSONArray arr = actions.getJSONObject("show").getJSONArray(
                                "images");
                        int i = new Random().nextInt(arr.length());
                        imgRef = arr.getString(i);
                        if (imgRef.startsWith("//"))
                            imgRef = "http:" + imgRef;
                        imgRef = "<a href=\"" + imgRef + "\"><img src=\"" + imgRef + "\"/></a>";
                    }
                }
                if (hint.equals(MagicStrings.sraix_event_hint) && !text.toString().startsWith("<year>"))
                    return MagicStrings.sraix_failed;
                else if (text.toString().equals(MagicStrings.sraix_failed))
                    return AIMLProcessor.respond(MagicStrings.sraix_failed, NOTHING, NOTHING, chatSession);
                else {
                    text = new StringBuilder(text.toString().replace("&#39;", "'"));
                    text = new StringBuilder(text.toString().replace("&apos;", "'"));
                    text = new StringBuilder(text.toString().replaceAll("\\[(.*)]", ""));
                    String[] sentences;
                    sentences = text.toString().split("\\. ");
                    StringBuilder clippedPage = new StringBuilder(sentences[0]);
                    for (int i = 1; i < sentences.length; i++) {
                        if (clippedPage.length() < 500)
                            clippedPage.append(". ").append(sentences[i]);
                    }

                    clippedPage.append(" ").append(imgRef);
                    return clippedPage.toString();
                }
            }
        } catch (Exception ex) {
            log.error("An error occurred.", ex);
            log.info("Sraix '" + input + "' failed");
        }
        return MagicStrings.sraix_failed;
    }
}


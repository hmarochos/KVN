package ua.uhk.mois.chatbot.model;

import java.util.HashMap;

/**
 * This class is here to simulate a Contacts database for the purpose of testing contactaction.aiml
 */

public class Contact {

    public static final String UNKNOWN = "unknown";
    public static int contactCount;
    public static HashMap<String, Contact> idContactMap = new HashMap<>();
    public static HashMap<String, String> nameIdMap = new HashMap<>();
    public String contactId;
    public String displayName;
    public String birthday;
    public HashMap<String, String> phones;
    public HashMap<String, String> emails;

    public Contact(String displayName, String phoneType, String dialNumber, String emailType, String emailAddress, String birthday) {
        contactId = "ID" + contactCount;
        contactCount++;
        phones = new HashMap<>();
        emails = new HashMap<>();
        idContactMap.put(contactId.toUpperCase(), this);
        addPhone(phoneType, dialNumber);
        addEmail(emailType, emailAddress);
        addName(displayName);
        addBirthday(birthday);
    }

    public static String birthday(String id) {
        Contact c = idContactMap.get(id.toUpperCase());
        return c == null ? UNKNOWN : c.birthday;
    }

    public void addPhone(String type, String dialNumber) {
        phones.put(type.toUpperCase(), dialNumber);
    }

    public void addEmail(String type, String emailAddress) {
        emails.put(type.toUpperCase(), emailAddress);
    }

    public void addName(String name) {
        displayName = name;
        nameIdMap.put(displayName.toUpperCase(), contactId);
    }

    public void addBirthday(String birthday) {
        this.birthday = birthday;
    }
}

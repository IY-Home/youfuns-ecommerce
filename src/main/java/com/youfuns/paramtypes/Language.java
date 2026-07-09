package com.youfuns.paramtypes;

import java.util.List;
import java.util.Locale;

public enum Language {
    // Major languages with ISO codes
    ENGLISH("en", "English"),
    SPANISH("es", "Spanish"),
    FRENCH("fr", "French"),
    GERMAN("de", "German"),
    CHINESE("zh", "Chinese"),
    JAPANESE("ja", "Japanese"),
    KOREAN("ko", "Korean"),
    PORTUGUESE("pt", "Portuguese"),
    RUSSIAN("ru", "Russian"),
    ARABIC("ar", "Arabic"),
    HINDI("hi", "Hindi"),
    ITALIAN("it", "Italian"),
    DUTCH("nl", "Dutch"),
    SWEDISH("sv", "Swedish"),
    NORWEGIAN("no", "Norwegian"),
    DANISH("da", "Danish"),
    FINNISH("fi", "Finnish"),
    POLISH("pl", "Polish"),
    CZECH("cs", "Czech"),
    HUNGARIAN("hu", "Hungarian"),
    ROMANIAN("ro", "Romanian"),
    GREEK("el", "Greek"),
    TURKISH("tr", "Turkish"),
    THAI("th", "Thai"),
    VIETNAMESE("vi", "Vietnamese"),
    INDONESIAN("id", "Indonesian"),
    MALAY("ms", "Malay"),
    FILIPINO("tl", "Filipino"),
    UKRAINIAN("uk", "Ukrainian"),
    HEBREW("he", "Hebrew"),
    PERSIAN("fa", "Persian"),
    URDU("ur", "Urdu"),
    BENGALI("bn", "Bengali"),
    TAMIL("ta", "Tamil"),
    TELUGU("te", "Telugu"),
    MARATHI("mr", "Marathi"),
    NEPALI("np", "Nepali");

    private final String code;
    private final String name;

    Language(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() { return code; }
    public String getName() { return name; }

    public static Language fromCode(String code) {
        for (Language lang : values()) {
            if (lang.code.equalsIgnoreCase(code)) {
                return lang;
            }
        }
        return null;
    }

    public static Language fromJavaLocale(Locale locale) {
        return fromCode(locale.getLanguage());
    }

    public static List<Language> allLanguages() {
        return List.of(values());
    }

    @Override
    public String toString() {
        return name + " (" + code + ")";
    }
}
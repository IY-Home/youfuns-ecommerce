package com.youfuns.paramtypes;

import com.youfuns.exceptions.IllegalFieldException;

import java.util.List;

public enum Country {
    AF("Afghanistan", 93, "AF"),
    AL("Albania", 355, "AL"),
    DZ("Algeria", 213, "DZ"),
    AS("American Samoa", 1684, "AS"),
    AD("Andorra", 376, "AD"),
    AO("Angola", 244, "AO"),
    AI("Anguilla", 1264, "AI"),
    AQ("Antarctica", 672, "AQ"),
    AG("Antigua and Barbuda", 1268, "AG"),
    AR("Argentina", 54, "AR"),
    AM("Armenia", 374, "AM"),
    AW("Aruba", 297, "AW"),
    AU("Australia", 61, "AU"),
    AT("Austria", 43, "AT"),
    AZ("Azerbaijan", 994, "AZ"),
    BS("Bahamas", 1242, "BS"),
    BH("Bahrain", 973, "BH"),
    BD("Bangladesh", 880, "BD"),
    BB("Barbados", 1246, "BB"),
    BY("Belarus", 375, "BY"),
    BE("Belgium", 32, "BE"),
    BZ("Belize", 501, "BZ"),
    BJ("Benin", 229, "BJ"),
    BM("Bermuda", 1441, "BM"),
    BT("Bhutan", 975, "BT"),
    BO("Bolivia", 591, "BO"),
    BQ("Caribbean Netherlands", 599, "BQ"),
    BA("Bosnia and Herzegovina", 387, "BA"),
    BW("Botswana", 267, "BW"),
    BR("Brazil", 55, "BR"),
    VG("British Virgin Islands", 1284, "VG"),
    BN("Brunei", 673, "BN"),
    BG("Bulgaria", 359, "BG"),
    BF("Burkina Faso", 226, "BF"),
    BI("Burundi", 257, "BI"),
    KH("Cambodia", 855, "KH"),
    CM("Cameroon", 237, "CM"),
    CA("Canada", 1, "CA"),
    CV("Cape Verde", 238, "CV"),
    KY("Cayman Islands", 1345, "KY"),
    CF("Central African Republic", 236, "CF"),
    TD("Chad", 235, "TD"),
    CL("Chile", 56, "CL"),
    CN("China", 86, "CN"),
    CX("Christmas Island", 61, "CX"),
    CC("Cocos (Keeling) Islands", 61, "CC"),
    CO("Colombia", 57, "CO"),
    KM("Comoros", 269, "KM"),
    CG("Congo", 242, "CG"),
    CK("Cook Islands", 682, "CK"),
    CR("Costa Rica", 506, "CR"),
    HR("Croatia", 385, "HR"),
    CU("Cuba", 53, "CU"),
    CW("Curaçao", 5999, "CW"),
    CY("Cyprus", 357, "CY"),
    CZ("Czech Republic", 420, "CZ"),
    CD("Democratic Republic of the Congo", 243, "CD"),
    DK("Denmark", 45, "DK"),
    DJ("Djibouti", 253, "DJ"),
    DM("Dominica", 1767, "DM"),
    DO("Dominican Republic", 1849, "DO"),
    EC("Ecuador", 593, "EC"),
    EG("Egypt", 20, "EG"),
    SV("El Salvador", 503, "SV"),
    GQ("Equatorial Guinea", 240, "GQ"),
    ER("Eritrea", 291, "ER"),
    EE("Estonia", 372, "EE"),
    SZ("Eswatini", 268, "SZ"),
    ET("Ethiopia", 251, "ET"),
    FK("Falkland Islands", 500, "FK"),
    FO("Faroe Islands", 298, "FO"),
    FJ("Fiji", 679, "FJ"),
    FI("Finland", 358, "FI"),
    FR("France", 33, "FR"),
    GF("French Guiana", 594, "GF"),
    PF("French Polynesia", 689, "PF"),
    GA("Gabon", 241, "GA"),
    GM("Gambia", 220, "GM"),
    GE("Georgia", 995, "GE"),
    DE("Germany", 49, "DE"),
    GH("Ghana", 233, "GH"),
    GI("Gibraltar", 350, "GI"),
    GR("Greece", 30, "GR"),
    GL("Greenland", 299, "GL"),
    GD("Grenada", 1473, "GD"),
    GP("Guadeloupe", 590, "GP"),
    GU("Guam", 1671, "GU"),
    GT("Guatemala", 502, "GT"),
    GG("Guernsey", 441481, "GG"),
    GN("Guinea", 224, "GN"),
    GW("Guinea-Bissau", 245, "GW"),
    GY("Guyana", 592, "GY"),
    HT("Haiti", 509, "HT"),
    HN("Honduras", 504, "HN"),
    HK("Hong Kong, China", 852, "HK"),
    HU("Hungary", 36, "HU"),
    IS("Iceland", 354, "IS"),
    IN("India", 91, "IN"),
    ID("Indonesia", 62, "ID"),
    IR("Iran", 98, "IR"),
    IQ("Iraq", 964, "IQ"),
    IE("Ireland", 353, "IE"),
    IM("Isle of Man", 441624, "IM"),
    IL("Israel", 972, "IL"),
    IT("Italy", 39, "IT"),
    JM("Jamaica", 1876, "JM"),
    JP("Japan", 81, "JP"),
    JE("Jersey", 441534, "JE"),
    JO("Jordan", 962, "JO"),
    KZ("Kazakhstan", 7, "KZ"),
    KE("Kenya", 254, "KE"),
    KI("Kiribati", 686, "KI"),
    XK("Kosovo", 383, "XK"),
    KW("Kuwait", 965, "KW"),
    KG("Kyrgyzstan", 996, "KG"),
    LA("Laos", 856, "LA"),
    LV("Latvia", 371, "LV"),
    LB("Lebanon", 961, "LB"),
    LS("Lesotho", 266, "LS"),
    LR("Liberia", 231, "LR"),
    LY("Libya", 218, "LY"),
    LI("Liechtenstein", 423, "LI"),
    LT("Lithuania", 370, "LT"),
    LU("Luxembourg", 352, "LU"),
    MO("Macau", 853, "MO"),
    MG("Madagascar", 261, "MG"),
    MW("Malawi", 265, "MW"),
    MY("Malaysia", 60, "MY"),
    MV("Maldives", 960, "MV"),
    ML("Mali", 223, "ML"),
    MT("Malta", 356, "MT"),
    MH("Marshall Islands", 692, "MH"),
    MQ("Martinique", 596, "MQ"),
    MR("Mauritania", 222, "MR"),
    MU("Mauritius", 230, "MU"),
    YT("Mayotte", 262, "YT"),
    MX("Mexico", 52, "MX"),
    FM("Micronesia", 691, "FM"),
    MD("Moldova", 373, "MD"),
    MC("Monaco", 377, "MC"),
    MN("Mongolia", 976, "MN"),
    ME("Montenegro", 382, "ME"),
    MS("Montserrat", 1664, "MS"),
    MA("Morocco", 212, "MA"),
    MZ("Mozambique", 258, "MZ"),
    MM("Myanmar", 95, "MM"),
    NA("Namibia", 264, "NA"),
    NR("Nauru", 674, "NR"),
    NP("Nepal", 977, "NP"),
    NL("Netherlands", 31, "NL"),
    NC("New Caledonia", 687, "NC"),
    NZ("New Zealand", 64, "NZ"),
    NI("Nicaragua", 505, "NI"),
    NE("Niger", 227, "NE"),
    NG("Nigeria", 234, "NG"),
    NU("Niue", 683, "NU"),
    NF("Norfolk Island", 672, "NF"),
    KP("North Korea", 850, "KP"),
    MK("North Macedonia", 389, "MK"),
    MP("Northern Mariana Islands", 1670, "MP"),
    NO("Norway", 47, "NO"),
    OM("Oman", 968, "OM"),
    PK("Pakistan", 92, "PK"),
    PW("Palau", 680, "PW"),
    PS("Palestine", 970, "PS"),
    PA("Panama", 507, "PA"),
    PG("Papua New Guinea", 675, "PG"),
    PY("Paraguay", 595, "PY"),
    PE("Peru", 51, "PE"),
    PH("Philippines", 63, "PH"),
    PN("Pitcairn Islands", 64, "PN"),
    PL("Poland", 48, "PL"),
    PT("Portugal", 351, "PT"),
    PR("Puerto Rico", 1787, "PR"),
    QA("Qatar", 974, "QA"),
    RE("Réunion", 262, "RE"),
    RO("Romania", 40, "RO"),
    RU("Russia", 7, "RU"),
    RW("Rwanda", 250, "RW"),
    BL("Saint Barthélemy", 590, "BL"),
    SH("Saint Helena", 290, "SH"),
    KN("Saint Kitts and Nevis", 1869, "KN"),
    LC("Saint Lucia", 1758, "LC"),
    MF("Saint Martin", 590, "MF"),
    PM("Saint Pierre and Miquelon", 508, "PM"),
    VC("Saint Vincent and the Grenadines", 1784, "VC"),
    WS("Samoa", 685, "WS"),
    SM("San Marino", 378, "SM"),
    ST("Sao Tome and Principe", 239, "ST"),
    SA("Saudi Arabia", 966, "SA"),
    SN("Senegal", 221, "SN"),
    RS("Serbia", 381, "RS"),
    SC("Seychelles", 248, "SC"),
    SL("Sierra Leone", 232, "SL"),
    SG("Singapore", 65, "SG"),
    SX("Sint Maarten", 1721, "SX"),
    SK("Slovakia", 421, "SK"),
    SI("Slovenia", 386, "SI"),
    SB("Solomon Islands", 677, "SB"),
    SO("Somalia", 252, "SO"),
    ZA("South Africa", 27, "ZA"),
    KR("South Korea", 82, "KR"),
    SS("South Sudan", 211, "SS"),
    ES("Spain", 34, "ES"),
    LK("Sri Lanka", 94, "LK"),
    SD("Sudan", 249, "SD"),
    SR("Suriname", 597, "SR"),
    SJ("Svalbard and Jan Mayen", 47, "SJ"),
    SE("Sweden", 46, "SE"),
    CH("Switzerland", 41, "CH"),
    SY("Syria", 963, "SY"),
    TW("Taiwan", 886, "TW"),
    TJ("Tajikistan", 992, "TJ"),
    TZ("Tanzania", 255, "TZ"),
    TH("Thailand", 66, "TH"),
    TL("Timor-Leste", 670, "TL"),
    TG("Togo", 228, "TG"),
    TK("Tokelau", 690, "TK"),
    TO("Tonga", 676, "TO"),
    TT("Trinidad and Tobago", 1868, "TT"),
    TN("Tunisia", 216, "TN"),
    TR("Turkey", 90, "TR"),
    TM("Turkmenistan", 993, "TM"),
    TC("Turks and Caicos Islands", 1649, "TC"),
    TV("Tuvalu", 688, "TV"),
    UG("Uganda", 256, "UG"),
    UA("Ukraine", 380, "UA"),
    AE("United Arab Emirates", 971, "AE"),
    GB("United Kingdom", 44, "GB"),
    US("United States of America", 1, "US"),
    VI("US Virgin Islands", 1340, "VI"),
    UY("Uruguay", 598, "UY"),
    UZ("Uzbekistan", 998, "UZ"),
    VU("Vanuatu", 678, "VU"),
    VA("Vatican City", 379, "VA"),
    VE("Venezuela", 58, "VE"),
    VN("Vietnam", 84, "VN"),
    WF("Wallis and Futuna", 681, "WF"),
    EH("Western Sahara", 212, "EH"),
    YE("Yemen", 967, "YE"),
    ZM("Zambia", 260, "ZM"),
    ZW("Zimbabwe", 263, "ZW");

    private final String fullName;
    private final int telephonePrefix;
    private final String code;

    Country(String fullName, int telephonePrefix, String code) {
        this.fullName = fullName;
        this.telephonePrefix = telephonePrefix;
        this.code = code;
    }

    public String getFullName() {
        return fullName;
    }

    public int getTelephonePrefix() {
        return telephonePrefix;
    }

    public String getCode() {
        return code;
    }

    // ===== LOOKUP METHODS =====

    public static Country fromCode(String code) {
        if (code == null || code.isBlank()) return null;
        for (Country country : values()) {
            if (country.code.equalsIgnoreCase(code.trim())) {
                return country;
            }
        }
        throw new IllegalFieldException(code, "Unknown Country code: " + code, ParamType.ADDRESS);
    }

    public static Country fromName(String name) {
        if (name == null || name.isBlank()) return null;
        for (Country country : values()) {
            if (country.fullName.equalsIgnoreCase(name.trim())) {
                return country;
            }
        }
        throw new IllegalFieldException(name, "Unknown Country: " + name, ParamType.ADDRESS);
    }

    public static Country fromTelephonePrefix(int prefix) {
        for (Country country : values()) {
            if (country.telephonePrefix == prefix) {
                return country;
            }
        }
        throw new IllegalFieldException(Integer.toString(prefix), "Unknown telephone prefix" + prefix, ParamType.ADDRESS);
    }

    // ===== LIST METHODS =====

    public static List<Country> allCountries() {
        return List.of(values());
    }

    // ===== OVERRIDES =====

    @Override
    public String toString() {
        return fullName + " (+" + telephonePrefix + ")";
    }
}
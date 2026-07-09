package com.youfuns.paramtypes;

import java.time.ZoneId;
import java.util.List;
import java.util.TimeZone;

public record LocaleInfo(
        Country country,
        Language language,
        Currency currency,
        TimeZone timeZone
) {
    // ===== FACTORY METHODS =====

    public static LocaleInfo fromCountry(Country country) {
        Language language = getDefaultLanguageForCountry(country);
        Currency currency = getDefaultCurrencyForCountry(country);
        TimeZone timeZone = getDefaultTimeZoneForCountry(country);
        return new LocaleInfo(country, language, currency, timeZone);
    }

    public static LocaleInfo fromLanguage(Language language) {
        Country country = getDefaultCountryForLanguage(language);
        Currency currency = getDefaultCurrencyForCountry(country);
        TimeZone timeZone = getDefaultTimeZoneForCountry(country);
        return new LocaleInfo(country, language, currency, timeZone);
    }

    public static LocaleInfo fromCurrency(Currency currency) {
        Country country = getDefaultCountryForCurrency(currency);
        Language language = getDefaultLanguageForCountry(country);
        TimeZone timeZone = getDefaultTimeZoneForCountry(country);
        return new LocaleInfo(country, language, currency, timeZone);
    }

    // ===== LOOKUP METHODS =====

    public static LocaleInfo fromCountryCode(String countryCode) {
        Country country = Country.fromCode(countryCode);
        if (country == null) return null;
        return fromCountry(country);
    }

    public static LocaleInfo fromLanguageCode(String languageCode) {
        Language language = Language.fromCode(languageCode);
        if (language == null) return null;
        return fromLanguage(language);
    }

    public static LocaleInfo fromCurrencyCode(String currencyCode) {
        Currency currency = Currency.fromCode(currencyCode);
        if (currency == null) return null;
        return fromCurrency(currency);
    }

    public static LocaleInfo fromTimeZone(TimeZone timeZone) {
        Country country = getDefaultCountryForTimeZone(timeZone);
        if (country == null) return null;
        return fromCountry(country);
    }

    public static LocaleInfo fromZoneId(ZoneId zoneId) {
        return fromTimeZone(TimeZone.getTimeZone(zoneId));
    }

    // ===== DEFAULT TIMEZONE MAPPINGS =====

    private static TimeZone getDefaultTimeZoneForCountry(Country country) {
        return switch (country) {
            // North America
            case US, CA -> TimeZone.getTimeZone("America/New_York");
            case MX -> TimeZone.getTimeZone("America/Mexico_City");

            // South America
            case BR -> TimeZone.getTimeZone("America/Sao_Paulo");
            case AR -> TimeZone.getTimeZone("America/Argentina/Buenos_Aires");
            case CL -> TimeZone.getTimeZone("America/Santiago");
            case CO -> TimeZone.getTimeZone("America/Bogota");
            case PE -> TimeZone.getTimeZone("America/Lima");
            case VE -> TimeZone.getTimeZone("America/Caracas");

            // Europe
            case GB -> TimeZone.getTimeZone("Europe/London");
            case FR, DE, IT, ES, NL, BE, CH, SE, NO, DK, FI, PL, CZ, HU, AT, GR, PT, IE -> TimeZone.getTimeZone("Europe/Paris");
            case RU -> TimeZone.getTimeZone("Europe/Moscow");
            case UA -> TimeZone.getTimeZone("Europe/Kiev");
            case RO -> TimeZone.getTimeZone("Europe/Bucharest");
            case BG -> TimeZone.getTimeZone("Europe/Sofia");
            case HR -> TimeZone.getTimeZone("Europe/Zagreb");
            case RS -> TimeZone.getTimeZone("Europe/Belgrade");

            // Asia
            case CN, HK, TW, MO -> TimeZone.getTimeZone("Asia/Shanghai");
            case JP -> TimeZone.getTimeZone("Asia/Tokyo");
            case KR -> TimeZone.getTimeZone("Asia/Seoul");
            case IN -> TimeZone.getTimeZone("Asia/Kolkata");
            case SG, MY -> TimeZone.getTimeZone("Asia/Singapore");
            case ID -> TimeZone.getTimeZone("Asia/Jakarta");
            case TH -> TimeZone.getTimeZone("Asia/Bangkok");
            case VN -> TimeZone.getTimeZone("Asia/Ho_Chi_Minh");
            case PH -> TimeZone.getTimeZone("Asia/Manila");
            case PK -> TimeZone.getTimeZone("Asia/Karachi");
            case BD -> TimeZone.getTimeZone("Asia/Dhaka");
            case LK -> TimeZone.getTimeZone("Asia/Colombo");
            case NP -> TimeZone.getTimeZone("Asia/Kathmandu");
            case IR -> TimeZone.getTimeZone("Asia/Tehran");
            case IL -> TimeZone.getTimeZone("Asia/Jerusalem");
            case TR -> TimeZone.getTimeZone("Europe/Istanbul");
            case SA, AE, QA, KW, BH, OM, JO -> TimeZone.getTimeZone("Asia/Riyadh");

            // Africa
            case ZA -> TimeZone.getTimeZone("Africa/Johannesburg");
            case EG -> TimeZone.getTimeZone("Africa/Cairo");
            case NG -> TimeZone.getTimeZone("Africa/Lagos");
            case KE -> TimeZone.getTimeZone("Africa/Nairobi");
            case GH -> TimeZone.getTimeZone("Africa/Accra");
            case TZ -> TimeZone.getTimeZone("Africa/Dar_es_Salaam");
            case UG -> TimeZone.getTimeZone("Africa/Kampala");
            case ZM -> TimeZone.getTimeZone("Africa/Lusaka");
            case MZ -> TimeZone.getTimeZone("Africa/Maputo");
            case MA -> TimeZone.getTimeZone("Africa/Casablanca");

            // Oceania
            case AU -> TimeZone.getTimeZone("Australia/Sydney");
            case NZ -> TimeZone.getTimeZone("Pacific/Auckland");

            default -> TimeZone.getTimeZone("UTC");
        };
    }

    private static Country getDefaultCountryForTimeZone(TimeZone timeZone) {
        String id = timeZone.getID();
        return switch (id) {
            // North America
            case "America/New_York", "America/Toronto" -> Country.US;
            case "America/Mexico_City" -> Country.MX;

            // South America
            case "America/Sao_Paulo" -> Country.BR;
            case "America/Argentina/Buenos_Aires" -> Country.AR;
            case "America/Santiago" -> Country.CL;
            case "America/Bogota" -> Country.CO;
            case "America/Lima" -> Country.PE;
            case "America/Caracas" -> Country.VE;

            // Europe
            case "Europe/London" -> Country.GB;
            case "Europe/Paris", "Europe/Berlin", "Europe/Rome", "Europe/Madrid", "Europe/Amsterdam", "Europe/Brussels", "Europe/Zurich", "Europe/Stockholm", "Europe/Oslo", "Europe/Copenhagen", "Europe/Helsinki", "Europe/Warsaw", "Europe/Prague", "Europe/Budapest", "Europe/Vienna", "Europe/Athens", "Europe/Lisbon", "Europe/Dublin" -> Country.FR;
            case "Europe/Moscow" -> Country.RU;
            case "Europe/Kiev" -> Country.UA;
            case "Europe/Istanbul" -> Country.TR;

            // Asia
            case "Asia/Shanghai", "Asia/Hong_Kong", "Asia/Taipei" -> Country.CN;
            case "Asia/Tokyo" -> Country.JP;
            case "Asia/Seoul" -> Country.KR;
            case "Asia/Kolkata" -> Country.IN;
            case "Asia/Singapore" -> Country.SG;
            case "Asia/Jakarta" -> Country.ID;
            case "Asia/Bangkok" -> Country.TH;
            case "Asia/Ho_Chi_Minh" -> Country.VN;
            case "Asia/Manila" -> Country.PH;
            case "Asia/Karachi" -> Country.PK;
            case "Asia/Dhaka" -> Country.BD;
            case "Asia/Colombo" -> Country.LK;
            case "Asia/Kathmandu" -> Country.NP;
            case "Asia/Tehran" -> Country.IR;
            case "Asia/Jerusalem" -> Country.IL;
            case "Asia/Riyadh" -> Country.SA;

            // Africa
            case "Africa/Johannesburg" -> Country.ZA;
            case "Africa/Cairo" -> Country.EG;
            case "Africa/Lagos" -> Country.NG;
            case "Africa/Nairobi" -> Country.KE;
            case "Africa/Casablanca" -> Country.MA;

            // Oceania
            case "Australia/Sydney" -> Country.AU;
            case "Pacific/Auckland" -> Country.NZ;

            default -> Country.US;
        };
    }

    // ===== DEFAULT LANGUAGE MAPPINGS =====

    private static Language getDefaultLanguageForCountry(Country country) {
        return switch (country) {
            case US, GB, AU, CA, NZ -> Language.ENGLISH;
            case ES, MX, AR, CO, PE, CL, VE -> Language.SPANISH;
            case FR, BE, CH -> Language.FRENCH;
            case DE, AT -> Language.GERMAN;
            case CN, HK, TW, MO -> Language.CHINESE;
            case JP -> Language.JAPANESE;
            case KR -> Language.KOREAN;
            case BR, PT -> Language.PORTUGUESE;
            case RU -> Language.RUSSIAN;
            case SA, AE, EG, QA, KW, OM, BH, JO -> Language.ARABIC;
            case IN -> Language.HINDI;
            case IT -> Language.ITALIAN;
            case NL -> Language.DUTCH;
            case SE -> Language.SWEDISH;
            case NO -> Language.NORWEGIAN;
            case DK -> Language.DANISH;
            case FI -> Language.FINNISH;
            case PL -> Language.POLISH;
            case CZ -> Language.CZECH;
            case HU -> Language.HUNGARIAN;
            case RO -> Language.ROMANIAN;
            case GR -> Language.GREEK;
            case TR -> Language.TURKISH;
            case TH -> Language.THAI;
            case VN -> Language.VIETNAMESE;
            case ID -> Language.INDONESIAN;
            case MY -> Language.MALAY;
            case PH -> Language.FILIPINO;
            case UA -> Language.UKRAINIAN;
            case IL -> Language.HEBREW;
            case IR -> Language.PERSIAN;
            case PK -> Language.URDU;
            case BD -> Language.BENGALI;
            case LK -> Language.TAMIL;
            default -> Language.ENGLISH;
        };
    }

    // ===== DEFAULT CURRENCY MAPPINGS =====

    private static Currency getDefaultCurrencyForCountry(Country country) {
        return switch (country) {
            case US, EC, SV, ZW -> Currency.USD;
            case GB -> Currency.GBP;
            case JP -> Currency.JPY;
            case CN -> Currency.CNY;
            case AU -> Currency.AUD;
            case CA -> Currency.CAD;
            case CH -> Currency.CHF;
            case HK -> Currency.HKD;
            case SG -> Currency.SGD;
            case SE -> Currency.SEK;
            case KR -> Currency.KRW;
            case NO -> Currency.NOK;
            case NZ -> Currency.NZD;
            case IN -> Currency.INR;
            case BR -> Currency.BRL;
            case RU -> Currency.RUB;
            case ZA -> Currency.ZAR;
            case MX -> Currency.MXN;
            case IL -> Currency.ILS;
            case AE -> Currency.AED;
            case SA -> Currency.SAR;
            case TR -> Currency.TRY;
            case TH -> Currency.THB;
            case VN -> Currency.VND;
            case ID -> Currency.IDR;
            case MY -> Currency.MYR;
            case PH -> Currency.PHP;
            case PL -> Currency.PLN;
            case DK -> Currency.DKK;
            case HU -> Currency.HUF;
            case CZ -> Currency.CZK;
            case RO -> Currency.RON;
            case CL -> Currency.CLP;
            case CO -> Currency.COP;
            case PE -> Currency.PEN;
            case EG -> Currency.EGP;
            case PK -> Currency.PKR;
            case NG -> Currency.NGN;
            case KE -> Currency.KES;
            case GH -> Currency.GHS;
            case TZ -> Currency.TZS;
            case UG -> Currency.UGS;
            case ZM -> Currency.ZMW;
            case MZ -> Currency.MZN;
            case LK -> Currency.LKR;
            case NP -> Currency.NPR;
            case BD -> Currency.BDT;
            case KW -> Currency.KWD;
            case BH -> Currency.BHD;
            case QA -> Currency.QAR;
            case OM -> Currency.OMR;
            case JO -> Currency.JOD;
            case FR, DE, IT, ES, NL, BE, AT, GR, PT, IE, FI, HR, BG -> Currency.EUR;
            default -> Currency.USD;
        };
    }

    private static Country getDefaultCountryForLanguage(Language language) {
        return switch (language) {
            case ENGLISH -> Country.US;
            case SPANISH -> Country.ES;
            case FRENCH -> Country.FR;
            case GERMAN -> Country.DE;
            case CHINESE -> Country.CN;
            case JAPANESE -> Country.JP;
            case KOREAN -> Country.KR;
            case PORTUGUESE -> Country.PT;
            case RUSSIAN -> Country.RU;
            case ARABIC -> Country.SA;
            case HINDI -> Country.IN;
            case ITALIAN -> Country.IT;
            case DUTCH -> Country.NL;
            case SWEDISH -> Country.SE;
            case NORWEGIAN -> Country.NO;
            case DANISH -> Country.DK;
            case FINNISH -> Country.FI;
            case POLISH -> Country.PL;
            case CZECH -> Country.CZ;
            case HUNGARIAN -> Country.HU;
            case ROMANIAN -> Country.RO;
            case GREEK -> Country.GR;
            case TURKISH -> Country.TR;
            case THAI -> Country.TH;
            case VIETNAMESE -> Country.VN;
            case INDONESIAN -> Country.ID;
            case MALAY -> Country.MY;
            case FILIPINO -> Country.PH;
            case UKRAINIAN -> Country.UA;
            case HEBREW -> Country.IL;
            case PERSIAN -> Country.IR;
            case URDU -> Country.PK;
            case BENGALI -> Country.BD;
            case TAMIL -> Country.LK;
            default -> Country.US;
        };
    }

    private static Country getDefaultCountryForCurrency(Currency currency) {
        return switch (currency) {
            case USD -> Country.US;
            case EUR -> Country.DE;
            case GBP -> Country.GB;
            case JPY -> Country.JP;
            case CNY -> Country.CN;
            case AUD -> Country.AU;
            case CAD -> Country.CA;
            case CHF -> Country.CH;
            case HKD -> Country.HK;
            case SGD -> Country.SG;
            case SEK -> Country.SE;
            case KRW -> Country.KR;
            case NOK -> Country.NO;
            case NZD -> Country.NZ;
            case INR -> Country.IN;
            case BRL -> Country.BR;
            case RUB -> Country.RU;
            case ZAR -> Country.ZA;
            case MXN -> Country.MX;
            case ILS -> Country.IL;
            case AED -> Country.AE;
            case SAR -> Country.SA;
            case TRY -> Country.TR;
            case THB -> Country.TH;
            case VND -> Country.VN;
            case IDR -> Country.ID;
            case MYR -> Country.MY;
            case PHP -> Country.PH;
            case PLN -> Country.PL;
            case DKK -> Country.DK;
            case HUF -> Country.HU;
            case CZK -> Country.CZ;
            case RON -> Country.RO;
            case CLP -> Country.CL;
            case COP -> Country.CO;
            case PEN -> Country.PE;
            case EGP -> Country.EG;
            case PKR -> Country.PK;
            case NGN -> Country.NG;
            case KES -> Country.KE;
            case GHS -> Country.GH;
            case TZS -> Country.TZ;
            case UGS -> Country.UG;
            case ZMW -> Country.ZM;
            case MZN -> Country.MZ;
            case LKR -> Country.LK;
            case NPR -> Country.NP;
            case BDT -> Country.BD;
            case KWD -> Country.KW;
            case BHD -> Country.BH;
            case QAR -> Country.QA;
            case OMR -> Country.OM;
            case JOD -> Country.JO;
            default -> Country.US;
        };
    }

    // ===== UTILITY METHODS =====

    public String getCountryCode() { return country.getCode(); }
    public String getLanguageCode() { return language.getCode(); }
    public String getCurrencyCode() { return currency.name(); }
    public String getTimeZoneId() { return timeZone.getID(); }

    public java.util.Locale toJavaLocale() {
        return new java.util.Locale(language.getCode(), country.getCode());
    }

    public ZoneId toZoneId() {
        return timeZone.toZoneId();
    }

    // ===== ALL LOCALES =====

    public static List<LocaleInfo> allLocales() {
        return List.of(
                new LocaleInfo(Country.US, Language.ENGLISH, Currency.USD, TimeZone.getTimeZone("America/New_York")),
                new LocaleInfo(Country.GB, Language.ENGLISH, Currency.GBP, TimeZone.getTimeZone("Europe/London")),
                new LocaleInfo(Country.FR, Language.FRENCH, Currency.EUR, TimeZone.getTimeZone("Europe/Paris")),
                new LocaleInfo(Country.DE, Language.GERMAN, Currency.EUR, TimeZone.getTimeZone("Europe/Berlin")),
                new LocaleInfo(Country.JP, Language.JAPANESE, Currency.JPY, TimeZone.getTimeZone("Asia/Tokyo")),
                new LocaleInfo(Country.CN, Language.CHINESE, Currency.CNY, TimeZone.getTimeZone("Asia/Shanghai")),
                new LocaleInfo(Country.KR, Language.KOREAN, Currency.KRW, TimeZone.getTimeZone("Asia/Seoul")),
                new LocaleInfo(Country.BR, Language.PORTUGUESE, Currency.BRL, TimeZone.getTimeZone("America/Sao_Paulo")),
                new LocaleInfo(Country.RU, Language.RUSSIAN, Currency.RUB, TimeZone.getTimeZone("Europe/Moscow")),
                new LocaleInfo(Country.SA, Language.ARABIC, Currency.SAR, TimeZone.getTimeZone("Asia/Riyadh")),
                new LocaleInfo(Country.IN, Language.HINDI, Currency.INR, TimeZone.getTimeZone("Asia/Kolkata")),
                new LocaleInfo(Country.IT, Language.ITALIAN, Currency.EUR, TimeZone.getTimeZone("Europe/Rome")),
                new LocaleInfo(Country.ES, Language.SPANISH, Currency.EUR, TimeZone.getTimeZone("Europe/Madrid")),
                new LocaleInfo(Country.MX, Language.SPANISH, Currency.MXN, TimeZone.getTimeZone("America/Mexico_City")),
                new LocaleInfo(Country.AU, Language.ENGLISH, Currency.AUD, TimeZone.getTimeZone("Australia/Sydney")),
                new LocaleInfo(Country.CA, Language.ENGLISH, Currency.CAD, TimeZone.getTimeZone("America/Toronto")),
                new LocaleInfo(Country.SG, Language.ENGLISH, Currency.SGD, TimeZone.getTimeZone("Asia/Singapore")),
                new LocaleInfo(Country.NL, Language.DUTCH, Currency.EUR, TimeZone.getTimeZone("Europe/Amsterdam")),
                new LocaleInfo(Country.SE, Language.SWEDISH, Currency.SEK, TimeZone.getTimeZone("Europe/Stockholm")),
                new LocaleInfo(Country.PL, Language.POLISH, Currency.PLN, TimeZone.getTimeZone("Europe/Warsaw")),
                new LocaleInfo(Country.TR, Language.TURKISH, Currency.TRY, TimeZone.getTimeZone("Europe/Istanbul")),
                new LocaleInfo(Country.TH, Language.THAI, Currency.THB, TimeZone.getTimeZone("Asia/Bangkok")),
                new LocaleInfo(Country.VN, Language.VIETNAMESE, Currency.VND, TimeZone.getTimeZone("Asia/Ho_Chi_Minh")),
                new LocaleInfo(Country.ID, Language.INDONESIAN, Currency.IDR, TimeZone.getTimeZone("Asia/Jakarta")),
                new LocaleInfo(Country.MY, Language.MALAY, Currency.MYR, TimeZone.getTimeZone("Asia/Kuala_Lumpur")),
                new LocaleInfo(Country.PH, Language.FILIPINO, Currency.PHP, TimeZone.getTimeZone("Asia/Manila")),
                new LocaleInfo(Country.AE, Language.ARABIC, Currency.AED, TimeZone.getTimeZone("Asia/Dubai")),
                new LocaleInfo(Country.IL, Language.HEBREW, Currency.ILS, TimeZone.getTimeZone("Asia/Jerusalem")),
                new LocaleInfo(Country.ZA, Language.ENGLISH, Currency.ZAR, TimeZone.getTimeZone("Africa/Johannesburg")),
                new LocaleInfo(Country.NG, Language.ENGLISH, Currency.NGN, TimeZone.getTimeZone("Africa/Lagos")),
                new LocaleInfo(Country.NZ, Language.ENGLISH, Currency.NZD, TimeZone.getTimeZone("Pacific/Auckland")),
                new LocaleInfo(Country.EG, Language.ARABIC, Currency.EGP, TimeZone.getTimeZone("Africa/Cairo")),
                new LocaleInfo(Country.PK, Language.URDU, Currency.PKR, TimeZone.getTimeZone("Asia/Karachi")),
                new LocaleInfo(Country.BD, Language.BENGALI, Currency.BDT, TimeZone.getTimeZone("Asia/Dhaka")),
                new LocaleInfo(Country.LK, Language.TAMIL, Currency.LKR, TimeZone.getTimeZone("Asia/Colombo")),
                new LocaleInfo(Country.NP, Language.NEPALI, Currency.NPR, TimeZone.getTimeZone("Asia/Kathmandu")),
                new LocaleInfo(Country.KW, Language.ARABIC, Currency.KWD, TimeZone.getTimeZone("Asia/Kuwait")),
                new LocaleInfo(Country.BH, Language.ARABIC, Currency.BHD, TimeZone.getTimeZone("Asia/Bahrain")),
                new LocaleInfo(Country.QA, Language.ARABIC, Currency.QAR, TimeZone.getTimeZone("Asia/Qatar")),
                new LocaleInfo(Country.OM, Language.ARABIC, Currency.OMR, TimeZone.getTimeZone("Asia/Muscat")),
                new LocaleInfo(Country.JO, Language.ARABIC, Currency.JOD, TimeZone.getTimeZone("Asia/Amman")),
                new LocaleInfo(Country.KE, Language.ENGLISH, Currency.KES, TimeZone.getTimeZone("Africa/Nairobi")),
                new LocaleInfo(Country.GH, Language.ENGLISH, Currency.GHS, TimeZone.getTimeZone("Africa/Accra")),
                new LocaleInfo(Country.TZ, Language.ENGLISH, Currency.TZS, TimeZone.getTimeZone("Africa/Dar_es_Salaam")),
                new LocaleInfo(Country.UG, Language.ENGLISH, Currency.UGS, TimeZone.getTimeZone("Africa/Kampala")),
                new LocaleInfo(Country.ZM, Language.ENGLISH, Currency.ZMW, TimeZone.getTimeZone("Africa/Lusaka")),
                new LocaleInfo(Country.MZ, Language.PORTUGUESE, Currency.MZN, TimeZone.getTimeZone("Africa/Maputo")),
                new LocaleInfo(Country.CL, Language.SPANISH, Currency.CLP, TimeZone.getTimeZone("America/Santiago")),
                new LocaleInfo(Country.CO, Language.SPANISH, Currency.COP, TimeZone.getTimeZone("America/Bogota")),
                new LocaleInfo(Country.PE, Language.SPANISH, Currency.PEN, TimeZone.getTimeZone("America/Lima")),
                new LocaleInfo(Country.VE, Language.SPANISH, Currency.VES, TimeZone.getTimeZone("America/Caracas")),
                new LocaleInfo(Country.AR, Language.SPANISH, Currency.ARS, TimeZone.getTimeZone("America/Argentina/Buenos_Aires"))
        );
    }

    @Override
    public String toString() {
        return country.getFullName() + " | " +
                language.getName() + " | " +
                currency.getName() + " (" + currency.getSymbol() + ") | " +
                timeZone.getID();
    }
}
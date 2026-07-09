package com.youfuns.paramtypes;

import java.util.List;
import java.util.Locale;

public enum Currency {
    // Major currencies with their ISO codes and symbols
    USD("US Dollar", "$"),
    EUR("Euro", "€"),
    GBP("British Pound", "£"),
    JPY("Japanese Yen", "¥"),
    CNY("Chinese Yuan", "¥"),
    AUD("Australian Dollar", "A$"),
    CAD("Canadian Dollar", "C$"),
    CHF("Swiss Franc", "CHF"),
    HKD("Hong Kong Dollar", "HK$"),
    SGD("Singapore Dollar", "S$"),
    SEK("Swedish Krona", "kr"),
    KRW("South Korean Won", "₩"),
    NOK("Norwegian Krone", "kr"),
    NZD("New Zealand Dollar", "NZ$"),
    INR("Indian Rupee", "₹"),
    BRL("Brazilian Real", "R$"),
    RUB("Russian Ruble", "₽"),
    ZAR("South African Rand", "R"),
    MXN("Mexican Peso", "$"),
    ILS("Israeli New Shekel", "₪"),
    AED("UAE Dirham", "د.إ"),
    SAR("Saudi Riyal", "﷼"),
    TRY("Turkish Lira", "₺"),
    THB("Thai Baht", "฿"),
    VND("Vietnamese Dong", "₫"),
    IDR("Indonesian Rupiah", "Rp"),
    MYR("Malaysian Ringgit", "RM"),
    PHP("Philippine Peso", "₱"),
    PLN("Polish Zloty", "zł"),
    DKK("Danish Krone", "kr"),
    HUF("Hungarian Forint", "Ft"),
    CZK("Czech Koruna", "Kč"),
    RON("Romanian Leu", "lei"),
    CLP("Chilean Peso", "$"),
    COP("Colombian Peso", "$"),
    PEN("Peruvian Sol", "S/"),
    EGP("Egyptian Pound", "E£"),
    PKR("Pakistani Rupee", "₨"),
    NGN("Nigerian Naira", "₦"),
    KES("Kenyan Shilling", "KSh"),
    GHS("Ghanaian Cedi", "GH₵"),
    TZS("Tanzanian Shilling", "TSh"),
    UGS("Ugandan Shilling", "USh"),
    ZMW("Zambian Kwacha", "ZK"),
    MZN("Mozambican Metical", "MT"),
    LKR("Sri Lankan Rupee", "Rs"),
    NPR("Nepalese Rupee", "Rs"),
    BDT("Bangladeshi Taka", "৳"),
    KWD("Kuwaiti Dinar", "د.ك"),
    BHD("Bahraini Dinar", ".د.ب"),
    QAR("Qatari Riyal", "ر.ق"),
    OMR("Omani Rial", "ر.ع."),
    JOD("Jordanian Dinar", "د.ا"),
    VES("Venezuelan Bolívar", "Bs.S"),
    ARS("Argentine Peso", "ARS$");


    private final String name;
    private final String symbol;

    Currency(String name, String symbol) {
        this.name = name;
        this.symbol = symbol;
    }

    public String getName() { return name; }
    public String getSymbol() { return symbol; }

    public static Currency fromCode(String code) {
        try {
            return valueOf(code.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static List<Currency> allCurrencies() {
        return List.of(values());
    }

    @Override
    public String toString() {
        return name + " (" + symbol + ")";
    }
}
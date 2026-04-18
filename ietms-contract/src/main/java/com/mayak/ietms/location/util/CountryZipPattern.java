package com.mayak.ietms.location.util;

import lombok.Getter;

import java.util.Optional;

public enum CountryZipPattern {

    AL("\\d{4}",         "\\d{2,4}"),
    AD("\\d{3}",         "\\d{2,3}"),
    AT("\\d{4}",         "\\d{2,4}"),
    BY("\\d{6}",         "\\d{2,6}"),
    BE("\\d{4}",         "\\d{2,4}"),
    BA("\\d{5}",         "\\d{2,5}"),
    BG("\\d{4}",         "\\d{2,4}"),
    HR("\\d{5}",         "\\d{2,5}"),
    CZ("\\d{5}",         "\\d{2,5}"),
    CY("\\d{4}",         "\\d{2,4}"),
    DK("\\d{4}",         "\\d{2,4}"),
    EE("\\d{5}",         "\\d{2,5}"),
    FI("\\d{5}",         "\\d{2,5}"),
    FR("\\d{5}",         "\\d{2,5}"),
    DE("\\d{5}",         "\\d{2,5}"),
    GR("\\d{5}",         "\\d{2,5}"),
    HU("\\d{4}",         "\\d{2,4}"),
    IS("\\d{3}",         "\\d{2,3}"),
    IE("[A-Z0-9]{7}",    "[A-Z0-9]{2,7}"),
    IT("\\d{5}",         "\\d{2,5}"),
    LV("\\d{4}",         "\\d{2,4}"),
    LI("\\d{4}",         "\\d{2,4}"),
    LT("\\d{5}",         "\\d{2,5}"),
    LU("\\d{4}",         "\\d{2,4}"),
    MK("\\d{4}",         "\\d{2,4}"),
    MT("[A-Z]{3}\\d{4}", "[A-Z]{2,3}\\d{0,4}"),
    MD("\\d{4}",         "\\d{2,4}"),
    MC("980\\d{2}",      "98\\d{0,3}"),
    NL("\\d{4}",         "\\d{2,4}"),
    NO("\\d{4}",         "\\d{2,4}"),
    PL("\\d{5}",         "\\d{2,5}"),
    PT("\\d{7}",         "\\d{2,7}"),
    RO("\\d{6}",         "\\d{2,6}"),
    RU("\\d{6}",         "\\d{2,6}"),
    SM("\\d{5}",         "\\d{2,5}"),
    RS("\\d{5}",         "\\d{2,5}"),
    SK("\\d{5}",         "\\d{2,5}"),
    SI("\\d{4}",         "\\d{2,4}"),
    ES("\\d{5}",         "\\d{2,5}"),
    SE("\\d{5}",         "\\d{2,5}"),
    CH("\\d{4}",         "\\d{2,4}"),
    TN("\\d{4}",         "\\d{2,4}"),
    TR("\\d{5}",         "\\d{2,5}"),
    UA("\\d{5}",         "\\d{2,5}"),
    GB("[A-Z0-9]{5,7}",  "[A-Z0-9]{2,7}"),
    VA("00120",          "00120");

    @Getter private final String pattern;
    @Getter private final String partialPattern;

    CountryZipPattern(String pattern, String partialPattern) {
        this.pattern = pattern;
        this.partialPattern = partialPattern;
    }

    public boolean matches(String zip) {
        return zip.matches(pattern);
    }

    public boolean matchesPartial(String zip) {
        return zip.matches(partialPattern);
    }

    public static Optional<CountryZipPattern> of(String code) {
        try {
            return Optional.of(valueOf(code));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
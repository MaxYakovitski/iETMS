package com.mayak.ietms.common.util;

import lombok.NoArgsConstructor;

import java.text.Normalizer;
import java.util.Map;

@NoArgsConstructor
public class UnicodeNormalizer {

    private static final Map<Character, String> MANUAL_MAP = Map.ofEntries(
            Map.entry('ø', "o"), Map.entry('Ø', "O"),
            Map.entry('æ', "ae"), Map.entry('Æ', "AE"),
            Map.entry('œ', "oe"), Map.entry('Œ', "OE"),
            Map.entry('ł', "l"), Map.entry('Ł', "L"),
            Map.entry('đ', "d"), Map.entry('Đ', "D"),
            Map.entry('ð', "d"), Map.entry('Ð', "D"),
            Map.entry('þ', "th"), Map.entry('Þ', "TH"),
            Map.entry('ħ', "h"), Map.entry('Ħ', "H"),
            Map.entry('ß', "ss")
    );

    public static String toAscii(String input) {
        if (input == null) return null;

        StringBuilder sb = new StringBuilder();
        for (char c : input.toCharArray()) {
            sb.append(MANUAL_MAP.getOrDefault(c, String.valueOf(c)));
        }

        return Normalizer.normalize(sb.toString(), Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "");
    }

    public static String normalize(String input) {
        if (input == null) return null;
        if (containsCyrillic(input)) return input.trim();
        return toAscii(input);
    }

    private static boolean containsCyrillic(String input) {
        if (input == null) return false;
        return input.chars().anyMatch(
                c -> Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CYRILLIC
        );
    }

}
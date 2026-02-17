package com.mayak.ietms.common.util;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public final class TextSanitizer {

    public static String safeTrim(String text) {
        if (text == null) return null;
        String t = text.trim();
        return t.isEmpty() ? null : t;
    }
}
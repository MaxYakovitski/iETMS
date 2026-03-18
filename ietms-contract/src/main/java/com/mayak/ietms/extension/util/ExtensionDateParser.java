package com.mayak.ietms.extension.util;

import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@NoArgsConstructor
public final class ExtensionDateParser {

    private static final Pattern DATE_TOKEN = Pattern.compile(
            "\\b(\\d{2}[./-]\\d{2}[./-]\\d{4}|\\d{4}-\\d{2}-\\d{2})\\b");

    private static final List<DateTimeFormatter> DATE_FORMATS = List.of(
            DateTimeFormatter.ofPattern("dd.MM.yyyy"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd"));

    public static LocalDate parseFirst(String raw) {
        return parseDateLoose(raw, false);
    }

    public static LocalDate parseLast(String raw) {
        return parseDateLoose(raw, true);
    }

    private static LocalDate parseDateLoose(String raw, boolean takeLast) {
        if (raw == null || raw.isBlank()) return null;

        Matcher matcher = DATE_TOKEN.matcher(raw);

        List<LocalDate> dates = new ArrayList<>();

        while (matcher.find()) {
            String token = matcher.group();
            LocalDate d = tryParseDate(token);
            if (d != null) {
                dates.add(d);
            }
        }

        if (dates.isEmpty()) return null;
        return takeLast ? dates.getLast() : dates.getFirst();
    }

    private static LocalDate tryParseDate(String token) {
        for (DateTimeFormatter f : DATE_FORMATS) {
            try {
                return LocalDate.parse(token, f);
            } catch (Exception ignored) {}
        }
        return null;
    }
}

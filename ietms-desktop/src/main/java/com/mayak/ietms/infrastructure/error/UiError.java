package com.mayak.ietms.infrastructure.error;

public record UiError(String message, Severity severity) {

    public enum Severity {
        INFO,
        WARNING,
        ERROR
    }

    public static UiError info(String msg) {
        return new UiError(msg, Severity.INFO);
    }

    public static UiError warning(String msg) {
        return new UiError(msg, Severity.WARNING);
    }

    public static UiError error(String msg) {
        return new UiError(msg, Severity.ERROR);
    }
}
package com.mayak.iet.app;

import javafx.application.Application;

public class DesktopLauncher {
    // ⬅️ СТАТЫЧНАЕ ПОЛЕ КЛАСА
    public static final long BOOT_START = System.currentTimeMillis();

    public static void main(String[] args) {
        log("main() enter");

        Application.launch(JavaFxApplication.class, args);

        log("main() exit");
    }

    // ⬅️ СТАТЫЧНЫ МЕТАД ГЭТАГА Ж КЛАСА
    public static void log(String msg) {
        long dt = System.currentTimeMillis() - BOOT_START;
        System.out.println("[BOOT] " + msg + " +" + dt + " ms");
    }
}
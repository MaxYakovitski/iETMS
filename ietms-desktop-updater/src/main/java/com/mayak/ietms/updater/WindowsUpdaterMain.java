package com.mayak.ietms.updater;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.LocalDateTime;

public class WindowsUpdaterMain {
    private static PrintWriter log;

    public static void main(String[] args) throws Exception {
        initLogger();

        log("Updater started");

        if (args.length == 0) {
            System.err.println("MSI path is required");
            System.exit(1);
        }

        Path msi = Path.of(args[0]).toAbsolutePath();
        log("MSI path: " + msi);

        if (!Files.exists(msi)) {
            log("MSI not found!");
            System.err.println("MSI not found: " + msi);
            System.exit(1);
        }

        waitForProcessToExit("iETMS.exe", Duration.ofSeconds(30));

        log("Starting MSI installation...");
        new ProcessBuilder("msiexec", "/i", msi.toString(), "/passive", "/norestart").start();
        log("MSI launched, updater exiting");
        System.exit(0);
    }

    private static void waitForProcessToExit(String processName, Duration timeout) throws Exception {
        long deadline = System.currentTimeMillis() + timeout.toMillis();

        while (System.currentTimeMillis() < deadline) {
            Process p = new ProcessBuilder("tasklist", "/FI", "IMAGENAME eq " + processName).start();

            String output = new String(p.getInputStream().readAllBytes());
            if (!output.contains(processName)) {
                log("Process stopped");
                return;
            }

            Thread.sleep(500);
        }

        log("Timeout waiting for process");
        throw new IllegalStateException("Desktop still running after timeout");
    }

    private static void initLogger() throws IOException {
        Path logDir = Path.of(System.getenv("LOCALAPPDATA"), "iETMS");
        Files.createDirectories(logDir);

        Path logFile = logDir.resolve("updater.log");

        log = new PrintWriter(
                Files.newBufferedWriter(
                        logFile,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.APPEND
                ),
                true
        );
    }

    private static void log(String message) {
        String line = "[" + LocalDateTime.now() + "] " + message;
        System.out.println(line);
        if (log != null) log.println(line);
    }
}
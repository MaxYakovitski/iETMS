package com.mayak.iet.updater;

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
        Process install = new ProcessBuilder("msiexec", "/i", msi.toString(), "/passive", "/norestart").start();
        int code = install.waitFor();
        log("MSI finished with code: " + code);

        Thread.sleep(3000); // даём Windows скончыць фіналізацыю

        Path exe = resolveInstalledExe();
        new ProcessBuilder("cmd", "/c", "start", "", exe.toAbsolutePath().toString()).start();

        log("Desktop restarted");

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

    private static Path resolveInstalledExe() {

        String pf = System.getenv("ProgramFiles");
        if (pf != null) {
            Path exe = Path.of(pf, "iETMS", "iETMS.exe");
            if (Files.exists(exe)) return exe;
        }

        String pf86 = System.getenv("ProgramFiles(x86)");
        if (pf86 != null) {
            Path exe = Path.of(pf86, "iETMS", "iETMS.exe");
            if (Files.exists(exe)) return exe;
        }

        throw new IllegalStateException("Installed iETMS.exe not found");
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
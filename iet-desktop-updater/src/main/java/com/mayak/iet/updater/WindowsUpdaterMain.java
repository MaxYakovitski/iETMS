package com.mayak.iet.updater;

import java.nio.file.Path;
import java.time.Duration;

public class WindowsUpdaterMain {

    public static void main(String[] args) throws Exception {

        if (args.length == 0) {
            System.err.println("MSI path is required");
            System.exit(1);
        }

        Path msi = Path.of(args[0]).toAbsolutePath();

        waitForProcessToExit("iETMS.exe", Duration.ofSeconds(30));

        Process install = new ProcessBuilder("msiexec", "/i", msi.toString(), "/qb", "/norestart")
                .inheritIO().start();

        int code = install.waitFor();
        if (code != 0) System.exit(code);

        new ProcessBuilder("cmd", "/c", "start", "", "iETMS").start();

        System.exit(0);
    }

    private static void waitForProcessToExit(String processName, Duration timeout) throws Exception {
        long deadline = System.currentTimeMillis() + timeout.toMillis();

        while (System.currentTimeMillis() < deadline) {
            Process p = new ProcessBuilder("tasklist", "/FI", "IMAGENAME eq " + processName).start();

            String output = new String(p.getInputStream().readAllBytes());
            if (!output.contains(processName)) return;

            Thread.sleep(500);
        }

        throw new IllegalStateException("Desktop still running after timeout");
    }
}
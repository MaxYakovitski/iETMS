package com.mayak.iet.updater;

import java.nio.file.Path;

public class WindowsUpdaterMain {

    public static void main(String[] args) throws Exception {

        if (args.length == 0) {
            System.err.println("MSI path is required");
            System.exit(1);
        }

        Path msi = Path.of(args[0]).toAbsolutePath();

        Thread.sleep(3000);

        Process process = new ProcessBuilder("msiexec", "/i", msi.toString(), "/qn", "/norestart")
                .inheritIO().start();

        int code = process.waitFor();
        if (code != 0) System.exit(code);

        new ProcessBuilder("cmd", "/c", "start", "\"\"", "\"C:\\Program Files\\iETMS\\iETMS.exe\"")
                .start();
    }
}
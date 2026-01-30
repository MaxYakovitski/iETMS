package com.mayak.iet.infrastructure.nativehost;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class NativeHostRegistrar {

    private NativeHostRegistrar() {}

    public static void ensureRegistered() {
        try {
            if (isWindows()) {
                registerWindows();
            }
        } catch (Exception e) {
            log.warn("Native host registration failed", e);
        }
    }

    private static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    private static void registerWindows() throws Exception {
        Process process = new ProcessBuilder(
                "cmd", "/c",
                "reg add \"HKCU\\Software\\Google\\Chrome\\NativeMessagingHosts\\com.mayak.iet.nativehost\" " +
                        "/ve /t REG_SZ /d \"C:\\Program Files\\iETMS\\app\\native-host\\com.mayak.iet.nativehost.json\" /f"
        ).start();

        int code = process.waitFor();
        if (code == 0) {
            log.info("Native host registered");
        } else {
            log.warn("Native host registry command exited with {}", code);
        }
    }
}
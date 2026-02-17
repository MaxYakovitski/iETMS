package com.mayak.ietms.infrastructure.util;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class OsUtils {

    public static boolean isWindows() {
        return System.getProperty("os.name")
                .toLowerCase()
                .contains("win");
    }
}
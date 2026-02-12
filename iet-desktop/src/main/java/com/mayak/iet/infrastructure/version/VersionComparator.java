package com.mayak.iet.infrastructure.version;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public final class VersionComparator {

    public static int compare(String v1, String v2) {
        if (v1 == null || v2 == null) return 0;

        String[] a1 = v1.split("\\.");
        String[] a2 = v2.split("\\.");

        int length = Math.max(a1.length, a2.length);

        for (int i = 0; i < length; i++) {
            int n1 = i < a1.length ? parse(a1[i]) : 0;
            int n2 = i < a2.length ? parse(a2[i]) : 0;

            if (n1 != n2) {
                return Integer.compare(n1, n2);
            }
        }

        return 0;
    }

    private static int parse(String s) {
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return 0;
        }
    }
}
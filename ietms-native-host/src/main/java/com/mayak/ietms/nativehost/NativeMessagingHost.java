package com.mayak.ietms.integration.bridge;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;


/**
 * Native messaging host for the browser extension.
 * <p>
 * Launched by Chrome as a subprocess. Communicates via stdin/stdout
 * using the Chrome native messaging protocol:
 * each message is prefixed with a 4-byte little-endian length.
 */
public class NativeMessagingHost {

    /**
     * Reads a single native messaging request from stdin,
     * responds with the current user's JWT token from
     * {@code %LOCALAPPDATA%\iETMS\token}, then exits.
     */
    public static void run () throws IOException {
        DataInputStream in = new DataInputStream(System.in);
        DataOutputStream out = new DataOutputStream(System.out);

        int length = Integer.reverseBytes(in.readInt());
        in.skipNBytes(length);

        String token = readTokenFile();
        String response = token != null
                ? "{\"token\":\"" + token + "\"}"
                : "{\"token\":null}";

        byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
        out.writeInt(Integer.reverseBytes(responseBytes.length));
        out.write(responseBytes);
        out.flush();
    }

    private static String readTokenFile() {
        try {
            Path path = Path.of(System.getenv("LOCALAPPDATA"), "iETMS", "token");
            return Files.readString(path, StandardCharsets.UTF_8).trim();
        } catch (IOException e) {
            return null;
        }
    }
}
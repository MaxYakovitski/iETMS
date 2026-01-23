package com.mayak.iet.integration.nativehost;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

public class NativeHostPingTest {

    public static void main(String[] args) throws Exception {

        Process process = new ProcessBuilder(
                "java",
                "-cp",
                System.getProperty("java.class.path"),
                "com.mayak.iet.integration.nativehost.NativeHostMain"
        ).start();

        OutputStream out = process.getOutputStream();
        InputStream in = process.getInputStream();

        // ---- send PING ----
        String json = "{\"type\":\"PING\"}";
        byte[] payload = json.getBytes(StandardCharsets.UTF_8);

        ByteBuffer header = ByteBuffer
                .allocate(4)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putInt(payload.length);

        out.write(header.array());
        out.write(payload);
        out.flush();

        // ---- read response ----
        byte[] lenBytes = in.readNBytes(4);
        int len = ByteBuffer.wrap(lenBytes)
                .order(ByteOrder.LITTLE_ENDIAN)
                .getInt();

        byte[] responseBytes = in.readNBytes(len);
        String response = new String(responseBytes, StandardCharsets.UTF_8);

        System.out.println("Response: " + response);

        process.destroy();
    }
}

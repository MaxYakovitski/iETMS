package com.mayak.iet.infrastructure.update;

public interface UpdateListener {

    void onStart(String currentVersion, String targetVersion);

    void onMessage(String message);

    void onProgress(double progress);

    void onError(Throwable error);
}
package com.mayak.iet.infrastructure.update;

public enum UpdateState {
    IDLE,
    CHECKING,
    DOWNLOADING,
    INSTALLING,
    VERIFYING,
    FAILED
}
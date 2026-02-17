package com.mayak.ietms.infrastructure.update;

public enum UpdateState {
    IDLE,
    CHECKING,
    DOWNLOADING,
    INSTALLING,
    VERIFYING,
    FAILED
}
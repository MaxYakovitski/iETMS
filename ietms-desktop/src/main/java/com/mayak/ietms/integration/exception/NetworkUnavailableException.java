package com.mayak.ietms.integration.exception;

public class NetworkUnavailableException extends  RuntimeException {

    public  NetworkUnavailableException(Throwable ex) {
        super("Network unavailable", ex);
    }
}

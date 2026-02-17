package com.mayak.ietms.integration.auth;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
public class AuthState {

    private String token;

    public boolean isAuthenticated() {
        return token != null;
    }

    public void clear() {
        this.token = null;
    }
}
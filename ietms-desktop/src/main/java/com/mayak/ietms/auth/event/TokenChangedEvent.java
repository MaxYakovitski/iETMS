package com.mayak.ietms.auth.event;

/**
 * @author ma_yak
 */

public record TokenChangedEvent(String accessToken, String refreshToken) {
}

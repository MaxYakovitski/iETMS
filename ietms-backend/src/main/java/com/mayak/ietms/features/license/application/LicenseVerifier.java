package com.mayak.ietms.features.license.application;

import com.mayak.ietms.features.license.domain.model.LicenseInfo;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Component;

import java.security.PublicKey;
import java.time.LocalDate;

/**
 * Verifies the RSA signature of a license key and extracts its payload.
 * The public key is embedded in the application — the private key never leaves the vendor.
 */
@Component
public class LicenseVerifier {

    private final PublicKey publicKey;

    public LicenseVerifier(LicensePublicKeyProvider keyProvider) {
        this.publicKey = keyProvider.getPublicKey();
    }

    /**
     * Verifies the license key signature and returns parsed license info.
     *
     * @throws io.jsonwebtoken.JwtException if the key is invalid, tampered or expired
     */
    public LicenseInfo verify(String licenseKey) {
        Claims claims = Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(licenseKey)
                .getPayload();

        return new LicenseInfo(
                claims.get("company", String.class),
                claims.get("maxUsers", Integer.class),
                LocalDate.parse(claims.get("expiresAt", String.class))
        );
    }
}
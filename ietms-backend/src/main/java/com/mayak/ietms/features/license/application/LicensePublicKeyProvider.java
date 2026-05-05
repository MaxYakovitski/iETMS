package com.mayak.ietms.features.license.application;

import lombok.Getter;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * Loads the RSA public key from the classpath resource {@code license-public.pem}.
 * Used to verify signed license keys issued by the vendor.
 */
@Component
public class LicensePublicKeyProvider {

    private static final String KEY_PATH = "license-public.pem";

    @Getter
    private final PublicKey publicKey;

    public LicensePublicKeyProvider() throws Exception {
        this.publicKey = loadPublicKey();
    }

    private PublicKey loadPublicKey() throws Exception {
        ClassPathResource resource = new ClassPathResource(KEY_PATH);
        String pem = new String(resource.getContentAsByteArray())
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");

        byte[] decoded = Base64.getDecoder().decode(pem);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
        return KeyFactory.getInstance("RSA").generatePublic(spec);
    }
}
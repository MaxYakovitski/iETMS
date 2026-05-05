-- licenses: store signed JWT license key to prevent direct DB tampering
CREATE TABLE licenses (
                          id BIGSERIAL PRIMARY KEY,
                          license_key TEXT NOT NULL UNIQUE,
                          is_active   BOOLEAN NOT NULL DEFAULT true,
                          activated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
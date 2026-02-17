package com.mayak.ietms.infrastructure.update.installer;

import java.nio.file.Path;

public interface UpdateInstaller {
    void install(Path msiFile) throws Exception;
}

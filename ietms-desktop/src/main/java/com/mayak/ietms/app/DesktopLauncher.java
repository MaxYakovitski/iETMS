package com.mayak.ietms.app;

import com.mayak.ietms.integration.bridge.NativeMessagingHost;
import javafx.application.Application;

import java.io.IOException;

public class DesktopLauncher {

    public static void main(String[] args) throws IOException {

        if (args.length > 0 && "--native-messaging-host".equals(args[0])) {
            NativeMessagingHost.run();
            return;
        }

        Application.launch(JavaFxApplication.class, args);
    }
}
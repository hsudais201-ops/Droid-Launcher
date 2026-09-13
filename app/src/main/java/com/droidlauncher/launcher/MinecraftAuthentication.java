package com.droidlauncher.launcher;

import java.io.IOException;

/** Authentication abstraction used by the launcher; implementations can use Microsoft's supported OAuth flow. */
public interface MinecraftAuthentication {
    MinecraftAuthSession authenticate() throws IOException;
    void signOut();
}

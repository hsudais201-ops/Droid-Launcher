package com.droidlauncher.launcher;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Coordinates the interactive Microsoft device-code flow without blocking Android's UI thread. */
public final class MicrosoftSignInCoordinator {
    public interface Listener {
        void onDeviceCode(MicrosoftDeviceCode code);
        void onSuccess(MicrosoftOAuthClient.MicrosoftTokenResponse token);
        void onError(String message);
    }

    private final MicrosoftOAuthClient client;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public MicrosoftSignInCoordinator() {
        this(new MicrosoftOAuthClient());
    }

    public MicrosoftSignInCoordinator(MicrosoftOAuthClient client) {
        if (client == null) throw new IllegalArgumentException("client is required");
        this.client = client;
    }

    public void signIn(MicrosoftAuthConfig config, Listener listener) {
        if (config == null) throw new IllegalArgumentException("config is required");
        executor.execute(() -> {
            try {
                MicrosoftDeviceCode code = client.requestDeviceCode(config);
                if (listener != null) listener.onDeviceCode(code);
                MicrosoftOAuthClient.MicrosoftTokenResponse token = client.pollToken(config, code);
                if (listener != null) listener.onSuccess(token);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                if (listener != null) listener.onError("Microsoft sign-in cancelled");
            } catch (Exception e) {
                String message = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
                if (listener != null) listener.onError(message);
            }
        });
    }

    public void shutdown() {
        executor.shutdownNow();
    }
}

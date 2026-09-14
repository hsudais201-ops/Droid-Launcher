package com.droidlauncher;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.droidlauncher.launcher.AccountStore;
import com.droidlauncher.launcher.AuthenticatedProfile;
import com.droidlauncher.launcher.InstallationState;
import com.droidlauncher.launcher.InstallationStateStore;
import com.droidlauncher.launcher.LaunchObservation;
import com.droidlauncher.launcher.LaunchState;
import com.droidlauncher.launcher.LaunchUiController;
import com.droidlauncher.launcher.MinecraftAuthSession;
import com.droidlauncher.launcher.MinecraftDownloadOrchestrator;
import com.droidlauncher.launcher.MinecraftInstallationService;
import com.droidlauncher.launcher.MinecraftLaunchArguments;
import com.droidlauncher.launcher.MinecraftLaunchClasspath;
import com.droidlauncher.launcher.MinecraftNativePreparer;
import com.droidlauncher.launcher.MinecraftProfile;
import com.droidlauncher.launcher.MinecraftSessionRestorer;
import com.droidlauncher.launcher.MinecraftXboxAuthenticator;
import com.droidlauncher.launcher.MicrosoftAuthConfig;
import com.droidlauncher.launcher.MicrosoftDeviceCode;
import com.droidlauncher.launcher.MicrosoftSignInCoordinator;
import com.droidlauncher.launcher.NativeAbi;
import com.droidlauncher.launcher.ProfileStore;
import com.droidlauncher.launcher.ProfileValidator;
import com.droidlauncher.launcher.SecureTokenStore;
import com.droidlauncher.runtime.JavaRuntime;
import com.droidlauncher.runtime.JavaRuntimeDetector;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public final class MainActivity extends Activity {
    private ProfileStore profileStore;
    private MinecraftProfile profile;
    private TextView profileStatus;
    private TextView installationStatus;
    private TextView accountStatus;
    private TextView launchStatus;
    private TextView launchDetail;
    private Button playButton;
    private Button signInButton;
    private LaunchUiController launchController;
    private InstallationStateStore installationStateStore;
    private MicrosoftSignInCoordinator signInCoordinator;
    private SecureTokenStore tokenStore;
    private AccountStore accountStore;
    private MinecraftAuthSession minecraftSession;
    private final AtomicBoolean installing = new AtomicBoolean(false);
    private final AtomicBoolean restoringSession = new AtomicBoolean(false);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.rgb(5, 6, 10));
        getWindow().setNavigationBarColor(Color.rgb(5, 6, 10));

        profileStore = new ProfileStore(this);
        profile = profileStore.load();
        if (profile == null) {
            profile = new MinecraftProfile("default", "latest", getFilesDir().getAbsolutePath(), "", 512, 2048);
            profileStore.save(profile);
        }
        installationStateStore = new InstallationStateStore(this);
        signInCoordinator = new MicrosoftSignInCoordinator();
        tokenStore = new SecureTokenStore(this);
        accountStore = new AccountStore(this);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(48, 28, 48, 28);
        root.setBackgroundColor(Color.TRANSPARENT);

        TextView title = new TextView(this);
        title.setText("DROID LAUNCHER");
        title.setTextColor(Color.WHITE);
        title.setTextSize(30);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setShadowLayer(8f, 0f, 2f, Color.BLACK);
        root.addView(title, new LinearLayout.LayoutParams(-1, -2));

        TextView subtitle = new TextView(this);
        subtitle.setText("MINECRAFT JAVA  •  ANDROID");
        subtitle.setTextColor(Color.rgb(225, 232, 240));
        subtitle.setTextSize(14);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setShadowLayer(5f, 0f, 1f, Color.BLACK);
        LinearLayout.LayoutParams subtitleParams = new LinearLayout.LayoutParams(-1, -2);
        subtitleParams.topMargin = 8;
        root.addView(subtitle, subtitleParams);

        LinearLayout profilePanel = createPanel();
        profileStatus = createStatusText();
        profilePanel.addView(profileStatus, new LinearLayout.LayoutParams(-1, -2));
        LinearLayout.LayoutParams profilePanelParams = new LinearLayout.LayoutParams(560, -2);
        profilePanelParams.topMargin = 18;
        root.addView(profilePanel, profilePanelParams);

        accountStatus = createStatusText();
        accountStatus.setTextSize(13);
        accountStatus.setTextColor(Color.rgb(214, 224, 235));

        LinearLayout accountPanel = createPanel();
        accountPanel.addView(accountStatus, new LinearLayout.LayoutParams(-1, -2));

        LinearLayout accountActions = new LinearLayout(this);
        accountActions.setGravity(Gravity.CENTER);
        signInButton = new Button(this);
        signInButton.setText("SIGN IN WITH MICROSOFT");
        styleButton(signInButton, Color.argb(170, 25, 38, 52), Color.argb(220, 190, 215, 240));
        signInButton.setOnClickListener(v -> startMicrosoftSignIn());
        accountActions.addView(signInButton, new LinearLayout.LayoutParams(300, 64));
        Button signOutButton = new Button(this);
        signOutButton.setText("SIGN OUT");
        styleButton(signOutButton, Color.argb(150, 25, 25, 30), Color.argb(160, 180, 190, 205));
        signOutButton.setOnClickListener(v -> signOutMicrosoft());
        LinearLayout.LayoutParams signOutParams = new LinearLayout.LayoutParams(180, 64);
        signOutParams.leftMargin = 12;
        accountActions.addView(signOutButton, signOutParams);
        LinearLayout.LayoutParams accountActionsParams = new LinearLayout.LayoutParams(-2, -2);
        accountActionsParams.topMargin = 10;
        accountPanel.addView(accountActions, accountActionsParams);
        LinearLayout.LayoutParams accountPanelParams = new LinearLayout.LayoutParams(560, -2);
        accountPanelParams.topMargin = 10;
        root.addView(accountPanel, accountPanelParams);

        installationStatus = createStatusText();
        installationStatus.setTextSize(12);
        LinearLayout installPanel = createPanel();
        installPanel.addView(installationStatus, new LinearLayout.LayoutParams(-1, -2));
        LinearLayout.LayoutParams installPanelParams = new LinearLayout.LayoutParams(560, -2);
        installPanelParams.topMargin = 10;
        root.addView(installPanel, installPanelParams);

        launchStatus = createStatusText();
        launchStatus.setTextSize(15);
        launchStatus.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        launchDetail = createStatusText();
        launchDetail.setTextSize(12);

        LinearLayout launchPanel = createPanel();
        launchPanel.addView(launchStatus, new LinearLayout.LayoutParams(-1, -2));
        LinearLayout.LayoutParams detailParams = new LinearLayout.LayoutParams(-1, -2);
        detailParams.topMargin = 4;
        launchPanel.addView(launchDetail, detailParams);

        launchController = new LaunchUiController(launchStatus, launchDetail);
        launchController.onLaunchUpdate(LaunchObservation.state(LaunchState.IDLE, "Launcher ready"));

        LinearLayout actions = new LinearLayout(this);
        actions.setGravity(Gravity.CENTER);
        playButton = new Button(this);
        playButton.setText("PLAY");
        playButton.setTextSize(20);
        playButton.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        styleButton(playButton, Color.argb(215, 45, 95, 42), Color.argb(240, 215, 245, 215));
        playButton.setOnClickListener(v -> startMinecraft());
        LinearLayout.LayoutParams playParams = new LinearLayout.LayoutParams(320, 82);
        actions.addView(playButton, playParams);
        Button stopButton = new Button(this);
        stopButton.setText("STOP");
        styleButton(stopButton, Color.argb(150, 48, 32, 34), Color.argb(180, 240, 205, 210));
        stopButton.setOnClickListener(v -> launchController.stop());
        LinearLayout.LayoutParams stopParams = new LinearLayout.LayoutParams(180, 76);
        stopParams.leftMargin = 12;
        actions.addView(stopButton, stopParams);
        Button settingsButton = new Button(this);
        settingsButton.setText("SETTINGS");
        settingsButton.setTextSize(13);
        styleButton(settingsButton, Color.argb(145, 30, 38, 50), Color.argb(170, 195, 215, 235));
        settingsButton.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
        LinearLayout.LayoutParams settingsParams = new LinearLayout.LayoutParams(180, 76);
        settingsParams.leftMargin = 12;
        actions.addView(settingsButton, settingsParams);
        LinearLayout.LayoutParams actionsParams = new LinearLayout.LayoutParams(-2, -2);
        actionsParams.topMargin = 10;
        launchPanel.addView(actions, actionsParams);

        LinearLayout.LayoutParams launchPanelParams = new LinearLayout.LayoutParams(560, -2);
        launchPanelParams.topMargin = 10;
        root.addView(launchPanel, launchPanelParams);

        setContentView(root);
        startPlayPulse();
        refreshProfileStatus();
        refreshInstallationStatus();
        refreshAccountStatus();
        restoreSavedMinecraftSession();
    }

    private LinearLayout createPanel() {
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setGravity(Gravity.CENTER);
        panel.setPadding(22, 14, 22, 14);
        GradientDrawable background = new GradientDrawable();
        background.setColor(Color.argb(135, 8, 12, 18));
        background.setCornerRadius(24f);
        background.setStroke(1, Color.argb(95, 255, 255, 255));
        panel.setBackground(background);
        return panel;
    }

    private TextView createStatusText() {
        TextView text = new TextView(this);
        text.setTextColor(Color.WHITE);
        text.setTextSize(14);
        text.setGravity(Gravity.CENTER);
        text.setShadowLayer(5f, 0f, 1f, Color.BLACK);
        return text;
    }

    private void styleButton(Button button, int fillColor, int strokeColor) {
        GradientDrawable background = new GradientDrawable();
        background.setColor(fillColor);
        background.setCornerRadius(22f);
        background.setStroke(2, strokeColor);
        button.setTextColor(Color.WHITE);
        button.setAllCaps(false);
        button.setBackground(background);
        button.setStateListAnimator(null);
    }

    private void startPlayPulse() {
        if (playButton == null) return;
        ScaleAnimation pulse = new ScaleAnimation(
                1.0f, 1.035f, 1.0f, 1.035f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        pulse.setDuration(1100L);
        pulse.setRepeatMode(Animation.REVERSE);
        pulse.setRepeatCount(Animation.INFINITE);
        playButton.startAnimation(pulse);
    }

    private void stopPlayPulse() {
        if (playButton != null) playButton.clearAnimation();
    }

    private void showLaunchFeedback() {
        if (playButton == null) return;
        AlphaAnimation fade = new AlphaAnimation(1f, 0.75f);
        fade.setDuration(180L);
        fade.setRepeatMode(Animation.REVERSE);
        fade.setRepeatCount(3);
        playButton.startAnimation(fade);
    }

    private void restoreSavedMinecraftSession() {
        String clientId = getString(com.droidlauncher.R.string.microsoft_client_id).trim();
        if (clientId.isEmpty() || restoringSession.getAndSet(true)) return;
        launchController.onLaunchUpdate(LaunchObservation.state(LaunchState.PREPARING,
                "Restoring saved Minecraft account..."));
        signInButton.setEnabled(false);
        new Thread(() -> {
            try {
                MinecraftAuthSession restored = new MinecraftSessionRestorer(this)
                        .restore(new MicrosoftAuthConfig(clientId));
                if (restored == null) {
                    runOnUiThread(() -> {
                        signInButton.setEnabled(true);
                        refreshAccountStatus();
                        launchController.onLaunchUpdate(LaunchObservation.state(LaunchState.IDLE,
                                "Sign in with Microsoft to play"));
                    });
                    return;
                }
                minecraftSession = restored;
                String name = restored.getProfile().getDisplayName();
                runOnUiThread(() -> {
                    signInButton.setEnabled(true);
                    refreshAccountStatus();
                    launchController.onLaunchUpdate(LaunchObservation.state(LaunchState.IDLE,
                            "Account restored: " + name));
                });
            } catch (Exception e) {
                minecraftSession = null;
                String message = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
                runOnUiThread(() -> {
                    signInButton.setEnabled(true);
                    refreshAccountStatus();
                    launchController.onLaunchUpdate(LaunchObservation.state(LaunchState.IDLE,
                            "Saved session needs sign-in again: " + message));
                });
            } finally {
                restoringSession.set(false);
            }
        }, "droid-session-restore").start();
    }

    private void startMicrosoftSignIn() {
        String clientId = getString(com.droidlauncher.R.string.microsoft_client_id).trim();
        if (clientId.isEmpty()) {
            Toast.makeText(this, "Microsoft client ID is not configured in the build", Toast.LENGTH_LONG).show();
            return;
        }
        launchController.onLaunchUpdate(LaunchObservation.state(LaunchState.PREPARING,
                "Starting Microsoft sign-in..."));
        signInButton.setEnabled(false);
        signInCoordinator.signIn(new MicrosoftAuthConfig(clientId), new MicrosoftSignInCoordinator.Listener() {
            @Override public void onDeviceCode(MicrosoftDeviceCode code) {
                runOnUiThread(() -> showDeviceCode(code));
            }
            @Override public void onSuccess(com.droidlauncher.launcher.MicrosoftOAuthClient.MicrosoftTokenResponse token) {
                try {
                    if (!token.getRefreshToken().isEmpty()) tokenStore.saveRefreshToken(token.getRefreshToken());
                } catch (Exception e) {
                    runOnUiThread(() -> {
                        signInButton.setEnabled(true);
                        Toast.makeText(MainActivity.this,
                                "Could not save account securely: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
                    return;
                }
                launchController.onLaunchUpdate(LaunchObservation.state(LaunchState.PREPARING,
                        "Microsoft authorized. Verifying Minecraft account..."));
                new Thread(() -> completeMinecraftAuthentication(token.getAccessToken()),
                        "droid-minecraft-auth").start();
            }
            @Override public void onError(String message) {
                runOnUiThread(() -> {
                    signInButton.setEnabled(true);
                    launchController.onLaunchUpdate(LaunchObservation.state(LaunchState.FAILED, message));
                    Toast.makeText(MainActivity.this, "Microsoft sign-in failed: " + message, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void completeMinecraftAuthentication(String microsoftAccessToken) {
        try {
            MinecraftAuthSession session = new MinecraftXboxAuthenticator().authenticate(microsoftAccessToken);
            minecraftSession = session;
            AuthenticatedProfile authenticated = session.getProfile();
            accountStore.save(authenticated);
            runOnUiThread(() -> {
                signInButton.setEnabled(true);
                refreshAccountStatus();
                launchController.onLaunchUpdate(LaunchObservation.state(LaunchState.IDLE,
                        "Minecraft account ready: " + authenticated.getDisplayName()));
                Toast.makeText(MainActivity.this,
                        "Minecraft account verified: " + authenticated.getDisplayName(), Toast.LENGTH_LONG).show();
            });
        } catch (Exception e) {
            String message = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
            minecraftSession = null;
            runOnUiThread(() -> {
                signInButton.setEnabled(true);
                refreshAccountStatus();
                launchController.onLaunchUpdate(LaunchObservation.state(LaunchState.FAILED,
                        "Minecraft authentication failed: " + message));
                Toast.makeText(MainActivity.this,
                        "Minecraft authentication failed: " + message, Toast.LENGTH_LONG).show();
            });
        }
    }

    private void showDeviceCode(MicrosoftDeviceCode code) {
        final android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(this)
                .setTitle("Sign in with Microsoft")
                .setMessage("Open:\n" + code.getVerificationUri() + "\n\nCode:\n" + code.getUserCode()
                        + "\n\nComplete sign-in in your browser, then return here.")
                .setPositiveButton("OPEN", (d, which) -> {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(code.getVerificationUri())));
                })
                .setNeutralButton("COPY CODE", null)
                .setNegativeButton("CLOSE", null)
                .create();
        dialog.setOnShowListener(d -> dialog.getButton(android.app.AlertDialog.BUTTON_NEUTRAL)
                .setOnClickListener(v -> {
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    if (clipboard != null) clipboard.setPrimaryClip(ClipData.newPlainText("Microsoft code", code.getUserCode()));
                    Toast.makeText(this, "Code copied", Toast.LENGTH_SHORT).show();
                }));
        dialog.show();
    }

    private void signOutMicrosoft() {
        tokenStore.clear();
        minecraftSession = null;
        accountStore.clearAll();
        refreshAccountStatus();
        Toast.makeText(this, "Microsoft account signed out", Toast.LENGTH_SHORT).show();
    }

    private void refreshAccountStatus() {
        if (minecraftSession != null) {
            AuthenticatedProfile authenticated = minecraftSession.getProfile();
            accountStatus.setText("Minecraft account: " + authenticated.getDisplayName()
                    + " • verified • session active");
            return;
        }
        try {
            String token = tokenStore.loadRefreshToken();
            String savedName = accountStore.getSelectedDisplayName();
            accountStatus.setText(token.isEmpty()
                    ? "Minecraft account: not connected"
                    : (savedName.isEmpty()
                        ? "Microsoft account: authorized • restoring Minecraft session"
                        : "Microsoft account: authorized • saved as " + savedName));
        } catch (Exception e) {
            accountStatus.setText("Microsoft account: token recovery error");
        }
    }

    private void startMinecraft() {
        if (!installing.compareAndSet(false, true)) return;
        if (minecraftSession == null || minecraftSession.getProfile().isExpired(System.currentTimeMillis())) {
            installing.set(false);
            launchController.onLaunchUpdate(LaunchObservation.state(LaunchState.FAILED,
                    "Sign in with Microsoft and verify Minecraft before pressing PLAY"));
            Toast.makeText(this, "Please sign in and verify your Minecraft account first", Toast.LENGTH_LONG).show();
            return;
        }
        ProfileValidator.ValidationResult result = ProfileValidator.validate(profile);
        if (!result.isValid()) {
            installing.set(false);
            launchController.onLaunchUpdate(LaunchObservation.state(LaunchState.FAILED, result.getMessage()));
            return;
        }
        stopPlayPulse();
        playButton.setEnabled(false);
        showLaunchFeedback();
        launchController.onLaunchUpdate(LaunchObservation.state(LaunchState.PREPARING,
                "Installing and preparing Minecraft..."));
        new Thread(() -> {
            try {
                File gameDirectory = new File(profile.getGameDirectory());
                MinecraftInstallationService service = new MinecraftInstallationService();
                MinecraftInstallationService.InstallationPlan plan = service.prepare(profile.getVersion(), gameDirectory);
                int total = plan.getTasks().size();
                installationStateStore.save(new InstallationState(plan.getMetadata().getId(),
                        InstallationState.Status.RUNNING, 0, total, "preparing", "", System.currentTimeMillis()));
                service.install(plan, new MinecraftDownloadOrchestrator(), progress -> {
                    installationStateStore.save(new InstallationState(plan.getMetadata().getId(),
                            InstallationState.Status.RUNNING, progress.getCompletedTasks(), progress.getTotalTasks(),
                            progress.getTaskName(), "", System.currentTimeMillis()));
                    postInstallationStatus("Installing " + progress.getCompletedTasks() + "/" + progress.getTotalTasks(), false);
                });
                MinecraftLaunchClasspath.Result resolved = new MinecraftLaunchClasspath().resolve(plan.getVersionJson(), gameDirectory);
                File nativesDirectory = new File(gameDirectory, "natives");
                File preparedNatives = new MinecraftNativePreparer().prepare(resolved.getNativeLibraries(), NativeAbi.detect(), nativesDirectory);
                installationStateStore.save(new InstallationState(plan.getMetadata().getId(),
                        InstallationState.Status.COMPLETED, total, total, "", "", System.currentTimeMillis()));
                postInstallationStatus("Minecraft installed and launch files verified.", false);
                runOnUiThread(() -> launchInstalledVersion(gameDirectory, plan, resolved.getClasspath(), preparedNatives));
            } catch (Exception e) {
                String message = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
                installationStateStore.save(new InstallationState(profile.getVersion(),
                        InstallationState.Status.FAILED, 0, 0, "", message, System.currentTimeMillis()));
                postInstallationStatus("Preparation failed: " + message, true);
            } finally {
                installing.set(false);
                runOnUiThread(() -> {
                    playButton.setEnabled(true);
                    startPlayPulse();
                });
            }
        }, "droid-install-and-launch").start();
    }

    private void postInstallationStatus(String message, boolean failed) {
        runOnUiThread(() -> {
            installationStatus.setText(message);
            launchController.onLaunchUpdate(LaunchObservation.state(
                    failed ? LaunchState.FAILED : LaunchState.PREPARING, message));
        });
    }

    private void launchInstalledVersion(File gameDirectory, MinecraftInstallationService.InstallationPlan plan,
                                        String classpath, File nativesDirectory) {
        MinecraftAuthSession session = minecraftSession;
        if (session == null || session.getProfile().isExpired(System.currentTimeMillis())) {
            launchController.onLaunchUpdate(LaunchObservation.state(LaunchState.FAILED,
                    "Minecraft authentication session is missing or expired"));
            return;
        }
        List<JavaRuntime> runtimes = new JavaRuntimeDetector().detect();
        JavaRuntime runtime = null;
        if (!profile.getJavaExecutable().trim().isEmpty()) {
            for (JavaRuntime candidate : runtimes) {
                if (candidate.getJavaExecutable().getAbsolutePath().equals(profile.getJavaExecutable())) {
                    runtime = candidate; break;
                }
            }
        } else if (!runtimes.isEmpty()) runtime = runtimes.get(0);
        if (runtime == null) {
            launchController.onLaunchUpdate(LaunchObservation.state(LaunchState.FAILED, "No usable Java runtime found"));
            return;
        }
        String mainClass = plan.getMetadata().getMainClass().isEmpty()
                ? "net.minecraft.client.main.Main" : plan.getMetadata().getMainClass();
        AuthenticatedProfile authenticated = session.getProfile();
        List<String> gameArguments = new MinecraftLaunchArguments().build(
                plan.getMetadata().getId(), mainClass, authenticated.getDisplayName(),
                authenticated.getUuid(), session.getMinecraftAccessToken(), gameDirectory,
                new File(gameDirectory, "assets"), plan.getMetadata().getAssetsIndexId());
        ArrayList<String> jvmArguments = new ArrayList<>();
        jvmArguments.add("-Djava.library.path=" + nativesDirectory.getAbsolutePath());
        jvmArguments.add("-Xms" + profile.getMinRamMb() + "M");
        jvmArguments.add("-Xmx" + profile.getMaxRamMb() + "M");
        launchController.launch(runtime, gameDirectory, nativesDirectory, classpath,
                mainClass, jvmArguments, gameArguments, Collections.emptyMap());
    }

    private void refreshInstallationStatus() {
        InstallationState state = installationStateStore.load();
        if (state.getVersionId().isEmpty()) {
            installationStatus.setText("Installation: not started"); return;
        }
        String progress = state.getTotalTasks() > 0 ? " (" + state.getCompletedTasks() + "/" + state.getTotalTasks() + ")" : "";
        String detail = state.getCurrentTask().isEmpty() ? "" : "\n" + state.getCurrentTask();
        String error = state.getError().isEmpty() ? "" : "\n" + state.getError();
        installationStatus.setText("Installation " + state.getVersionId() + ": " + state.getStatus().name()
                + progress + detail + error);
    }

    private void refreshProfileStatus() {
        ProfileValidator.ValidationResult result = ProfileValidator.validate(profile);
        String state = result.isValid() ? "READY" : "NEEDS SETUP";
        profileStatus.setText("Profile: " + profile.getId() + "\nVersion: " + profile.getVersion() + "\nStatus: " + state);
    }

    @Override
    protected void onDestroy() {
        if (launchController != null) launchController.shutdown();
        if (signInCoordinator != null) signInCoordinator.shutdown();
        super.onDestroy();
    }
}
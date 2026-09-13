package com.droidlauncher;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.droidlauncher.launcher.InstallationState;
import com.droidlauncher.launcher.InstallationStateStore;
import com.droidlauncher.launcher.LaunchObservation;
import com.droidlauncher.launcher.LaunchState;
import com.droidlauncher.launcher.LaunchUiController;
import com.droidlauncher.launcher.MinecraftDownloadOrchestrator;
import com.droidlauncher.launcher.MinecraftInstallationService;
import com.droidlauncher.launcher.MinecraftProfile;
import com.droidlauncher.launcher.ProfileStore;
import com.droidlauncher.launcher.ProfileValidator;
import com.droidlauncher.runtime.JavaRuntime;
import com.droidlauncher.runtime.JavaRuntimeDetector;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public final class MainActivity extends Activity {
    private ProfileStore profileStore;
    private MinecraftProfile profile;
    private TextView profileStatus;
    private TextView installationStatus;
    private TextView launchStatus;
    private TextView launchDetail;
    private Button playButton;
    private LaunchUiController launchController;
    private InstallationStateStore installationStateStore;
    private final AtomicBoolean installing = new AtomicBoolean(false);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.rgb(5, 6, 10));
        getWindow().setNavigationBarColor(Color.rgb(5, 6, 10));

        profileStore = new ProfileStore(this);
        profile = profileStore.load();
        if (profile == null) {
            profile = new MinecraftProfile("default", "latest", "", "", 512, 2048);
            profileStore.save(profile);
        }
        installationStateStore = new InstallationStateStore(this);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(48, 32, 48, 32);
        root.setBackgroundColor(Color.rgb(10, 12, 18));

        TextView title = new TextView(this);
        title.setText("DROID LAUNCHER");
        title.setTextColor(Color.WHITE);
        title.setTextSize(30);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        root.addView(title, new LinearLayout.LayoutParams(-1, -2));

        TextView subtitle = new TextView(this);
        subtitle.setText("Minecraft Java • Install Before Launch");
        subtitle.setTextColor(Color.LTGRAY);
        subtitle.setTextSize(15);
        subtitle.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams subtitleParams = new LinearLayout.LayoutParams(-1, -2);
        subtitleParams.topMargin = 12;
        root.addView(subtitle, subtitleParams);

        profileStatus = new TextView(this);
        profileStatus.setTextColor(Color.LTGRAY);
        profileStatus.setTextSize(14);
        profileStatus.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams statusParams = new LinearLayout.LayoutParams(-1, -2);
        statusParams.topMargin = 24;
        root.addView(profileStatus, statusParams);
        refreshProfileStatus();

        installationStatus = new TextView(this);
        installationStatus.setTextColor(Color.rgb(190, 200, 215));
        installationStatus.setTextSize(13);
        installationStatus.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams installParams = new LinearLayout.LayoutParams(-1, -2);
        installParams.topMargin = 14;
        root.addView(installationStatus, installParams);
        refreshInstallationStatus();

        launchStatus = new TextView(this);
        launchStatus.setTextColor(Color.WHITE);
        launchStatus.setTextSize(15);
        launchStatus.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams launchStatusParams = new LinearLayout.LayoutParams(-1, -2);
        launchStatusParams.topMargin = 18;
        root.addView(launchStatus, launchStatusParams);

        launchDetail = new TextView(this);
        launchDetail.setTextColor(Color.LTGRAY);
        launchDetail.setTextSize(12);
        launchDetail.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams detailParams = new LinearLayout.LayoutParams(-1, -2);
        detailParams.topMargin = 8;
        root.addView(launchDetail, detailParams);

        launchController = new LaunchUiController(launchStatus, launchDetail);
        launchController.onLaunchUpdate(LaunchObservation.state(LaunchState.IDLE, "Launcher ready"));

        LinearLayout actions = new LinearLayout(this);
        actions.setGravity(Gravity.CENTER);

        playButton = new Button(this);
        playButton.setText("PLAY");
        playButton.setTextSize(18);
        playButton.setOnClickListener(v -> startMinecraft());
        LinearLayout.LayoutParams playParams = new LinearLayout.LayoutParams(320, 76);
        playParams.topMargin = 22;
        actions.addView(playButton, playParams);

        Button stopButton = new Button(this);
        stopButton.setText("STOP");
        stopButton.setOnClickListener(v -> launchController.stop());
        LinearLayout.LayoutParams stopParams = new LinearLayout.LayoutParams(220, 76);
        stopParams.leftMargin = 16;
        actions.addView(stopButton, stopParams);
        root.addView(actions);

        setContentView(root);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (installationStateStore != null) refreshInstallationStatus();
    }

    private void startMinecraft() {
        if (!installing.compareAndSet(false, true)) return;
        ProfileValidator.ValidationResult result = ProfileValidator.validate(profile);
        if (!result.isValid()) {
            installing.set(false);
            launchController.onLaunchUpdate(LaunchObservation.state(LaunchState.FAILED, result.getMessage()));
            return;
        }

        playButton.setEnabled(false);
        launchController.onLaunchUpdate(LaunchObservation.state(LaunchState.PREPARING,
                "Preparing Minecraft installation..."));

        new Thread(() -> {
            try {
                File gameDirectory = new File(profile.getGameDirectory());
                MinecraftInstallationService service = new MinecraftInstallationService();
                MinecraftInstallationService.InstallationPlan plan = service.prepare(profile.getVersion(), gameDirectory);
                int total = plan.getTasks().size();
                installationStateStore.save(new InstallationState(plan.getMetadata().getId(),
                        InstallationState.Status.RUNNING, 0, total, "preparing", "", System.currentTimeMillis()));
                postInstallationStatus("Installing Minecraft " + plan.getMetadata().getId() + "...", false);

                service.install(plan, new MinecraftDownloadOrchestrator(), progress -> {
                    installationStateStore.save(new InstallationState(plan.getMetadata().getId(),
                            InstallationState.Status.RUNNING, progress.getCompletedTasks(),
                            progress.getTotalTasks(), progress.getTaskName(), "", System.currentTimeMillis()));
                    postInstallationStatus("Installing " + progress.getCompletedTasks() + "/"
                            + progress.getTotalTasks(), false);
                });

                installationStateStore.save(new InstallationState(plan.getMetadata().getId(),
                        InstallationState.Status.COMPLETED, total, total, "", "", System.currentTimeMillis()));
                postInstallationStatus("Minecraft installed and verified.", false);
                runOnUiThread(() -> launchInstalledVersion(gameDirectory, plan));
            } catch (Exception e) {
                String message = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
                installationStateStore.save(new InstallationState(profile.getVersion(),
                        InstallationState.Status.FAILED, 0, 0, "", message, System.currentTimeMillis()));
                postInstallationStatus("Installation failed: " + message, true);
            } finally {
                installing.set(false);
                runOnUiThread(() -> playButton.setEnabled(true));
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

    private void launchInstalledVersion(File gameDirectory,
                                        MinecraftInstallationService.InstallationPlan plan) {
        File nativesDirectory = new File(gameDirectory, "natives");
        List<JavaRuntime> runtimes = new JavaRuntimeDetector().detect();
        JavaRuntime runtime = null;
        if (!profile.getJavaExecutable().trim().isEmpty()) {
            for (JavaRuntime candidate : runtimes) {
                if (candidate.getJavaExecutable().getAbsolutePath().equals(profile.getJavaExecutable())) {
                    runtime = candidate;
                    break;
                }
            }
        } else if (!runtimes.isEmpty()) {
            runtime = runtimes.get(0);
        }
        String versionId = plan.getMetadata().getId();
        String classpath = new File(new File(new File(gameDirectory, "versions"), versionId),
                versionId + ".jar").getAbsolutePath();
        String mainClass = plan.getMetadata().getMainClass().isEmpty()
                ? "net.minecraft.client.main.Main" : plan.getMetadata().getMainClass();
        launchController.launch(runtime, gameDirectory, nativesDirectory, classpath,
                mainClass, Collections.emptyList(), Collections.emptyList(), Collections.emptyMap());
    }

    private void refreshInstallationStatus() {
        InstallationState state = installationStateStore.load();
        if (state.getVersionId().isEmpty()) {
            installationStatus.setText("Installation: not started");
            return;
        }
        String progress = state.getTotalTasks() > 0
                ? " (" + state.getCompletedTasks() + "/" + state.getTotalTasks() + ")" : "";
        String detail = state.getCurrentTask().isEmpty() ? "" : "\n" + state.getCurrentTask();
        String error = state.getError().isEmpty() ? "" : "\n" + state.getError();
        installationStatus.setText("Installation " + state.getVersionId() + ": "
                + state.getStatus().name() + progress + detail + error);
    }

    private void refreshProfileStatus() {
        ProfileValidator.ValidationResult result = ProfileValidator.validate(profile);
        String state = result.isValid() ? "READY" : "NEEDS SETUP";
        profileStatus.setText("Profile: " + profile.getId()
                + "\nVersion: " + profile.getVersion()
                + "\nStatus: " + state);
    }

    @Override
    protected void onDestroy() {
        if (launchController != null) launchController.shutdown();
        super.onDestroy();
    }
}

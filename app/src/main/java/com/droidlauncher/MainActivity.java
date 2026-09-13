package com.droidlauncher;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.droidlauncher.launcher.LaunchObservation;
import com.droidlauncher.launcher.LaunchState;
import com.droidlauncher.launcher.LaunchUiController;
import com.droidlauncher.launcher.MinecraftProfile;
import com.droidlauncher.launcher.ProfileStore;
import com.droidlauncher.launcher.ProfileValidator;
import com.droidlauncher.runtime.JavaRuntime;
import com.droidlauncher.runtime.JavaRuntimeDetector;

import java.io.File;
import java.util.Collections;
import java.util.List;

public final class MainActivity extends Activity {
    private ProfileStore profileStore;
    private MinecraftProfile profile;
    private TextView profileStatus;
    private TextView launchStatus;
    private TextView launchDetail;
    private Button playButton;
    private LaunchUiController launchController;

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
        subtitle.setText("Minecraft Java • Real Launch Pipeline");
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

    private void startMinecraft() {
        ProfileValidator.ValidationResult result = ProfileValidator.validate(profile);
        if (!result.isValid()) {
            launchController.onLaunchUpdate(LaunchObservation.state(LaunchState.FAILED, result.getMessage()));
            return;
        }

        playButton.setEnabled(false);
        File gameDirectory = new File(profile.getGameDirectory());
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

        String classpath = new File(gameDirectory, "versions/" + profile.getVersion()
                + "/" + profile.getVersion() + ".jar").getAbsolutePath();

        launchController.launch(runtime, gameDirectory, nativesDirectory, classpath,
                "net.minecraft.client.main.Main", Collections.emptyList(), Collections.emptyList(),
                Collections.emptyMap());
        playButton.postDelayed(() -> playButton.setEnabled(true), 1000L);
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

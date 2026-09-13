package com.droidlauncher;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.droidlauncher.launcher.MinecraftProfile;
import com.droidlauncher.launcher.ProfileStore;
import com.droidlauncher.launcher.ProfileValidator;

public final class MainActivity extends Activity {
    private ProfileStore profileStore;
    private MinecraftProfile profile;
    private TextView profileStatus;

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
        subtitle.setText("Minecraft Java • Step 162 Profile Engine");
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

        Button play = new Button(this);
        play.setText("PLAY");
        play.setTextSize(18);
        play.setOnClickListener(v -> {
            ProfileValidator.ValidationResult result = ProfileValidator.validate(profile);
            if (!result.isValid()) {
                Toast.makeText(this, "Cannot launch: " + result.getMessage(), Toast.LENGTH_LONG).show();
                return;
            }
            Toast.makeText(this,
                    "Profile ready. Next engine stages: Java → libraries → natives → GLFW/LWJGL → Minecraft",
                    Toast.LENGTH_LONG).show();
        });
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(320, 76);
        buttonParams.topMargin = 30;
        root.addView(play, buttonParams);

        setContentView(root);
    }

    private void refreshProfileStatus() {
        ProfileValidator.ValidationResult result = ProfileValidator.validate(profile);
        String state = result.isValid() ? "READY" : "NEEDS SETUP";
        profileStatus.setText("Profile: " + profile.getId()
                + "\nVersion: " + profile.getVersion()
                + "\nStatus: " + state);
    }
}

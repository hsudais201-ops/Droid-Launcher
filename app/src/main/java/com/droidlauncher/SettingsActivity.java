package com.droidlauncher;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public final class SettingsActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.rgb(5, 6, 10));
        getWindow().setNavigationBarColor(Color.rgb(5, 6, 10));

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(48, 24, 48, 24);
        root.setBackgroundColor(Color.rgb(7, 10, 15));

        TextView title = new TextView(this);
        title.setText("SETTINGS");
        title.setTextColor(Color.WHITE);
        title.setTextSize(28);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        root.addView(title, new LinearLayout.LayoutParams(-1, -2));

        TextView subtitle = new TextView(this);
        subtitle.setText("DROID LAUNCHER CONFIGURATION");
        subtitle.setTextColor(Color.rgb(185, 200, 215));
        subtitle.setTextSize(12);
        subtitle.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams subtitleParams = new LinearLayout.LayoutParams(-1, -2);
        subtitleParams.topMargin = 6;
        root.addView(subtitle, subtitleParams);

        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(24, 20, 24, 20);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.argb(180, 14, 19, 27));
        bg.setCornerRadius(26f);
        bg.setStroke(1, Color.argb(90, 255, 255, 255));
        panel.setBackground(bg);
        LinearLayout.LayoutParams panelParams = new LinearLayout.LayoutParams(620, -2);
        panelParams.topMargin = 24;
        root.addView(panel, panelParams);

        addSettingButton(panel, "CONTROLS", "Customize touch controls", v ->
                startActivity(new Intent(this, CustomizeControlsActivity.class)));
        addSettingButton(panel, "CONTROL PROFILES", "Survival, PvP, Building and custom layouts", v ->
                startActivity(new Intent(this, TouchControlProfilesActivity.class)));
        addSettingButton(panel, "MEMORY & PERFORMANCE", "Runtime and device tuning", v ->
                showInfo("Performance settings are wired for the next optimization step."));
        addSettingButton(panel, "ACCOUNT", "Microsoft / Minecraft account", v ->
                showInfo("Account management is available on the launcher home screen."));
        addSettingButton(panel, "ABOUT", "Droid Launcher build information", v ->
                showInfo("Droid Launcher • Minecraft Java • Android"));

        Button back = createButton("BACK");
        back.setOnClickListener(v -> finish());
        LinearLayout.LayoutParams backParams = new LinearLayout.LayoutParams(220, 64);
        backParams.gravity = Gravity.CENTER_HORIZONTAL;
        backParams.topMargin = 18;
        root.addView(back, backParams);

        setContentView(root);
    }

    private void addSettingButton(LinearLayout parent, String title, String subtitle,
                                  android.view.View.OnClickListener listener) {
        Button button = createButton(title + "\n" + subtitle);
        button.setTextSize(15);
        button.setOnClickListener(listener);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, 76);
        params.topMargin = 8;
        parent.addView(button, params);
    }

    private Button createButton(String text) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextColor(Color.WHITE);
        button.setAllCaps(false);
        button.setStateListAnimator(null);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.argb(150, 30, 40, 53));
        bg.setCornerRadius(20f);
        bg.setStroke(1, Color.argb(100, 190, 215, 240));
        button.setBackground(bg);
        return button;
    }

    private void showInfo(String message) {
        new android.app.AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }
}

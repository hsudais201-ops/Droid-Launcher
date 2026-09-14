package com.droidlauncher;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

/** Built-in presets plus unlimited named touch-control layouts. */
public final class TouchControlProfilesActivity extends Activity {
    private TouchControlProfileStore profiles;
    private TouchControlStore controls;
    private LinearLayout customList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.rgb(5, 6, 10));
        getWindow().setNavigationBarColor(Color.rgb(5, 6, 10));
        profiles = new TouchControlProfileStore(this);
        controls = new TouchControlStore(this);
        buildUi();
        refreshCustomList();
    }

    private void buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(24, 20, 24, 20);
        root.setBackgroundColor(Color.rgb(7, 10, 15));

        TextView title = title("CONTROL PROFILES");
        root.addView(title, new LinearLayout.LayoutParams(-1, 58));
        TextView subtitle = text("Choose a preset or save unlimited custom layouts. Applied layouts become the active control configuration.");
        root.addView(subtitle, new LinearLayout.LayoutParams(-1, 50));

        LinearLayout presets = panel();
        root.addView(label("BUILT-IN PRESETS"));
        String[] names = {"Survival", "PvP", "Building", "Parkour", "Creative", "Controller"};
        for (String name : names) addPreset(presets, name);
        root.addView(presets, new LinearLayout.LayoutParams(-1, 0, 1));

        root.addView(label("CUSTOM LAYOUTS"));
        Button save = button("SAVE CURRENT AS PROFILE");
        save.setOnClickListener(v -> showSaveDialog());
        root.addView(save, margin(320, 56, 0, 8));

        customList = new LinearLayout(this);
        customList.setOrientation(LinearLayout.HORIZONTAL);
        customList.setGravity(Gravity.CENTER);
        root.addView(customList, new LinearLayout.LayoutParams(-1, 74));

        Button edit = button("OPEN CUSTOMIZE CONTROLS");
        edit.setOnClickListener(v -> startActivity(new android.content.Intent(this, CustomizeControlsActivity.class)));
        root.addView(edit, margin(320, 56, 0, 8));

        Button back = button("BACK");
        back.setOnClickListener(v -> finish());
        root.addView(back, centered(180, 54, 4));
        setContentView(root);
    }

    private void addPreset(LinearLayout parent, String name) {
        Button b = button(name);
        b.setTextSize(13);
        b.setOnClickListener(v -> applyPreset(name));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, 58, 1);
        p.setMargins(5, 5, 5, 5);
        parent.addView(b, p);
    }

    private void applyPreset(String name) {
        List<TouchControlConfig> preset = profiles.preset(name);
        if (preset == null) return;
        controls.save(preset);
        profiles.saveProfile(name, preset);
        Toast.makeText(this, name + " layout applied", Toast.LENGTH_SHORT).show();
        refreshCustomList();
    }

    private void showSaveDialog() {
        final android.widget.EditText input = new android.widget.EditText(this);
        input.setSingleLine(true);
        input.setHint("Profile name");
        new AlertDialog.Builder(this)
                .setTitle("Save Control Profile")
                .setView(input)
                .setPositiveButton("SAVE", (d, w) -> {
                    String name = input.getText().toString().trim();
                    if (name.isEmpty()) { Toast.makeText(this, "Enter a profile name", Toast.LENGTH_SHORT).show(); return; }
                    profiles.saveProfile(name, controls.load());
                    refreshCustomList();
                    Toast.makeText(this, "Profile saved: " + name, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("CANCEL", null)
                .show();
    }

    private void refreshCustomList() {
        if (customList == null) return;
        customList.removeAllViews();
        List<String> names = profiles.listProfiles();
        for (String name : names) {
            Button load = button(name + "  ▶");
            load.setTextSize(11);
            load.setOnClickListener(v -> loadProfile(name));
            load.setOnLongClickListener(v -> {
                confirmDelete(name);
                return true;
            });
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, 58, 1);
            p.setMargins(4, 0, 4, 0);
            customList.addView(load, p);
        }
    }

    private void loadProfile(String name) {
        List<TouchControlConfig> layout = profiles.loadProfile(name);
        if (layout == null) { Toast.makeText(this, "No saved layout for " + name, Toast.LENGTH_SHORT).show(); return; }
        controls.save(layout);
        Toast.makeText(this, name + " layout loaded", Toast.LENGTH_SHORT).show();
    }

    private void confirmDelete(String name) {
        if (name.equalsIgnoreCase("Custom") || name.equalsIgnoreCase("Survival") || name.equalsIgnoreCase("PvP")
                || name.equalsIgnoreCase("Building") || name.equalsIgnoreCase("Parkour")
                || name.equalsIgnoreCase("Creative") || name.equalsIgnoreCase("Controller")) {
            Toast.makeText(this, "Built-in profile cannot be deleted", Toast.LENGTH_SHORT).show();
            return;
        }
        new AlertDialog.Builder(this).setTitle("Delete " + name + "?")
                .setMessage("This removes the saved control profile.")
                .setPositiveButton("DELETE", (d, w) -> { profiles.deleteProfile(name); refreshCustomList(); })
                .setNegativeButton("CANCEL", null).show();
    }

    private TextView title(String value) { TextView t = text(value); t.setTextSize(24); t.setTypeface(Typeface.DEFAULT, Typeface.BOLD); t.setGravity(Gravity.CENTER); return t; }
    private TextView label(String value) { TextView t = text(value); t.setTextSize(12); t.setTypeface(Typeface.DEFAULT, Typeface.BOLD); t.setPadding(4, 8, 4, 4); return t; }
    private TextView text(String value) { TextView t = new TextView(this); t.setText(value); t.setTextColor(Color.WHITE); t.setTextSize(13); t.setGravity(Gravity.CENTER_VERTICAL); return t; }

    private LinearLayout panel() { LinearLayout p = new LinearLayout(this); p.setOrientation(LinearLayout.HORIZONTAL); p.setPadding(4, 4, 4, 4); GradientDrawable bg = new GradientDrawable(); bg.setColor(Color.rgb(13, 18, 25)); bg.setCornerRadius(20f); bg.setStroke(1, Color.argb(90, 255, 255, 255)); p.setBackground(bg); return p; }

    private Button button(String value) { Button b = new Button(this); b.setText(value); b.setTextColor(Color.WHITE); b.setAllCaps(false); b.setStateListAnimator(null); GradientDrawable bg = new GradientDrawable(); bg.setColor(Color.rgb(29, 40, 52)); bg.setCornerRadius(18f); bg.setStroke(1, Color.argb(120, 200, 225, 245)); b.setBackground(bg); return b; }
    private LinearLayout.LayoutParams margin(int w, int h, int left, int top) { LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(w, h); p.gravity = Gravity.CENTER_HORIZONTAL; p.setMargins(left, top, 0, 0); return p; }
    private LinearLayout.LayoutParams centered(int w, int h, int top) { return margin(w, h, 0, top); }
}

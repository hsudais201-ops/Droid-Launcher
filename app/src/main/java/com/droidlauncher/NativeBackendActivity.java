package com.droidlauncher;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.droidlauncher.launcher.NativeAbi;
import com.droidlauncher.launcher.NativeBackendInspector;
import com.droidlauncher.launcher.NativeBackendRuntime;
import com.droidlauncher.launcher.NativeBackendStatus;

import java.io.File;

/** Settings screen for inspecting and repairing the Android native rendering backend. */
public final class NativeBackendActivity extends Activity {
    private TextView status;
    private TextView detail;
    private File backendRoot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.rgb(5, 6, 10));
        getWindow().setNavigationBarColor(Color.rgb(5, 6, 10));
        backendRoot = new File(getFilesDir(), "native-backends");

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(48, 24, 48, 24);
        root.setBackgroundColor(Color.rgb(7, 10, 15));

        TextView title = new TextView(this);
        title.setText("NATIVE BACKEND");
        title.setTextColor(Color.WHITE);
        title.setTextSize(28);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        root.addView(title, new LinearLayout.LayoutParams(-1, -2));

        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(24, 20, 24, 20);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.argb(180, 14, 19, 27));
        bg.setCornerRadius(26f);
        bg.setStroke(1, Color.argb(90, 255, 255, 255));
        panel.setBackground(bg);
        LinearLayout.LayoutParams panelParams = new LinearLayout.LayoutParams(700, -2);
        panelParams.topMargin = 22;
        root.addView(panel, panelParams);

        status = new TextView(this);
        status.setTextColor(Color.WHITE);
        status.setTextSize(18);
        status.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        status.setGravity(Gravity.CENTER);
        panel.addView(status, new LinearLayout.LayoutParams(-1, -2));

        detail = new TextView(this);
        detail.setTextColor(Color.rgb(190, 205, 220));
        detail.setTextSize(13);
        detail.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams detailParams = new LinearLayout.LayoutParams(-1, -2);
        detailParams.topMargin = 10;
        panel.addView(detail, detailParams);

        LinearLayout actions = new LinearLayout(this);
        actions.setGravity(Gravity.CENTER);
        Button refresh = createButton("REFRESH");
        refresh.setOnClickListener(v -> inspect());
        actions.addView(refresh, new LinearLayout.LayoutParams(180, 64));
        Button repair = createButton("REPAIR / INSTALL");
        repair.setOnClickListener(v -> repair());
        LinearLayout.LayoutParams repairParams = new LinearLayout.LayoutParams(220, 64);
        repairParams.leftMargin = 12;
        actions.addView(repair, repairParams);
        panel.addView(actions, new LinearLayout.LayoutParams(-1, 70));

        Button back = createButton("BACK");
        back.setOnClickListener(v -> finish());
        LinearLayout.LayoutParams backParams = new LinearLayout.LayoutParams(220, 64);
        backParams.gravity = Gravity.CENTER_HORIZONTAL;
        backParams.topMargin = 18;
        root.addView(back, backParams);
        setContentView(root);
        inspect();
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

    private void inspect() {
        NativeBackendStatus snapshot = new NativeBackendInspector().inspect(this, backendRoot);
        status.setText(snapshot.isVerified() ? "VERIFIED" : (snapshot.isPresent() ? "INVALID" : "NOT INSTALLED"));
        String path = snapshot.getBackendFile() == null ? "Not available" : snapshot.getBackendFile().getPath();
        detail.setText("ABI: " + snapshot.getAbi().getAndroidAbi()
                + "\nLibrary: " + snapshot.getLibraryFileName()
                + "\nPath: " + path
                + "\nStatus: " + snapshot.getMessage());
    }

    private void repair() {
        status.setText("PREPARING");
        detail.setText("Loading ABI-specific backend configuration…");
        new Thread(() -> {
            try {
                new NativeBackendRuntime().prepare(this, backendRoot);
                runOnUiThread(this::inspect);
            } catch (Exception e) {
                runOnUiThread(() -> {
                    status.setText("REPAIR FAILED");
                    detail.setText(e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage());
                });
            }
        }, "droid-native-backend-repair").start();
    }
}

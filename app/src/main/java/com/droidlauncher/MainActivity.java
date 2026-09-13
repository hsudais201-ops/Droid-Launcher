package com.droidlauncher;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public final class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.rgb(5, 6, 10));
        getWindow().setNavigationBarColor(Color.rgb(5, 6, 10));

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
        subtitle.setText("Minecraft Java • Step 161 Real Launch Engine");
        subtitle.setTextColor(Color.LTGRAY);
        subtitle.setTextSize(15);
        subtitle.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams subtitleParams = new LinearLayout.LayoutParams(-1, -2);
        subtitleParams.topMargin = 12;
        root.addView(subtitle, subtitleParams);

        Button play = new Button(this);
        play.setText("PLAY");
        play.setTextSize(18);
        play.setOnClickListener(v -> Toast.makeText(this,
                "Launch pipeline is being prepared: profile → Java → libraries → natives → GLFW/LWJGL → Minecraft",
                Toast.LENGTH_LONG).show());
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(320, 76);
        buttonParams.topMargin = 36;
        root.addView(play, buttonParams);

        setContentView(root);
    }
}

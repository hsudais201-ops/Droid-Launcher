package com.droidlauncher;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

/** Hosts the Minecraft surface plus the configurable Android touch-control overlay. */
public final class MinecraftGameplayActivity extends Activity {
    private MinecraftGameplayInputLayer gameplayLayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setStatusBarColor(Color.BLACK);
        getWindow().setNavigationBarColor(Color.BLACK);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        gameplayLayer = new MinecraftGameplayInputLayer(this);
        gameplayLayer.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        setContentView(gameplayLayer);
    }

    @Override
    protected void onPause() {
        if (gameplayLayer != null) gameplayLayer.releaseInputs();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (gameplayLayer != null) gameplayLayer.releaseInputs();
        super.onDestroy();
    }
}

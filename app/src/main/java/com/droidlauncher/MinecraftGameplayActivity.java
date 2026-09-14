package com.droidlauncher;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.droidlauncher.launcher.AndroidSurfaceBridge;

/** Hosts the Minecraft surface plus the configurable Android touch-control overlay. */
public final class MinecraftGameplayActivity extends Activity {
    private MinecraftGameplayInputLayer gameplayLayer;
    private AndroidSurfaceBridge surfaceBridge;

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

        surfaceBridge = new AndroidSurfaceBridge(gameplayLayer.getSurfaceView());
        surfaceBridge.setListener(new AndroidSurfaceBridge.Listener() {
            @Override public void onSurfaceAvailable(android.view.Surface surface, int width, int height) {
                // Native renderer hookup belongs here once the Android-native GLFW/LWJGL runtime exists.
            }

            @Override public void onSurfaceSizeChanged(android.view.Surface surface, int width, int height) {
                // Forward size changes to the native renderer when implemented.
            }

            @Override public void onSurfaceDestroyed() {
                // Native renderer must release its surface reference here.
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (gameplayLayer != null) gameplayLayer.refreshControls();
        if (surfaceBridge != null) surfaceBridge.attach();
    }

    @Override
    protected void onPause() {
        if (surfaceBridge != null) surfaceBridge.detach();
        if (gameplayLayer != null) gameplayLayer.releaseInputs();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (surfaceBridge != null) surfaceBridge.detach();
        if (gameplayLayer != null) gameplayLayer.releaseInputs();
        super.onDestroy();
    }
}

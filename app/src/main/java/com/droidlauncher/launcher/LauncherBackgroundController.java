package com.droidlauncher.launcher;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/** Loads a cached Minecraft landscape image, particles, and lightweight launcher motion. */
public final class LauncherBackgroundController {
    private static final String IMAGE_URL =
            "https://commons.wikimedia.org/wiki/Special:Redirect/file/Minecraft_-_Taiga.jpg";
    private static final String CACHE_FILE = "launcher-background.jpg";
    private static final String PARTICLE_TAG = "droid-launcher-particles";
    private static final String PLAY_PULSE_TAG = "droid-launcher-play-pulse";
    private static final long MAX_CACHE_AGE_MS = 7L * 24L * 60L * 60L * 1000L;

    private LauncherBackgroundController() { }

    public static void install(Activity activity) {
        if (activity == null) return;
        final View content = activity.findViewById(android.R.id.content);
        if (content == null) return;
        content.post(() -> loadAndApply(activity, content));
    }

    private static void loadAndApply(Activity activity, View content) {
        File cache = new File(activity.getFilesDir(), CACHE_FILE);
        Bitmap bitmap = decodeCache(cache);
        if (bitmap != null) {
            apply(content, bitmap);
            installParticles(content);
            installPlayPulse(content);
            if (System.currentTimeMillis() - cache.lastModified() < MAX_CACHE_AGE_MS) return;
        }
        new Thread(() -> {
            Bitmap downloaded = download(cache);
            if (downloaded == null) return;
            activity.runOnUiThread(() -> {
                apply(content, downloaded);
                installParticles(content);
                installPlayPulse(content);
            });
        }, "droid-background-loader").start();
    }

    private static Bitmap decodeCache(File cache) {
        if (!cache.isFile() || cache.length() <= 0L) return null;
        try (FileInputStream input = new FileInputStream(cache)) {
            return BitmapFactory.decodeStream(input);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static Bitmap download(File cache) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(IMAGE_URL).openConnection();
            connection.setConnectTimeout(8000);
            connection.setReadTimeout(12000);
            connection.setInstanceFollowRedirects(true);
            connection.setRequestProperty("User-Agent", "DroidLauncher/1.0");
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) return null;
            try (InputStream input = connection.getInputStream();
                 FileOutputStream output = new FileOutputStream(cache)) {
                byte[] buffer = new byte[8192];
                int read;
                long total = 0L;
                while ((read = input.read(buffer)) != -1) {
                    total += read;
                    if (total > 2_000_000L) return null;
                    output.write(buffer, 0, read);
                }
            }
            return decodeCache(cache);
        } catch (Exception ignored) {
            return null;
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    private static void apply(View content, Bitmap bitmap) {
        Drawable photo = new BitmapDrawable(content.getResources(), bitmap);
        photo.setAlpha(150);
        Drawable shade = new ColorDrawable(Color.argb(115, 0, 0, 0));
        LayerDrawable background = new LayerDrawable(new Drawable[]{photo, shade});
        if (content instanceof ViewGroup) {
            View root = ((ViewGroup) content).getChildCount() > 0
                    ? ((ViewGroup) content).getChildAt(0) : null;
            if (root != null) root.setBackground(background);
        }
    }

    private static void installParticles(View content) {
        if (!(content instanceof ViewGroup)) return;
        ViewGroup container = (ViewGroup) content;
        if (container.findViewWithTag(PARTICLE_TAG) != null) return;
        ParticleOverlayView particles = new ParticleOverlayView(content.getContext());
        particles.setTag(PARTICLE_TAG);
        particles.setClickable(false);
        particles.setFocusable(false);
        container.addView(particles, 0, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        particles.startAnimation();
    }

    private static void installPlayPulse(View root) {
        Button play = findButton(root, "PLAY");
        if (play == null || play.getTag(PLAY_PULSE_TAG.hashCode()) != null) return;

        ObjectAnimator pulse = ObjectAnimator.ofFloat(play, View.SCALE_X, 1.0f, 1.025f);
        pulse.setRepeatMode(ValueAnimator.REVERSE);
        pulse.setRepeatCount(ValueAnimator.INFINITE);
        pulse.setDuration(1300L);
        pulse.setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator());
        pulse.addUpdateListener(animation -> {
            if (!play.isEnabled() || play.isPressed()) {
                play.setScaleX(1.0f);
                play.setScaleY(1.0f);
            } else {
                play.setScaleY(play.getScaleX());
            }
        });
        play.setTag(PLAY_PULSE_TAG.hashCode(), pulse);
        pulse.start();
    }

    private static Button findButton(View root, String label) {
        if (root instanceof Button) {
            CharSequence text = ((Button) root).getText();
            if (text != null && label.equalsIgnoreCase(text.toString().trim())) return (Button) root;
        }
        if (root instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) root;
            for (int i = 0; i < group.getChildCount(); i++) {
                Button found = findButton(group.getChildAt(i), label);
                if (found != null) return found;
            }
        }
        return null;
    }

    /** Small, low-cost floating particles for visual depth; no bitmap generation or GL effects. */
    private static final class ParticleOverlayView extends View {
        private static final int PARTICLE_COUNT = 18;
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final float[] x = new float[PARTICLE_COUNT];
        private final float[] y = new float[PARTICLE_COUNT];
        private final float[] radius = new float[PARTICLE_COUNT];
        private final float[] speed = new float[PARTICLE_COUNT];
        private long animationStart;
        private boolean running;

        ParticleOverlayView(android.content.Context context) {
            super(context);
            paint.setColor(Color.WHITE);
            setAlpha(0.18f);
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            for (int i = 0; i < PARTICLE_COUNT; i++) {
                x[i] = (i * 0.173f) % 1.0f;
                y[i] = (i * 0.271f) % 1.0f;
                radius[i] = 1.5f + (i % 4) * 0.7f;
                speed[i] = 0.000018f + (i % 5) * 0.000006f;
            }
        }

        void startAnimation() {
            if (running) return;
            running = true;
            animationStart = SystemClock.uptimeMillis();
            postInvalidateOnAnimation();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (!running) return;
            long elapsed = SystemClock.uptimeMillis() - animationStart;
            float width = getWidth();
            float height = getHeight();
            if (width <= 0 || height <= 0) return;
            for (int i = 0; i < PARTICLE_COUNT; i++) {
                float drift = (elapsed * speed[i]) % 1.15f;
                float py = (y[i] - drift + 1.15f) % 1.15f;
                float px = x[i] + (float) Math.sin((elapsed * speed[i] * 120.0) + i) * 0.012f;
                if (px < 0f) px += 1f;
                if (px > 1f) px -= 1f;
                canvas.drawCircle(px * width, py * height, radius[i], paint);
            }
            postInvalidateOnAnimation();
        }
    }
}

package com.droidlauncher.launcher;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/** Loads a cached Minecraft landscape image behind the existing launcher UI. */
public final class LauncherBackgroundController {
    private static final String IMAGE_URL =
            "https://commons.wikimedia.org/wiki/Special:Redirect/file/Minecraft_-_Taiga.jpg";
    private static final String CACHE_FILE = "launcher-background.jpg";
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
            if (System.currentTimeMillis() - cache.lastModified() < MAX_CACHE_AGE_MS) return;
        }
        new Thread(() -> {
            Bitmap downloaded = download(cache);
            if (downloaded == null) return;
            activity.runOnUiThread(() -> apply(content, downloaded));
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
        content.getBackground();
        if (content instanceof ViewGroup) {
            View root = ((ViewGroup) content).getChildCount() > 0
                    ? ((ViewGroup) content).getChildAt(0) : null;
            if (root != null) root.setBackground(background);
        }
    }
}

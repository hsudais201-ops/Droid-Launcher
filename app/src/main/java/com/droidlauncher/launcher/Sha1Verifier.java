package com.droidlauncher.launcher;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;

/** Small, dependency-free checksum verifier used before accepting downloaded artifacts. */
public final class Sha1Verifier {
    private Sha1Verifier() {}

    public static boolean verify(File file, String expectedSha1) {
        if (file == null || !file.isFile() || expectedSha1 == null || expectedSha1.trim().isEmpty()) return false;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] buffer = new byte[8192];
            try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(file))) {
                int read;
                while ((read = in.read(buffer)) != -1) digest.update(buffer, 0, read);
            }
            return toHex(digest.digest()).equalsIgnoreCase(expectedSha1.trim());
        } catch (Exception ignored) {
            return false;
        }
    }

    private static String toHex(byte[] bytes) {
        StringBuilder out = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) out.append(String.format("%02x", b & 0xff));
        return out.toString();
    }
}

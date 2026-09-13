package com.droidlauncher.launcher;

import java.io.File;

/** Validates the prerequisites that must exist before the process launcher runs. */
public final class ProfileValidator {
    private ProfileValidator() {}

    public static ValidationResult validate(MinecraftProfile profile) {
        if (profile == null) return ValidationResult.error("No Minecraft profile selected");
        if (profile.getVersion().trim().isEmpty()) return ValidationResult.error("Minecraft version is missing");
        if (!profile.getGameDirectory().trim().isEmpty()) {
            File gameDir = new File(profile.getGameDirectory());
            if (gameDir.exists() && !gameDir.isDirectory()) {
                return ValidationResult.error("Game directory is not a directory");
            }
        }
        if (!profile.getJavaExecutable().trim().isEmpty()) {
            File java = new File(profile.getJavaExecutable());
            if (!java.exists() || !java.isFile()) {
                return ValidationResult.error("Configured Java executable was not found");
            }
        }
        if (profile.getMinRamMb() > profile.getMaxRamMb()) {
            return ValidationResult.error("Invalid RAM configuration");
        }
        return ValidationResult.ok();
    }

    public static final class ValidationResult {
        private final boolean valid;
        private final String message;

        private ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }

        public static ValidationResult ok() { return new ValidationResult(true, "OK"); }
        public static ValidationResult error(String message) { return new ValidationResult(false, message); }
        public boolean isValid() { return valid; }
        public String getMessage() { return message; }
    }
}

package com.droidlauncher.launcher;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Immutable process command assembled from the validated launcher runtime inputs. */
public final class MinecraftCommand {
    private final List<String> arguments;
    private final File workingDirectory;
    private final File javaExecutable;

    public MinecraftCommand(File javaExecutable, File workingDirectory, List<String> arguments) {
        if (javaExecutable == null || !javaExecutable.isFile()) {
            throw new IllegalArgumentException("Java executable is missing: " + javaExecutable);
        }
        if (workingDirectory == null || !workingDirectory.isDirectory()) {
            throw new IllegalArgumentException("Working directory is missing: " + workingDirectory);
        }
        if (arguments == null || arguments.isEmpty()) {
            throw new IllegalArgumentException("Process arguments are required");
        }
        List<String> copy = new ArrayList<>();
        for (String argument : arguments) {
            if (argument == null || argument.indexOf('\0') >= 0) {
                throw new IllegalArgumentException("Invalid process argument");
            }
            copy.add(argument);
        }
        this.javaExecutable = javaExecutable;
        this.workingDirectory = workingDirectory;
        this.arguments = Collections.unmodifiableList(copy);
    }

    public File getJavaExecutable() { return javaExecutable; }
    public File getWorkingDirectory() { return workingDirectory; }
    public List<String> getArguments() { return arguments; }

    public List<String> toProcessBuilderCommand() {
        List<String> command = new ArrayList<>(arguments.size() + 1);
        command.add(javaExecutable.getAbsolutePath());
        command.addAll(arguments);
        return command;
    }
}

# Step 183 — Installation UI Recovery

Droid Launcher now restores persisted installation state when the main screen is created or resumed.

## Behavior
- Reads `InstallationStateStore` from the application context.
- Shows the selected Minecraft version and installation status.
- Shows completed/total artifact counts when available.
- Shows the current artifact and error text when present.
- Refreshes the displayed installation state in `onResume()`.
- Keeps installation persistence separate from the Minecraft process-launch state.

The underlying `InstallationResumeService` can resume a persisted RUNNING, FAILED, or CANCELLED installation when the current download plan is available.

This step does not claim that Minecraft has successfully booted; actual device/CI boot verification remains a separate milestone.

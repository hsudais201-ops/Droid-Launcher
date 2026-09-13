# Step 167 — Managed Java Runtime Provisioner

## Goal
Provide a deterministic launcher-owned location for Java runtimes and fail early when a compatible runtime is unavailable.

## Added
- `JavaRuntimeProvisioner`: resolves managed runtime directories such as `java-17/bin/java` and `java-21/bin/java`.
- Runtime lookup is constrained to the launcher-owned runtime root.
- Required Java major-version ranges are validated before a runtime is accepted.
- Missing or non-executable managed runtimes produce an explicit error rather than a later JVM/classpath failure.

## Architecture
The provisioner does not download or unpack arbitrary archives itself. Acquisition, checksum verification, archive extraction, and platform/architecture selection stay in the download/install layer so those operations can be tested independently.

## Important limitation
A full Android-compatible JRE is not yet bundled in the repository. This step supplies the provisioning contract and validation path; it does not claim that Java 17/21 or any other runtime can already execute on every target Android device.

## Next step
Step 168 should add Minecraft library metadata parsing, OS/architecture rule evaluation, deterministic dependency ordering, and final classpath construction using the existing verified download/cache layer.

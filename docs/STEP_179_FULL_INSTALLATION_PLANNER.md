# Step 179 — Full Installation Planner

Step 179 expands installation planning from client JAR + asset index to the complete version dependency set.

## Added

- `AssetObject` models a content-addressed Minecraft asset.
- `FullInstallationPlanner` reads version metadata and an asset index to create verified download tasks.
- Client JAR is planned from `downloads.client`.
- Library artifacts are planned from `libraries[].downloads.artifact`.
- Asset objects are downloaded from the Mojang content-addressed resource endpoint using their SHA-1 hash.
- Library paths are canonicalized and rejected when they are absolute or attempt traversal outside the managed libraries directory.
- Every generated download task requires a URL and SHA-1 checksum.

## Important limitation

This planner consumes an already-fetched version JSON and asset-index JSON. It does not yet fetch those metadata documents itself, resolve advanced library classifiers/native rules, or extract/install native libraries. Those remain separate pipeline stages.

## Verification

The source has been committed, but APK compilation and an end-to-end Minecraft installation/boot have not been verified by a successful GitHub Actions run and device test.

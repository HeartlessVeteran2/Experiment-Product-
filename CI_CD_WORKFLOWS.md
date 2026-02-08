# CI/CD Workflows Documentation

This document describes the Continuous Integration and Continuous Deployment (CI/CD) workflows configured for the Experiment Product Android project.

## Overview

The project includes four automated workflows to ensure code quality, security, and streamlined releases:

1. **Build & Test** - Validates builds and runs unit tests
2. **Static Analysis (Lint)** - Enforces code quality standards
3. **Dependency Scanning (Dependabot)** - Monitors and updates dependencies
4. **Release Automation** - Builds and publishes releases

All workflows are aligned with modern Android/Kotlin best practices and Clean Architecture principles.

---

## 1. Build & Test Workflow

**File**: `.github/workflows/ci.yml`

### Purpose
Automatically builds the project and runs all unit tests on every push and pull request to the `main` branch.

### What It Does
- Sets up JDK 17 (Temurin distribution)
- Configures Gradle with caching for faster builds
- Builds the project with `./gradlew build`
- Runs unit tests with `./gradlew test`
- Uploads build and test reports as artifacts (retained for 30 days)
- Publishes test results to PR checks

### Triggers
- Push to `main` branch
- Pull requests targeting `main` branch

### Required Secrets
- `MAPBOX_DOWNLOADS_TOKEN` - Required for resolving Mapbox dependencies

### Artifacts
Build and test reports are uploaded and available for 30 days after each run.

---

## 2. Static Analysis (Lint) Workflow

**File**: `.github/workflows/lint.yml`

### Purpose
Runs Android Lint checks to enforce code quality standards and catch potential issues early.

### What It Does
- Sets up JDK 17 (Temurin distribution)
- Configures Gradle with caching
- Runs Android Lint with `./gradlew lint`
- Uploads lint reports (HTML and XML) as artifacts
- Annotates PR with inline lint issues for easy review

### Triggers
- Push to `main` branch
- Pull requests targeting `main` branch

### Required Secrets
- `MAPBOX_DOWNLOADS_TOKEN` - Required for resolving Mapbox dependencies

### Artifacts
Lint reports (HTML and XML) are uploaded and available for 30 days.

### Viewing Lint Results
1. Check the workflow run in GitHub Actions
2. Download the lint reports artifact
3. Open the HTML report in a browser
4. Review inline annotations in the PR (if applicable)

---

## 3. Dependency Scanning (Dependabot)

**File**: `.github/dependabot.yml`

### Purpose
Automatically scans Gradle dependencies for security vulnerabilities and version updates.

### What It Does
- **Gradle Dependencies**: Checks daily for updates to project dependencies
- **GitHub Actions**: Checks weekly for updates to workflow actions
- Groups related dependencies together (Android, Compose, Kotlin, Mapbox)
- Opens pull requests with dependency updates
- Labels PRs appropriately for easy filtering

### Configuration Details
- **Gradle**: Daily checks, up to 10 open PRs
- **GitHub Actions**: Weekly checks, up to 5 open PRs
- Dependency groups:
  - `android`: All androidx.* and com.android.* packages
  - `compose`: All androidx.compose.* packages
  - `kotlin`: Kotlin language and kotlinx packages
  - `mapbox`: All com.mapbox.* packages

### How to Handle Dependabot PRs
1. Review the changelog and compatibility notes
2. Check if any breaking changes are introduced
3. Run tests locally if needed
4. Merge the PR to update the dependency

---

## 4. Release Automation Workflow

**File**: `.github/workflows/release.yml`

### Purpose
Automates the build and publication of release artifacts when a version tag is pushed.

### What It Does
- Triggers on version tags (e.g., `v1.0.0`, `v2.1.3`)
- Builds release APK and AAB (Android App Bundle)
- Renames artifacts with version number
- Creates a GitHub Release with:
  - Unsigned APK and AAB attachments
  - Auto-generated release notes
  - Installation instructions

### Triggers
Push tags matching pattern: `v*.*.*` (e.g., `v1.0.0`, `v2.1.3`)

### Required Secrets
- `MAPBOX_DOWNLOADS_TOKEN` - Required for resolving Mapbox dependencies
- (Optional) Signing secrets if you configure APK signing:
  - `SIGNING_KEY`
  - `KEY_ALIAS`
  - `KEY_STORE_PASSWORD`
  - `KEY_PASSWORD`

### Creating a Release

1. **Tag a release:**
   ```bash
   git tag v1.0.0
   git push origin v1.0.0
   ```

2. **Wait for the workflow to complete:**
   The workflow will automatically build and create a GitHub Release.

3. **View the release:**
   Navigate to the repository's Releases page to download the APK or AAB.

### APK Signing (Optional)

The workflow includes commented-out steps for APK signing. To enable:

1. Generate a keystore for your app
2. Convert the keystore to base64:
   ```bash
   base64 -i your-keystore.jks
   ```
3. Add the following secrets to your repository:
   - `SIGNING_KEY`: Base64-encoded keystore
   - `KEY_ALIAS`: Key alias in the keystore
   - `KEY_STORE_PASSWORD`: Keystore password
   - `KEY_PASSWORD`: Key password
4. Uncomment the signing step in `.github/workflows/release.yml`

---

## Security Best Practices

### Secrets Management
All sensitive credentials are stored as GitHub Secrets:
- Never commit secrets to the repository
- Use repository secrets or environment secrets for sensitive data
- Rotate secrets regularly

### Required Secrets
Configure these secrets in your repository settings:

1. **MAPBOX_DOWNLOADS_TOKEN**
   - Navigate to Settings → Secrets and variables → Actions
   - Click "New repository secret"
   - Name: `MAPBOX_DOWNLOADS_TOKEN`
   - Value: Your Mapbox downloads token

2. **(Optional) Signing Secrets** - See "APK Signing" section above

---

## Workflow Status Badges

Add these badges to your README.md to display workflow status:

```markdown
[![Build & Test](https://github.com/Heartless-Veteran/Experiment-Product-/actions/workflows/ci.yml/badge.svg)](https://github.com/Heartless-Veteran/Experiment-Product-/actions/workflows/ci.yml)
[![Lint](https://github.com/Heartless-Veteran/Experiment-Product-/actions/workflows/lint.yml/badge.svg)](https://github.com/Heartless-Veteran/Experiment-Product-/actions/workflows/lint.yml)
[![CodeQL](https://github.com/Heartless-Veteran/Experiment-Product-/actions/workflows/codeql.yml/badge.svg)](https://github.com/Heartless-Veteran/Experiment-Product-/actions/workflows/codeql.yml)
```

---

## Troubleshooting

### Build Failures
1. Check that `MAPBOX_DOWNLOADS_TOKEN` is configured correctly
2. Verify JDK 17 compatibility
3. Review build logs in the Actions tab
4. Ensure `gradlew` has execute permissions

### Lint Failures
1. Run `./gradlew lint` locally to see issues
2. Fix reported issues or update lint baseline
3. Check lint reports in the workflow artifacts

### Dependabot Issues
1. Verify that Dependabot has access to your repository
2. Check that the `dependabot.yml` syntax is correct
3. Review Dependabot logs in the Security → Dependabot tab

### Release Workflow Issues
1. Ensure tag follows the `v*.*.*` pattern
2. Verify all build steps complete successfully
3. Check that GitHub token has write permissions

---

## Additional Resources

- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [Gradle Build Tool](https://gradle.org/)
- [Android Lint Documentation](https://developer.android.com/studio/write/lint)
- [Dependabot Documentation](https://docs.github.com/en/code-security/dependabot)
- [Project Architecture](./ARCHITECTURE.md)

---

## Maintenance

These workflows should be reviewed and updated periodically:

- **JDK Version**: Currently set to JDK 17. Update when project requirements change.
- **Actions Versions**: Dependabot will automatically open PRs for action updates.
- **Gradle Version**: Uses wrapper, update via `./gradlew wrapper --gradle-version X.X.X`
- **Android SDK**: Update in `app/build.gradle.kts` as needed

---

**Last Updated**: 2026-02-08  
**Maintained by**: Development Team

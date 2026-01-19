# Code Formatting Guide

This project uses **Spotless** with **Eclipse formatter** for consistent code formatting.

**Note:** Eclipse formatter is used because Google Java Format doesn't yet support Java 25. Once Java 25 support is available, we may switch back to Google Java Format.

## Quick Commands

### Format all code
```bash
./gradlew spotlessApply
```

### Check if code is formatted (without changing files)
```bash
./gradlew spotlessCheck
```

### Format only Java files
```bash
./gradlew spotlessJavaApply
```

### Format only Kotlin files (build scripts)
```bash
./gradlew spotlessKotlinApply
```

## Usage Examples

### Before committing
```bash
# Format all code before committing
./gradlew spotlessApply

# Then commit
git add .
git commit -m "feat: add new feature"
```

### In CI/CD pipeline
```bash
# Check formatting in CI (fails if code is not formatted)
./gradlew spotlessCheck
```

### Format specific files
```bash
# Format a single file (using Gradle task)
./gradlew spotlessApply -PspotlessFiles=src/main/java/dev/irako/topics/grpc/service/NotificationService.java
```

## Integration with Git

### Pre-commit hook (optional)
You can add a Git pre-commit hook to auto-format:

```bash
# Create pre-commit hook
cat > .git/hooks/pre-commit << 'EOF'
#!/bin/sh
./gradlew spotlessApply
git add -u
EOF

chmod +x .git/hooks/pre-commit
```

## What Gets Formatted

- **Java files** (`src/**/*.java`): Google Java Format
  - Removes unused imports
  - Trims trailing whitespace
  - Ensures files end with newline

- **Kotlin files** (`**/*.kts`): ktlint
  - Formats build scripts and Kotlin code

- **Proto files** (`**/*.proto`): Basic cleanup
  - Trims trailing whitespace
  - Ensures files end with newline

## Editor Integration

The formatter also works in your editor:
- **Cursor/VS Code**: Use `Shift + Option + F` (Mac) or `Shift + Alt + F` (Windows/Linux)
- **Format on save**: Already configured in `.vscode/settings.json`

## Troubleshooting

### If formatting fails
```bash
# Clean and rebuild
./gradlew clean spotlessApply
```

### Check Spotless version
```bash
./gradlew spotless --version
```

### See all available tasks
```bash
./gradlew tasks --all | grep spotless
```

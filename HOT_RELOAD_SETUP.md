# Hot Reload Setup Guide

## IntelliJ IDEA Configuration

1. Enable "Build project automatically"
   - Go to Settings/Preferences (Ctrl+Alt+S)
   - Navigate to Build, Execution, Deployment > Compiler
   - Check "Build project automatically"

2. Enable "Allow auto-make to start even if developed application is currently running"
   - Go to Settings/Preferences (Ctrl+Alt+S)
   - Navigate to Advanced Settings
   - Check "Allow auto-make to start even if developed application is currently running"

3. Configure Registry (for older IntelliJ versions)
   - Press Ctrl+Shift+A (or Help > Find Action)
   - Type "Registry"
   - Find and enable "compiler.automake.allow.when.app.running"

## Running the Application

1. Run the application using:
```bash
mvn spring-boot:run
```

2. For development, you can also run directly from IntelliJ IDEA:
   - Right-click on TransactionManagementApplication.java
   - Select "Run 'TransactionManagementApplication'"

## Hot Reload Features

- Java code changes will trigger automatic restart
- Static resources (templates, static files) will be reloaded without restart
- Excluded paths (static/**, public/**) will not trigger restarts
- LiveReload is enabled for browser refresh on static content changes

## Troubleshooting

If hot reload is not working:

1. Ensure DevTools dependency is properly added to pom.xml
2. Verify application.yml configuration
3. Check if IDE settings are correctly configured
4. Try invalidating caches and restarting IDE:
   - File > Invalidate Caches
   - Select "Invalidate and Restart"

## Notes

- Hot reload works best with small changes
- Some changes (like modifying @Configuration classes) will still require a full restart
- The application will restart automatically when you save changes to Java files
- Static resources (HTML, CSS, JS) will be reloaded without restart 
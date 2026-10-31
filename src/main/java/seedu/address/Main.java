package seedu.address;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.logging.Logger;

import javafx.application.Application;
import seedu.address.commons.core.LogsCenter;

/**
 * The main entry point to the application.
 * <p>
 * This is a workaround for the following error when MainApp is made the
 * entry point of the application:
 * <p>
 * Error: JavaFX runtime components are missing, and are required to run this application
 * <p>
 * The reason is that MainApp extends Application. In that case, the
 * LauncherHelper will check for the javafx.graphics module to be present
 * as a named module. We don't use JavaFX via the module system so it can't
 * find the javafx.graphics module, and so the launch is aborted.
 * <p>
 * By having a separate main class (Main) that doesn't extend Application
 * to be the entry point of the application, we avoid this issue.
 */
public class Main {
    private static final Logger logger = LogsCenter.getLogger(Main.class);

    static void main(String[] args) {

        logger.warning("The warnings about a 'restricted method in java.lang.System' "
                + "and 'enabling native access' appearing below (if any) can be ignored.");
        launchWithLock(Path.of("data", "uniteam.lock"), () -> Application.launch(MainApp.class, args));
    }

    /**
     * Holds a separate file lock until the application exits, preventing concurrent use of the same data folder.
     */
    static void launchWithLock(Path lockPath, Runnable launch) {
        try {
            Files.createDirectories(lockPath.getParent());
            try (
                    FileChannel channel = FileChannel.open(lockPath,
                    StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                    FileLock lock = channel.tryLock()
            ) {
                if (lock == null) {
                    logger.warning("UniTeam is already running in this folder.");
                    return;
                }

                launch.run();
            }
        } catch (IOException e) {
            logger.severe("Unable to acquire or release the UniTeam instance lock: " + e.getMessage());
        }
    }
}

package seedu.address.testutil;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import javafx.application.Platform;

/**
 * Shares the JavaFX toolkit across UI tests running in the same JVM.
 */
public class JavaFxTestUtil {

    /**
     * Starts JavaFX once, or schedules setup on its existing application thread.
     */
    public static void startToolkit() throws Exception {
        boolean isLinux = System
            .getProperty("os.name")
            .toLowerCase()
            .contains("linux");
        assumeTrue(!isLinux || System.getenv("DISPLAY") != null || System.getenv("WAYLAND_DISPLAY") != null,
            "JavaFX UI tests require a graphical display.");
        FutureTask<Void> startup = new FutureTask<>(() -> Platform.setImplicitExit(false), null);
        try {
            Platform.startup(startup);
        } catch (IllegalStateException alreadyStarted) {
            // Another UI test has already started the one toolkit allowed per JVM.
            Platform.runLater(startup);
        }
        startup.get(20, TimeUnit.SECONDS);
    }
}

package seedu.address;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.application.Application;

public class MainTest {

    @TempDir
    public Path testFolder;

    @Test
    public void launchWithLock_missingDirectory_createsDirectoryAndLaunches() {
        Path lockPath = testFolder.resolve("data").resolve("uniteam.lock");
        AtomicBoolean launched = new AtomicBoolean();

        Main.launchWithLock(lockPath, () -> launched.set(true));

        assertTrue(launched.get());
        assertTrue(Files.isRegularFile(lockPath));
    }

    @Test
    public void launchWithLock_previousLaunchReturned_launchesAgainWithExistingLockFile() {
        Path lockPath = testFolder.resolve("uniteam.lock");
        AtomicBoolean relaunched = new AtomicBoolean();
        Main.launchWithLock(lockPath, () -> {});

        Main.launchWithLock(lockPath, () -> relaunched.set(true));

        assertTrue(relaunched.get());
    }

    @Test
    public void launchWithLock_startupThrows_releasesLock() {
        Path lockPath = testFolder.resolve("uniteam.lock");
        assertThrows(IllegalStateException.class, () -> Main.launchWithLock(lockPath, () -> {
            throw new IllegalStateException("Startup failed");
        }));
        AtomicBoolean relaunched = new AtomicBoolean();

        Main.launchWithLock(lockPath, () -> relaunched.set(true));

        assertTrue(relaunched.get());
    }

    @Test
    public void launchWithLock_lockFileCannotBeOpened_doesNotLaunch() throws IOException {
        Path lockPath = Files.createDirectory(testFolder.resolve("uniteam.lock"));
        AtomicBoolean launched = new AtomicBoolean();

        Main.launchWithLock(lockPath, () -> launched.set(true));

        assertFalse(launched.get());
    }

    @Test
    public void launchWithLock_otherProcessRunning_blocksLaunchUntilNormalExit() throws Exception {
        assertLockReleasedAfterProcessExit(false);
    }

    @Test
    public void launchWithLock_otherProcessCrashes_releasesLock() throws Exception {
        assertLockReleasedAfterProcessExit(true);
    }

    private void assertLockReleasedAfterProcessExit(boolean forceExit) throws Exception {
        Path lockPath = testFolder.resolve("uniteam.lock");
        Path dataFile = testFolder.resolve("addressbook.json");
        String savedData = "{\"persons\":[],\"projects\":[],\"memberships\":[]}";
        Files.writeString(dataFile, savedData);
        Process holder = startLockProcess(lockPath);

        try {
            var output = holder.inputReader();
            String message = CompletableFuture.supplyAsync(() -> {
                try {
                    return output.readLine();
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }).get(15, TimeUnit.SECONDS);
            assertEquals("LOCKED", message);
            AtomicBoolean launched = new AtomicBoolean();

            Main.launchWithLock(lockPath, () -> launched.set(true));

            assertFalse(launched.get());
            assertEquals(savedData, Files.readString(dataFile));

            if (forceExit) {
                holder.destroyForcibly();
            } else {
                holder.getOutputStream().close();
            }
            assertTrue(holder.waitFor(15, TimeUnit.SECONDS));

            if (!forceExit) {
                assertEquals(0, holder.exitValue());
            }

            Main.launchWithLock(lockPath, () -> launched.set(true));

            assertTrue(launched.get());
            assertTrue(Files.isRegularFile(lockPath));
        } finally {
            holder.destroyForcibly();
            assertTrue(holder.waitFor(15, TimeUnit.SECONDS));
            holder.getInputStream().close();
        }
    }

    private Process startLockProcess(Path lockPath) throws Exception {
        String executable = System.getProperty("os.name").startsWith("Windows") ? "java.exe" : "java";
        String classPath = classPathFor(Main.class) + File.pathSeparator + classPathFor(MainTest.class)
                + File.pathSeparator + classPathFor(Application.class);

        return new ProcessBuilder(Path.of(System.getProperty("java.home"), "bin", executable).toString(),
                "-cp", classPath, LockProcess.class.getName(), lockPath.toString())
                .redirectError(testFolder.resolve("lock-process.log").toFile())
                .start();
    }

    private String classPathFor(Class<?> type) throws Exception {
        return Path.of(type.getProtectionDomain().getCodeSource().getLocation().toURI()).toString();
    }

    /**
     * Holds the production startup lock in a separate JVM until its input closes or the process is terminated.
     */
    public static class LockProcess {

        static void main(String[] args) {
            Main.launchWithLock(Path.of(args[0]), () -> {
                System.out.println("LOCKED");
                System.out.flush();
                try {
                    System.in.read();
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        }
    }
}

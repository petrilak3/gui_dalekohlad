package fmfi.dalekohlad.LockInstance;

import fmfi.dalekohlad.Mediator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.nio.file.Path;
import java.nio.file.Paths;

public class LockInstance {
    private static final Logger lgr = LogManager.getLogger(LockInstance.class);

    private static boolean lock(final String lockFile) {
        // source: https://stackoverflow.com/questions/177189/how-to-implement-a-single-instance-java-application
        try {
            final File file = new File(lockFile);
            final RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
            final FileLock fileLock = randomAccessFile.getChannel().tryLock();
            if (fileLock != null) {
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    try {
                        fileLock.release();
                        randomAccessFile.close();
                        file.delete();
                    } catch (Exception e) {
                        lgr.error("Unable to remove lock file: " + lockFile, e);
                    }
                }));
                return true;
            }
        } catch (Exception e) {
            lgr.error("Unable to create and/or lock file: " + lockFile, e);
        }
        return false;
    }

    public static void lock_instance() {
        String tmp_dir = System.getProperty("java.io.tmpdir");
        Path tmp_file_path = Paths.get(tmp_dir, "dalekohlad_gui.lock");
        boolean is_locked = lock(tmp_file_path.toString());
        if (!is_locked) {
            lgr.fatal("Lock couldn't be acquired, exiting.");
            System.exit(Mediator.EXIT_LOCK_INSTANCE_ERROR);
        }
        lgr.debug("Successfully acquired lock.");
    }
}

package DJT_Company;
import java.time.LocalDateTime;

public class DailyTrigger {
    private LocalDateTime lastBackupTime;
    private Thread checkerThread;
    private volatile boolean running = true;

    public DailyTrigger() {
        lastBackupTime = LocalDateTime.now();
    }
    public void startChecking(Task task) {
        checkerThread = new Thread(() -> {
            while (running) {
                try {
                    Thread.sleep(60_000);
                    LocalDateTime now = LocalDateTime.now();
                    if (now.isAfter(lastBackupTime.plusHours(24))) {
                        try {
                            task.WriteStorageFile();
                            lastBackupTime = now;
                            System.err.println("Backup complete. Next Backup at: "  +
                                    lastBackupTime.plusHours(24));
                        } catch (Exception e) {
                            System.out.println("Backup failed: " + e.getMessage());
                            String ErrMessage = e.getMessage();
                            Task.WriteErrMsgs(ErrMessage);
                        }
                    }
                } catch (InterruptedException e) {
                    System.out.println("Daily checker stopped");
                    String ErrMessage = e.getMessage();
                    Task.WriteErrMsgs(ErrMessage);
                    break;
                }
            }
        });

        checkerThread.setDaemon(true);
        checkerThread.start();
    }

    public void stop() {
        running = false;
        if (checkerThread != null) {
            checkerThread.interrupt();
        }
    }
}
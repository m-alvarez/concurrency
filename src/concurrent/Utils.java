package concurrent;

import java.util.concurrent.atomic.AtomicInteger;

public class Utils {
    private static AtomicInteger nMessages = new AtomicInteger(0);
    private static final int MAX_MESSAGES = 100;
    public static void report(Object o) {
        if (nMessages.get() < MAX_MESSAGES) {
            System.out.println("Thread " + Thread.currentThread().getId() + " says " + o);
            nMessages.incrementAndGet();
        }
    }
}

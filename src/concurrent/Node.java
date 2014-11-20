package concurrent;
/* The node of an integer list. At creation, default pointer is null */

import java.util.concurrent.atomic.AtomicInteger;

public class Node {

    public final AtomicInteger versionedLock = new AtomicInteger(VL.make(false, 0));

    public final int key;
    public Node next = null;

    Node(int item) {
        key = item;
    }

    public void updateToVersion(int version) {
        int vl = this.versionedLock.get();
        this.versionedLock.set(VL.setVersion(vl, version));
    }

    public boolean lock() {
        int vl = this.versionedLock.get();
        if (VL.isLocked(vl)) { return false; }
        int lockedVL = VL.setLocked(vl, true);
        return this.versionedLock.compareAndSet(vl, lockedVL);
    }

    public synchronized void unlock() {
        int vl = this.versionedLock.get();
        this.versionedLock.set(VL.setLocked(vl, false));
    }
}



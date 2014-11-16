package concurrent;
/* The node of an integer list. At creation, default pointer is null */

import java.util.concurrent.atomic.AtomicReference;

public class Node {

    public final AtomicReference<VL> versionedLock =
            new AtomicReference<VL>(new VL(0, false));
    public final int key;
    public Node next = null;

    Node(int item) {
        key = item;
    }

    public void updateToVersion(int version) {
        this.versionedLock.set(new VL(version, true));
    }

    public boolean lock() {
        VL vl = this.versionedLock.get();
        if (vl.locked) { return false; }
        VL lockedVL = new VL(vl.version, true);
        return this.versionedLock.compareAndSet(vl, lockedVL);
    }

    public synchronized void unlock() {
        int version = this.versionedLock.get().version;
        this.versionedLock.set(new VL(version, false));
    }
}



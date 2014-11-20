package concurrent;

/*
    Represent a set of integers as a list of ordered nodes
    with operations to add, remove, check membership, and print.
    At creation, we have two sentinel nodes. Integers are supposed
    to be greater than MIN_VALUE and smaller than MAX_VALUE.
*/

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import test.Set;

public class SetList implements Set {

    /* As per TL2, we require the version of each SetList instance */
    private final AtomicInteger version = new AtomicInteger(0);

    private final Node head;

    public SetList() {
        head = new Node(Integer.MIN_VALUE);
        head.next = new Node(Integer.MAX_VALUE);
    }

    public boolean add(int item) {

        /*
            We wrap the transaction in a while(true) so
            we can restart it with continue
         */
        transaction:
        while (true) {
            int rv = this.version.get();
            int vl, vl1;
            Node readSet;

            Node pred = head;

            vl = head.versionedLock.get();
            if (VL.getVersion(vl) > rv || VL.isLocked(vl)) {
                continue transaction;
            }
            Node curr = head.next;
            vl1 = head.versionedLock.get();
            if (vl1 != vl) {
                continue transaction;
            }

            readSet = head;

            while (curr.key < item) {
                pred = curr;

                vl = pred.versionedLock.get();
                if (VL.getVersion(vl) > rv || VL.isLocked(vl)) {
                    continue transaction;
                }

                curr = pred.next;
                vl1 = pred.versionedLock.get();
                if (vl1 != vl) {
                    continue transaction;
                }
                readSet = pred;
            }
            if (curr.key == item) {
                return false;
            } else {
                Node node = new Node(item);
                node.next = curr;

                /* TODO we could try to acquire the lock more than once... */
                if (!pred.lock()) {
                    continue transaction;
                }

                int wv = this.version.incrementAndGet();
                if (rv + 1 != wv) {
                    vl = readSet.versionedLock.get();
                    if ((readSet != pred && VL.isLocked(vl)) || VL.getVersion(vl) > rv) {
                        /*
                            TODO Perhaps we should decrement the global version
                            Because no change has taken place. This doesn't change
                            the semantics, but makes less transactions fail
                        */
                        pred.unlock();
                        continue transaction;
                    }
                }
                pred.next = node;
                pred.updateToVersion(wv);
                pred.unlock();
                return true;
            }
        }
    }

    public boolean remove(int item) {
        transaction:
        while (true) {
            int rv = this.version.get();
            int vl, vl1;
            Node readSet;

            Node pred = head;

            vl = head.versionedLock.get();
            if (VL.getVersion(vl) > rv || VL.isLocked(vl)) {
                continue transaction;
            }
            Node curr = head.next;
            vl1 = head.versionedLock.get();
            if (vl1 != vl) {
                continue transaction;
            }
            readSet = head;

            while (curr.key < item) {
                pred = curr;

                vl = pred.versionedLock.get();
                if (VL.getVersion(vl) > rv || VL.isLocked(vl)) {
                    continue transaction;
                }
                curr = pred.next;
                vl1 = pred.versionedLock.get();
                if (vl1 != vl) {
                    continue transaction;
                }

                readSet = pred;
            }
            if (curr.key == item) {
                if (!pred.lock()) continue transaction;
                if (!curr.lock()) {
                    pred.unlock();
                    continue transaction;
                }

                int wv = this.version.incrementAndGet();
                if (rv + 1 != wv) {
                    vl = readSet.versionedLock.get();
                    if ((readSet != pred && readSet != curr && VL.isLocked(vl))
                            || VL.getVersion(vl) > rv) {
                        curr.unlock();
                        pred.unlock();
                        continue transaction;
                    }
                }

                pred.next = curr.next;
                pred.updateToVersion(wv);
                curr.updateToVersion(wv);
                curr.unlock();
                pred.unlock();

                return true;
            } else {
                return false;
            }
        }
    }

    public boolean member(int item) {
        Node pred = head;
        Node curr = head.next;
        while (curr.key < item) {
            pred = curr;
            curr = pred.next;
        }
        if (curr.key == item) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * The function print has side-effects. Therefore
     * it's impossible to write it in a transactional
     * style.
     */
    public void print() {
        Node pred = head;
        Node curr = head.next;
        while (curr.next != null) {
            System.out.print(curr.key + " ");
            curr = curr.next;
        }
        System.out.println("");
    }

    /**
     * This function is not thread-safe (which is not required).
     */
    public List<Integer> asList() {
        ArrayList<Integer> l = new ArrayList<Integer>();

        Node pred = head;
        Node curr = head.next;
        while (curr.next != null) {
            l.add(curr.key);
            curr = curr.next;
        }
        return l;
    }
}

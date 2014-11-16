package concurrent;

/*
    Represent a set of integers as a list of ordered nodes
    with operations to add, remove, check membership, and print.
    At creation, we have two sentinel nodes. Integers are supposed
    to be greater than MIN_VALUE and smaller than MAX_VALUE.
*/

import java.util.LinkedList;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import test.Set;

public class SetList implements Set {
    private static final int NUM_TRIES = 1;
    private static final boolean DEBUG = false;

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
            VL vl, vl1;
            List<Node> readSet = new LinkedList<Node>();

            Node pred = head;

            vl = head.versionedLock.get();
            if (vl.version > rv || vl.locked) {
                continue transaction;
            }
            Node curr = head.next;
            vl1 = head.versionedLock.get();
            if (vl1 != vl) {
                continue transaction;
            }
            readSet.add(head);

            //Utils.report("Searching place");
            while (curr.key < item) {
                pred = curr;

                vl = pred.versionedLock.get();
                if (vl.version > rv || vl.locked) {
                    /*
                    if (vl.locked) {
                        Utils.report("Node " + pred.key + " Locked. Restarting.");
                    } else {
                        Utils.report("New. Restarting.");
                    } */
                    continue transaction;
                }
                curr = pred.next;
                vl1 = pred.versionedLock.get();
                if (vl1 != vl) {
                    continue transaction;
                }
                /* TODO if removal breaks something, it's this */
                readSet.add(0, pred);
                if (readSet.size() > 1) readSet.remove(1);
            }
            if (curr.key == item) {
                /* TODO should we re-check the read-set? I don't think so */
                return false;
            } else {
                Node node = new Node(item);
                node.next = curr;

                /* TODO we could try to acquire the lock more than once... */
                //Utils.report("Trying to acquire lock for node " + pred.key);
                if (!pred.lock()) {
                    //Utils.report("Failed to acquire lock");
                    continue transaction;
                }

                int wv = this.version.incrementAndGet();
                if (rv + 1 != wv) {
                    for (Node n : readSet) {
                        vl = n.versionedLock.get();
                        if ((n != pred && vl.locked) || vl.version > rv) {
                            /*
                                TODO Perhaps we should decrement the global version
                                Because no change has taken place. This doesn't change
                                the semantics, but makes less transactions fail
                            */
                            pred.unlock();
                            continue transaction;
                        }
                    }
                }
                //Utils.report("Started writing " + pred.key);
                pred.next = node;
                pred.updateToVersion(wv);
                pred.unlock();
                //Utils.report("Unlocked " + pred.key);
                return true;
            }
        }
    }

    public boolean remove(int item) {
        transaction:
        while (true) {
            int rv = this.version.get();
            VL vl, vl1;
            List<Node> readSet = new LinkedList<Node>();

            Node pred = head;

            vl = head.versionedLock.get();
            if (vl.version > rv || vl.locked) {
                continue transaction;
            }
            Node curr = head.next;
            vl1 = head.versionedLock.get();
            if (vl1 != vl) {
                continue transaction;
            }
            readSet.add(head);

            while (curr.key < item) {
                pred = curr;

                vl = pred.versionedLock.get();
                if (vl.version > rv || vl.locked) {
                    continue transaction;
                }
                curr = pred.next;
                vl1 = pred.versionedLock.get();
                if (vl1 != vl) {
                    continue transaction;
                }
                readSet.add(0, pred);
                /* TODO check this */
                if (readSet.size() > 1) readSet.remove(1);
            }
            if (curr.key == item) {
                if (!pred.lock()) continue transaction;
                if (!curr.lock()) {
                    pred.unlock(); continue transaction;
                }

                int wv = this.version.incrementAndGet();
                if (rv + 1 != wv) {
                    for (Node n : readSet) {
                        vl = n.versionedLock.get();
                        if ((n != pred && n != curr && vl.locked)
                                || vl.version > rv) {
                            curr.unlock();
                            pred.unlock();
                            continue transaction;
                        }
                    }
                }

                pred.next = curr.next;
                pred.updateToVersion(wv);
                curr.updateToVersion(wv);
                curr.unlock();
                pred.unlock();

                return true;
            } else {
                /* TODO should we re-check the read-set? */
                return false;
            }
        }
    }

    public boolean member(int item) {
        /* TODO does this need synchronization? */
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

    /*
        The function print has side-effects. Therefore
        it's impossible to write it in a transactional
        style. For transactional printing use toString
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

    /*
      This function is not concurrency-proof (which is not required).
     */
    public List<Integer> asList()
    {
        ArrayList<Integer> l = new ArrayList<Integer>();

        Node pred=head;
        Node curr=head.next;
        while (curr.next != null) {
            l.add(curr.key);
            curr=curr.next;
        }
        return l;
    }

    @Override
    public String toString() {
        /* TODO */
        return "";
    }
}

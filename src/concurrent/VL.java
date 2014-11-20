package concurrent;

/**
 *  According to tl2, we need a versioned lock.
 *  This class provides a bunch of utility
 *  functions to treat integers as versioned locks.
 *
 *  The previous version used a class with version
 *  and isLocked fields but this turned out to be
 *  grossly inefficient (as Java doesn't have value
 *  types)
 */
public final class VL {
    private static int LOCKED_BIT = 1 << (Integer.SIZE - 1);
    private static int VERSION_BITS = ~LOCKED_BIT;

    public static int getVersion(int vl) {
        return vl & VERSION_BITS;
    }

    public static boolean isLocked(int vl) {
        return (vl & LOCKED_BIT) != 0;
    }

    public static int setLocked(int vl, boolean locked) {
        if (locked) {
            return vl | LOCKED_BIT;
        } else {
            return vl & ~LOCKED_BIT;
        }
    }

    public static int setVersion(int vl, int version) {
        return (version & VERSION_BITS) | (vl & LOCKED_BIT);
    }

    public static int make(boolean locked, int version) {
        return setVersion(setLocked(0, locked), version);
    }
}

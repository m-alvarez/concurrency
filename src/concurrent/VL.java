package concurrent;

/*
    According to tl2, we need a versioned lock.
    This class provides it. This is less
    space-efficient than the representation
    suggested in the original paper but does
    not require fiddling with bits.
 */
public class VL {
    public final int version;
    public final boolean locked;

    public VL(int version, boolean locked) {
        this.version = version;
        this.locked = locked;
    }
}

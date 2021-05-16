package math2D;

/**
 * Item class will be the super class of both Points and Vectors...
 * An Item should never be initialized on its own.
 */
public class Item {
    private double[] vals;

    Item(double... vals) {
        if (vals.length != 3) {
            throw new IllegalArgumentException("Required 3 values, found " + vals.length + ".");
        }

        // The 3rd coordinate should always be 1 or 0.
        // In places of floating point inaccuracy, we will
        // always reset vals[2] to 1 or 0.
        if (vals[2] != 0 && vals[2] != 1) {
            throw new IllegalArgumentException("Required 3rd coordinate to be 1 or 0, found " + vals[2] + ".");
        }

        this.vals = vals;
    }

    double[] vals() {
        return vals;
    }
}

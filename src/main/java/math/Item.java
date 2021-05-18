package math;

import static math.Util.*;

import static org.lwjgl.opengl.GL11.*;

/**
 * Item class can either be a point or vector.
 * This class is dimension independent.
 *
 * Also note, this represents a row... not a column.
 * If v is an Item and A a matrix.
 * vA represents the product.
 */
public class Item {
    public static Item vector2D(double x, double y) {
        return new Item(x, y, 0);
    }

    public static Item point2D(double x, double y) {
        return new Item(x, y, 1);
    }

    public static Item item2D(double x, double y, double w) {
        return new Item(x, y, w);
    }

    private double[] vals;

    Item(double... vals) {
        if (vals == null || vals.length == 0) {
            throw new IllegalArgumentException("Given values most be non-null and non-empty!");
        }

        this.vals = vals;
    }

    double[] vals() {
        return vals;
    }

    public boolean is2DPoint() {
        return vals.length == 3 && vals[2] != 0;
    }

    public boolean is2DVector() {
        return vals.length == 3 && vals[2] == 0;
    }

    // NOTE, for speed, this function assumes we are working with a 2D Point.
    public void draw2D() {
        glVertex2d(vals[0], vals[1]);
    }

    public double val(int i) {
        return vals[i];
    }

    public Item times(Matrix m) {
        return new Item(mult(vals, m.mat()));
    }

    public Item times(double s) {
        double[] newVals = new double[vals.length];

        for (int i = 0; i < vals.length; i++) {
            newVals[i] = vals[i] * s;
        }

        return new Item(newVals);
    }

    public Item plus(Item i) {
        double[] oVals = i.vals();

        if (oVals.length != vals.length) {
            throw new IllegalArgumentException("Dimension mismatch for addition!");
        }

        double[] newVals = new double[vals.length];

        for (int j = 0; j < vals.length; j++) {
            newVals[j] = vals[j] + oVals[j];
        }

        return new Item(newVals);
    }

    public double dot(Item i) {
        double[] oVals = i.vals();

        if (oVals.length != vals.length) {
            throw new IllegalArgumentException("Dimension mismatch for dot product!");
        }

        if (oVals[oVals.length - 1] != 0 || vals[vals.length - 1] != 0) {
            throw new IllegalArgumentException("Dot product can only be performed between vectors!");
        }

        double dot = 0;

        for (int j = 0; j < vals.length; j++) {
            dot += vals[j] * oVals[j];
        }

        return dot;
    }

    public double magnitude() {
        return Math.sqrt(this.dot(this));
    }

    public Item normalize() {
        return this.times(1.0 / this.magnitude());
    }

    public boolean equals(Object o) {
        if (!(o instanceof Item)) {
            return false;
        }

        Item i = (Item) o;
        double[] oVals = i.vals();

        if (oVals.length != vals.length) {
            return false;
        }

        int j;
        for (j = 0; j < vals.length - 1; j++) {
            if (Math.abs(vals[j] - oVals[j]) > EPS) {
                return false;
            }
        }

        // NOTE...
        // The last coordinate of an Item determines whether it is a Point or Vector.
        // 0.0 represents a vector... everything else... a point.
        // This is what this fancy logic checks.
        // A Point and Vector can never be equal.
        return !((vals[j] != oVals[j] && (vals[j] == 0.0 || oVals[j] == 0.0)) ||
                Math.abs(vals[j] - oVals[j]) > EPS);

    }
}

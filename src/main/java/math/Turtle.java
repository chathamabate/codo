package math;

import java.util.function.UnaryOperator;

import static math.Matrix.*;

public class Turtle {

    private static final Turtle T = new Turtle(Item.vector2D(1, 0), Item.point2D(0, 0), null);

    public static Turtle t() {
        return T;
    }

    private final Item dir;   // Can be unit... doesn't need to be.
    private final Item pos;

    private final Matrix mat;

    Turtle(Item d, Item p, Matrix m) {
        if (!d.is2DVector() || !p.is2DPoint()) {
            throw new IllegalArgumentException("Turtle requires 2D vector!");
        }

        pos = p;
        dir = d;

        mat = m;
    }

    public Turtle move(double s) {
        Item p1 = pos.plus(dir.times(s));

        return new Turtle(
                dir,
                p1,
                mat
        );
    }

    public Turtle forward(double s) {
        Item p1 = pos.plus(dir.times(s));

        return new Turtle(
                dir,
                p1,
                mat == null ? pos.asMatrix().concat(p1) : mat.concat(pos).concat(p1)
        );
    }

    public Turtle rotate(double theta) {
        return new Turtle(
                dir.times(i(3).rotate2D(theta)),
                pos,
                mat
        );
    }

    public Turtle scale(double s) {
        return new Turtle(
                dir.times(s),
                pos,
                mat
        );
    }

    public void draw2D() {
        mat.draw2D();
    }

    public Matrix image() {
        return mat;
    }
}

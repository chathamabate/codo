package math;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

public interface IFS {

    static IFS ifs(Matrix... ms) {
        IFS s = Empty.ONLY;

        for (int i = ms.length - 1; i >= 0; i--) {
            s = s.prepend(ms[i]);
        }

        return s;
    }

    static IFS trianglePulse(double theta) {
        double segLength = 1.0 / (2.0 + (2.0 * Math.sin(theta / 2.0)));
        double halfpWidth = segLength * Math.sin(theta / 2.0);
        double pHeight = segLength * Math.cos(theta / 2.0);

        return ifs(
                Matrix.i(3).scale2D(segLength),
                Matrix.i(3).scale2D(segLength).rotate2D((Math.PI - theta) / 2.0)
                        .shift2D(segLength, 0.0),
                Matrix.i(3).scale2D(segLength).rotate2D((theta - Math.PI) / 2.0)
                        .shift2D((segLength) + halfpWidth,pHeight),
                Matrix.i(3).scale2D(segLength).shift2D((segLength) + (2.0 * halfpWidth), 0.0)
        );
    }

    static IFS trianglePulseReduced(double theta) {
        double phi = (Math.PI - theta) / 4.0;
        double cosine = Math.cos(phi);
        double sine = Math.sin(phi);

        double l = 1.0 / (2.0 * cosine);
        double w = l * cosine;
        double h = l * sine;

        Matrix one = Matrix.i(3).scale2D(l).rotate2D(Math.PI + phi).shift2D(w, h);
        Matrix two = Matrix.i(3).scale2D(l).rotate2D(Math.PI - phi).shift2D(1.0, 0);

        return ifs(one, two);
    }

    static IFS quadPulse(double theta) {
        double l = 1.0 / (3.0 + 2 * Math.cos(Math.PI - theta));
        double w = l * Math.cos(Math.PI - theta);
        double h = l * Math.sin(Math.PI - theta);

        return ifs(
                Matrix.i(3).scale2D(l),
                Matrix.i(3).scale2D(l).rotate2D(Math.PI - theta).shift2D(l, 0),
                Matrix.i(3).scale2D(l).shift2D(l + w, h),
                Matrix.i(3).scale2D(l).rotate2D(theta - Math.PI).shift2D((2 * l) + w, h),
                Matrix.i(3).scale2D(l).shift2D(2 * (l + w), 0)
        );
    }

    static IFS empty() {
        return Empty.ONLY;
    }

    Matrix first();
    IFS rest();
    boolean isEmpty();

    Matrix of(Matrix m);
    List<Matrix> ofList(Matrix m);
    default Matrix iterate(int amt, Matrix m) {
        return amt <= 0 ? m : iterate(amt - 1, of(m));
    }

    IFS map(UnaryOperator<Matrix> mapper);

    default IFS prepend(Matrix f) {
        return new Cons(f, this);
    }

    class Empty implements IFS {
        private static final Empty ONLY = new Empty();

        private Empty() {

        }

        @Override
        public Matrix first() {
            throw new RuntimeException("Empty IFS has no first element!");
        }

        @Override
        public IFS rest() {
            throw new RuntimeException("Empty IFS has no rest!");
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public Matrix of(Matrix m) {
            throw new RuntimeException("Empty IFS cannot be applied!");
        }

        @Override
        public List<Matrix> ofList(Matrix m) {
            return new ArrayList<>();
        }

        @Override
        public IFS map(UnaryOperator<Matrix> mapper) {
            return this;
        }
    }

    class Cons implements IFS {
        private final Matrix first;
        private final IFS rest;

        private Cons(Matrix f, IFS r) {
            first = f;
            rest = r;
        }

        @Override
        public Matrix first() {
            return first;
        }

        @Override
        public IFS rest() {
            return rest;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public Matrix of(Matrix m) {
            Matrix mp = m.times(first);
            return rest.isEmpty() ? mp : mp.concat(rest.of(m));
        }

        @Override
        public List<Matrix> ofList(Matrix m) {
            List<Matrix> results = rest.ofList(m);
            results.add(m.times(first));
            return results;
        }

        @Override
        public IFS map(UnaryOperator<Matrix> mapper) {
            return new Cons(mapper.apply(first), rest.map(mapper));
        }
    }
}

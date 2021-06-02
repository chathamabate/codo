package math;

import java.util.function.UnaryOperator;

public interface IFS {

    static IFS ifs(Matrix... ms) {
        IFS s = Empty.ONLY;

        for (int i = ms.length - 1; i >= 0; i--) {
            s = s.prepend(ms[i]);
        }

        return s;
    }

    static IFS empty() {
        return Empty.ONLY;
    }

    Matrix first();
    IFS rest();
    boolean isEmpty();

    Matrix of(Matrix m);
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
        public IFS map(UnaryOperator<Matrix> mapper) {
            return new Cons(mapper.apply(first), rest.map(mapper));
        }
    }
}

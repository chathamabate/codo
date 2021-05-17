package math;

public class Util {

    // Error to use.
    static final double EPS = 1e-6;

    // NOTE, below, and everywhere in this package,
    // We shall assume all Matrices/Items are non-null and non-empty.
    // Additionally, for Matrices, we will assume they are rectangular.
    // i.e. every inner array must have the same length.
    // If these assumptions are not true for given inputs, behavior is undefined.

    static double[] mult(double[] vals, double[][] mat) {
        if (mat.length != vals.length) {
            throw new IllegalArgumentException("Bad dimensions for multiplication.");
        }

        double[] newVals = new double[mat[0].length];

        for (int c = 0; c < mat[0].length; c++) {
            newVals[c] = 0;

            for (int r = 0; r < mat.length; r++) {
                newVals[c] += vals[r] * mat[r][c];
            }
        }

        return newVals;
    }

    static double[][] mult(double[][] mat1, double[][] mat2) {
        if (mat1[0].length != mat2.length) {
            throw new IllegalArgumentException("Bad dimensions for multiplication.");
        }

        // (A x B) * (B x C) -> (A x C)
        double[][] newMat = new double[mat1.length][mat2[0].length];

        for (int r = 0; r < mat1.length; r++) {
            for (int c = 0; c < mat2[0].length; c++) {
                newMat[r][c] = 0;

                for (int k = 0; k < mat1[0].length; k++) {
                    newMat[r][c] += mat1[r][k] * mat2[k][c];
                }
            }
        }

        return newMat;
    }
}

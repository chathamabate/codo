package math;

import static math.Util.*;

public class Matrix {
    private double[][] mat;

    Matrix(double[][] mat) {
        if (mat.length != 3 || mat[0].length != 3) {
            throw new IllegalArgumentException("Given matrix is not 3 x 3.");
        }

        this.mat = mat;
    }

    double[][] mat() {
        return mat;
    }

    public Matrix times(Matrix o) {
        return new Matrix(mult(mat, o.mat()));
    }

    public Matrix times(double s) {
        double[][] newMat = new double[mat.length][mat[0].length];

        for (int i = 0; i < mat.length; i++) {
            for (int j = 0; j < mat[0].length; j++) {
                newMat[i][j] = mat[i][j] * s;
            }
        }

        return new Matrix(newMat);
    }

    public boolean equals(Object o) {
        if (!(o instanceof Matrix)) {
            return false;
        }

        Matrix m = (Matrix) o;
        double[][] oMat = m.mat();

        if (mat.length != oMat.length || mat[0].length != oMat[0].length) {
            return false;
        }

        for (int i = 0; i < mat.length; i++) {
            for (int j = 0; j < mat[0].length; j++) {
                if (Math.abs(mat[i][j] - oMat[i][j]) > EPS) {
                    return false;
                }
            }
        }

        return true;
    }
}

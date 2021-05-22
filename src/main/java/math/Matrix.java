package math;

import static math.Util.*;
import static math.Item.*;

import static org.lwjgl.opengl.GL11.*;

public class Matrix {
    public static Matrix sprite2D(Item... vertices) {
        if (vertices.length == 0) {
            throw new IllegalArgumentException("Cannot build empty sprite!");
        }

        double[][] mat = new double[vertices.length][3];

        for (int i = 0; i < vertices.length; i++) {
            double[] vert = vertices[i].vals();

            if (vert.length != 3) {
                throw new IllegalArgumentException("Bad item dimensions for sprite2D!");
            }

            if (vert[2] == 0) {
                throw new IllegalArgumentException("Sprites must contain points only!");
            }

            for (int j = 0; j < 3; j++) {
                mat[i][j] = vert[j];
            }
        }

        return new Matrix(mat);
    }

    public static Matrix sprite2D(double... vertCoords) {
        if (vertCoords.length % 2 == 1) {
            throw new IllegalArgumentException("Sprite2D requires an even number of vertices coordinates!");
        }

        int verts = vertCoords.length / 2;

        double[][] mat = new double[verts][3];

        for (int i = 0; i < verts; i++) {
            mat[i][0] = vertCoords[2 * i];
            mat[i][1] = vertCoords[(2 * i) + 1];
            mat[i][2] = 1;
        }

        return new Matrix(mat);
    }

    static double[][] identityArray(int n) {
        double[][] mat = new double[n][n];

        // Default value is all 0s, just need to set diagonal.
        for (int i = 0; i < n; i++) {
            mat[i][i] = 1;
        }

        return mat;
    }

    public static Matrix i(int n) {
        return new Matrix(identityArray(n));
    }

    private final double[][] mat;

    Matrix(double[][] mat) {
        this.mat = mat;
    }

    double[][] mat() {
        return mat;
    }

    public double[] flat() {
        double[] glMat = new double[mat.length * mat[0].length];
        for (int i = 0; i < mat.length; i++) {
            System.arraycopy(mat[i], 0, glMat, (i * mat[0].length), mat[0].length);
        }

        return glMat;
    }

    // NOTE, for speed there is no 2D check.
    public void draw2D() {
        for (int i = 0; i < mat.length; i++) {
            glVertex2d(mat[i][0], mat[i][1]);
        }
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

    // NOTE, this is not fast for big mats...
    // Switch to dynamic programming in the future.
    public double determinant() {
        if (mat.length != mat[0].length) {
            throw new RuntimeException("Can only take the determinant of a square matrix!");
        }

        if (mat.length == 1) {
            return mat[0][0];
        }

        double det = 0.0;

        for (int j = 0; j < mat.length; j++) {
            det += Math.pow(-1, j % 2) * mat[0][j] * minor(0, j).determinant();
        }

        return det;
    }

    public Matrix minor(int x, int y) {
        double[][] newMat = new double[mat.length - 1][mat[0].length - 1];

        for (int i = 0; i < newMat.length; i++) {
            for (int j = 0; j < newMat[0].length; j++) {
                newMat[i][j] = mat[i < x ? i : i + 1][j < y ? j : j + 1];
            }
        }

        return new Matrix(newMat);
    }

    public Matrix inverse() {
        if (mat.length != mat[0].length) {
            throw new RuntimeException("Can only invert a square matrix.");
        }

        double[][] newMat = new double[mat.length][mat.length];

        double det = 0.0;

        for (int i = 0; i < mat.length; i++) {
            for (int j = 0; j < mat.length; j++) {
                newMat[j][i] = Math.pow(-1, (i + j) % 2) * minor(i, j).determinant();
            }

            det += mat[i][0] * newMat[0][i];
        }

        return (new Matrix(newMat)).times(1.0 / det);
    }

    public Matrix concat(Matrix o) {
        double[][] oMat = o.mat();

        if (mat[0].length != oMat[0].length) {
            throw new IllegalArgumentException("Cannot concat two matrices with different row lengths!");
        }

        double[][] newMat = new double[mat.length + oMat.length][mat[0].length];

        for (int i = 0; i < mat.length; i++) {
            System.arraycopy(mat[i], 0, newMat[i], 0, mat[0].length);
        }

        for (int i = 0; i < oMat.length; i++) {
            System.arraycopy(oMat[i], 0, newMat[i + mat.length], 0, oMat[0].length);
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

    public String toString() {
        StringBuilder str = new StringBuilder("[");

        for (int i = 0; i < mat.length; i++) {
            str.append("[");

            for (int j = 0; j < mat[0].length; j++) {
                str.append(mat[i][j]);

                if (j < mat[0].length - 1) {
                    str.append(", ");
                }
            }

            str.append("]");

            if (i < mat.length - 1) {
                str.append(", ");
            }
        }

        return str.toString();
    }

    public Matrix shift2D(Item i) {
        double[] vals = i.vals();

        if (!i.is2DVector()) {
            throw new IllegalArgumentException("Can only shift2D by 2D Vector!");
        }

        return this.shift2D(vals[0], vals[1]);
    }

    public Matrix shift2D(double x, double y) {
        // NOTE, this shift ignores weight!
        double[][] mat = identityArray(3);
        mat[2][0] = x;
        mat[2][1] = y;

        return this.times(new Matrix(mat));
    }

    public Matrix rotate2D(double theta) {
        double[][] mat = identityArray(3);

        mat[0][0] = Math.cos(theta);
        mat[0][1] = Math.sin(theta);

        mat[1][0] = -Math.sin(theta);
        mat[1][1] = Math.cos(theta);

        return this.times(new Matrix(mat));
    }

    public Matrix scale2D(double s) {
        double[][] mat = identityArray(3);

        mat[0][0] = s;
        mat[1][1] = s;

        return this.times(new Matrix(mat));
    }

    public Matrix rotate2D(Item p, double theta) {
        if (!p.is2DPoint()) {
            throw new IllegalArgumentException("rotate2D requires 2D Point!");
        }

        double[] vals = p.vals();

        return this.rotate2D(vals[0], vals[1], theta);
    }

    public Matrix rotate2D(double x, double y, double theta) {
        return this.shift2D(-x, -y).rotate2D(theta).shift2D(x, y);
    }

    public Matrix scale2D(Item p, double s) {
        if (!p.is2DPoint()) {
            throw new IllegalArgumentException("scale2D requires a 2D Point!");
        }

        double[] vals = p.vals();
        return this.scale2D(vals[0], vals[1], s);
    }

    public Matrix scale2D(double x, double y, double s) {
        return this.shift2D(-x, -y).scale2D(s).shift2D(x, y);
    }

    public Matrix scale2D(Item p, Item v, double s) {
        if (!p.is2DPoint() || !v.is2DVector()) {
            throw new IllegalArgumentException("scale2D requires a 2D point and a 2D vector!");
        }

        double[] pvals = p.vals();
        double[] vvals = v.vals();

        return this.scale2D(pvals[0], pvals[1], vvals[0], vvals[1], s);
    }

    public Matrix scale2D(double px, double py, double vx, double vy, double s) {
        double[][] mat = identityArray(3);
        mat[0][0] = s;

        Matrix xDirScale = new Matrix(mat);

        double theta = Math.atan(vy / vx);

        return this.shift2D(-px, -py)
                .rotate2D(-theta)
                .times(xDirScale)
                .rotate2D(theta)
                .shift2D(px, py);
    }

    public Matrix affineTrans2D(Item q1, Item q2, Item q3, Item p1, Item p2, Item p3) {
        if (!q1.is2DPoint() || !q2.is2DPoint() || !q3.is2DPoint() ||
                !p1.is2DPoint() || !p2.is2DPoint() || !p3.is2DPoint()) {
            throw new IllegalArgumentException("AffineTrans2D requires 6 points!");
        }

        Item qv1 = q2.minus(q1).asVector();
        Item qv2 = q3.minus(q1).asVector();

        Matrix back = affineTrans2D(q1, qv1, qv2).inverse();

        Item pv1 = p2.minus(p1).asVector();
        Item pv2 = p3.minus(p1).asVector();

        Matrix forward = affineTrans2D(p1, pv1, pv2);

        return this.times(back.times(forward));
    }

    public Matrix affineTrans2D(Item p, Item v1, Item v2) {
        if (!p.is2DPoint() || !v1.is2DVector() || !v2.is2DVector()) {
            throw new IllegalArgumentException("Affine Transform 2D requires a 2D point and 2 2D vectors.");
        }

        double[] pvals = p.vals();
        double[] v1vals = v1.vals();
        double[] v2vals = v2.vals();

        return this.affineTrans2D(v1vals[0], v1vals[1], v2vals[0], v2vals[1], pvals[0], pvals[1]);
    }

    public Matrix affineTrans2D(double a, double b, double c, double d, double e, double f) {
        double[][] mat = identityArray(3);

        mat[0][0] = a;
        mat[0][1] = b;

        mat[1][0] = c;
        mat[1][1] = d;

        mat[2][0] = e;
        mat[2][1] = f;

        return this.times(new Matrix(mat));
    }

    public Matrix flip2D(Item v) {
        if (!v.is2DVector()) {
            throw new IllegalArgumentException("Can only flip2D over 2D vector!");
        }

        Item i = vector2D(1, 0);
        Item j = vector2D(0, 1);

        Item vp = v.normalize();

        Item ip = i.minus((i.minus(vp.times(i.dot(vp)))).times(2.0));
        double[] ivals = ip.vals();

        Item jp = j.minus((j.minus(vp.times(j.dot(vp)))).times(2.0));
        double[] jvals = jp.vals();

        double[][] newMat = identityArray(3);

        newMat[0][0] = ivals[0];
        newMat[0][1] = ivals[1];

        newMat[1][0] = jvals[0];
        newMat[1][1] = jvals[1];

        return this.times(new Matrix(newMat));
    }

    public Matrix flip2D(double x, double y) {
        return this.flip2D(vector2D(x, y));
    }
}

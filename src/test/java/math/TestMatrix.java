package math;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static math.Matrix.*;
import static math.Util.*;

public class TestMatrix {
    @Test
    public void testDeterminant() {
        assertTrue(Math.abs(i(3).rotate2D(.1).determinant() - 1) <= EPS);
        assertTrue(Math.abs(i(3).scale2D(10).determinant() - 100) <= EPS);
    }

    @Test
    public void testInverse() {
        assertEquals(i(3), i(3).inverse());
        assertEquals(i(3), i(3).rotate2D(.1).times(i(3).rotate2D(.1).inverse()));

        assertEquals(i(3).scale2D(1 / 3.0), i(3).scale2D(3.0).inverse());

        assertNotEquals(i(3), i(3).rotate2D(.1).times(i(3).rotate2D(.2).inverse()));
    }
}

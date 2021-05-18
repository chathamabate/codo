package math;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static math.Matrix.*;
import static math.Util.*;

public class TestMatrix {
    @Test
    public void testDeterminant() {
        assertTrue(Math.abs(rotate2D(.1).determinant() - 1) <= EPS);
        assertTrue(Math.abs(scale2D(10).determinant() - 100) <= EPS);
    }

    @Test
    public void testInverse() {
        assertEquals(identity(3), identity(3).inverse());
        assertEquals(identity(3), rotate2D(.1).times(rotate2D(.1).inverse()));

        assertNotEquals(identity(3), rotate2D(.1).times(rotate2D(.2).inverse()));
    }
}

package math;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static math.Item.*;

public class TestItem {

    @Test
    public void testEquals() {
        assertEquals(vector2D(1, 1), vector2D(1, 1));
        assertEquals(point2D(2, 4), point2D(2, 4));

        assertNotEquals(point2D(1, 0), point2D(1, 1));
    }

    @Test
    public void testPointVectorEquals() {
        assertEquals(vector2D(1e-7, 1 + 1e-7), vector2D(0, 1));
        assertNotEquals(item2D(0, 0, 0), item2D(0, 0, 1e-7));
    }
}

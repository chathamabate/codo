package math;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static math.Item.*;

public class TestItem {

    @Test
    public void testEquals() {
        assertEquals(vector2D(1, 1), vector2D(1, 1));
        assertEquals(point2D(2, 4), point2D(2, 4));
        assertEquals(item2D(1, 1, 1e-7), item2D(1, 1, 0));

        assertNotEquals(point2D(1, 0), point2D(1, 1));
    }


}

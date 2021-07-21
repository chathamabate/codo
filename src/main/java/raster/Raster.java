package raster;

import java.math.BigDecimal;
import java.util.function.BiFunction;

import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

/**
 * Class for running a function on all pixels of the screen.
 *
 * Goal here is higher performance than the old rasterizer.
 * * High-Speed panning.
 * * Multithreading.
 *
 * This uses current OpenGL window... NOT FUNCTIONAL!
 */
public class Raster {
    private final long window;

    private BigDecimal x;
    private BigDecimal y;

    private BigDecimal stepSize;

    // Starts at top right corner.
    // pixels[r][c * 3] ... columns are expanded to hold rgb values.
    private float[][] pixels;

    public Raster(long wind) {
        window = wind;

        x = BigDecimal.ZERO;
        y = BigDecimal.ZERO;

        stepSize = new BigDecimal("0.01");

        resizePixels();
    }

    public void resizePixels() {
        int[] width = new int[1];
        int[] height = new int[1];

        glfwGetWindowSize(window, width, height);

        pixels = new float[height[0]][width[0] * 3];
    }
//
//    public void raster() {
//
//    }
//
//    public void raster() {
//
//    }
}

package main;

import math.IFS;
import math.Item;
import math.Matrix;
import math.Turtle;
import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.lang.reflect.Array;
import java.nio.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

import static math.Item.*;
import static math.Matrix.*;
import static math.IFS.*;

public class Runner {
    // The window handle
    private long window;

    public void run() {
        System.out.println("Hello LWJGL " + Version.getVersion() + "!");

        init();
        loop();

        // Free the window callbacks and destroy the window
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        // Terminate GLFW and free the error callback
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    private void init() {
        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if ( !glfwInit() )
            throw new IllegalStateException("Unable to initialize GLFW");

        // Configure GLFW
        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable

        // Create the window
        window = glfwCreateWindow(900, 900, "Graphics", NULL, NULL);

        if ( window == NULL )
            throw new RuntimeException("Failed to create the GLFW window");

        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
        glfwSetKeyCallback(window, this::keyboardHandler);

        // Get the thread stack and push a new frame
        try ( MemoryStack stack = stackPush() ) {
            IntBuffer pWidth = stack.mallocInt(1); // int*
            IntBuffer pHeight = stack.mallocInt(1); // int*

            // Get the window size passed to glfwCreateWindow
            glfwGetWindowSize(window, pWidth, pHeight);

            // Get the resolution of the primary monitor
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            // Center the window
            glfwSetWindowPos(
                    window,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2
            );
        } // the stack frame is popped automatically

        // Make the OpenGL context current
        glfwMakeContextCurrent(window);
        // Enable v-sync
        glfwSwapInterval(1);

        // Make the window visible
        glfwShowWindow(window);
    }

    private void keyboardHandler(long wind, int key, int scancode, int action, int mods) {
        if (action == GLFW_RELEASE) {
            redraw = true;

            switch (key) {
                case '1':
                    stepSize *= .75;
                    break;
                case '2':
                    stepSize *= 1.25;
                    break;
                case 'W':
                    y += 50 * stepSize;
                    break;
                case 'A':
                    x -= 50 * stepSize;
                    break;
                case 'S':
                    y -= 50 * stepSize;
                    break;
                case 'D':
                    x += 50 * stepSize;
                    break;
                case 'Z':
                    iterations = (int) (iterations * 1.25);
                    break;
                case 'X':
                    iterations = (int) (iterations * .75);
                    break;
                default:
                    redraw = false;
                    break;
            }
        }
    }

    private void loop() {
        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities();

        glMatrixMode(GL_MODELVIEW);
        // Set the clear color
        glClearColor(1.0f, 1.0f, 1.0f, 1.0f);

        // Run the rendering loop until the user has attempted to close
        // the window or has pressed the ESCAPE key.
        while (!glfwWindowShouldClose(window)) {
            // Poll for window events. The key callback above will only be
            // invoked during this call.
            glfwPollEvents();

            if (redraw) {
                render();
                // redraw = false;
            }

            try {
                // Best Case Scenario 20 FPS
                Thread.sleep(25);
            } catch (InterruptedException e) {
                System.out.println("Interrupt Exception!");
            }

        }
    }

    private double x;
    private double y;
    private double stepSize;
    private int iterations;

    private boolean redraw;

    private Item op(Item i) {
        double x = i.val(0);
        double y = i.val(1);

        return mandlebrotColor(vector2Dp(x, y), iterations);
    }

    public Runner() {
        x = 0;
        y = 0;
        stepSize = .01;
        iterations = 100;

        redraw = true;
    }

    // Sprites should be contained in Quadrant I.
    private static Matrix combineSprites(List<Matrix> sprites, int rows, int cols, double scale,
                                         double iWidth, double iHeight, double hSpace, double vSpace) {
        double cellWidth = (iWidth * scale) + (2 * hSpace);
        double cellHeight = (iHeight * scale) + (2 * vSpace);

        Matrix image = null;

        for (int i = 0; i < rows && i * cols < sprites.size(); i++) {
            for (int j = 0; j < cols && (i * cols) + j < sprites.size(); j++) {
                double newX = ((j + .5) * cellWidth);
                double newY = (rows - i - .5) * cellHeight;

                Matrix newSprite = sprites.get((i * cols) + j)
                        .shift2D(-iWidth / 2.0, -iHeight / 2.0)
                        .scale2D(scale)
                        .shift2D(newX, newY);

                image = image == null ? newSprite : image.concat(newSprite);
            }
        }

        return image;
    }

    private static Turtle kco(Turtle turtle, int level) {
        if (level == 0) {
            return turtle.forward(1);
        }

        Turtle nt = turtle.scale(1.0 / 3.0);
        nt = kco(nt, level - 1).rotate(Math.PI / 3.0);
        nt = kco(nt, level - 1).rotate(- 2 * Math.PI / 3.0);
        nt = kco(nt, level - 1).rotate(Math.PI / 3.0);
        return kco(nt, level - 1).scale(3.0);
    }

    private static Turtle kcn(Turtle turtle, int level) {
        if (level == 0) {
            return turtle.forward(1);
        }

        Turtle nt = turtle.move(1.0)
                .rotate(5.0 * Math.PI / 6.0)
                .scale(1.0 / Math.sqrt(3.0));

        nt = kcn(nt, level - 1).rotate(Math.PI / 3.0);
        return kcn(nt, level - 1)
                .rotate(5 * Math.PI / 6.0)
                .scale(Math.sqrt(3.0))
                .move(1.0);
    }

    private static Item mandlebrot(Item zn, Item c) {
        double x = zn.val(0);
        double y = zn.val(1);

        return vector2Dp((x * x) - (y * y), 2.0 * x * y).plus(c);
    }

    private static Item mandlebrotColor(Item c, int iterations) {
        Item z = c;
        for (int i = 0; i < iterations; i++) {
            double x = z.val(0);
            double y = z.val(1);

            z = mandlebrot(z, c);

            if ((x * x) + (y * y) > 4) {
                return item2D(
                        (iterations - (i + 1.0)) / iterations,
                        Math.pow((iterations - (i + 1.0)) / iterations, 2.0), 1.0)
                        .times(i(3).rotate2D(i * Math.PI / 12.0).shift2D(1.0, 1.0).scale2D(.5));
               //  return item2D(0, 0, 0);
            }
        }

        return item2D(0, 0, 0);
//        Item cv = c.minus(z);
//        double dot = cv.val(0) * cv.val(0) + cv.val(1) + cv.val(1);
//
//        return item2D(0.0, 1.0, 1.0).times(i(3).rotate2D(dot * Math.PI));
    }

    private double theta = Math.PI / 2;
    private int dir = 1;

    private void render() {

        // Right now, render only happens once!

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer

        glClearColor(1.0f, 1.0f, 1.0f, 1.0f);

        Matrix image = quadPulse(theta).iterate(6, sprite2D(0, 0, 1, 0));

        glColor3d(0, 0, 0);
        glBegin(GL_LINES);
        image.draw2D();
        glEnd();

        theta += (dir * .01);

        if ((dir == 1 && theta >= Math.PI / 2) || (dir == -1 && theta <= Math.PI / 3)) {
            dir *= -1;
        }

        // Curve Comparison Code.
//        final double theta = Math.PI / 12;
//        final int iterations = 11;
//        final Matrix start = Matrix.sprite2D(0, 0, 1, 0);
//
//        final double[][] colors = new double[][]{
//                new double[] {1.0, 0.0, 0.0},
//                new double[] {0.0, .5, 0.0},
//                new double[] {0.0, 0.0, 1.0},
//                new double[] {1.0, 0.5, 0.0}
//        };
//
//        IFS normal = trianglePulse(theta);
//        IFS reduced = trianglePulseReduced(theta);
//
//        List<Matrix> normalResults = normal.ofList(normal.iterate((iterations - 1) / 2, start));
//        List<Matrix> reducedResults = reduced.ofList(reduced.iterate(iterations, start));
//
//        glColor3d(0, 0, 0);
//        glBegin(GL_LINES);
//
//        for (int i = 0; i < reducedResults.size(); i++) {
//            glColor3dv(colors[i]);
//            reducedResults.get(i).shift2D(-.5, -.3).draw2D();
//        }
//
//        for (int i = 0; i < normalResults.size(); i++) {
//            glColor3dv(colors[i]);
//            normalResults.get(i).shift2D(-.5, .3).draw2D();
//        }
//
//        glEnd();

        glfwSwapBuffers(window); // swap the color buffers
    }

    private void rasterize(UnaryOperator<Item> f) {
        glPointSize(2.0f);

        glPushMatrix();
        glLoadIdentity();

        int[] widthArr = new int[1];
        int[] heightArr = new int[1];
        glfwGetWindowSize(window, widthArr, heightArr);

        int width = widthArr[0];
        int height = heightArr[0];

        double widthScale = (width / 2.0) * stepSize;
        double heightScale = (height / 2.0) * stepSize;

        glScaled(1.0 / widthScale, 1.0 / heightScale, 1);
        glTranslated(-x, -y, 0);

        double cornerX = x - widthScale;
        double cornerY = y - heightScale;

        glBegin(GL_POINTS);

        for (double xc = 0; xc < width; xc++) {
            for (double yc = 0; yc < height; yc++) {
                double xp = cornerX + (stepSize * xc);
                double yp = cornerY + (stepSize * yc);

                Item color = f.apply(vector2D(xp, yp));
                glColor3d(color.val(0), color.val(1), color.val(2));
                glVertex2d(xp, yp);
            }
        }

        glEnd();
        glPopMatrix();
    }


    public static void main(String[] args) {
        (new Runner()).run();
    }
}

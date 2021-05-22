package main;

import math.Item;
import math.Matrix;
import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.lang.reflect.Array;
import java.nio.*;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

import static math.Item.*;
import static math.Matrix.*;

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
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if ( key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE )
                glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
        });

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

    private void loop() {
        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities();

        // Set the clear color
        glClearColor(1.0f, 1.0f, 1.0f, 1.0f);

        // Run the rendering loop until the user has attempted to close
        // the window or has pressed the ESCAPE key.
        while (!glfwWindowShouldClose(window)) {
            // Poll for window events. The key callback above will only be
            // invoked during this call.
            glfwPollEvents();

            // Update.
//            update();

            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer

            render();

            glfwSwapBuffers(window); // swap the color buffers

            try {
                // Best Case Scenario 20 FPS
                Thread.sleep(500);
            } catch (InterruptedException e) {
                System.out.println("Interrupt Exception!");
            }

        }
    }

    private Matrix sprite;

    private List<Matrix> sprites;

    private List<Matrix> sprites1;

    private List<Matrix> wTrans;

    private List<Matrix> wwTrans;

    public Runner() {
        sprite = sprite2D(
                0, 0,
                1.0, 0
        );

        Item p0 = point2D(0, 0);
        Item p1 = point2D(1.0 / 3.0, 0);
        Item p2 = point2D(1.0 / 2.0, Math.sqrt(3.0) / 6.0);
        Item p3 = point2D(2.0 / 3.0, 0);
        Item p4 = point2D(1.0, 0);

        wTrans = new ArrayList<>();
        wTrans.add(i(3).affineTrans2D(p0, p4, p2, p2, p0, p1));
        wTrans.add(i(3).affineTrans2D(p0, p4, p2, p4, p2, p3));

        wwTrans = new ArrayList<>();
        for (Matrix wi : wTrans) {
            for (Matrix wj : wTrans) {
                wwTrans.add(wi.times(wj));
            }
        }

        sprite = iterate(sprite, wwTrans, 8);

        sprites = new ArrayList<>();

        for (Matrix wwTran : wwTrans) {
            sprites.add(sprite.times(wwTran));
        }

        sprites1 = new ArrayList<>();
        for (Matrix wTran : wTrans) {
            sprites1.add(sprite.times(wTran));
        }
    }

    private static Matrix iterate(Matrix s0, List<Matrix> ifs, int reps) {
        Matrix s = s0;

        for (int i = 0; i < reps; i++) {
            Matrix next = s.times(ifs.get(0));
            for (int j = 1; j < ifs.size(); j++) {
                next = next.concat(s.times(ifs.get(j)));
            }

            s = next;
        }

        return s;
    }

    private static Matrix combineSprites(List<Matrix> sprites) {
        double width = 1.0 / sprites.size();

        Matrix S = i(3).scale2D(width - .1).shift2D(.05, 0);

        Matrix combine = sprites.get(0).times(S);

        for (int i = 1; i < sprites.size(); i++) {
            S = S.shift2D(width, 0);

            combine = combine.concat(sprites.get(i).times(S));
        }

        return combine;
    }


    private void render() {
        glClearColor(1.0f, 1.0f, 1.0f, 1.0f);

        glLineWidth(2);

        glColor3d(0.0, 0.0, 0.0);
        glBegin(GL_LINES);

        double[][] colors = new double[][] {
          new double[] {1.0, 0.0, 0.0},
          new double[] {0.0, 0.0, 0.0},
          new double[] {0.0, 0.0, 1.0},
          new double[] {0.0, 0.5, 0.0}
        };

        for (int i = 0; i < sprites.size(); i++) {
            glColor3dv(colors[i]);
            sprites.get(i).scale2D(.8).shift2D(-.85, 0).draw2D();
        }

        for (int i = 0; i < sprites1.size(); i++) {
            glColor3dv(colors[i]);
            sprites1.get(i).flip2D(1.0, 0).shift2D(0, Math.tan(Math.PI / 6.0) / 2.0).scale2D(.8).shift2D(.05, 0).draw2D();
        }

//        sprite.shift2D(-.5, 0).draw2D();
        glEnd();
    }


    public static void main(String[] args) {
        (new Runner()).run();
    }
}

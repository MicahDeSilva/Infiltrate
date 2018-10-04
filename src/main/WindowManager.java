package main;

import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import game.Block;
import game.Bullet;
import game.CameraManager;
import game.DataPacket;
import game.Main;
import game.SelectionManager;
import game.ServerManager;
import game.Unit;
import title.Font;
import title.Title;

import java.awt.Toolkit;
import java.nio.*;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class WindowManager
{
	// The window handle
	private static long window;
	private static Toolkit toolkit = Toolkit.getDefaultToolkit();
	public static double width = toolkit.getScreenSize().getWidth(), height = toolkit.getScreenSize().getHeight(), cameraX = 0, cameraY = 0,
			deltaTime;
	public static double globalGameSpeedModifier = 300;
	long lastTime = System.nanoTime();
	Main main;
	SelectionManager selectionManager;
	CameraManager cameraManager;
	double[] cameraPos;
	public static State state = State.TITLE;
	Title title;
	public static boolean gameLaunching = false;
	Font font;

	enum State
	{
		TITLE, GAME
	}

	public WindowManager()
	{
		if (state == State.TITLE)
			title = new Title(this);
		try
		{
			init();
			loop();

			glfwFreeCallbacks(window);
			glfwDestroyWindow(window);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			glfwTerminate();
			// glfwSetErrorCallback(null).free();
		}
	}

	public static void main(String args[])
	{
		try
		{
			new WindowManager();
		}
		finally
		{
			try
			{
				ServerManager.unbindPort();
			}
			catch (Exception e)
			{

			}
		}
	}

	/**
	 * Initialises LWJGL as per https://www.lwjgl.org/guide. Creates a window ready for drawing to.
	 **/
	public void init()
	{
		// Setup an error callback. The default implementation
		// will print the error message in System.err.
		GLFWErrorCallback.createPrint(System.err).set();

		// Initialize GLFW. Most GLFW functions will not work before doing this.
		if (!glfwInit())
			throw new IllegalStateException("Unable to initialize GLFW");

		// Configure GLFW
		// glfwDefaultWindowHints(); // optional, the current window hints are already the default
		// glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
		// glfwWindowHint(GLFW_STENCIL_BITS, 4);
		// glfwWindowHint(GLFW_SAMPLES, 4);

		Toolkit toolkit = Toolkit.getDefaultToolkit();
		glfwWindowHint(GLFW_DECORATED, 0);
		// Create the window
		window = glfwCreateWindow((int) toolkit.getScreenSize().getWidth(), (int) toolkit.getScreenSize().getHeight(), "Infiltrate", NULL, NULL);
		if (window == NULL)
			throw new RuntimeException("Failed to create the GLFW window");

		// Setup a key callback. It will be called every time a key is pressed, repeated or released.
		glfwSetKeyCallback(window, (window, key, scancode, action, mods) ->
		{
			if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
				glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
		});

		// Get the thread stack and push a new frame
		try (MemoryStack stack = stackPush())
		{
			IntBuffer pWidth = stack.mallocInt(1); // int*
			IntBuffer pHeight = stack.mallocInt(1); // int*

			// Get the window size passed to glfwCreateWindow
			glfwGetWindowSize(window, pWidth, pHeight);

			// Get the resolution of the primary monitor
			GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

			// Center the window
			glfwSetWindowPos(window, (vidmode.width() - pWidth.get(0)) / 2, (vidmode.height() - pHeight.get(0)) / 2);
		} // the stack frame is popped automatically

		// Make the OpenGL context current
		glfwMakeContextCurrent(window);
		// Enable v-sync
		glfwSwapInterval(1);

		// Make the window visible
		glfwShowWindow(window);

		GL.createCapabilities();

		font = new Font();
		cameraManager = new CameraManager();
	}

	private void loop()
	{
		// This line is critical for LWJGL's interoperation with GLFW's
		// OpenGL context, or any context that is managed externally.
		// LWJGL detects the context that is current in the current thread,
		// creates the GLCapabilities instance and makes the OpenGL
		// bindings available for use.

		// Set the clear color
		glClearColor(0.1f, 0.1f, 0.1f, 1.0f);

		glViewport(0, 0, (int) width, (int) height);

		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		glOrtho(0, width, 0, height, -1, 1);
		glMatrixMode(GL_MODELVIEW);

		while (!glfwWindowShouldClose(window))
		{
			deltaTime = (System.nanoTime() - lastTime) * 1.0E-9;

			if ((System.nanoTime() - nanoTimeLastCount) * 1.0E-9 > 1.0)
			{
				System.out.println("FPS: " + frameNumber);
				frameNumber = 0;
				nanoTimeLastCount = System.nanoTime();
			}
			frameNumber++;

			lastTime = System.nanoTime();
			if (state == State.GAME)
			{
				gameUpdateLoop();
				gameRenderLoop();
			}
			else if (state == State.TITLE)
			{
				title.render(window, font);
				title.update();
			}
		}
		// Run the rendering loop until the user has attempted to close
		// the window or has pressed the ESCAPE key.
	}

	private int frameNumber = 0;
	private long nanoTimeLastCount = 0;

	private void gameUpdateLoop()
	{
		DataPacket.clearDataPacket();

		selectionManager.update();
		cameraPos = CameraManager.updateCameraPos();
		cameraX = cameraPos[0];
		cameraY = cameraPos[1];

		for (Unit unit : main.friendly)
			unit.update();

		for (Unit unit : main.enemy)
			unit.update();

		for (Bullet bullet : main.bulletsFriendly)
			bullet.update();

		for (Bullet bullet : main.bulletsEnemy)
			bullet.update();

		main.update();
	}

	private void gameRenderLoop()
	{
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

		// Friendlies
		for (Unit unit : main.friendly)
		{
			glColor3d(0, 199.0 / 255, 117.0 / 255);
			// unit.update();
			unit.renderFriendly(cameraX, cameraY);
		}

		// Enemy units
		glColor3d(230.0 / 255, 50.0 / 255, 125.0 / 255);

		for (Unit unit : main.enemy)
		{
			double modifiedX = unit.x - cameraX;
			double modifiedY = unit.y - cameraY;

			glBegin(GL_LINE_LOOP);
			glVertex2d(modifiedX + 22.4 * Math.sin(Math.toRadians(unit.direction + 150)), modifiedY + 22.4 * Math.cos(Math.toRadians(unit.direction + 150)));
			glVertex2d(modifiedX + 22.4 * Math.sin(Math.toRadians(unit.direction - 150)), modifiedY + 22.4 * Math.cos(Math.toRadians(unit.direction - 150)));
			glVertex2d(modifiedX + 30 * Math.sin(Math.toRadians(unit.direction)), modifiedY + 30 * Math.cos(Math.toRadians(unit.direction)));
			glEnd();
		}

		// Selection Box
		if (selectionManager.drawSelection)
			drawRect(selectionManager.selection[0] - cameraX, selectionManager.selection[1] - cameraY, selectionManager.selection[2] - cameraX, selectionManager.selection[3] - cameraY);

		// Bullets
		for (Bullet bullet : main.bulletsFriendly)
			bullet.render(cameraX, cameraY);

		for (Bullet bullet : main.bulletsEnemy)
			bullet.render(cameraX, cameraY);

		// Walls
		for (Block block : main.blocks)
			block.render(cameraX, cameraY, gameLaunching);

		// Walls
		for (Block block : main.uncollidableBlocks)
			block.render(cameraX, cameraY, false);

		glfwSwapBuffers(window); // swap the color buffers

		// Poll for window events. The key callback above will only be
		// invoked during this call.
		glfwPollEvents();

	}

	void drawRect(double x1, double y1, double x2, double y2)
	{
		glColor3f(0.3f, 0.3f, 0.3f);
		glLineWidth(3);

		glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);

		glBegin(GL_POLYGON);
		glVertex2d(x1, y1);
		glVertex2d(x1, y2);
		glVertex2d(x2, y2);
		glVertex2d(x2, y1);
		glEnd();

		glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
	}

	public static double[] getCursorPosition()
	{
		double[] returnVal = new double[2];
		DoubleBuffer xBuffer = BufferUtils.createDoubleBuffer(1);
		DoubleBuffer yBuffer = BufferUtils.createDoubleBuffer(1);
		glfwGetCursorPos(window, xBuffer, yBuffer);
		returnVal[0] = cameraX + xBuffer.get(0);
		returnVal[1] = cameraY + height - yBuffer.get(0);

		return returnVal;
	}

	public static double[] getRawCursorPosition()
	{
		double[] returnVal = new double[2];
		DoubleBuffer xBuffer = BufferUtils.createDoubleBuffer(1);
		DoubleBuffer yBuffer = BufferUtils.createDoubleBuffer(1);
		glfwGetCursorPos(window, xBuffer, yBuffer);
		returnVal[0] = xBuffer.get(0);
		returnVal[1] = height - yBuffer.get(0);

		return returnVal;
	}

	public static int getMouseButtonState(int button)
	{
		return glfwGetMouseButton(window, button);
	}

	public static int getKeyState(int key)
	{
		return glfwGetKey(window, key);
	}

	public boolean startGameState(Main main, SelectionManager selectionManager)
	{
		if (state == State.TITLE)
		{
			this.main = main;
			this.selectionManager = selectionManager;
			state = State.GAME;
			gameLaunching = true;
			globalGameSpeedModifier = 40;
			glfwSetKeyCallback(window, (window, key, scancode, action, mods) ->
			{
				/*if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
					glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop*/
			});
			return true;
		}
		return false;
	}
}

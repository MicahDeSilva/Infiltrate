package title;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.stb.STBEasyFont.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Random;

import org.lwjgl.BufferUtils;

import game.*;
import main.WindowManager;

public class Title
{
	// Main
	WindowManager window;
	boolean split = false;
	public TitleUnit[] units = new TitleUnit[300];
	double startX = WindowManager.width / 2/* - 550*/, startY = WindowManager.height / 2;
	boolean titleFinished = false;

	// Glitches
	long glitchStartTime = 0;
	Random rand = new Random();
	boolean glitch = false;
	boolean glitch2 = false;
	long glitch2StartTime = 0;
	boolean glitchOccurred = false;
	int quads;

	// Text
	String text = "Hello World";

	// Audio
	
	
	
	public Title(WindowManager window)
	{
		this.window = window;
		//AudioDriver.load();
		//Music music = new Music("resource/Title.mp3");
		//music.play();
	}

	void startGame()
	{
		try
		{
			Main main = new Main();
			SelectionManager selectionManager = new SelectionManager(main);
			window.startGameState(main, selectionManager);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public void update()
	{
		if (!titleFinished)
		{
			if (WindowManager.getMouseButtonState(GLFW_MOUSE_BUTTON_LEFT) == GLFW_PRESS && split == false)
			{
				split = true;
				createUnits();
				int i = 0;
				/*for (i = 0; i < units.length / 2; i++)
				{
					units[i].goalSet = true;
					units[i].xTarget = WindowManager.width * 0.25;
					units[i].yTarget = WindowManager.height * 0.5;
				}
				while (i < units.length)
				{
					units[i].goalSet = true;
					units[i].xTarget = WindowManager.width * 0.75;
					units[i].yTarget = WindowManager.height * 0.5;
					i++;
				}*/
			}
			if (!split)
			{
				createUnits();
			}

			if (split)
			{
				WindowManager.globalGameSpeedModifier *= 0.95;
				WindowManager.globalGameSpeedModifier = Math.max(100, WindowManager.globalGameSpeedModifier);
				boolean unitsLeft = false;
				for (TitleUnit unit : units)
				{
					if (unit.enabled)
					{
						unitsLeft = true;
						unit.update();
					}
				}
				if (unitsLeft == false)
					titleFinished = true;
			}
		}
		else
		{
			startGame();
		}
	}

	private void createUnits()
	{
		int currentUnit = 0;
		double modX = startX - 550;
		currentUnit = createI(modX - 140, WindowManager.height / 2, units.length / 10, currentUnit);
		currentUnit = createN(modX, WindowManager.height / 2, units.length / 10, currentUnit);
		currentUnit = createF(modX + 190, WindowManager.height / 2, units.length / 10, currentUnit);
		currentUnit = createI(modX + 310, WindowManager.height / 2, units.length / 10, currentUnit);
		currentUnit = createL(modX + 430, WindowManager.height / 2, units.length / 10, currentUnit);
		currentUnit = createT(modX + 550, WindowManager.height / 2, units.length / 10, currentUnit);

		currentUnit = createF(modX + 720, WindowManager.height / 2, units.length / 10, currentUnit);
		currentUnit = createF(modX + 900, WindowManager.height / 2, units.length / 10, currentUnit);

		currentUnit = createT(modX + 1070, WindowManager.height / 2, units.length / 10, currentUnit);

		currentUnit = createF(modX + 1240, WindowManager.height / 2, units.length / 10, currentUnit);

		while (currentUnit < units.length)
		{
			units[currentUnit++] = new TitleUnit(this, startX, WindowManager.height / 2);
		}
	}

	private int createI(double x, double y, int size, int currentUnit)
	{
		int i = 0;
		for (i = currentUnit; i < currentUnit + size; i++)
			units[i] = new TitleUnit(this, x, y - 100 + (i - currentUnit) * (200 / (size)));
		return i;
	}

	private int createN(double x, double y, int size, int currentUnit)
	{
		int i = 0;
		for (i = currentUnit; i < currentUnit + size / 3; i++)
			units[i] = new TitleUnit(this, x - 70, y - 100 + (i - currentUnit) * (200 / (size / 3)));
		currentUnit = i;

		for (i = currentUnit; i < currentUnit + size / 3; i++)
			units[i] = new TitleUnit(this, x + 70, y - 100 + (i - currentUnit) * (200 / (size / 3)));

		currentUnit = i;
		for (i = currentUnit; i < currentUnit + size / 3; i++)
			units[i] = new TitleUnit(this, x - 100 + (i - currentUnit) * (200 / (size / 3)), y + 100 - (i - currentUnit) * (200 / (size / 3)));

		return i;
	}

	private int createF(double x, double y, int size, int currentUnit)
	{
		int i = 0;
		for (i = currentUnit; i < currentUnit + size / 3; i++)
			units[i] = new TitleUnit(this, x - 50, y - 100 + (i - currentUnit) * (200 / (size / 3)));
		currentUnit = i;

		for (i = currentUnit; i < currentUnit + size / 3; i++)
			units[i] = new TitleUnit(this, x - 70 + (i - currentUnit) * (140 / (size / 3)), y + 85);

		currentUnit = i;
		for (i = currentUnit; i < currentUnit + size / 3; i++)
			units[i] = new TitleUnit(this, x - 70 + (i - currentUnit) * (140 / (size / 3)), y + 15);

		return i;
	}

	private int createL(double x, double y, int size, int currentUnit)
	{
		int i = 0;
		for (i = currentUnit; i < currentUnit + size / 2; i++)
			units[i] = new TitleUnit(this, x - 50, y - 100 + (i - currentUnit) * (200 / (size / 2)));
		currentUnit = i;

		for (i = currentUnit; i < currentUnit + size / 2; i++)
			units[i] = new TitleUnit(this, x - 70 + (i - currentUnit) * (140 / (size / 2)), y - 85);

		return i;
	}

	private int createT(double x, double y, int size, int currentUnit)
	{
		int i = 0;
		for (i = currentUnit; i < currentUnit + size / 2; i++)
			units[i] = new TitleUnit(this, x, y - 100 + (i - currentUnit) * (200 / (size / 2)));
		currentUnit = i;

		for (i = currentUnit; i < currentUnit + size / 2; i++)
			units[i] = new TitleUnit(this, x - 70 + (i - currentUnit) * (140 / (size / 2)), y + 85);

		return i;
	}

	public void render(long windowLong, Font font)
	{
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

		if (!split)
			glitchHandling();

		//font.drawString((int) WindowManager.width / 2, (int) WindowManager.height / 2, text);
		
		if (!split && !glitch)
		{
			TextRenderer.drawString("INFILTRATE", startX, startY, 100);
			/*TextRenderer.renderI(startX - 140, startY, size);
			TextRenderer.renderN(startX, startY, size);
			TextRenderer.renderF(startX + 190, startY, size);
			TextRenderer.renderI(startX + 310, startY, size);
			TextRenderer.renderL(startX + 430, startY, size);

			TextRenderer.renderT(startX + 550, startY, size);
			TextRenderer.renderR(startX + 720, startY, size);
			TextRenderer.renderA(startX + 900, startY, size);
			TextRenderer.renderT(startX + 1070, startY, size);
			TextRenderer.renderE(startX + 1240, startY, size);*/
		}
		if (split || glitch)
		{
			if (!titleFinished)
				renderUnits(!split);
		}

		glfwSwapBuffers(windowLong); // swap the color buffers

		glfwPollEvents();
	}

	private void drawText(float x, float y, String text)
	{
		ByteBuffer charBuffer = BufferUtils.createByteBuffer(text.length() * 270);
		int num_quads;

		num_quads = stb_easy_font_print(x, y, text, null, charBuffer);

		glEnableClientState(GL_VERTEX_ARRAY);
		glVertexPointer(2, GL_FLOAT, 16, charBuffer);
		nglDrawArrays(GL_QUADS, 0, num_quads * 4);
		glDisableClientState(GL_VERTEX_ARRAY);
	}

	private void glitchHandling()
	{
		if (glitch && System.currentTimeMillis() - glitchStartTime > 100)
			glitch = false;
		if (glitch2 && System.currentTimeMillis() - glitch2StartTime > 100)
			glitch2 = false;

		if (!glitch && rand.nextInt(70) < 1 && units[0] != null)
		{
			glitchStartTime = System.currentTimeMillis();
			glitch = true;
			glitchOccurred = true;
		}

		if (glitchOccurred && !glitch2 && rand.nextInt(70) < 1 || glitch && rand.nextInt(30) < 1)
		{
			glitch2StartTime = System.currentTimeMillis();
			glitch2 = true;
		}

		if (!glitch2)
		{
			startX = WindowManager.width / 2;
			startY = WindowManager.height / 2;
		}
		else
		{
			startX = WindowManager.width / 2 - 100 + rand.nextDouble() * 200;
			startY = WindowManager.height / 2 - 100 + rand.nextDouble() * 200;
		}
	}

	private void renderUnits(boolean glitched)
	{
		if (glitched)
			glBegin(GL_LINE_STRIP);

		glColor3d(1, 1, 1);

		for (TitleUnit unit : units)
		{
			if (unit.enabled)
			{
				if(!glitched)
					glBegin(GL_LINE_LOOP);
				glVertex2d(unit.x + 22.4 * Math.sin(Math.toRadians(unit.direction + 150)), unit.y + 22.4 * Math.cos(Math.toRadians(unit.direction + 150)));
				glVertex2d(unit.x + 22.4 * Math.sin(Math.toRadians(unit.direction - 150)), unit.y + 22.4 * Math.cos(Math.toRadians(unit.direction - 150)));
				glVertex2d(unit.x + 30 * Math.sin(Math.toRadians(unit.direction)), unit.y + 30 * Math.cos(Math.toRadians(unit.direction)));
				if(!glitched)
					glEnd();
			}
		}

		glEnd();
	}
}

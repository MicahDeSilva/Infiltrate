package game;

import static org.lwjgl.opengl.GL11.*;

import java.util.Random;

public class Block
{
	final int x, y;
	boolean render = false;
	static Random rand = new Random();

	public Block(double x, double y)
	{
		this.x = Algorithms.snapToGrid(x) * Algorithms.tileResolution;
		this.y = Algorithms.snapToGrid(y) * Algorithms.tileResolution;
	}

	// Draw self
	public void render(double cameraX, double cameraY, boolean gameLaunching)
	{
		if (!render && gameLaunching)
		{
			if (rand.nextInt(60) < 1)
				render = true;
		}
		else
			render = true;

		if (render)
		{
			glColor3d(1, 1, 1);

			double modifiedX = x - cameraX;
			double modifiedY = y - cameraY;

			glBegin(GL_LINE_LOOP);
			glVertex2d(modifiedX - Algorithms.tileResolution / 2, modifiedY - Algorithms.tileResolution / 2);
			glVertex2d(modifiedX + Algorithms.tileResolution / 2, modifiedY - Algorithms.tileResolution / 2);
			glVertex2d(modifiedX + Algorithms.tileResolution / 2, modifiedY + Algorithms.tileResolution / 2);
			glVertex2d(modifiedX - Algorithms.tileResolution / 2, modifiedY + Algorithms.tileResolution / 2);
			glEnd();
		}
	}
}

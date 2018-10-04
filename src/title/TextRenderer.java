package title;

import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glColor3d;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glVertex2d;

import java.util.HashMap;
import java.util.Map;

public class TextRenderer
{
	public static Map<Character, Double> map = new HashMap<Character, Double>();
	static
	{
		map.put('A', 1.4d);
		map.put('E', 1.4d);
		map.put('F', 1.4d);
		map.put('H', 1.4d);
		map.put('I', 0.4d);
		map.put('L', 1.2d);
		map.put('N', 1.8d);
		map.put('O', 1.4d);
		map.put('R', 1.4d);
		map.put('T', 1.4d);
		map.put('S', 1.4d);
	}

	static Halign halign = Halign.CENTRE;

	public enum Halign
	{
		LEFT, RIGHT, CENTRE
	}

	public static void drawString(String text, double x, double y, double size, double spacing)
	{
		char[] chars = text.toCharArray();

		if (halign == Halign.CENTRE)
		{
			for (char c : chars)
			{
				x -= map.get(c) * size * 0.5;
				x -= spacing * 0.5;
				if (c == 'L')
					x += size * 0.1;
			}
		}

		for (char c : chars)
		{
			x += map.get(c) * size * 0.5;
			renderChar(c, x, y, size);
			x += map.get(c) * size * 0.5;
			x += spacing;

			if (c == 'L')
				x -= size * 0.2;
		}
	}

	public static void drawString(String text, double x, double y, double size)
	{
		drawString(text, x, y, size, size * 0.4);
	}

	private static void renderChar(char c, double x, double y, double size)
	{
		switch (c)
		{
			case ('A'):
				renderA(x, y, size);
				break;
			case ('E'):
				renderE(x, y, size);
				break;
			case ('F'):
				renderF(x, y, size);
				break;
			case ('H'):
				renderH(x, y, size);
				break;
			case ('I'):
				renderI(x, y, size);
				break;
			case ('L'):
				renderL(x, y, size);
				break;
			case ('N'):
				renderN(x, y, size);
				break;
			case ('O'):
				renderO(x, y, size);
				break;
			case ('R'):
				renderR(x, y, size);
				break;
			case ('S'):
				renderS(x, y, size);
				break;
			case ('T'):
				renderT(x, y, size);
				break;
			default:
				System.out.println("Cannot print character " + c);
		}
	}

	public static void renderA(double x, double y, double size)
	{
		renderF(x, y, size);

		glColor3d(1, 1, 1);

		glBegin(GL_QUADS);

		glVertex2d(x + size * 0.3, y - size);
		glVertex2d(x + size * 0.7, y - size);
		glVertex2d(x + size * 0.7, y + size);
		glVertex2d(x + size * 0.3, y + size);

		glEnd();
	}

	public static void renderH(double x, double y, double size)
	{
		glColor3d(1, 1, 1);

		glBegin(GL_QUADS);

		renderRectangle(x - size * 0.7, y - size, x - size * 0.3, y + size);
		renderRectangle(x - size * 0.7, y + size * 0.2, x + size * 0.5, y - size * 0.1);
		renderRectangle(x + size * 0.3, y - size, x + size * 0.7, y + size);

		glEnd();
	}

	public static void renderI(double x, double y, double size)
	{
		glColor3d(1, 1, 1);

		glBegin(GL_QUADS);
		glVertex2d(x - size * 0.2, y - size);
		glVertex2d(x + size * 0.2, y - size);
		glVertex2d(x + size * 0.2, y + size);
		glVertex2d(x - size * 0.2, y + size);
		glEnd();
	}

	public static void renderN(double x, double y, double size)
	{
		glColor3d(1, 1, 1);

		glBegin(GL_QUADS);
		glVertex2d(x - size * 0.9, y - size);
		glVertex2d(x - size * 0.5, y - size);
		glVertex2d(x - size * 0.5, y + size);
		glVertex2d(x - size * 0.9, y + size);

		glVertex2d(x + size * 0.5, y - size);
		glVertex2d(x + size * 0.9, y - size);
		glVertex2d(x + size * 0.9, y + size);
		glVertex2d(x + size * 0.5, y + size);

		// Diagonal
		glVertex2d(x - size * 0.9, y + size);
		glVertex2d(x - size * 0.5, y + size);
		glVertex2d(x + size * 0.9, y - size);
		glVertex2d(x + size * 0.5, y - size);

		glEnd();
	}

	public static void renderO(double x, double y, double size)
	{
		glColor3d(1, 1, 1);

		glBegin(GL_QUADS);

		renderRectangle(x - size * 0.7, y - size, x - size * 0.4, y + size);
		renderRectangle(x - size * 0.7, y + size, x + size * 0.7, y + size * 0.7);
		renderRectangle(x + size * 0.4, y - size, x + size * 0.7, y + size);
		renderRectangle(x - size * 0.7, y - size, x + size * 0.7, y - size * 0.7);

		glEnd();
	}

	
	public static void renderF(double x, double y, double size)
	{
		glColor3d(1, 1, 1);

		glBegin(GL_QUADS);

		glVertex2d(x - size * 0.7, y - size);
		glVertex2d(x - size * 0.3, y - size);
		glVertex2d(x - size * 0.3, y + size);
		glVertex2d(x - size * 0.7, y + size);

		glVertex2d(x - size * 0.7, y + size);
		glVertex2d(x + size * 0.7, y + size);
		glVertex2d(x + size * 0.7, y + size * 0.7);
		glVertex2d(x - size * 0.7, y + size * 0.7);

		glVertex2d(x - size * 0.7, y + size * 0.2);
		glVertex2d(x + size * 0.5, y + size * 0.2);
		glVertex2d(x + size * 0.5, y - size * 0.1);
		glVertex2d(x - size * 0.7, y - size * 0.1);

		glEnd();
	}

	public static void renderL(double x, double y, double size)
	{
		glColor3d(1, 1, 1);

		glBegin(GL_QUADS);

		glVertex2d(x - size * 0.6, y - size);
		glVertex2d(x - size * 0.2, y - size);
		glVertex2d(x - size * 0.2, y + size);
		glVertex2d(x - size * 0.6, y + size);

		glVertex2d(x - size * 0.6, y - size);
		glVertex2d(x + size * 0.6, y - size);
		glVertex2d(x + size * 0.6, y - size * 0.7);
		glVertex2d(x - size * 0.6, y - size * 0.7);

		glEnd();
	}

	public static void renderT(double x, double y, double size)
	{
		glColor3d(1, 1, 1);

		glBegin(GL_QUADS);

		glVertex2d(x - size * 0.2, y - size);
		glVertex2d(x + size * 0.2, y - size);
		glVertex2d(x + size * 0.2, y + size);
		glVertex2d(x - size * 0.2, y + size);

		glVertex2d(x - size * 0.7, y + size);
		glVertex2d(x + size * 0.7, y + size);
		glVertex2d(x + size * 0.7, y + size * 0.7);
		glVertex2d(x - size * 0.7, y + size * 0.7);

		glEnd();
	}

	public static void renderR(double x, double y, double size)
	{
		glColor3d(1, 1, 1);

		glBegin(GL_QUADS);

		glVertex2d(x - size * 0.7, y - size);
		glVertex2d(x - size * 0.3, y - size);
		glVertex2d(x - size * 0.3, y + size);
		glVertex2d(x - size * 0.7, y + size);

		glVertex2d(x - size * 0.7, y + size);
		glVertex2d(x + size * 0.7, y + size);
		glVertex2d(x + size * 0.7, y + size * 0.7);
		glVertex2d(x - size * 0.7, y + size * 0.7);

		glVertex2d(x - size * 0.7, y + size * 0.2);
		glVertex2d(x + size * 0.7, y + size * 0.2);
		glVertex2d(x + size * 0.7, y - 10);
		glVertex2d(x - size * 0.7, y - 10);

		glVertex2d(x + size * 0.3, y + size);
		glVertex2d(x + size * 0.7, y + size);
		glVertex2d(x + size * 0.7, y);
		glVertex2d(x + size * 0.3, y);

		// Diagonal
		glVertex2d(x - size * 0.3, y);
		glVertex2d(x + size * 0.15, y);
		glVertex2d(x + size * 0.9, y - size);
		glVertex2d(x + size * 0.45, y - size);

		glEnd();
	}

	public static void renderS(double x, double y, double size)
	{
		glColor3d(1, 1, 1);

		renderRectangle(x - size * 0.7, y + size, x + size * 0.7, y + size * 0.7);
		renderRectangle(x - size * 0.7, y + size * 0.2, x + size * 0.7, y - size * 0.1);
		renderRectangle(x - size * 0.7, y - size * 0.7, x + size * 0.7, y - size);

		renderRectangle(x - size * 0.7, y + size, x - size * 0.4, y);
		renderRectangle(x + size * 0.7, y, x + size * 0.4, y - size);

		glEnd();
	}

	public static void renderE(double x, double y, double size)
	{
		renderF(x, y, size);

		glColor3d(1, 1, 1);

		glBegin(GL_QUADS);

		glVertex2d(x - size * 0.7, y - size);
		glVertex2d(x + size * 0.7, y - size);
		glVertex2d(x + size * 0.7, y - size * 0.7);
		glVertex2d(x - size * 0.7, y - size * 0.7);

		glEnd();
	}

	private static void renderRectangle(double x1, double y1, double x2, double y2)
	{
		glBegin(GL_QUADS);

		glVertex2d(x1, y1);
		glVertex2d(x1, y2);
		glVertex2d(x2, y2);
		glVertex2d(x2, y1);

		glEnd();
	}
}

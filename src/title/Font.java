package title;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.lwjgl.opengl.GL11;

public class Font
{
	public java.awt.Font font;
	private FontMetrics metrics;

	private int textureWidth = 512 * 4;
	private int textureHeight = 512 * 4;

	private int fontHeight = 100;

	private IntObject[] charArray = new IntObject[256];

	public Texture tex;

	boolean antialias = false;

	public Font()
	{
		try
		{
			font = java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT, new FileInputStream("resource/ColabThi.otf"));
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (FontFormatException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		font = font.deriveFont(java.awt.Font.PLAIN, fontHeight);

		BufferedImage imgTemp = new BufferedImage(textureWidth, textureHeight, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = (Graphics2D) imgTemp.getGraphics();

		g.setColor(new Color(0, 0, 0, 0));
		g.fillRect(0, 0, textureWidth, textureHeight);

		int rowHeight = 0;
		int positionX = 0;
		int positionY = 0;

		for (int i = 0; i < 256; i++)
		{
			char ch = (char) i;

			BufferedImage fontImage = getCharImage(ch);

			IntObject newIntObject = new IntObject();

			newIntObject.width = fontImage.getWidth();
			newIntObject.height = fontImage.getHeight();

			if (positionX + newIntObject.width >= textureWidth)
			{
				positionX = 0;
				positionY += rowHeight;
				rowHeight = 0;
			}

			newIntObject.storedX = positionX;
			newIntObject.storedY = positionY;

			if (newIntObject.height > fontHeight)
				fontHeight = newIntObject.height;

			if (newIntObject.height > rowHeight)
				rowHeight = newIntObject.height;

			g.drawImage(fontImage, positionX, positionY, null);

			positionX += newIntObject.width;

			charArray[i] = newIntObject;

			fontImage = null;
		}

		tex = TextureLoader.getTextureFromImageTexture(TextureLoader.loadTextureFromImage(imgTemp), false, false, true);
	}

	private BufferedImage getCharImage(char c)
	{
		BufferedImage tempfontImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = (Graphics2D) tempfontImage.getGraphics();
		if (antialias)
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setFont(font);
		metrics = g.getFontMetrics();
		int charwidth = metrics.charWidth(c) + 8;

		if (charwidth <= 0)
			charwidth = 7;
		int charheight = metrics.getHeight() + 3;
		if (charheight <= 0)
			charheight = fontHeight;

		BufferedImage fontImage;
		fontImage = new BufferedImage(charwidth, charheight, BufferedImage.TYPE_INT_ARGB);
		Graphics2D gt = (Graphics2D) fontImage.getGraphics();
		if (antialias)
			gt.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		gt.setFont(font);

		gt.setColor(Color.WHITE);
		int charx = 3;
		int chary = 1;
		gt.drawString(String.valueOf(c), (charx), (chary) + metrics.getAscent());

		return fontImage;
	}

	public final static int ALIGN_LEFT = 0, ALIGN_RIGHT = 1, ALIGN_CENTER = 2;

	private int correctL = 9;

	public void drawString(int x, int y, String string)
	{
		drawString(x, y, string, -1);
	}

	public void drawString(int x, int y, String string, int wrapWidth)
	{
		IntObject intObject = null;

		int totalwidth = 0;
		int c = correctL - 1;
		float startY = 0;

		tex.bind();
		GL11.glBegin(GL11.GL_QUADS);

		for (char character : string.toCharArray())
		{
			intObject = charArray[character];
			if (intObject == null)
				continue;

			if (wrapWidth >= 0 && totalwidth + intObject.width - c >= wrapWidth)
			{
				startY += fontHeight * 0.8;
				totalwidth = 0;
			}

			if (character == '\n')
			{
				startY += fontHeight * 0.8;
				totalwidth = 0;
			}
			else
			{
				//drawQuad(totalwidth + x, startY + y + intObject.height, totalwidth + intObject.width + x, startY + y, intObject.storedX, intObject.storedY + intObject.height, intObject.storedX + intObject.width, intObject.storedY);
				totalwidth += (intObject.width - c);
			}
		}
		
		GL11.glColor4d(1, 1, 1, 1);
		GL11.glTexCoord2f(0, 0);
		GL11.glVertex2f(x, y);
		GL11.glTexCoord2f(1, 0);
		GL11.glVertex2f(x + 300, y);
		GL11.glTexCoord2f(1, 1);
		GL11.glVertex2f(x + 300, y + 300);
		GL11.glTexCoord2f(0, 1);
		GL11.glVertex2f(x, y + 300);
		GL11.glEnd();
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, -1);
	}

	public int getLength(String string, int wrapWidth)
	{
		int c = correctL - 1;
		int length = 0;
		int lineLength = 0;
		for (char character : string.toCharArray())
		{
			IntObject intObject = charArray[character];
			if (intObject == null)
				continue;

			if (wrapWidth >= 0 && lineLength + intObject.width - c >= wrapWidth)
				lineLength = 0;

			lineLength += intObject.width - c;
			length = Math.max(length, lineLength);
		}
		return length;
	}

	private void drawQuad(float x1, float y1, float x2, float y2, float srcX, float srcY, float srcX2, float srcY2)
	{
		float width = x2 - x1;
		float height = y2 - y1;
		float textureSrcX = srcX / (float) textureWidth;
		float textureSrcY = srcY / (float) textureHeight;
		float srcWidth = srcX2 - srcX;
		float srcHeight = srcY2 - srcY;
		float renderWidth = srcWidth / (float) textureWidth;
		float renderHeight = srcHeight / (float) textureHeight;

		GL11.glTexCoord2f(textureSrcX, textureSrcY);
		GL11.glVertex2f(x1, y1);

		GL11.glTexCoord2f(textureSrcX + renderWidth, textureSrcY);
		GL11.glVertex2f(x1 + width, y1);

		GL11.glTexCoord2f(textureSrcX + renderWidth, textureSrcY + renderHeight);
		GL11.glVertex2f(x1 + width, y1 + height);

		GL11.glTexCoord2f(textureSrcX, textureSrcY + renderHeight);
		GL11.glVertex2f(x1, y1 + height);
	}

	private class IntObject
	{
		public int width;
		public int height;
		public int storedX;
		public int storedY;
	}
}

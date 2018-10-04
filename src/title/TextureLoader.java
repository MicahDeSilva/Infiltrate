package title;

import static org.lwjgl.opengl.GL11.*;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import javax.imageio.ImageIO;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL;

public class TextureLoader
{
	public static Texture getTextureFromFile(String path, boolean repeat, boolean invert, boolean smooth)
	{
		return getTextureFromImageTexture(loadTextureFromImageFile(path), repeat, invert, smooth);
	}
	
	public static ImageTexture loadTextureFromImage(BufferedImage image)
	{
		int width = image.getWidth();
		int height = image.getHeight();

		int sourceImageBytes[] = new int[width * height];
		image.getRGB(0, 0, width, height, sourceImageBytes, 0, width);

		return new ImageTexture(sourceImageBytes, width, height);
	}
	
	public static Texture getTextureFromImageTexture(ImageTexture image, boolean repeat, boolean invert, boolean smooth)
	{
		int sourcePixel, sourcePixelIndex, targetPixelIndex, targetByteIndex;
		byte targetImageBytes[] = new byte[image.width * image.height * 4];

		for (int rowIndex = 0; rowIndex < image.height; ++rowIndex)
		{
			if (invert)
				sourcePixelIndex = (image.height - 1 - rowIndex) * image.width;
			else
				sourcePixelIndex = rowIndex * image.width;

			targetPixelIndex = (rowIndex * image.width);

			for (int columnIndex = 0; columnIndex < image.width; ++columnIndex)
			{
				targetByteIndex = 4 * targetPixelIndex;

				sourcePixel = image.image[sourcePixelIndex];

				byte blue = (byte) sourcePixel;
				byte green = (byte) (sourcePixel >> 8);
				byte red = (byte) (sourcePixel >> 16);
				byte alpha = (byte) (sourcePixel >> 24);

				targetImageBytes[targetByteIndex + 0] = red;
				targetImageBytes[targetByteIndex + 1] = green;
				targetImageBytes[targetByteIndex + 2] = blue;
				targetImageBytes[targetByteIndex + 3] = alpha;

				++sourcePixelIndex;
				++targetPixelIndex;
			}
		}
		
		ByteBuffer imageData = ByteBuffer.allocateDirect(image.width * image.height * 4).order(ByteOrder.nativeOrder());
		imageData.clear();
		imageData.put(targetImageBytes);
		imageData.position(0).limit(targetImageBytes.length);
		int texture = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, texture);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, image.width, image.height, 0, GL_RGBA, GL_UNSIGNED_BYTE, imageData);

		glTexEnvf(GL_TEXTURE_2D, GL_TEXTURE_ENV_MODE, GL_MODULATE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, smooth ? GL_LINEAR : GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, smooth ? GL_LINEAR : GL_NEAREST);
		if (repeat)
		{
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
		}
		else
		{
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP);
		}

		Texture result = new Texture(texture, image.width, image.height);
		result.unbind();

		return result;
	}

	public static ImageTexture loadTextureFromImageFile(String path) throws TextureLoadException
	{
		BufferedImage image = null;

		try
		{
			InputStream input = new FileInputStream(new File(path));
			image = ImageIO.read(input);
		}
		catch (Exception e)
		{
			throw new TextureLoadException("Failed to load image \"" + path + "\"");
		}

		return loadTextureFromImage(image);
	}

	public static class TextureLoadException extends NonFatalErrorException
	{
		private static final long serialVersionUID = 1L;

		public TextureLoadException(String message)
		{
			super(message);
		}
	}
}

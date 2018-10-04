package title;

import static org.lwjgl.opengl.GL11.*;

public class Texture
{
	public final int handle;
	public final int width;
	public final int height;

	public Texture(int handle, int width, int height)
	{
		this.handle = handle;
		this.width = width;
		this.height = height;
	}

	public void bind()
	{
		glBindTexture(GL_TEXTURE_2D, handle);
	}
	
	public void unbind()
	{
		glBindTexture(GL_TEXTURE_2D, 0);
	}
}

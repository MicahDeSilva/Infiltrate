package title;

import java.io.File;
import java.util.HashMap;

public class TextureManager
{
	private static HashMap<String, Texture> textures = new HashMap<String, Texture>();
	
	private TextureManager()
	{
	}
	
	public static Texture loadTexture(String path)
	{
		if (textures.containsKey(path))
			return textures.get(path);
		
		Texture texture = TextureLoader.getTextureFromFile(path, false, false, false);
		textures.put(path, texture);
		return texture;
	}
	
	public static Texture loadTexture(File file)
	{
		return loadTexture(file.getAbsolutePath());
	}
}

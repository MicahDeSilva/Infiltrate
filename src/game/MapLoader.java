package game;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

import main.WindowManager;

public class MapLoader
{
	Main main;
	int x = 0, y = 0;
	int fileLines = 54;

	public MapLoader(Main main, String aFileName)
	{
		fFilePath = Paths.get(aFileName);
		this.main = main;
	}

	public final void processLineByLine() throws IOException
	{
		try (Scanner scanner = new Scanner(fFilePath, ENCODING.name()))
		{
			while (scanner.hasNextLine())
			{
				processLine(scanner.nextLine());
			}
		}
	}

	protected void processLine(String aLine)
	{
		Main.mapWidth = (aLine.length() - 1) * Algorithms.tileResolution * 2;
		Main.mapHeight = fileLines * Algorithms.tileResolution * 2;
		CameraManager.maxCameraX = Main.mapWidth - WindowManager.width;
		CameraManager.maxCameraY = (Main.mapHeight / 2) - WindowManager.height;

		for (char c : aLine.toCharArray())
		{
			if (c == "1".charAt(0))
			{
				main.blocks.add(new Block(x * Algorithms.tileResolution, y * Algorithms.tileResolution));
				main.blocks.add(new Block(Main.mapWidth - x * Algorithms.tileResolution, y * Algorithms.tileResolution));
				main.blocks.add(new Block(x * Algorithms.tileResolution, -y * Algorithms.tileResolution));
				main.blocks.add(new Block(Main.mapWidth - x * Algorithms.tileResolution, -y * Algorithms.tileResolution));
			}
			
			if (c == "2".charAt(0))
			{
				main.uncollidableBlocks.add(new Block(x * Algorithms.tileResolution, y * Algorithms.tileResolution));
				main.uncollidableBlocks.add(new Block(Main.mapWidth - x * Algorithms.tileResolution, y * Algorithms.tileResolution));
				main.uncollidableBlocks.add(new Block(x * Algorithms.tileResolution, -y * Algorithms.tileResolution));
				main.uncollidableBlocks.add(new Block(Main.mapWidth - x * Algorithms.tileResolution, -y * Algorithms.tileResolution));
			}
			x += 1;
		}

		y += 1;
		Main.mapHeight = y * Algorithms.tileResolution;
		x = 0;
	}

	// Private vars
	private final Path fFilePath;
	private final static Charset ENCODING = StandardCharsets.UTF_8;
}

package game;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.glColor3d;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import main.WindowManager;

public class Main
{
	static int unitCount = 40;
	public Unit[] friendly = new Unit[unitCount];
	public Unit[] enemy = new Unit[unitCount];
	public ArrayList<Block> blocks = new ArrayList<Block>();
	ArrayList<Block> blockBin = new ArrayList<Block>();
	public ArrayList<Block> uncollidableBlocks = new ArrayList<Block>();
	CollisionManager collManager;
	public Bullet[] bulletsFriendly = new Bullet[unitCount * 2];
	public Bullet[] bulletsEnemy = new Bullet[unitCount * 2];
	static final int bulletLifeTimeMillis = 500;

	public static ServerManager serverManager;
	public static ClientManager clientManager;
	public static Connection connectionType;

	static enum Connection
	{
		SERVER, CLIENT, NONE
	}

	public static enum Team
	{
		ALLY, ENEMY, NEUTRAL
	}

	public static int mapWidth = (int) WindowManager.width, mapHeight = (int) WindowManager.height;

	public Main() throws IOException
	{
		MapLoader mapLoader = new MapLoader(this, "data/Map.txt");
		mapLoader.processLineByLine();
		collManager = new CollisionManager(this);
		for (int i = 0; i < friendly.length; i++)
			friendly[i] = new Unit(this, i, Team.ALLY);

		for (int i = 0; i < bulletsFriendly.length; i++)
			bulletsFriendly[i] = new Bullet(0, 0, 0, Team.ALLY, this, i);

		for (int i = 0; i < enemy.length; i++)
			enemy[i] = new Unit(this, i, Team.ENEMY);

		for (int i = 0; i < bulletsEnemy.length; i++)
			bulletsEnemy[i] = new Bullet(0, 0, 0, Team.ENEMY, this, i);

		connect();
	}

	public void update()
	{
		// Temporary test => add a new block when B is pressed
		if (WindowManager.getKeyState(GLFW_KEY_B) == GLFW_PRESS)
		{
			blocks.add(new Block(WindowManager.getCursorPosition()[0], WindowManager.getCursorPosition()[1]));
		}
		networking();

		while (blockBin.size() > 0)
		{
			blocks.remove(blockBin.get(0));
			blockBin.remove(0);
		}
	}

	private void networking()
	{
		if (connectionType == Connection.CLIENT)
		{
			try
			{
				ClientManager.sendData(DataPacket.dataPacket);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		else if (connectionType == Connection.SERVER)
		{
			try
			{
				ServerManager.sendData(DataPacket.dataPacket);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	void registerBullet(double x, double y, double direction)
	{
		for (Bullet bullet : bulletsFriendly)
		{
			if (!bullet.instantiated)
			{
				bullet.instantiated = true;
				bullet.x = x;
				bullet.y = y;
				bullet.direction = direction;
				bullet.timeInstantiated = System.currentTimeMillis();
				break;
			}
		}
	}

	public static void getTeamColour(Team team)
	{
		if (team == Team.ALLY)
		{
			glColor3d(0, 199.0 / 255, 117.0 / 255);
			return;
		}
		glColor3d(230.0 / 255, 50.0 / 255, 125.0 / 255);
	}

	private void connect()
	{
		try
		{
			connectionType = Connection.CLIENT;
			connectionType = Connection.SERVER;
			if (connectionType == Connection.SERVER)
			{
				serverManager = new ServerManager(25501);
			}
			else if (connectionType == Connection.CLIENT)
			{
				clientManager = new ClientManager("localhost", 25501);
			}

			DataPacket datapacket = new DataPacket();
			datapacket.startReceivingData();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

}

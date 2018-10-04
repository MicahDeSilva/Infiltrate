package game;

import static org.lwjgl.opengl.GL11.*;

import game.Main.Team;

public class Bullet
{
	double x, y, direction, velocity = 40;
	static double bulletSize = 3;
	boolean instantiated = false;
	long timeInstantiated = 0;
	public final int id;
	Team team;
	Main main;

	public Bullet(double x, double y, double direction, Team team, Main main, int id)
	{
		this.x = x;
		this.y = y;
		this.direction = direction;
		this.team = team;
		this.main = main;
		this.id = id;
	}

	public void render(double cameraX, double cameraY)
	{
		if (instantiated)
		{
			Main.getTeamColour(team);

			double modifiedX = x - cameraX;
			double modifiedY = y - cameraY;

			glBegin(GL_QUADS);
			glVertex2d(modifiedX - bulletSize, modifiedY - bulletSize);
			glVertex2d(modifiedX + bulletSize, modifiedY - bulletSize);
			glVertex2d(modifiedX + bulletSize, modifiedY + bulletSize);
			glVertex2d(modifiedX - bulletSize, modifiedY + bulletSize);
			glEnd();
		}
	}

	public void update()
	{
		if (team == Team.ENEMY)
		{
			instantiated = DataPacket.receivedPacket[Main.unitCount * DataPacket.dataPointsPerUnit + id * 4 + 3] > 1.0f ? false : true;
		}

		if (instantiated)
		{
			if (team == Team.ALLY)
			{
				x += velocity * Math.sin(Math.toRadians(direction));
				y += velocity * Math.cos(Math.toRadians(direction));

				if (x < 0 || y < -Main.mapHeight || x > Main.mapWidth || y > Main.mapHeight)
					instantiated = false;

				if (System.currentTimeMillis() - timeInstantiated > Main.bulletLifeTimeMillis)
					instantiated = false;

				for (Block block : main.blocks)
				{
					if (Algorithms.distanceBetween(x, y, block.x, block.y) < velocity)
					{
						instantiated = false;
						// game.blockBin.add(block); Makes bullets destroy blocks
					}
				}
			}
			else
			{
				x = Main.mapWidth - DataPacket.receivedPacket[Main.unitCount * DataPacket.dataPointsPerUnit + id * 4];
				y = -DataPacket.receivedPacket[Main.unitCount * DataPacket.dataPointsPerUnit + id * 4 + 1];
				direction = -DataPacket.receivedPacket[Main.unitCount * DataPacket.dataPointsPerUnit + id * 4 + 2];
			}

		}
		else
		{
			x = 0;
			y = 0;
			direction = 0;
		}

		if (team == Team.ALLY)
			DataPacket.add(this);
	}

}

package title;

import static org.lwjgl.opengl.GL11.*;

import java.util.Random;

import game.Algorithms;
import main.WindowManager;

/** The basic unit class. Holds its X, Y, Direction and Mode. Units can move, shoot and perform many other kinds of actions **/
public class TitleUnit
{
	public double x, y, direction;
	double targetDirection;
	double turnSpeed = 10;
	double xTarget;
	double yTarget;
	double attackTargetX;
	double attackTargetY;
	private boolean selected = false, highlighted = false;
	Mode mode = Mode.MOVE;
	static double unitSize = 50;
	boolean alive = true;
	Title title;
	public boolean enabled = true;

	// BOIDS
	private double[] velocity =
	{
			0, 0
	};

	double maxVelocity = 10;
	boolean goalSet = false;

	double currentRandomMoveX = 0, currentRandomMoveY = 0;

	long timeUpdated = System.currentTimeMillis();
	static Random rand = new Random();

	enum Mode
	{
		MOVE, ATTACK, HOLD, PATHING
	}

	TitleUnit(Title title, double x, double y)
	{
		this.title = title;
		Random rand = new Random();
		this.x = x;
		this.y = y;
		direction = rand.nextInt(360);
		targetDirection = direction;
		xTarget = x;
		yTarget = y;
	}

	/** Called every frame by the render thread, performs actions like movement or attacking **/
	public void update()
	{
		flocking();
		destroy();
	}

	private void destroy()
	{
		if (x < 0 || y < 0 || x > WindowManager.width || y > WindowManager.height)
			enabled = false;
	}

	private void flocking()
	{
		double[] v1, v2, v3, v4;

		v1 = cohesion();
		v2 = separation();
		v3 = alignment();

		velocity[0] += v1[0] / 8;
		velocity[1] += v1[1] / 8;

		velocity[0] += v2[0] / 20;
		velocity[1] += v2[1] / 20;

		velocity[0] += v3[0] / 40;
		velocity[1] += v3[1] / 40;

		if (goalSet)
		{
			v4 = goalSeeking();
			velocity[0] += v4[0];
			velocity[1] += v4[1];
		}

		double netVelocity = Algorithms.distanceBetween(0, 0, velocity[0], velocity[1]);
		if (netVelocity > maxVelocity)
		{
			velocity[0] *= maxVelocity / netVelocity;
			velocity[1] *= maxVelocity / netVelocity;
		}

		if (System.currentTimeMillis() - timeUpdated > 50)
		{
			currentRandomMoveX = 0.5 - rand.nextDouble() * 1;
			currentRandomMoveY = 0.5 - rand.nextDouble() * 1;
			timeUpdated = System.currentTimeMillis();
		}

		velocity[0] += currentRandomMoveX;
		velocity[1] += currentRandomMoveY;

		/*velocity[0] += 0.1;
		velocity[1] += 0.05;*/

		x += velocity[0] * WindowManager.deltaTime * WindowManager.globalGameSpeedModifier;
		y += velocity[1] * WindowManager.deltaTime * WindowManager.globalGameSpeedModifier;

		targetDirection = 90 - Math.toDegrees(Math.atan2(velocity[1], velocity[0]));

		turnTowards(targetDirection);
	}

	private void turnTowards(double targetDirection2)
	{
		if (Math.abs(targetDirection - direction) > turnSpeed)
		{
			if (Math.abs(targetDirection - direction) > Math.abs(targetDirection - direction - 360))
				direction += 360;
			if (Math.abs(targetDirection - direction) > Math.abs(targetDirection - direction + 360))
				direction -= 360;
			if (targetDirection > direction)
				direction += turnSpeed;
			else
				direction -= turnSpeed;
		}
		else
			direction = targetDirection;
	}

	private double[] goalSeeking()
	{
		double[] goalSeek =
		{
				(xTarget - x) / 200, (yTarget - y) / 200
		};
		return goalSeek;
	}

	private double[] alignment()
	{
		double[] alignment =
		{
				0, 0
		};

		int consideredUnits = 0;
		for (TitleUnit unit : title.units)
		{
			if (Algorithms.distanceBetween(x, y, unit.x, unit.y) < 500)
			{
				consideredUnits++;
				alignment[0] += unit.velocity[0];
				alignment[1] += unit.velocity[1];
			}
		}

		alignment[0] /= consideredUnits;
		alignment[1] /= consideredUnits;

		return alignment;
	}

	private double[] separation()
	{
		double[] seperation =
		{
				0, 0
		};

		for (TitleUnit unit : title.units)
		{
			if (unit != this && unit.enabled)
			{
				double dist = Algorithms.distanceBetween(x, y, unit.x, unit.y);
				if (dist < unitSize)
				{
					seperation[0] += (seperation[0] - (unit.x - x)) * 0.1f;
					seperation[1] += (seperation[1] - (unit.y - y)) * 0.1f;
				}
			}
		}

		double[] cursor = WindowManager.getCursorPosition();
		double dist = Algorithms.distanceBetween(x, y, cursor[0], cursor[1]);
		if (dist < 200)
		{
			seperation[0] += (seperation[0] - (cursor[0] - x));
			seperation[1] += (seperation[1] - (cursor[1] - y));
		}

		return seperation;

	}

	private double[] cohesion()
	{
		double[] averagePosition =
		{
				0, 0
		};

		int consideredUnits = 0;
		for (TitleUnit unit : title.units)
		{
			if (Algorithms.distanceBetween(x, y, unit.x, unit.y) < 500)
			{
				consideredUnits++;
				averagePosition[0] += unit.x;
				averagePosition[1] += unit.y;
			}
		}

		averagePosition[0] /= consideredUnits;
		averagePosition[1] /= consideredUnits;

		double[] cohesion =
		{
				(averagePosition[0] - x) / 100, (averagePosition[1] - y) / 100
		};

		return cohesion;
	}

	/** Renders a friendly unit, including modifying colour if unit is highlighted or selected **/
	public void renderFriendly(double cameraX, double cameraY)
	{
		if (alive)
		{
			glBegin(GL_TRIANGLES);
			if (highlighted)
				glColor3d(0, 1 / 1.2d, 0.7 / 1.2d);
			if (selected)
				glColor3d(0, 1, 0.7);

			double modifiedX = x - cameraX;
			double modifiedY = y - cameraY;

			glVertex2d(modifiedX + 22.4 * Math.sin(Math.toRadians(direction + 150)), modifiedY + 22.4 * Math.cos(Math.toRadians(direction + 150)));
			glVertex2d(modifiedX + 22.4 * Math.sin(Math.toRadians(direction - 150)), modifiedY + 22.4 * Math.cos(Math.toRadians(direction - 150)));
			glVertex2d(modifiedX + 30 * Math.sin(Math.toRadians(direction)), modifiedY + 30 * Math.cos(Math.toRadians(direction)));
			glEnd();
		}
	}

	void select()
	{
		selected = true;
	}

	boolean getSelected()
	{
		return selected;
	}

	void deselect()
	{
		selected = false;
	}

	void setHighlighted()
	{
		highlighted = true;
	}

	void clearHighlighted()
	{
		highlighted = false;
	}
}

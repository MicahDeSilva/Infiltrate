package game;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

import java.util.ArrayList;
import java.util.Random;

import game.Main.Team;
import main.WindowManager;

/** The basic unit class. Holds its X, Y, Direction and Mode. Units can move, shoot and perform many other kinds of actions **/
public class Unit
{
	public double x, y, direction;
	double targetDirection;
	double turnSpeed = 10;
	double xTarget;
	double yTarget;
	double attackTargetX;
	double attackTargetY;
	double shootDirection;
	private boolean selected = false, highlighted = false;
	Mode mode = Mode.MOVE;
	static double unitSize = 50;
	boolean alive = true;

	private ArrayList<int[]> path;
	private int currentPathNode = 0;
	int pathStatus = 0;
	final int id;
	final Main.Team team;
	boolean collided = false;
	private long lastPathCheck = System.currentTimeMillis();
	private Main main;

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

	Unit(Main main, int id, Main.Team team)
	{
		this.main = main;
		this.team = team;
		Random rand = new Random();
		x = 6528 / 2;
		y = - 1460;
		direction = rand.nextInt(360);
		targetDirection = direction;
		xTarget = x;
		yTarget = y;
		this.id = id;
	}

	/** Called every frame by the render thread, performs actions like movement or attacking **/
	public void update()
	{
		if (selected || (team == Team.ALLY && WindowManager.getKeyState(GLFW_KEY_LEFT_CONTROL) == GLFW_PRESS))
		{
			if (WindowManager.getMouseButtonState(GLFW_MOUSE_BUTTON_RIGHT) == GLFW_PRESS)
			{
				// A move command has been issued, find the target location
				double[] cursor = WindowManager.getCursorPosition();
				xTarget = cursor[0];
				yTarget = cursor[1];
				goalSet = true;
			}
		}

		updateMode();

		if (team == Team.ALLY)
		{
			if (alive)
			{
				flocking();

				checkIfShot();
			}

			DataPacket.add(this);
		}
		else
		{
			alive = DataPacket.receivedPacket[id * DataPacket.dataPointsPerUnit + 3] > 1.0f ? false : true;

			if (alive)
			{
				x = Main.mapWidth - DataPacket.receivedPacket[id * DataPacket.dataPointsPerUnit];
				y = -DataPacket.receivedPacket[id * DataPacket.dataPointsPerUnit + 1];
				direction = DataPacket.receivedPacket[id * DataPacket.dataPointsPerUnit + 2] + 180;
			}
			else
			{
				x = 0;
				y = 0;
				direction = 0;
			}
		}

		if (mode == Mode.ATTACK)
		{
			if (rand.nextInt(50) > 48)
				shoot(shootDirection);//attackTargetX, attackTargetY, 100);
		}
	}

	private void checkIfShot()
	{
		for (Bullet bullet : main.bulletsEnemy)
		{
			if (Algorithms.distanceBetween(x, y, bullet.x, bullet.y) < unitSize)
				alive = false;
		}
	}

	private void updateMode()
	{
		if (selected || (team == Team.ALLY && WindowManager.getKeyState(GLFW_KEY_LEFT_CONTROL) == GLFW_PRESS))
		{
			if (WindowManager.getKeyState(GLFW_KEY_A) == GLFW_PRESS)
			{
				mode = Mode.ATTACK;
				double[] cursor = WindowManager.getCursorPosition();
				attackTargetX = cursor[0];
				attackTargetY = cursor[1];
				shootDirection = 90 - Math.toDegrees(Math.atan2(attackTargetY - y, attackTargetX - x));
			}

			if (WindowManager.getKeyState(GLFW_KEY_S) == GLFW_PRESS)
			{
				mode = Mode.MOVE;
			}
		}
	}

	private void shoot()
	{
		main.registerBullet(x, y, direction);
	}
	
	private void shoot(double direction)
	{
		main.registerBullet(x, y, direction);
	}

	private void shoot(double aimX, double aimY)
	{
		main.registerBullet(x, y, 90 - Math.toDegrees(Math.atan2(aimY - y, aimX - x)));
	}

	/**
	 * @param inaccuracy
	 *            The random offset that will be added onto the shots aim (in pixels). If inaccuracy is 0, shots will be perfectly accurate and on target.
	 **/
	private void shoot(double aimX, double aimY, double inaccuracy)
	{
		main.registerBullet(x, y, 90 - Math.toDegrees(Math.atan2(aimY + (inaccuracy * (0.5 - rand.nextDouble())) - y, aimX + (inaccuracy * (0.5 - rand.nextDouble())) - x)));
	}

	private void flocking()
	{
		double[] v1, v2, v3, v4;

		v1 = cohesion();
		v2 = separation();
		v3 = alignment();

		velocity[0] += v1[0] / 10;
		velocity[1] += v1[1] / 10;

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
			currentRandomMoveX = 1 - rand.nextDouble() * 2;
			currentRandomMoveY = 1 - rand.nextDouble() * 2;
			timeUpdated = System.currentTimeMillis();
		}

		velocity[0] += currentRandomMoveX;
		velocity[1] += currentRandomMoveY;

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
		for (Unit unit : main.friendly)
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

		for (Unit unit : main.friendly)
		{
			if (unit != this)
			{
				double dist = Algorithms.distanceBetween(x, y, unit.x, unit.y);
				if (dist < unitSize)
				{
					seperation[0] += (seperation[0] - (unit.x - x)) * 0.1f;
					seperation[1] += (seperation[1] - (unit.y - y)) * 0.1f;
				}
			}
		}

		for (Block block : main.blocks)
		{
			double dist = Algorithms.distanceBetween(x, y, block.x, block.y);
			if (dist < Algorithms.tileResolution + 10)
			{
				seperation[0] += 10 * (seperation[0] - (block.x - x));
				seperation[1] += 10 * (seperation[1] - (block.y - y));
				if (dist < Algorithms.tileResolution)
				{
					double direction2 = 90 - Math.toDegrees(Math.atan2(block.y - y, block.x - x));
					x -= maxVelocity * Math.sin(Math.toRadians(direction2));
					y -= maxVelocity * Math.cos(Math.toRadians(direction2));
				}
			}
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
		for (Unit unit : main.friendly)
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

	/*
	 * private void doPathFinding() { for (Unit unit : game.friendly) { if (unit != this) { if (Algorithms.distanceBetween(x, y, unit.x, unit.y) < Algorithms.tileResolution) { double direction2 = 90 - Math.toDegrees(Math.atan2(unit.y - y,
	 * unit.x - x)); x -= 2 * Math.sin(Math.toRadians(direction2)); y -= 2 * Math.cos(Math.toRadians(direction2));
	 * 
	 * if (mode == Mode.MOVE) { path = Algorithms.pathFind(x, y, xTarget, yTarget); lastPathCheck = System.currentTimeMillis(); if (path != null) { if (path.size() > 2) currentPathNode = path.size() - 2; else currentPathNode = path.size() -
	 * 1; } } } } }
	 * 
	 * for (Block block : game.blocks) { if (Algorithms.distanceBetween(x, y, block.x, block.y) < Algorithms.tileResolution) { double direction2 = 90 - Math.toDegrees(Math.atan2(block.y - y, block.x - x)); x -= 2 *
	 * Math.sin(Math.toRadians(direction2)); y -= 2 * Math.cos(Math.toRadians(direction2)); } }
	 * 
	 * if (selected) { if (WindowManager.getMouseButtonState(GLFW_MOUSE_BUTTON_RIGHT) == GLFW_PRESS) { // A move command has been issued, find the target location double[] cursor = WindowManager.getCursorPosition(); xTarget = cursor[0];
	 * yTarget = cursor[1];
	 * 
	 * /* if (Algorithms.status[id] == 0) { Algorithms.pathFindNewThread(x, y, xTarget, yTarget, id); mode = Mode.PATHING; }
	 */

	// Find the shortest path to the target
	/*
	 * path = Algorithms.pathFind(x, y, xTarget, yTarget); lastPathCheck = System.currentTimeMillis(); // If a path was found, move! if (path != null) { mode = Mode.MOVE;
	 * 
	 * if (path.size() > 2) currentPathNode = path.size() - 2; else currentPathNode = path.size() - 1; } } }
	 * 
	 * if (mode == Mode.PATHING) { if (Algorithms.status[id] == 1) { Algorithms.status[id] = 0; mode = Mode.MOVE; // path = Algorithms.path[id]; currentPathNode = path.size() - 1; } }
	 * 
	 * if (mode == Mode.MOVE) {
	 * 
	 * if (System.currentTimeMillis() - lastPathCheck > 500) { path = Algorithms.pathFind(x, y, xTarget, yTarget); lastPathCheck = System.currentTimeMillis(); // If a path was found, move! if (path != null) { mode = Mode.MOVE;
	 * 
	 * if (path.size() > 2) currentPathNode = path.size() - 2; else currentPathNode = path.size() - 1; } }
	 * 
	 * // Make sure path still exists if (path != null) { if (collided && path.size() > currentPathNode + 2) { currentPathNode += 2; }
	 * 
	 * if (path.size() - 1 > currentPathNode) { if (path.get(currentPathNode) != null) { // Move towards next point on the path if (Algorithms.distanceBetween(x, y, path.get(currentPathNode)[0], path.get(currentPathNode)[1]) > 2) {
	 * direction = 90 - Math.toDegrees(Math.atan2(path.get(currentPathNode)[1] - y, path.get(currentPathNode)[0] - x)); x += 2 * Math.sin(Math.toRadians(direction)); y += 2 * Math.cos(Math.toRadians(direction)); } else { // If we have
	 * reached the current point on the path, move onto the next one. x = path.get(currentPathNode)[0]; y = path.get(currentPathNode)[1]; if (currentPathNode > 0) currentPathNode--; else { // direction = 0; mode = Mode.HOLD; } } } else { //
	 * mode = Mode.HOLD; // direction = 0; // x = Algorithms.snapToGrid(x) * Algorithms.tileResolution; // y = Algorithms.snapToGrid(y) * Algorithms.tileResolution; } } } }
	 * 
	 * }
	 */

	/** Renders a friendly unit, including modifying colour if unit is highlighted or selected **/
	public void renderFriendly(double cameraX, double cameraY)
	{
		if (alive)
		{
			glBegin(GL_LINE_LOOP);
			if (highlighted)
				glColor3d(0, 1, 0.7);
			if (selected)
				glColor3d(1, 1, 1);

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

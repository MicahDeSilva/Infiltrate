package game;

import java.util.ArrayList;

public class Algorithms
{
	static int tileResolution = 32;
	static int maxUnitCount = 100;
	static GridSquare[][] gridSquares = new GridSquare[(int) Math.ceil(Main.mapWidth / tileResolution) + 1][];
	static boolean[][] solid = new boolean[(int) Math.ceil(Main.mapWidth / tileResolution) + 1][];
	static int status[] = new int[maxUnitCount]; // 0 = Ready, 1 = Done, 2 = Processing
	static double[][] pathFindingCoords = new double[maxUnitCount][];
	static int[][][] path;

	public static void clearSolids()
	{
		for (int i = 0; i < solid.length; i++)
		{
			solid[i] = new boolean[(int) Math.ceil(Main.mapHeight / tileResolution) + 1];
			for (int j = 0; j < solid[i].length; j++)
			{
				solid[i][j] = false;
			}
		}
	}

	/** Used to clear the gridSquares used in the previous path find before a new path find begins **/
	public static void clearGrid()
	{
		for (int i = 0; i < gridSquares.length; i++)
		{
			gridSquares[i] = new GridSquare[(int) Math.ceil(Main.mapHeight / tileResolution) + 1];
		}
	}

	/**
	 * Uses Pythagoras' theorem to find the distance between 2 points (2d)
	 * 
	 * @return The distance between the 2 points as a double.
	 **/
	public static double distanceBetween(double x1, double y1, double x2, double y2)
	{
		return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
	}

	public static void pathFindNewThread(double x1, double y1, double x2, double y2, int id)
	{
		pathFindingCoords[id] = new double[4];
		pathFindingCoords[id][0] = x1;
		pathFindingCoords[id][1] = y1;
		pathFindingCoords[id][2] = x2;
		pathFindingCoords[id][3] = y2;

		Thread t = new Thread(new Runnable()
		{
			public void run()
			{
				status[id] = 2;
				// path[id] = pathFind(pathFindingCoords[id][0], pathFindingCoords[id][1], pathFindingCoords[id][2], pathFindingCoords[id][3]);
				status[id] = 1;
			}
		});

		t.start();

	}

	/**
	 * Uses the A* pathfinding algorithm to calculate the shortest route from (x1, y1) to (x2, y2)
	 * 
	 * @param x1
	 *            The start position x
	 * @param y1
	 *            The start position y
	 * @param x2
	 *            The target x position
	 * @param y2
	 *            The target y position
	 * @return The list of tiles that the unit should pass through to reach its destination, stored in an integer array: path[moveNumber(0,1,2,...n)][coordinate(x,y)]
	 **/
	public static ArrayList<int[]> pathFind(double x1, double y1, double x2, double y2)
	{
		clearGrid();

		// Convert coordinates to integers for use in our reduced / abstract grid
		int x1int = snapToGrid(x1);
		int y1int = snapToGrid(y1);

		int x2int = snapToGrid(x2);
		int y2int = snapToGrid(y2);

		// Record the existence of these two tiles in our grid of known tiles
		gridSquares[x1int][y1int] = new GridSquare(x1int, y1int, 0, null);
		gridSquares[x2int][y2int] = new GridSquare(x2int, y2int, Integer.MAX_VALUE, null);

		// Record these tiles as they will be used later
		GridSquare startSquare = gridSquares[x1int][y1int];
		GridSquare endSquare = gridSquares[x2int][y2int];

		// The openList stores a list of all possible tiles that have yet to be considered (passable locations)
		ArrayList<GridSquare> openList = new ArrayList<GridSquare>();

		// The closedList stores the tiles that have already been considered (NOT the ones that have been rejected)
		ArrayList<GridSquare> closedList = new ArrayList<GridSquare>();

		openList.add(startSquare);

		// Execute until either a path is found, or there are no more possible squares to move to (the openList is empty)
		while (!openList.isEmpty())
		{
			// A lower F score suggests the tile is more likely to be on the shortest path, so we consider the tiles with lower FScores first
			GridSquare currentSquare = getLowestFScore(openList, startSquare, endSquare);

			// Now that this square has been considered, we remove it from the openList and add it to the closedList
			closedList.add(currentSquare);
			openList.remove(currentSquare);

			if (closedList.contains(endSquare))
			{
				break;
			}

			// Add any unconsidered adjacentSquares to the openList
			openList = addAdjacentSquares(currentSquare, openList);
		}

		// If path has been found we recur back through the parents of each tile to find the path taken
		if (endSquare.gScore != Integer.MAX_VALUE)
		{
			ArrayList<int[]> path = new ArrayList<int[]>();
			
			GridSquare parent = endSquare;

			int[] endTileLoc =
				{
					endSquare.x * tileResolution, endSquare.y * tileResolution
				};
			path.add(endTileLoc);
			
			// Iterate through the path in reverse, finding the tiles used in reverse order based on their parents
			while (parent != startSquare)
			{
				int[] thisTile =
				{
						parent.x * tileResolution, parent.y * tileResolution
				};
				path.add(thisTile);
				parent = parent.parent;
			}

			return path;
		}

		// If no complete path was found we return the path which gets closest to the target
		endSquare = getLowestHScore(closedList, endSquare);

		ArrayList<int[]> path = new ArrayList<int[]>();

		GridSquare parent = endSquare.parent;

		// Iterate through the path in reverse, finding the tiles used in reverse order based on their parents
		while (parent != startSquare)
		{
			if (parent == null)
				break;
			int[] thisTile =
			{
					parent.x * tileResolution, parent.y * tileResolution
			};
			path.add(thisTile);
			parent = parent.parent;
		}

		return path;
	}

	/** Divides the value by the tile resolution of the path finding algorithm and rounds and converts it into an integer **/
	public static int snapToGrid(double x)
	{
		return (int) Math.round(x / tileResolution);
	}

	/**
	 * Adds all the possible tiles that surround the current tile to the openList. Does not add any tiles that are impassable, or that already exist in the closedList
	 * 
	 * @return the openList with new adjacent tiles added.
	 **/
	private static ArrayList<GridSquare> addAdjacentSquares(GridSquare currentSquare, ArrayList<GridSquare> openList)
	{
		openList = addSquare(openList, currentSquare.x + 1, currentSquare.y, currentSquare, 1);
		openList = addSquare(openList, currentSquare.x - 1, currentSquare.y, currentSquare, 1);
		openList = addSquare(openList, currentSquare.x, currentSquare.y + 1, currentSquare, 1);
		openList = addSquare(openList, currentSquare.x, currentSquare.y - 1, currentSquare, 1);

		openList = addSquare(openList, currentSquare.x + 1, currentSquare.y + 1, currentSquare, 1.4);
		openList = addSquare(openList, currentSquare.x + 1, currentSquare.y - 1, currentSquare, 1.4);
		openList = addSquare(openList, currentSquare.x - 1, currentSquare.y + 1, currentSquare, 1.4);
		openList = addSquare(openList, currentSquare.x - 1, currentSquare.y - 1, currentSquare, 1.4);

		return openList;
	}

	/**
	 * Adds a tile to the openList, but only does so if it hasn't been considered yet. Updates any tiles that have already been considered with a lower gScore if the gScore given is lower than its current value.
	 * 
	 * @return the openList with the tile either added or not.
	 **/
	private static ArrayList<GridSquare> addSquare(ArrayList<GridSquare> openList, int x, int y, GridSquare currentSquare, double moveCost)
	{
		if (x > gridSquares.length - 1 || x < 0 || y > gridSquares[0].length - 1 || y < 0)
			return openList;
		if (gridSquares[x][y] == null)
		{
			// Add check for solid here
			if (!solid[x][y])
			{
				GridSquare tile = new GridSquare(x, y, currentSquare.gScore + moveCost, currentSquare);
				gridSquares[x][y] = tile;
				openList.add(tile);
			}
		}
		else
		{
			if (gridSquares[x][y].getGScore() > currentSquare.gScore + moveCost)
			{
				gridSquares[x][y].setGScore(currentSquare.gScore + moveCost);
				gridSquares[x][y].setParent(currentSquare);
			}
		}
		return openList;
	}

	/**
	 * Used to find the next tile that should be considered for the A* pathfinding algorithm. Runs through the list of all tiles in the openList.
	 * 
	 * @return the tile with the lowest FScore.
	 **/
	private static GridSquare getLowestFScore(ArrayList<GridSquare> openList, GridSquare start, GridSquare dest)
	{
		GridSquare lowestFScoreTile = null;
		double lowestFScore = Double.MAX_VALUE;
		for (GridSquare tile : openList)
		{
			double fScore = tile.getFScore(start, dest);
			if (fScore < lowestFScore)
			{
				lowestFScore = fScore;
				lowestFScoreTile = tile;
			}
		}
		return lowestFScoreTile;
	}

	/**
	 * Used to find the closest tile to the destinationRuns through the list of all tiles in the list given.
	 * 
	 * @return the tile with the lowest FScore.
	 **/
	private static GridSquare getLowestHScore(ArrayList<GridSquare> list, GridSquare dest)
	{
		GridSquare lowestHScoreTile = null;
		double lowestHScore = Double.MAX_VALUE;
		for (GridSquare tile : list)
		{
			double hScore = tile.getHScore(dest);
			if (hScore < lowestHScore)
			{
				lowestHScore = hScore;
				lowestHScoreTile = tile;
			}
		}
		return lowestHScoreTile;
	}
}

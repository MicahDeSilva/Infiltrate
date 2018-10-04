package game;

public class GridSquare
{
	final int x, y;
	double gScore;
	GridSquare parent;

	GridSquare(int x, int y, double gScore, GridSquare parent)
	{
		this.x = x;
		this.y = y;
		if (parent != null)
		{
			this.parent = parent;
		}
		this.gScore = gScore;
	}

	/** @return The score for this square lower score = shorter path **/
	public double getFScore(GridSquare start, GridSquare dest)
	{
		return getGScore() + getHScore(dest);
	}

	/** @return The estimated distance from this square to the target/destination square **/
	public double getHScore(GridSquare targetSquare)
	{
		return Algorithms.distanceBetween(x, y, targetSquare.x, targetSquare.y);
	}

	/** @return The cost from the start square to this square **/
	public double getGScore()
	{
		return gScore;
	}

	public void setGScore(double gScore)
	{
		this.gScore = gScore;
	}

	public String toString()
	{
		return "X: " + x + ", Y: " + y + ", G Score: " + gScore + ".";
	}

	public void setParent(GridSquare parent)
	{
		this.parent = parent;
	}
}

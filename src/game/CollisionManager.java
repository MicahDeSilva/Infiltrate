package game;

public class CollisionManager
{
	public CollisionManager(Main main)
	{
		this.main = main;
	}

	Main main;

	void update()
	{
		for (Unit unit : main.friendly)
		{
			unit.collided = false;
			for (Unit unit2 : main.friendly)
			{
				if (Algorithms.distanceBetween(unit.x, unit.y, unit2.x, unit2.y) < 50)
				{
					//unit.collided = true;
					//unit2.collided = true;
					//System.out.println("Collided!");
				}
			}
		}
	}
}

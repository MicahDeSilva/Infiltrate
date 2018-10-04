package game;

public class Logger
{
	@SuppressWarnings("rawtypes")
	public static void log(String s, Class c)
	{
		System.out.println("[" + c.getName() + "] " + s);
	}
}

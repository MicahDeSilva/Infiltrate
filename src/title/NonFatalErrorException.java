package title;

public class NonFatalErrorException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public NonFatalErrorException(String message)
	{
		super(message);
	}
}

package various;


import one.microstream.X;

public class MainTestCheck
{
	public static void main(final String[] args)
	{
		// forced check with message (nonsensical condition for demonstraction purposes only)
		X.check(() -> System.currentTimeMillis() < 0, "System time must be negative.");
		
		// simple forced check (nonsensical condition for demonstraction purposes only)
		X.check(() -> System.currentTimeMillis() < 0);
	}
}

package one.microstream.arm;

public class MainTestThrowableArray
{
	
	public static void main(final String[] args)
	{
		final Throwable[] array = new Throwable[1];
		System.out.println(array);
		
		final TestThrowableArray instance = new TestThrowableArray(1);
		System.out.println(instance.problems);
	}
		
}


class TestThrowableArray
{
	final Throwable[] problems;

	public TestThrowableArray(final int size)
	{
		super();
		this.problems = new Throwable[size];
	}
	
}

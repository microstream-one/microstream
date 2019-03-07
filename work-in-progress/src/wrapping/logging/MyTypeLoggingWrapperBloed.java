package wrapping.logging;

import wrapping.MyType;

public class MyTypeLoggingWrapperBloed implements MyType
{
	private final MyType delegate;

	public MyTypeLoggingWrapperBloed(final MyType delegate)
	{
		super();
		this.delegate = delegate;
	}

	@Override
	public void methodA(final int value)
	{
		// bl�d
		this.delegate.methodA(value);
	}

	@Override
	public String methodB()
	{
		// bl�d
		return this.delegate.methodB();
	}

	@Override
	public String methodC(final long value, final String stuff)
	{
		System.out.println("Stuff: " + stuff);

		// okay
		return this.delegate.methodC(value, stuff);
	}

	// bl�d
	// bl�d
	// bl�d
	// bl�d
	// bl�d
	// bl�d
	// bl�d
	// bl�d
	// bl�d
	// bl�d
	// bl�d
	// bl�d
	// bl�d
	// bl�d
	// bl�d
	// bl�d
	// bl�d
	// bl�d
	// bl�d
	// bl�d
	// bl�d
	// bl�d
	// bl�d
	// bl�d

	@Override
	public int methodZ()
	{
		// bl�d
		return this.delegate.methodZ();
	}
	
}

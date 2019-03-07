package wrapping.logging;


import one.microstream.wrapping.Wrapper;
import wrapping.MyType;
import wrapping.generated.WrapperMyType;

public class MyTypeLoggingWrapperCool
extends Wrapper.AbstractImplementation<MyType> // optional
implements WrapperMyType
{
	public MyTypeLoggingWrapperCool(final MyType wrapped)
	{
		super(wrapped);
	}
	
	// cool

	@Override
	public String methodC(final long value, final String stuff)
	{
		System.out.println("Stuff: " + stuff);

		// okay
		return this.wrapped().methodC(value, stuff);
	}

	// cool
	
}

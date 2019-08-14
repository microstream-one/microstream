package wrapping.logging;


import wrapping.MyType;
import wrapping.Wrapper;
import wrapping.generated.WrapperMyType;

public class MyTypeLoggingWrapperCool
extends Wrapper.Abstract<MyType> // optional
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

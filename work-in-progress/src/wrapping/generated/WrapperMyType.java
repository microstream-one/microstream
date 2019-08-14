package wrapping.generated;
import wrapping.MyType;
import wrapping.Wrapper;


public interface WrapperMyType extends Wrapper<MyType>, MyType
{
	@Override
	public default void methodA(final int value)
	{
		this.wrapped().methodA(value);
	}
	
	@Override
	public default String methodB()
	{
		return this.wrapped().methodB();
	}
	
	@Override
	public default String methodC(final long value, final String stuff)
	{
		return this.wrapped().methodC(value, stuff);
	}
	
	// ...
	
	@Override
	public default int methodZ()
	{
		return this.wrapped().methodZ();
	}
	
}

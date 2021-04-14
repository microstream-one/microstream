package one.microstream.reference;

@FunctionalInterface
public interface _booleanReference
{
	public boolean get();
	
	
	
	public static _booleanReference True()
	{
		// Singleton is an anti-pattern.
		return new True();
	}
	
	public static _booleanReference False()
	{
		// Singleton is an anti-pattern.
		return new False();
	}
	
	public static _booleanReference New(final boolean value)
	{
		return new Default(value);
	}
		
	public final class Default implements _booleanReference
	{
		final boolean value;

		Default(final boolean value)
		{
			super();
			this.value = value;
		}

		@Override
		public final boolean get()
		{
			return this.value;
		}
		
	}
	
	public final class True implements _booleanReference
	{

		@Override
		public final boolean get()
		{
			return true;
		}
		
	}
	
	public final class False implements _booleanReference
	{

		@Override
		public final boolean get()
		{
			return false;
		}
		
	}
	
}

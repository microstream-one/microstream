package one.microstream.cache.types;

public interface CacheValueValidator
{
	public void validate(
		Object value
	);
	
	
	public static CacheValueValidator New(
		final String slot, 
		final Class<?> expectedType
	)
	{
		return expectedType == null || Object.class.equals(expectedType)
			? new Simple(slot)
			: new Typed(slot, expectedType)
		;
	}
	
	
	public static class Simple implements CacheValueValidator
	{
		final String slot;

		Simple(
			final String slot
		)
		{
			super();
			this.slot = slot;
		}
				
		@Override
		public void validate(
			final Object value
		)
		{
			if(value == null)
			{
				throw new NullPointerException(
					this.slot + " cannot be null"
				);
			}
		}
		
	}
	
	public static class Typed extends Simple
	{
		final Class<?> expectedType;

		Typed(
			final String slot, 
			final Class<?> expectedType
		)
		{
			super(slot);
			this.expectedType = expectedType;
		}
		
		@Override
		public void validate(
			final Object value
		)
		{
			super.validate(value); // null check
			
			if(!this.expectedType.isInstance(value))
			{
				throw new ClassCastException(
					"Type mismatch for " + this.slot + ": " + 
					value + " <> " + this.expectedType.getName()
				);
			}
		}
		
	}
	
}

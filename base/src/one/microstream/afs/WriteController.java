package one.microstream.afs;


@FunctionalInterface
public interface WriteController
{
	public default void validateIsWritable()
	{
		if(this.isWritable())
		{
			return;
		}
		
		// (07.08.2020 TM)EXCP: proper exception
		throw new RuntimeException("Writing is not enabled.");
	}
	
	public boolean isWritable();
	
	
	
	public static WriteController Enabled()
	{
		// Singleton is (usually) an anti pattern.
		return new WriteController.Enabled();
	}
	
	public final class Enabled implements WriteController
	{
		Enabled()
		{
			super();
		}
		
		@Override
		public final void validateIsWritable()
		{
			// no-op
		}

		@Override
		public final boolean isWritable()
		{
			return true;
		}
		
	}
	
	public static WriteController Disabled()
	{
		// Singleton is (usually) an anti pattern.
		return new WriteController.Disabled();
	}
	
	public final class Disabled implements WriteController
	{
		Disabled()
		{
			super();
		}

		@Override
		public final boolean isWritable()
		{
			return false;
		}
		
	}
	
}

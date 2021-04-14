package one.microstream.persistence.types;

import static one.microstream.X.notNull;

import one.microstream.afs.types.WriteController;
import one.microstream.persistence.exceptions.PersistenceException;


@FunctionalInterface
public interface PersistenceWriteController extends WriteController
{
	public default void validateIsStoringEnabled()
	{
		if(this.isStoringEnabled())
		{
			return;
		}

		throw new PersistenceException("Storing is not enabled.");
	}

	public default boolean isStoringEnabled()
	{
		return this.isWritable();
	}
	
	
	public static PersistenceWriteController Wrap(final WriteController writeController)
	{
		return new PersistenceWriteController.Wrapper(
			notNull(writeController)
		);
	}
	
	public final class Wrapper implements PersistenceWriteController
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final WriteController writeController;

		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Wrapper(final WriteController writeController)
		{
			super();
			this.writeController = writeController;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final void validateIsWritable()
		{
			this.writeController.validateIsWritable();
		}
		
		@Override
		public final boolean isWritable()
		{
			return this.writeController.isWritable();
		}
		
		@Override
		public final boolean isStoringEnabled()
		{
			return this.isWritable();
		}
				
	}
	
	public static PersistenceWriteController Enabled()
	{
		// Singleton is (usually) an anti pattern.
		return new PersistenceWriteController.Enabled();
	}
	
	public final class Enabled implements PersistenceWriteController
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
		public final void validateIsStoringEnabled()
		{
			// no-op
		}

		@Override
		public final boolean isWritable()
		{
			return true;
		}
		
		@Override
		public final boolean isStoringEnabled()
		{
			return true;
		}
		
	}
	
	public static PersistenceWriteController Disabled()
	{
		// Singleton is (usually) an anti pattern.
		return new PersistenceWriteController.Disabled();
	}
	
	public final class Disabled implements PersistenceWriteController
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
		
		@Override
		public final boolean isStoringEnabled()
		{
			return false;
		}
		
	}
	
}

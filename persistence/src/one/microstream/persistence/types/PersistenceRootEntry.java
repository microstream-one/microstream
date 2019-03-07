package one.microstream.persistence.types;

import static one.microstream.X.mayNull;
import static one.microstream.X.notNull;

import java.util.function.Supplier;

public interface PersistenceRootEntry
{
	public String identifier();
	
	public Object instance();
	
	public boolean isRemoved();
	
	
	
	public static PersistenceRootEntry New(final String identifier, final Supplier<?> instanceSupplier)
	{
		return new PersistenceRootEntry.Implementation(
			notNull(identifier)      ,
			mayNull(instanceSupplier) // null means deleted
		);
	}
	
	public final class Implementation implements PersistenceRootEntry
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final String      identifier      ;
		private final Supplier<?> instanceSupplier;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Implementation(final String identifier, final Supplier<?> instanceSupplier)
		{
			super();
			this.identifier       = identifier      ;
			this.instanceSupplier = instanceSupplier;
		}


		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final String identifier()
		{
			return this.identifier;
		}
		
		@Override
		public boolean isRemoved()
		{
			return this.instanceSupplier == null;
		}

		@Override
		public final Object instance()
		{
			return this.instanceSupplier != null
				? this.instanceSupplier.get()
				: null
			;
		}
		
	}
	
}

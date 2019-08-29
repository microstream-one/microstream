package one.microstream.persistence.types;

import static one.microstream.X.mayNull;
import static one.microstream.X.notNull;

import java.util.function.Supplier;

import one.microstream.chars.XChars;
import one.microstream.meta.XDebug;

public interface PersistenceRootEntry
{
	public String identifier();
	
	public Object instance();
	
	public boolean isRemoved();
	
	
	
	public static PersistenceRootEntry New(final String identifier, final Supplier<?> instanceSupplier)
	{
		return new PersistenceRootEntry.Default(
			notNull(identifier)      ,
			mayNull(instanceSupplier) // null means deleted
		);
	}
	
	public final class Default implements PersistenceRootEntry
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final String      identifier      ;
		private final Supplier<?> instanceSupplier;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default(final String identifier, final Supplier<?> instanceSupplier)
		{
			super();
			
			// (29.08.2019 TM)FIXME: /!\ DEBUG priv#23
			if(identifier.contains("1000031"))
			{
				XDebug.println("registering enum 1000031");
			}
			
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
//			XDebug.println("Calling supplier.get() from " + XChars.systemString(this));
			
			return this.instanceSupplier != null
				? this.instanceSupplier.get()
				: null
			;
		}
		
		@Override
		public String toString()
		{
			return this.identifier + ": " + XChars.systemString(this.instance());
		}
		
	}
	
	
	@FunctionalInterface
	public interface Provider
	{
		public PersistenceRootEntry provideRootEntry(String identifier, Supplier<?> instanceSupplier);
	}
	
}

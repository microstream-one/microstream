package one.microstream.persistence.types;

import static one.microstream.X.mayNull;

import java.util.function.Supplier;

public interface PersistenceRootReference extends Supplier<Object>
{
	@Override
	public Object get();
	
	public default Object setRoot(final Object newRoot)
	{
		return this.setRootSupplier(() ->
			newRoot
		);
	}
	
	public Object setRootSupplier(Supplier<?> rootSupplier);

	public <F extends PersistenceFunction> F iterate(F iterator);
	
	

	public static PersistenceRootReference New()
	{
		return New(null);
	}
	
	public static PersistenceRootReference New(final Object root)
	{
		final PersistenceRootReference.Default instance = new PersistenceRootReference.Default(null);
		instance.setRoot(root);
		
		return instance;
	}
	
	public static PersistenceRootReference New(final Supplier<?> rootSupplier)
	{
		return new PersistenceRootReference.Default(
			mayNull(rootSupplier)
		);
	}
	
	public final class Default implements PersistenceRootReference
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		// there is no problem that cannot be solved through one more level of indirection
		private Supplier<?> rootSupplier;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(final Supplier<?> rootSupplier)
		{
			super();
			this.rootSupplier = rootSupplier;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final Object get()
		{
			return this.rootSupplier != null
				? this.rootSupplier.get()
				: null
			;
		}
		
		@Override
		public final Object setRootSupplier(final Supplier<?> rootSupplier)
		{
			final Object currentRoot = this.get();
			this.rootSupplier = rootSupplier;
			
			return currentRoot;
		}
		
		@Override
		public final <F extends PersistenceFunction> F iterate(final F iterator)
		{
			final Object currentRoot = this.get();
			if(currentRoot == null)
			{
				return iterator;
			}
			iterator.apply(currentRoot);
			
			return iterator;
		}
		
	}
	
}

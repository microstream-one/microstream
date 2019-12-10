package one.microstream.persistence.types;

import static one.microstream.X.mayNull;

public interface PersistenceRootReference
{
	public Object getRoot();
	
	public Object setRoot(Object newRoot);
	
	
	
	public static PersistenceRootReference New(final Object root)
	{
		return new PersistenceRootReference.Default(
			mayNull(root)
		);
	}
	
	public final class Default implements PersistenceRootReference
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private Object root;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(final Object root)
		{
			super();
			this.root = root;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final Object getRoot()
		{
			return this.root;
		}
		
		@Override
		public final Object setRoot(final Object newRoot)
		{
			final Object currentRoot = this.root;
			this.root = newRoot;
			
			return currentRoot;
		}
		
		public final void iterate(final PersistenceFunction iterator)
		{
			if(this.root == null)
			{
				return;
			}
			iterator.apply(this.root);
		}
		
	}
	
}

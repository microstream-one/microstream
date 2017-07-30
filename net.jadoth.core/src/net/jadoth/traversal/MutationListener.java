package net.jadoth.traversal;

public interface MutationListener
{
	public void registerChange(Object parent, Object oldReference, Object newReference);
	
	
	public static MutationListener.Provider Provider(final MutationListener mutationListener)
	{
		return new Provider.Implementation(mutationListener);
	}
	
	public interface Provider
	{
		public MutationListener provideMutationListener(TraversalEnqueuer traversalEnqueuer);
		
		
		
		public final class Implementation implements MutationListener.Provider
		{
			///////////////////////////////////////////////////////////////////////////
			// instance fields //
			////////////////////
			
			private final MutationListener mutationListener;

			
			///////////////////////////////////////////////////////////////////////////
			// constructors //
			/////////////////
			Implementation(final MutationListener mutationListener)
			{
				super();
				this.mutationListener = mutationListener;
			}
			
			
			
			///////////////////////////////////////////////////////////////////////////
			// methods //
			////////////
			
			@Override
			public MutationListener provideMutationListener(final TraversalEnqueuer traversalEnqueuer)
			{
				return this.mutationListener;
			}
		}
		
	}
}

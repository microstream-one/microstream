package one.microstream.concurrent;

public interface ApplicationTaskItem<A>
{
	public void link(A applicationRoot, DomainLinker linker);
	
	public final class Default<A, E> implements ApplicationTaskItem<A>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final DomainLookup<A, E> lookup;
		private final DomainLogic<E, ?>  logic ;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(final DomainLookup<A, E> lookup, final DomainLogic<E, ?> logic)
		{
			super();
			this.lookup = lookup;
			this.logic  = logic ;
		}

		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public void link(final A applicationRoot, final DomainLinker linker)
		{
			final Domain<E> domain = this.lookup.lookupDomain(applicationRoot);
			if(domain == null)
			{
				// (24.09.2019 TM)EXCP: proper exception
				throw new RuntimeException();
			}
			
			linker.link(domain, this.logic);
		}

	}
	
}

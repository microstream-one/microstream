package one.microstream.concurrent;

public interface ApplicationTaskItem<A, R>
{
	public R executeOn(A applicationRoot);
	
	public final class Default<A, E, R> implements ApplicationTaskItem<A, R>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final DomainLookup<A, E> lookup;
		private final DomainLogic<E, R>  logic ;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(final DomainLookup<A, E> lookup, final DomainLogic<E, R> logic)
		{
			super();
			this.lookup = lookup;
			this.logic  = logic ;
		}

		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public R executeOn(final A applicationRoot)
		{
			final Domain<E> domain = this.lookup.lookupDomain(applicationRoot);
			if(domain == null)
			{
				// (24.09.2019 TM)EXCP: proper exception
				throw new RuntimeException();
			}
			
			/* (24.09.2019 TM)FIXME: must defer execution to the queue and wait for the result in here.
			 * But is this really the appropriate type/place to do the concurrency stuff?
			 * Shouldn't that be in DomainTask itself?
			 * Or maybe should the two types be merged?
			 */
			
			return domain.executeLogic(this.logic);
		}
	}
}

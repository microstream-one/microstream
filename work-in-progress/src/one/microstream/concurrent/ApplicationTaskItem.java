package one.microstream.concurrent;

public interface ApplicationTaskItem<A, E, R>
{
	public void link(
		A                 applicationRoot  ,
		DomainTaskLinker  linker           ,
		DomainTaskCreator domainTaskCreator
	);
	
	public final class Default<A, E, R> implements ApplicationTaskItem<A, E, R>
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
		public void link(
			final A                 applicationRoot  ,
			final DomainTaskLinker  linker           ,
			final DomainTaskCreator domainTaskCreator
		)
		{
			final Domain<E> domain = this.lookup.lookupDomain(applicationRoot);
			if(domain == null)
			{
				// (24.09.2019 TM)EXCP: proper exception
				throw new RuntimeException();
			}
			
			final DomainTask<E, R> domainTask = domainTaskCreator.createDomainTask(domain, this.logic);
			
			linker.link(domain, domainTask);
		}

	}
	
}

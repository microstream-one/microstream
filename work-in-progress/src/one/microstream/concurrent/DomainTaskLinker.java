package one.microstream.concurrent;

import static one.microstream.X.notNull;

import one.microstream.collections.types.XTable;

public interface DomainTaskLinker
{
	public <E> boolean link(Domain<E> domain, DomainTask<? super E, ?> domainTask);
	
	
	
	public static DomainTaskLinker New(final XTable<Domain<?>, DomainTaskLink<?>> taskLinks)
	{
		return new DomainTaskLinker.Default(
			notNull(taskLinks)
		);
	}
	
	// a type just to link the two Es together and then that is not supported by lambdas. Great.
	public final class Default implements DomainTaskLinker
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		final XTable<Domain<?>, DomainTaskLink<?>> taskLinks;

		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default(final XTable<Domain<?>, DomainTaskLink<?>> taskLinks)
		{
			super();
			this.taskLinks = taskLinks;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public <E> boolean link(final Domain<E> domain, final DomainTask<? super E, ?> domainTask)
		{
			final DomainTaskLink<E> link = DomainTaskLink.New((EnqueingDomain<E>)domain, domainTask);
			
			return this.taskLinks.add(domain, link);
		}
		
	}
	
}

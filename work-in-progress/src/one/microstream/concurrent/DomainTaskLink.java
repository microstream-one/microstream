package one.microstream.concurrent;

import static one.microstream.X.notNull;

public interface DomainTaskLink<E>
{
	public void enqueueTask();
	
	public static <E> DomainTaskLink<E> New(
		final EnqueingDomain<E> domain,
		final DomainTask<? super E, ?>  task
	)
	{
		return new DomainTaskLink.Default<>(
			notNull(domain),
			notNull(task)
		);
	}
	
	public final class Default<E> implements DomainTaskLink<E>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		final EnqueingDomain<E> domain;
		final DomainTask<? super E, ?>  task  ;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default(
			final EnqueingDomain<E> domain,
			final DomainTask<? super E, ?>  task
		)
		{
			super();
			this.domain = domain;
			this.task   = task  ;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public void enqueueTask()
		{
			this.domain.enqueueTask(this.task);
		}
		
	}
	
}

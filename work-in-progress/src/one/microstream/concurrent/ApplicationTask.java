package one.microstream.concurrent;

import one.microstream.collections.types.XGettingCollection;

public interface ApplicationTask<A, R>
{
	public void createDomainTasks(
		A                applicationRoot ,
		DomainTaskLinker domainTaskLinker
	);
	
	public boolean isComplete();
	
	public R result();
	
	public final class Default<A, R> implements ApplicationTask<A, R>, DomainTaskCreator
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		// (25.09.2019 TM)TODO: Concurrency: Just "A" or "? extends A" or "? super A"?
		private final XGettingCollection<? extends ApplicationTaskItem<A, ?, ?>> taskitems;

		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default(final XGettingCollection<? extends ApplicationTaskItem<A, ?, ?>> taskitems)
		{
			super();
			this.taskitems = taskitems;
		}


		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public void createDomainTasks(
			final A                applicationRoot ,
			final DomainTaskLinker domainTaskLinker
		)
		{
			for(final ApplicationTaskItem<A, ?, ?> item : this.taskitems)
			{
				item.link(applicationRoot, domainTaskLinker, this);
			}
		}

		@Override
		public <E, T> DomainTask<E, T> createDomainTask(
			final Domain<E>                 domain     ,
			final DomainLogic<? super E, T> linkedLogic
		)
		{
			// (26.09.2019 TM)FIXME: Concurrency: link to this instance, see code task in DomainTask class.
			// (26.09.2019 TM)FIXME: Concurrency: register DomainTask
			return DomainTask.New(linkedLogic);
		}

		@Override
		public boolean isComplete()
		{
			// (26.09.2019 TM)FIXME: Concurrency: check some counter of DomainTasks that reported completed.
			// (26.09.2019 TM)FIXME: Concurrency:
			throw new one.microstream.meta.NotImplementedYetError();
		}

		@Override
		public R result()
		{
			// (26.09.2019 TM)FIXME: Concurrency: throw exception if not completed, yet.
			// (26.09.2019 TM)FIXME: Concurrency: create result instance ("reduce"?) and return it.
			throw new one.microstream.meta.NotImplementedYetError();
		}
		
	}
	
}

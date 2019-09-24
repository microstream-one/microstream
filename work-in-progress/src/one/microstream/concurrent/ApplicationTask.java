package one.microstream.concurrent;

import one.microstream.collections.types.XGettingCollection;

public interface ApplicationTask<A>
{
	public void executeOn(A applicationRoot);
	
	public final class Default<A> implements ApplicationTask<A>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final XGettingCollection<? extends ApplicationTaskItem<? super A, ?>> taskitems;

		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default(final XGettingCollection<? extends ApplicationTaskItem<? super A, ?>> taskitems)
		{
			super();
			this.taskitems = taskitems;
		}


		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public void executeOn(final A applicationRoot)
		{
			/* (24.09.2019 TM)FIXME: ApplicationTask execution
			 * 1.) lookup all domain instances for all TaskItems via their lookup logics.
			 * 2.) create and enqueu DomainTasks for all determined Domain instances with the tasks domain logic
			 * 3.) wait on completion of all DomainTasks.
			 * 4.) query their results
			 * ?? what to do with the results? pass to another modular logic?
			 * ?? and wouldn't that logic have to except a parameter like XGettingTable<E, R>?
			 * ?? what if the tasks needs to enqueue another DomainTask before it can be completed?
			 * 
			 * So maybe an ApplicationTask instance must have a sequence of sub tasks, which:
			 * - expect a result of the form XGettingTable<E, R> (potenzially null for the first sub task)
			 * - have a sequence of TaskItems defining the required domain instances and logics
			 * - build a resulting XGettingTable<E, R> when being executed
			 * 
			 * executing an ApplicationTask would mean to execute sub task by sub task, usually just a single one.
			 * But that leaves out the DomainTask that is the actual thing getting enqueued, the "anchor" of an
			 * application task in the processig queues.
			 * 
			 * Hm...
			 */
			
			throw new one.microstream.meta.NotImplementedYetError(); // FIXME ApplicationTask<A>#executeOn()
		}
		
		
	}
}

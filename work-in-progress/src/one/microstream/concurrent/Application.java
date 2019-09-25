package one.microstream.concurrent;

import one.microstream.collections.HashTable;

public interface Application<A>
{
	// (24.09.2019 TM)TODO: Concurrency: not sure this is needed at all. Maybe for special cases and/or controlling/debugging.
//	public <E> E getDomainRootEntity(DomainLookup<? super A, E> lookup);
	
	// (24.09.2019 TM)NOTE: ideally, this should be the sole method in the whole type
	public void executeTask(ApplicationTask<? super A> task);
	
	
	public final class Default<A> implements Application<A>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final A                 applicationRoot  ;
		private final DomainTaskCreator domainTaskCreator;

		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default(
			final A                 applicationRoot  ,
			final DomainTaskCreator domainTaskCreator
		)
		{
			super();
			this.applicationRoot   = applicationRoot  ;
			this.domainTaskCreator = domainTaskCreator;
		}
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		private synchronized void synchronizedLinkDomains(
			final ApplicationTask<? super A>              task        ,
			final HashTable<Domain<?>, DomainLogic<?, ?>> linkedLogics
		)
		{
			/*
			 * while shortly locking the whole application to prevent
			 * concurrent modifications to the domain managing structure,
			 * all logics are linked to their target domains.
			 */
			task.linkDomains(this.applicationRoot, linkedLogics);
		}
				
		@Override
		public void executeTask(final ApplicationTask<? super A> task)
		{
			final HashTable<Domain<?>, DomainLogic<?, ?>> linkedLogics = HashTable.New();
			final HashTable<Domain<?>, DomainTask> linkedDomainTasks = HashTable.New();
			
			this.synchronizedLinkDomains(task, linkedLogics);
			
			// creator implementation must lock internally if necessary
			this.domainTaskCreator.createDomainTasks(linkedLogics, linkedDomainTasks);
			
			// all required domain tasks have been successfully created. Now they can be enqueued.
			
			/* (25.09.2019 TM)FIXME: Concurrency: ApplicationTask DomainTask creation
			 * Wouldn't the ApplicationTask have to create the DomainTasks itself and internally,
			 * so that it can query their results etc.?
			 * Must be changed ...
			 * 
			 */
			
			/* (24.09.2019 TM)FIXME: Concurrency: ApplicationTask execution
			 * v 1.) lookup all domain instances for all TaskItems via their lookup logics.
			 * i 2.) create and enqueue DomainTasks for all determined Domain instances with the tasks domain logic
			 *   3.) wait on completion of all DomainTasks.
			 *   4.) query their results
			 *   ?? what to do with the results? pass to another modular logic?
			 *   ?? and wouldn't that logic have to except a parameter like XGettingTable<E, R>?
			 *   ?? what if the tasks needs to enqueue another DomainTask before it can be completed?
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
			
			throw new one.microstream.meta.NotImplementedYetError(); // FIXME Application.Default#executeTask()
		}
		
		
	}
	
}

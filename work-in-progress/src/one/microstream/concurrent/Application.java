package one.microstream.concurrent;

import one.microstream.collections.HashTable;
import one.microstream.typing.KeyValue;

public interface Application<A>
{
	// (24.09.2019 TM)TODO: Concurrency: not sure this is needed at all. Maybe for special cases and/or controlling/debugging.
//	public <E> E getDomainRootEntity(DomainLookup<? super A, E> lookup);
	
	// (24.09.2019 TM)NOTE: ideally, this should be the sole method in the whole type
	public <R> R executeTask(ApplicationTask<? super A, R> task);
	
	
	public final class Default<A> implements Application<A>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final A applicationRoot;

		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default(final A applicationRoot)
		{
			super();
			this.applicationRoot = applicationRoot;
		}
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		private synchronized void synchronizedEnqueueDomainTasks(
			final ApplicationTask<? super A, ?>           applicationTask  ,
			final HashTable<Domain<?>, DomainTaskLink<?>> linkedDomainTasks,
			final DomainTaskLinker                        linker
		)
		{
			/*
			 * while shortly locking the whole application to prevent
			 * concurrent modifications to the domain managing structure,
			 * all required domain tasks are created and enqueued
			 */
			applicationTask.createDomainTasks(this.applicationRoot, linker);
			
			for(final KeyValue<Domain<?>, DomainTaskLink<?>> e : linkedDomainTasks)
			{
				e.value().enqueueTask();
			}
		}
				
		@Override
		public <R> R executeTask(final ApplicationTask<? super A, R> task)
		{
			final HashTable<Domain<?>, DomainTaskLink<?>> linkedDomainTasks = HashTable.New();
			final DomainTaskLinker linker = DomainTaskLinker.New(linkedDomainTasks);

			synchronized(task)
			{
				task.createDomainTasks(this.applicationRoot, linker);
				this.synchronizedEnqueueDomainTasks(task, linkedDomainTasks, linker);

				// all required domain tasks have been successfully created and enqueued.
				while(!task.isComplete())
				{
					try
					{
						task.wait();
						// (26.09.2019 TM)FIXME: Concurrency: timed waiting to perform intermediate checks
					}
					catch(final InterruptedException e)
					{
						// (26.09.2019 TM)XXX: Concurrency: still not sure if that exception is valid, but here it might.
						return null;
					}
				}
				/* (26.09.2019 TM)FIXME: Concurrency: multi-action tasks
				 * Distinct between "current batch of sub tasks is completed" and "no more sub task batch to execute"
				 * The result of one batch must be fed into the next batch as input.
				 * This is necessary to allow logic where a logic depends on the result of a previous logic.
				 * Example:
				 * First determine a certain value accross some/all entites, then select entities with
				 * reference to that value. This may not be split accross multiple ApplicationTasks,
				 * since other tasks executed in the meantime could make the first value meaningless/inconsistent.
				 * 
				 * However:
				 * executing an ApplicationTask would mean to execute DomainTasks by DomainTasks, usually just a single one.
				 * But that leaves out the DomainTask that is the actual thing getting enqueued, the "anchor" of an
				 * application task in the processig queues.
				 * 
				 * Hm...
				 * So maybe there can only be one batch of DomainTasks that does the iteration internally?
				 * Would that help with or complicate the return type typing?
				 * Hm...
				 */
				
				return task.result();
			}
						
		}
		
		
	}
	
}

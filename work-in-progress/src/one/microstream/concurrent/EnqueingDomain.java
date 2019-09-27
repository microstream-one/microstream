package one.microstream.concurrent;

// extra interface to keep the API towards the using threads clean in order to avoid confusion and errors.
public interface EnqueingDomain<E> extends Domain<E>
{
	public void enqueueTask(DomainTask<? super E, ?> task);
	
	
	
	public final class Default<E> implements EnqueingDomain<E>, Runnable
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		/* (27.09.2019 TM)TODO: Concurrency: owner could change to dynamically distribute load
		 * Or maybe this is done better by replacing a domain instance with another one with different owner.
		 */
		private final Thread owner     ;
		private final E      rootEntity;
		
		private QueueEntry<E> head, tail;
		private volatile boolean running = false;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default(final Thread owner, final E rootEntity)
		{
			super();
			this.owner = owner;
			this.rootEntity = rootEntity;
			this.tail = this.head = new QueueEntry<>();
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public <R> R executeLogic(final DomainLogic<? super E, R> logic)
		{
			// core part of the whole concept: only the owner thread may execute a logic on a domain.
			if(Thread.currentThread() != this.owner)
			{
				throw new RuntimeException(); // (26.09.2019 TM)EXCP: proper exception
			}
			
			return logic.executeDomainLogic(this.rootEntity);
		}

		@Override
		public void enqueueTask(final DomainTask<? super E, ?> task)
		{
			// enqueue thread-safely and notify domain thread about the newly arrived work
			synchronized(this)
			{
				this.tail = this.tail.next = new QueueEntry<>(task);
				this.notifyAll();
			}
		}
		
		@Override
		public void run()
		{
			while(this.running)
			{
				final DomainTask<? super E, ?> currentTask;
				synchronized(this)
				{
					while(this.head.next == null)
					{
						try
						{
							// (27.09.2019 TM)TODO: timeout and check again for running or maybe continue outer loop.
							this.wait();
						}
						catch(final InterruptedException e)
						{
							// (27.09.2019 TM)NOTE: once again not sure how to react.
							
							// (27.09.2019 TM)EXCP: proper exception
							throw new RuntimeException(e);
						}
					}
					
					currentTask = this.head.next.task;
					
					// entry is cleared to prevent idle domain thread strongly referencing the last logic
					// (27.09.2019 TM)XXX: Concurrency: but then what about losing the reference for debuggability?
					(this.head = this.head.next).task = null;
				}
				
				/* (27.09.2019 TM)TODO: Concurrency: Domain running handling
				 * Really no synchronized lock needed?
				 * 
				 * Also:
				 * Maybe set some "is busy" flag/state to indicate the entity sub graph in this domain
				 * is currently worked on, i.e. at least read, maybe even changed?
				 * Might be usefull for debugging/internal/custom accesses to it circumventing the domain concept.
				 */
				currentTask.executeOn(this.rootEntity);
			}
			
		}
		
		
		static final class QueueEntry<E>
		{
			///////////////////////////////////////////////////////////////////////////
			// instance fields //
			////////////////////
			
			DomainTask<? super E, ?> task;
			
			QueueEntry<E> next;
			
			
			
			///////////////////////////////////////////////////////////////////////////
			// constructors //
			/////////////////

			QueueEntry()
			{
				super();
				// dummy constructor is only used for the initial head dummy
			}
				
			QueueEntry(final DomainTask<? super E, ?> task)
			{
				super();
				this.task = task;
			}
			
			
		}
		
	}
}

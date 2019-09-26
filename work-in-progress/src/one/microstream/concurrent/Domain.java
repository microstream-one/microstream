package one.microstream.concurrent;

public interface Domain<E>
{
	public <R> R executeLogic(final DomainLogic<? super E, R> logic);
	
	
	
	public final class Default<E> implements EnqueingDomain<E>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final Thread owner     ;
		private final E      rootEntity;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default(final Thread owner, final E rootEntity)
		{
			super();
			this.owner = owner;
			this.rootEntity = rootEntity;
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
			task.executeOn(this.rootEntity);
			
			throw new one.microstream.meta.NotImplementedYetError(); // FIXME EnqueingDomain<E>#enqueueTask()
		}
		
	}
	
}


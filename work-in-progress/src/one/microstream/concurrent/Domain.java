package one.microstream.concurrent;

public interface Domain<E>
{
	public <R> R executeLogic(final DomainLogic<? super E, R> logic);
	
	
	public final class Default<E> implements Domain<E>
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
			return logic.executeDomainLogic(this.rootEntity);
		}
		
	}
	
}


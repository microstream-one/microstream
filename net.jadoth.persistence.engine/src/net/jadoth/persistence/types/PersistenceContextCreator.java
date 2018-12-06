package net.jadoth.persistence.types;


@FunctionalInterface
public interface PersistenceContextCreator<M>
{
	public PersistenceContext<M> createContext(
		PersistenceObjectRegistry       registry  ,
		PersistenceTypeHandlerLookup<M> typeLookup
	);
	
	
	
	public final class Implementation<M> implements PersistenceContextCreator<M>
	{
		@Override
		public PersistenceContext<M> createContext(
			final PersistenceObjectRegistry       registry  ,
			final PersistenceTypeHandlerLookup<M> typeLookup
		)
		{
			return PersistenceContext.New(registry, typeLookup);
		}
		
	}
	
}

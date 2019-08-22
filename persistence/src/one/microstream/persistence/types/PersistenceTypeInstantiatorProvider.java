package one.microstream.persistence.types;

import static one.microstream.X.notNull;

import one.microstream.collections.ConstHashTable;
import one.microstream.collections.types.XGettingMap;

public interface PersistenceTypeInstantiatorProvider<M>
{
	public <T> PersistenceTypeInstantiator<M, T> provideTypeInstantiator(Class<T> type);
	
	public static <M> PersistenceTypeInstantiatorProvider<M> Provider()
	{
		return new PersistenceInstantiator.Default<>();
	}
	
	public static <M> PersistenceTypeInstantiatorProvider<M> New(
		final PersistenceInstantiator<M> instantiator
	)
	{
		return new PersistenceTypeInstantiatorProvider.Default<>(
			notNull(instantiator)
		);
	}
	
	public static <M> PersistenceTypeInstantiatorProvider<M> New(
		final XGettingMap<Class<?>, PersistenceTypeInstantiator<M, ?>> instantiatorMapping,
		final PersistenceInstantiator<M>                               instantiator
	)
	{
		// there must always be a universal instantiator. Even it's just a dummy throwing an exception.
		return instantiatorMapping.isEmpty()
			? New(instantiator)
			: new PersistenceTypeInstantiatorProvider.Mapped<>(
				ConstHashTable.New(instantiatorMapping),
				notNull(instantiator)
			)
		;
	}
	

	public class Default<M> implements PersistenceTypeInstantiatorProvider<M>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final PersistenceInstantiator<M> instantiator;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(final PersistenceInstantiator<M> instantiator)
		{
			super();
			this.instantiator = instantiator;
		}


		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public <T> PersistenceTypeInstantiator<M, T> provideTypeInstantiator(final Class<T> type)
		{
			return PersistenceTypeInstantiator.New(type, this.instantiator);
		}
		
	}
	
	public final class Mapped<M> extends Default<M>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final ConstHashTable<Class<?>, PersistenceTypeInstantiator<M, ?>> instantiatorMapping;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Mapped(
			final ConstHashTable<Class<?>, PersistenceTypeInstantiator<M, ?>> instantiatorMapping  ,
			final PersistenceInstantiator<M>                                  universalInstantiator
		)
		{
			super(universalInstantiator);
			this.instantiatorMapping = instantiatorMapping;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final <T> PersistenceTypeInstantiator<M, T> provideTypeInstantiator(final Class<T> type)
		{
			final PersistenceTypeInstantiator<M, ?> mappedInstatiator = this.instantiatorMapping.get(type);
			
			@SuppressWarnings("unchecked") // cast safety ensured by mapping logic
			final PersistenceTypeInstantiator<M, T> casted = mappedInstatiator != null
				? (PersistenceTypeInstantiator<M, T>)mappedInstatiator
				: super.provideTypeInstantiator(type)
			;
				
			return casted;
		}
		
	}
	
}

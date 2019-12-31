package one.microstream.persistence.types;

import static one.microstream.X.notNull;

import one.microstream.collections.ConstHashTable;
import one.microstream.collections.types.XGettingMap;

public interface PersistenceTypeInstantiatorProvider<D>
{
	public <T> PersistenceTypeInstantiator<D, T> provideTypeInstantiator(Class<T> type);
	
	public static <D> PersistenceTypeInstantiatorProvider<D> Provider()
	{
		return new PersistenceInstantiator.Default<>();
	}
	
	public static <D> PersistenceTypeInstantiatorProvider<D> New(
		final PersistenceInstantiator<D> instantiator
	)
	{
		return new PersistenceTypeInstantiatorProvider.Default<>(
			notNull(instantiator)
		);
	}
	
	public static <D> PersistenceTypeInstantiatorProvider<D> New(
		final XGettingMap<Class<?>, PersistenceTypeInstantiator<D, ?>> instantiatorMapping,
		final PersistenceInstantiator<D>                               instantiator
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
	

	public class Default<D> implements PersistenceTypeInstantiatorProvider<D>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final PersistenceInstantiator<D> instantiator;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(final PersistenceInstantiator<D> instantiator)
		{
			super();
			this.instantiator = instantiator;
		}


		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public <T> PersistenceTypeInstantiator<D, T> provideTypeInstantiator(final Class<T> type)
		{
			return PersistenceTypeInstantiator.New(type, this.instantiator);
		}
		
	}
	
	public final class Mapped<D> extends Default<D>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final ConstHashTable<Class<?>, PersistenceTypeInstantiator<D, ?>> instantiatorMapping;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Mapped(
			final ConstHashTable<Class<?>, PersistenceTypeInstantiator<D, ?>> instantiatorMapping  ,
			final PersistenceInstantiator<D>                                  universalInstantiator
		)
		{
			super(universalInstantiator);
			this.instantiatorMapping = instantiatorMapping;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final <T> PersistenceTypeInstantiator<D, T> provideTypeInstantiator(final Class<T> type)
		{
			final PersistenceTypeInstantiator<D, ?> mappedInstatiator = this.instantiatorMapping.get(type);
			
			@SuppressWarnings("unchecked") // cast safety ensured by mapping logic
			final PersistenceTypeInstantiator<D, T> casted = mappedInstatiator != null
				? (PersistenceTypeInstantiator<D, T>)mappedInstatiator
				: super.provideTypeInstantiator(type)
			;
				
			return casted;
		}
		
	}
	
}

package net.jadoth.traversal2;

import static net.jadoth.Jadoth.coalesce;
import static net.jadoth.Jadoth.notNull;

import net.jadoth.collections.HashTable;
import net.jadoth.collections.types.XGettingCollection;
import net.jadoth.collections.types.XGettingMap;
import net.jadoth.collections.types.XGettingTable;
import net.jadoth.util.KeyValue;

public interface TraverserAcceptingProvider
{
	public <T> TraverserAccepting<T> provideTraversalHandler(T instance);
	
	public default boolean isUnhandled(final Object instance)
	{
		return this.provideTraversalHandler(instance) == null;
	}

	
	
	public static TraverserAcceptingProvider New(
		final XGettingMap<Object, TraverserAccepting<?>>     handlersPerInstance     ,
		final XGettingMap<Class<?>, TraverserAccepting<?>>   handlersPerConcreteType ,
		final XGettingTable<Class<?>, TraverserAccepting<?>> handlersPerPolymorphType,
		final XGettingCollection<Class<?>>                   leafTypes               ,
		final TraverserAccepting.Creator                     traversalHandlerCreator
	)
	{
		return new TraverserAcceptingProvider.Implementation(
			handlersPerInstance != null && handlersPerInstance.isEmpty() ? null : handlersPerInstance,
			notNull(handlersPerConcreteType) ,
			notNull(handlersPerPolymorphType),
			notNull(leafTypes)               ,
			notNull(traversalHandlerCreator)
		);
	}
	
	public final class Implementation implements TraverserAcceptingProvider
	{
		///////////////////////////////////////////////////////////////////////////
		// constants        //
		/////////////////////
		
		static final TraverserAccepting<?> UNHANDLED = (a, b, c) ->
		{
			// empty
		};
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final XGettingMap<Object, TraverserAccepting<?>>     handlersPerInstance     ;
		private final HashTable<Class<?>, TraverserAccepting<?>>     handlersPerConcreteType ;
		private final XGettingTable<Class<?>, TraverserAccepting<?>> handlersPerPolymorphType;
		private final TraverserAccepting.Creator                      traversalHandlerCreator ;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Implementation(
			final XGettingMap<Object, TraverserAccepting<?>>     handlersPerInstance     ,
			final XGettingMap<Class<?>, TraverserAccepting<?>>   handlersPerConcreteType ,
			final XGettingTable<Class<?>, TraverserAccepting<?>> handlersPerPolymorphType,
			final XGettingCollection<Class<?>>                 leafTypes               ,
			final TraverserAccepting.Creator                      traversalHandlerCreator
		)
		{
			super();
			this.handlersPerInstance      = handlersPerInstance     ;
			this.handlersPerPolymorphType = handlersPerPolymorphType;
			this.traversalHandlerCreator  = traversalHandlerCreator ;
			this.handlersPerConcreteType  = initializeHandlersPerConcreteType(handlersPerConcreteType, leafTypes);
		}
		
		private HashTable<Class<?>, TraverserAccepting<?>> initializeHandlersPerConcreteType(
			final XGettingMap<Class<?>, TraverserAccepting<?>> handlersPerConcreteType ,
			final XGettingCollection<Class<?>>               leafTypes
		)
		{
			final HashTable<Class<?>, TraverserAccepting<?>> localMap = HashTable.New(handlersPerConcreteType);
			leafTypes.iterate(t -> localMap.add(t, UNHANDLED));
			return localMap;
		}
		
		private <T> TraverserAccepting<T> handleNewType(final Class<T> type)
		{
			for(final KeyValue<Class<?>, TraverserAccepting<?>> entry : this.handlersPerPolymorphType)
			{
				if(entry.key().isAssignableFrom(type))
				{
					return this.registerForConcreteType(entry.value(), type);
				}
			}
			
			return this.registerForConcreteType(
				coalesce(this.traversalHandlerCreator.createTraverserAccepting(type), UNHANDLED),
				type
			);
		}
		
		@SuppressWarnings("unchecked")
		private <T> TraverserAccepting<T> registerForConcreteType(final TraverserAccepting<?> traversalHandler, final Class<T> type)
		{
			this.handlersPerConcreteType.add(type, traversalHandler);
			return (TraverserAccepting<T>)traversalHandler;
		}
		
		private TraverserAccepting<?> internalProvideTraversalHandler(final Object instance)
		{
			if(instance == null)
			{
				return null;
			}
			
			if(this.handlersPerInstance != null)
			{
				final TraverserAccepting<?> perInstanceHandler;
				if((perInstanceHandler = this.handlersPerInstance.get(instance)) != null)
				{
					return perInstanceHandler;
				}
			}
						
			final TraverserAccepting<?> perTypeHandler;
			if((perTypeHandler = this.handlersPerConcreteType.get(instance.getClass())) != null)
			{
				return perTypeHandler == UNHANDLED ? null : perTypeHandler;
			}
			
			return this.handleNewType(instance.getClass());
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> TraverserAccepting<T> provideTraversalHandler(final T instance)
		{
			return (TraverserAccepting<T>)this.internalProvideTraversalHandler(instance);
		}
		
		
	}
}

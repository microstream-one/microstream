package net.jadoth.traversal2;

import static net.jadoth.Jadoth.coalesce;

import net.jadoth.collections.HashTable;
import net.jadoth.collections.types.XGettingCollection;
import net.jadoth.collections.types.XGettingMap;
import net.jadoth.collections.types.XGettingTable;
import net.jadoth.util.KeyValue;

public interface TraversalHandlerProvider
{
	public TraversalHandler provideTraversalHandler(Object instance);
	
	public default boolean isUnhandled(final Object instance)
	{
		return this.provideTraversalHandler(instance) == null;
	}

	
	public final class Implementation implements TraversalHandlerProvider
	{
		///////////////////////////////////////////////////////////////////////////
		// constants        //
		/////////////////////
		
		static final TraversalHandler UNHANDLED = (a, b, c) ->
		{
			// empty
		};
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final XGettingMap<Object, TraversalHandler>     handlersPerInstance     ;
		private final HashTable<Class<?>, TraversalHandler>     handlersPerConcreteType ;
		private final XGettingTable<Class<?>, TraversalHandler> handlersPerPolymorphType;
		private final TraversalHandlerCreator                   traversalHandlerCreator ;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Implementation(
			final XGettingMap<Object, TraversalHandler>     handlersPerInstance     ,
			final XGettingMap<Class<?>, TraversalHandler>   handlersPerConcreteType ,
			final XGettingTable<Class<?>, TraversalHandler> handlersPerPolymorphType,
			final XGettingCollection<Class<?>>              leafTypes               ,
			final TraversalHandlerCreator                   traversalHandlerCreator
		)
		{
			super();
			this.handlersPerInstance      = handlersPerInstance     ;
			this.handlersPerPolymorphType = handlersPerPolymorphType;
			this.traversalHandlerCreator  = traversalHandlerCreator ;
			this.handlersPerConcreteType  = initializeHandlersPerConcreteType(handlersPerConcreteType, leafTypes);
		}
		
		private HashTable<Class<?>, TraversalHandler> initializeHandlersPerConcreteType(
			final XGettingMap<Class<?>, TraversalHandler> handlersPerConcreteType ,
			final XGettingCollection<Class<?>>            leafTypes
		)
		{
			final HashTable<Class<?>, TraversalHandler> localMap = HashTable.New(handlersPerConcreteType);
			leafTypes.iterate(t -> localMap.add(t, UNHANDLED));
			return localMap;
		}
		
		private TraversalHandler handleNewType(final Class<?> type)
		{
			for(final KeyValue<Class<?>, TraversalHandler> entry : this.handlersPerPolymorphType)
			{
				if(entry.key().isAssignableFrom(type))
				{
					return this.registerForConcreteType(entry.value(), type);
				}
			}
			
			return this.registerForConcreteType(
				coalesce(this.traversalHandlerCreator.createTraversalHandler(type), UNHANDLED),
				type
			);
		}
		
		private TraversalHandler registerForConcreteType(final TraversalHandler traversalHandler, final Class<?> type)
		{
			this.handlersPerConcreteType.add(type, traversalHandler);
			return traversalHandler;
		}

		@Override
		public TraversalHandler provideTraversalHandler(final Object instance)
		{
			if(instance == null)
			{
				return null;
			}
			
			if(this.handlersPerInstance != null)
			{
				final TraversalHandler perInstanceHandler;
				if((perInstanceHandler = this.handlersPerInstance.get(instance)) != null)
				{
					return perInstanceHandler;
				}
			}
			
			final TraversalHandler perTypeHandler;
			if((perTypeHandler = this.handlersPerConcreteType.get(instance.getClass())) != null)
			{
				return perTypeHandler == UNHANDLED ? null : perTypeHandler;
			}
			
			return this.handleNewType(instance.getClass());
		}
		
		
	}
}

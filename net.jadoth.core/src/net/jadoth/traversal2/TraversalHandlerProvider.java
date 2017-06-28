package net.jadoth.traversal2;

import net.jadoth.collections.HashTable;
import net.jadoth.collections.types.XGettingTable;
import net.jadoth.util.KeyValue;

public interface TraversalHandlerProvider
{
	public TraversalHandler provideTraversalHandler(Object instance);
		

	
	public final class Implementation implements TraversalHandlerProvider
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final HashTable<Object, TraversalHandler>       handlersPerInstance     ;
		private final HashTable<Class<?>, TraversalHandler>     handlersPerConcreteType ;
		private final XGettingTable<Class<?>, TraversalHandler> handlersPerPolymorphType;
		private final TraversalHandlerCreator                   traversalHandlerCreator ;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Implementation(
			final HashTable<Object, TraversalHandler>       handlersPerInstance     ,
			final HashTable<Class<?>, TraversalHandler>     handlersPerConcreteType ,
			final XGettingTable<Class<?>, TraversalHandler> handlersPerPolymorphType,
			final TraversalHandlerCreator                   traversalHandlerCreator
		)
		{
			super();
			this.handlersPerInstance      = handlersPerInstance     ;
			this.handlersPerConcreteType  = handlersPerConcreteType ;
			this.handlersPerPolymorphType = handlersPerPolymorphType;
			this.traversalHandlerCreator  = traversalHandlerCreator ;
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
				this.traversalHandlerCreator.createTraversalHandler(type),
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
				return perTypeHandler;
			}
			
			return this.handleNewType(instance.getClass());
		}
		
		
	}
}

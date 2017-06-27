package net.jadoth.traversal2;

import net.jadoth.collections.types.XGettingMap;

public interface TraversalHandlerProvider
{
	public TraversalHandler provideTraversalHandler(Object instance);
		
	// every instance must be thread-local / thread-exclusive
	/* (27.06.2017 TM)FIXME: but that contradicts/complicates all the handler registration
	 * There must be a decoupling of (registerable) traversal handler logic and traversing state
	 * There has to be a TraversalHandlerProviderProvider that provides a thread-exclusive TraversalHandlerProvider
	 * on every call / thread.
	 * The callback to analyse a new type and create a suitable handler for it can be routed back to the TraversalHandlerProviderProvider
	 * which in turn registers a suitable TraversalHandlerTypeProvider.
	 * 
	 * ...
	 * On the other hand ...
	 * Forseeing / securing multithreading for a task like graph traversal is pretty tricky.
	 * E.g. alreadyHandled management, race conditions in case of handlers registering skip instances, etc.
	 * 
	 * Maybe the implementation should strictly only support single threaded usage.
	 * At least initially or in the default implementation
	 * 
	 * 
	 */
	public final class Implementation implements TraversalHandlerProvider
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final XGettingMap<Object, TraversalHandler>   handlersPerInstance      ;
		private final XGettingMap<Class<?>, TraversalHandler> handlersPerConcreteType  ;
		
		// must contain Object[] and XReplacingBag[]
		// (27.06.2017 TM)FIXME: but wouldn't that suffice to be done once upon analysis of an unhandled type?
		private final XGettingMap<Class<?>, TraversalHandler> handlersPerPolymorphTypes;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Implementation(
			final XGettingMap<Object, TraversalHandler>   handlersPerInstance      ,
			final XGettingMap<Class<?>, TraversalHandler> handlersPerConcreteType  ,
			final XGettingMap<Class<?>, TraversalHandler> handlersPerPolymorphTypes,
			final TraverserXCollection                    traverserXCollection     ,
			final TraverserArray                          traverserArray
		)
		{
			super();
			this.handlersPerInstance       = handlersPerInstance      ;
			this.handlersPerConcreteType   = handlersPerConcreteType  ;
			this.handlersPerPolymorphTypes = handlersPerPolymorphTypes;
		}
		
		private TraversalHandler handleNewType(final Class<?> type)
		{
			throw new net.jadoth.meta.NotImplementedYetError(); // FIXME TraversalHandlerProvider.Implementation#handleNewType()
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

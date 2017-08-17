package net.jadoth.traversal;

import static net.jadoth.Jadoth.notNull;

import net.jadoth.collections.HashTable;
import net.jadoth.collections.types.XGettingMap;
import net.jadoth.collections.types.XGettingTable;
import net.jadoth.util.KeyValue;

public interface TypeTraverserProvider
{
	public default boolean isUnhandled(final Object instance)
	{
		return this.provide(instance) == null;
	}
	
	public <T> TypeTraverser<T> provide(T instance);
	
	
	
	public static TypeTraverserProvider.Implementation New(
		final TypeTraverser.Creator                     traverserCreator          ,
		final XGettingMap<Object, TypeTraverser<?>>     traversersPerInstance     ,
		final XGettingMap<Class<?>, TypeTraverser<?>>   traversersPerConcreteType ,
		final XGettingTable<Class<?>, TypeTraverser<?>> traversersPerPolymorphType
	)
	{
		return new TypeTraverserProvider.Implementation(
			notNull(traverserCreator)          ,
			        traversersPerInstance      ,
			notNull(traversersPerConcreteType) ,
			notNull(traversersPerPolymorphType)
		);
	}
	
	public final class Implementation implements TypeTraverserProvider
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final XGettingMap<Object, TypeTraverser<?>>     traversersPerInstance     ;
		private final HashTable<Class<?>, TypeTraverser<?>>     traversersPerConcreteType ;
		private final XGettingTable<Class<?>, TypeTraverser<?>> traversersPerPolymorphType;
		private final TypeTraverser.Creator                     traverserCreator          ;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Implementation(
			final TypeTraverser.Creator                       traverserCreator          ,
			final XGettingMap<Object, TypeTraverser<?>>       traversersPerInstance     ,
			final XGettingMap<Class<?>, TypeTraverser<?>>     traversersPerConcreteType ,
			final XGettingTable<Class<?>, TypeTraverser<?>>   traversersPerPolymorphType
		)
		{
			super();
			this.traverserCreator           = traverserCreator ;
			this.traversersPerInstance      = traversersPerInstance     ;
			this.traversersPerPolymorphType = traversersPerPolymorphType;
			this.traversersPerConcreteType  = initializeHandlersPerConcreteType(traversersPerConcreteType);
		}
		
		private static HashTable<Class<?>, TypeTraverser<?>> initializeHandlersPerConcreteType(
			final XGettingMap<Class<?>, TypeTraverser<?>> traversersPerConcreteType
		)
		{
			final HashTable<Class<?>, TypeTraverser<?>> localMap = HashTable.New(traversersPerConcreteType);
			return localMap;
		}
		
		private TypeTraverser<?> handleNewType(final Class<?> type)
		{
			for(final KeyValue<Class<?>, TypeTraverser<?>> entry : this.traversersPerPolymorphType)
			{
				if(entry.key().isAssignableFrom(type))
				{
					this.traversersPerConcreteType.add(type, entry.value());
					return entry.value();
				}
			}
			
			final TypeTraverser<?> created = this.traverserCreator.createTraverser(type);
			if(created != null)
			{
				this.traversersPerConcreteType.add(type, created);
				return created;
			}

			// (17.08.2017 TM)EXCP: proper exception
			throw new RuntimeException("Untraversable type: " + type.getName());
		}
				
		protected final TypeTraverser<?> internalProvideTraversalHandler(final Object instance)
		{
			if(instance == null)
			{
				return null;
			}
			
			if(this.traversersPerInstance != null)
			{
				final TypeTraverser<?> perInstanceHandler;
				if((perInstanceHandler = this.traversersPerInstance.get(instance)) != null)
				{
					return perInstanceHandler;
				}
			}
						
			final TypeTraverser<?> perTypeHandler;
			if((perTypeHandler = this.traversersPerConcreteType.get(instance.getClass())) != null)
			{
				return perTypeHandler;
			}
			
			return this.handleNewType(instance.getClass());
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public <T> TypeTraverser<T> provide(final T instance)
		{
			return (TypeTraverser<T>)this.internalProvideTraversalHandler(instance);
		}
				
	}
	
}

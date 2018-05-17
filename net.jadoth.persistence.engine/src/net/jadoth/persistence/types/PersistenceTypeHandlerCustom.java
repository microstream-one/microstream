package net.jadoth.persistence.types;

/**
 * This type extends the {@link PersistenceTypeHandler} type only by the following reflection contract:<p>
 * An implemention implementing this type must have a constructor requiring exactely one argument
 * of type {@code long} which takes an arbitrary long value greater than 0 as the type id to be associated
 * with the handled type and that will create a fully functional type handler instance.
 * This constructor must be callable at will any amount of times without having negative influence to the
 * logical state of the rest of the application (i.e. be implemented in a "clean" way).
 *
 * @author Thomas Muenz
 * @param <M>
 * @param <T>
 */
public interface PersistenceTypeHandlerCustom<M, T> extends PersistenceTypeHandler<M, T>
{
//	public interface Creator<M, T> extends PersistenceTypeHandler.Creator<M, T>
//	{
//		@Override
//		public PersistenceTypeHandlerCustom<M, T> createTypeHandler(long typeId);
//
//
//		public final class ReflectiveImplementation<M, T> implements PersistenceTypeHandlerCustom.Creator<M, T>
//		{
//			private final Class<? extends PersistenceTypeHandlerCustom<M, T>> typeHandlerClass;
//
//			public ReflectiveImplementation(final Class<? extends PersistenceTypeHandlerCustom<M, T>> typeHandlerClass)
//			{
//				super();
//				this.typeHandlerClass = typeHandlerClass;
//			}
//
//			@Override
//			public final PersistenceTypeHandlerCustom<M, T> createTypeHandler(final long typeId)
//			{
//				return instantiateCustomTypeHandler(this.typeHandlerClass, typeId);
//			}
//
//		}
//	}




//	public static <M, T> PersistenceTypeHandlerCustom<M, T> instantiateBlankCustomTypeHandler(
//		final Class<? extends PersistenceTypeHandlerCustom<M, T>> typeHandlerClass
//	)
//	{
//		return instantiateCustomTypeHandler(typeHandlerClass, Long.MAX_VALUE);
//	}
//
//	public static <M, T> PersistenceTypeHandlerCustom<M, T> instantiateCustomTypeHandler(
//		final Class<? extends PersistenceTypeHandlerCustom<M, T>> typeHandlerClass,
//		final Long typeId
//	)
//	{
//		final PersistenceTypeHandlerCustom<M, T> handler;
//		try
//		{
//			handler = typeHandlerClass.newInstance();
//
////			final Constructor<? extends PersistenceTypeHandlerCustom<M, T>> constructor =
////				typeHandlerClass.getConstructor(long.class)
////			;
////			return constructor.newInstance(typeId); // potential NPE intentional at this place (for now)
//		}
//		catch(final ReflectiveOperationException e)
//		{
//			// (30.03.2013)EXCP: proper exception
//			throw new RuntimeException(e);
//		}
//
//		handler.initializeTypeId(typeId);
//
//		return handler;
//	}


//	public static <M, T> PersistenceTypeHandlerCustom.Creator<M, T> createReflectiveCreator(
//		final Class<? extends PersistenceTypeHandlerCustom<M, T>> typeHandlerClass
//	)
//	{
//		return new PersistenceTypeHandlerCustom.Creator.ReflectiveImplementation<>(typeHandlerClass);
//	}
//
//	public static <T> Class<T> getHandledType(final PersistenceTypeHandler.Creator<?, T> creator)
//	{
//		return creator.createTypeHandler(Long.MAX_VALUE).type();
//	}
//
//	static final class MappingBuilder<M> implements Aggregator<
//		Class<? extends PersistenceTypeHandlerCustom<M, ?>>,
//		HashTable<Class<?>, PersistenceTypeHandlerCustom.Creator<M, ?>>
//	>
//	{
//		///////////////////////////////////////////////////////////////////////////
//		// instance fields  //
//		/////////////////////
//
//		private final HashTable<Class<?>, PersistenceTypeHandlerCustom.Creator<M, ?>> mapping = HashTable.New();
//
//
//
//		private <T> void internalAccept(final Class<? extends PersistenceTypeHandlerCustom<M, T>> typeHandlerClass)
//		{
//			final PersistenceTypeHandlerCustom.Creator<M, T> creator =
//				new PersistenceTypeHandlerCustom.Creator.ReflectiveImplementation<>(typeHandlerClass)
//			;
//			this.mapping.put(
//				creator.createTypeHandler(Long.MAX_VALUE).type(),
//				creator
//			);
//		}
//
//
//		///////////////////////////////////////////////////////////////////////////
//		// constructors     //
//		/////////////////////
//
//		MappingBuilder()
//		{
//			super();
//		}
//
//		@SuppressWarnings("unchecked") // (18.10.2013 TM)XXX: a little messy, improvable? (specialize apply() )
//		@Override
//		public void accept(final Class<? extends PersistenceTypeHandlerCustom<M, ?>> typeHandlerClass)
//		{
//			this.internalAccept((Class<? extends PersistenceTypeHandlerCustom<M, Object>>)typeHandlerClass);
//		}
//
//		@Override
//		public HashTable<Class<?>, PersistenceTypeHandlerCustom.Creator<M, ?>> yield()
//		{
//			return this.mapping;
//		}
//
//	}
//
//	public static <M> XGettingMap<Class<?>, PersistenceTypeHandlerCustom.Creator<M, ?>>
//	createTypeHandlerClassMapping(
//		final XGettingCollection<Class<? extends PersistenceTypeHandlerCustom<M, ?>>> typeHandlerClasses
//	)
//	{
//		final HashTable<Class<?>, PersistenceTypeHandlerCustom.Creator<M, ?>> mapping =
//			typeHandlerClasses.iterate(new MappingBuilder<M>()).yield()
//		;
//		return mapping;
//	}
	
}

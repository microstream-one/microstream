package net.jadoth.persistence.types;

import static net.jadoth.Jadoth.notNull;
import net.jadoth.collections.BulkList;
import net.jadoth.collections.HashTable;
import net.jadoth.collections.types.XGettingCollection;
import net.jadoth.functional.Aggregator;
import net.jadoth.persistence.exceptions.PersistenceExceptionTypeNotPersistable;
import net.jadoth.swizzling.types.Swizzle;
import net.jadoth.swizzling.types.SwizzleTypeIdLookup;
import net.jadoth.swizzling.types.SwizzleTypeIdOwner;
import net.jadoth.swizzling.types.SwizzleTypeManager;
import net.jadoth.util.KeyValue;

public interface PersistenceCustomTypeHandlerRegistry<M> extends PersistenceTypeHandlerCreator<M>
{
	public <T> PersistenceCustomTypeHandlerRegistry<M> registerTypeHandlerClass(
		Class<? extends PersistenceTypeHandlerCustom<M, T>> typeHandlerClass
	);

	public <T> PersistenceCustomTypeHandlerRegistry<M> registerTypeHandlerClass(
		Class<T> type, Class<? extends PersistenceTypeHandlerCustom<M, T>> typeHandlerClass
	);

	public PersistenceCustomTypeHandlerRegistry<M> registerTypeHandlerClasses(
		XGettingCollection<Class<? extends PersistenceTypeHandlerCustom<M, ?>>> typeHandlerClasses
	);

	public <T> PersistenceCustomTypeHandlerRegistry<M> registerTypeHandlerCreator(
		PersistenceTypeHandler.Creator<M, T> typeHandlerCreator
	);

	public <T> PersistenceCustomTypeHandlerRegistry<M> registerTypeHandlerCreator(
		Class<T> type, PersistenceTypeHandler.Creator<M, T> typeHandlerCreator
	);

	public boolean knowsType(Class<?> type);

	public <D extends PersistenceTypeDictionary> D updateTypeDictionary(D typeDictionary, SwizzleTypeIdLookup typeLookup);



	public final class Implementation<M> implements PersistenceCustomTypeHandlerRegistry<M>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		private final HashTable<Class<?>, PersistenceTypeHandler.Creator<M, ?>> mapping = HashTable.New();



		///////////////////////////////////////////////////////////////////////////
		// override methods //
		/////////////////////

		@Override
		public boolean knowsType(final Class<?> type)
		{
			return this.mapping.keys().contains(type);
		}

		@Override
		public final <T> PersistenceCustomTypeHandlerRegistry<M> registerTypeHandlerClass(
			final Class<T>                                            type,
			final Class<? extends PersistenceTypeHandlerCustom<M, T>> typeHandlerClass
		)
		{
			this.registerTypeHandlerCreator(
				type,
				new PersistenceTypeHandlerCustom.Creator.ReflectiveImplementation<>(typeHandlerClass)
			);
			return this;
		}

		@Override
		public final <T> PersistenceCustomTypeHandlerRegistry<M> registerTypeHandlerCreator(
			final Class<T>                             type              ,
			final PersistenceTypeHandler.Creator<M, T> typeHandlerCreator
		)
		{
			this.mapping.put(type, typeHandlerCreator);
			return this;
		}

		@Override
		public <T> PersistenceCustomTypeHandlerRegistry.Implementation<M> registerTypeHandlerClass(
			final Class<? extends PersistenceTypeHandlerCustom<M, T>> typeHandlerClass
		)
		{
			this.registerTypeHandlerClass(
				PersistenceTypeHandlerCustom.instantiateBlankCustomTypeHandler(typeHandlerClass).type(),
				typeHandlerClass
			);
			return this;
		}

		@Override
		public <T> PersistenceCustomTypeHandlerRegistry<M> registerTypeHandlerCreator(
			final PersistenceTypeHandler.Creator<M, T> typeHandlerCreator
		)
		{
			this.registerTypeHandlerCreator(
				PersistenceTypeHandlerCustom.getHandledType(typeHandlerCreator),
				typeHandlerCreator
			);
			return this;
		}

		private <T> void addMapping(final Class<? extends PersistenceTypeHandlerCustom<M, ?>> thc)
		{
			// no idea why a "?" is not directly a viable type for an unspecified T, so specify a fake one
			@SuppressWarnings("unchecked")
			final PersistenceTypeHandlerCustom.Creator<M, ?> c = PersistenceTypeHandlerCustom.createReflectiveCreator(
				(Class<? extends PersistenceTypeHandlerCustom<M, T>>)thc
			);
			this.mapping.add(PersistenceTypeHandlerCustom.getHandledType(c), c);
		}

		@Override
		public PersistenceCustomTypeHandlerRegistry.Implementation<M> registerTypeHandlerClasses(
			final XGettingCollection<Class<? extends PersistenceTypeHandlerCustom<M, ?>>> typeHandlerClasses
		)
		{
			for(final Class<? extends PersistenceTypeHandlerCustom<M, ?>> thc : typeHandlerClasses)
			{
				this.addMapping(thc);
			}
			return this;
		}

		@SuppressWarnings("unchecked") // cast type safety guaranteed by management logic
		private <T> PersistenceTypeHandler.Creator<M, T> lookupCreator(final Class<T> type)
		{
			return (PersistenceTypeHandler.Creator<M, T>)this.mapping.get(type);
		}

		@Override
		public <T> PersistenceTypeHandler<M, T> createTypeHandler(
			final Class<T>                      type,
			final long                          typeId,
			final SwizzleTypeManager            typeManager
		)
			throws PersistenceExceptionTypeNotPersistable
		{
			final PersistenceTypeHandler.Creator<M, T> typeHandlerCreator = this.lookupCreator(type);
			if(typeHandlerCreator == null)
			{
				throw new RuntimeException(); // (30.03.2013)EXCP: proper exception
			}

			final PersistenceTypeHandler<M, T> typeHandler = typeHandlerCreator.createTypeHandler(typeId);
			if(typeHandler.type() != type)
			{
				// just in case
				throw new RuntimeException(); // (18.10.2013 TM)EXCP: proper exception
			}

			return typeHandler;
		}

		@Override
		public <D extends PersistenceTypeDictionary> D updateTypeDictionary(
			final D                   typeDictionary,
			final SwizzleTypeIdLookup typeLookup
		)
		{
			final BulkList<PersistenceTypeDescription> typeDescs = this.mapping.iterate(
				new TypeDescriptionBuilder<M>(typeLookup)
			).yield();
			typeDictionary.registerTypes(typeDescs);
			return typeDictionary;
		}


		static final class TypeDescriptionBuilder<M>
		implements Aggregator<
			KeyValue<Class<?>, PersistenceTypeHandler.Creator<M, ?>>,
			BulkList<PersistenceTypeDescription>
		>
		{
			///////////////////////////////////////////////////////////////////////////
			// instance fields  //
			/////////////////////

			private final SwizzleTypeIdLookup                  typeLookup      ;
			private final BulkList<PersistenceTypeDescription> typeDescriptions = new BulkList<>();



			///////////////////////////////////////////////////////////////////////////
			// constructors     //
			/////////////////////

			public TypeDescriptionBuilder(final SwizzleTypeIdLookup typeLookup)
			{
				super();
				this.typeLookup = notNull(typeLookup);
			}

			@Override
			public void accept(final KeyValue<Class<?>, PersistenceTypeHandler.Creator<M, ?>> element)
			{
				final long typeId = this.typeLookup.lookupTypeId(element.key());

				/* if no typeId known by the local typeLookup, abort without exception.
				 * Rationale: there may be custom type handlers for native types but no defined native type Id for them.
				 * E.g. collection handlers.
				 * The intention is, that the description for those handlers is added later on not as a native type
				 * but as a dynamically encountered type (but with a native handler)
				 *
				 * Danger / potential downside: inconsistent type lookups for other cases (meaning not initializing
				 * custom native handlers but normal dynamically analyzed types later on) is not recognized here
				 * but swallowed. If this is a problem, another solution has to be found, maybe better seperation of the
				 * two concerns.
				 */
				if(typeId == Swizzle.nullId())
				{
					return; // type not known, abort
				}
				this.typeDescriptions.add(
					element.value().createTypeHandler(typeId)
					.typeDescription()
				);
			}

			@Override
			public BulkList<PersistenceTypeDescription> yield()
			{
				return SwizzleTypeIdOwner.sortByTypeIdAscending(this.typeDescriptions);
			}

		}

	}

}

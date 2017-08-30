package net.jadoth.persistence.types;

import static net.jadoth.Jadoth.notNull;

import net.jadoth.collections.BulkList;
import net.jadoth.collections.HashTable;
import net.jadoth.collections.types.XGettingCollection;
import net.jadoth.functional.Aggregator;
import net.jadoth.persistence.exceptions.PersistenceExceptionTypeNotPersistable;
import net.jadoth.swizzling.types.Swizzle;
import net.jadoth.swizzling.types.SwizzleTypeIdOwner;
import net.jadoth.swizzling.types.SwizzleTypeLookup;
import net.jadoth.swizzling.types.SwizzleTypeManager;
import net.jadoth.util.KeyValue;

public interface PersistenceCustomTypeHandlerRegistry<M> extends PersistenceTypeHandlerEnsurer<M>
{
	public <T> PersistenceCustomTypeHandlerRegistry<M> registerTypeHandler(
		PersistenceTypeHandler.Initializer<M, ?> typeHandlerInitializer
	);

	public <T> PersistenceCustomTypeHandlerRegistry<M> registerTypeHandler(
		Class<T>                                 type            ,
		PersistenceTypeHandler.Initializer<M, ?> typeHandlerInitializer
	);

	public PersistenceCustomTypeHandlerRegistry<M> registerTypeHandlers(
		XGettingCollection<? extends PersistenceTypeHandler.Initializer<M, ?>> typeHandlerInitializers
	);


	public boolean knowsType(Class<?> type);

	public <D extends PersistenceTypeDictionary> D updateTypeDictionary(D typeDictionary);



	public final class Implementation<M> implements PersistenceCustomTypeHandlerRegistry<M>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////
		
		private final SwizzleTypeLookup                                             typeLookup;
		private final HashTable<Class<?>, PersistenceTypeHandler.Initializer<M, ?>> mapping    = HashTable.New();

		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		public Implementation(final SwizzleTypeLookup typeLookup)
		{
			super();
			this.typeLookup = typeLookup;
		}
		


		///////////////////////////////////////////////////////////////////////////
		// override methods //
		/////////////////////

		@Override
		public boolean knowsType(final Class<?> type)
		{
			return this.mapping.keys().contains(type);
		}

		@Override
		public final <T> PersistenceCustomTypeHandlerRegistry<M> registerTypeHandler(
			final Class<T>                                 type                  ,
			final PersistenceTypeHandler.Initializer<M, ?> typeHandlerInitializer
		)
		{
			this.mapping.put(type, typeHandlerInitializer);
			return this;
		}

		@Override
		public <T> PersistenceCustomTypeHandlerRegistry.Implementation<M> registerTypeHandler(
			final PersistenceTypeHandler.Initializer<M, ?> typeHandlerInitializer
		)
		{
			this.registerTypeHandler(
				typeHandlerInitializer.type(),
				typeHandlerInitializer
			);
			return this;
		}

		@Override
		public synchronized PersistenceCustomTypeHandlerRegistry.Implementation<M> registerTypeHandlers(
			final XGettingCollection<? extends PersistenceTypeHandler.Initializer<M, ?>> typeHandlerInitializers
		)
		{
			for(final PersistenceTypeHandler.Initializer<M, ?> tdi : typeHandlerInitializers)
			{
				this.registerTypeHandler(tdi);
			}
			return this;
		}

		@SuppressWarnings("unchecked") // cast type safety guaranteed by management logic
		private <T> PersistenceTypeHandler.Initializer<M, T> lookupInitializer(final Class<T> type)
		{
			return (PersistenceTypeHandler.Initializer<M, T>)this.mapping.get(type);
		}

		@Override
		public <T> PersistenceTypeHandler<M, T> ensureTypeHandler(
			final Class<T>           type       ,
			final long               typeId     ,
			final SwizzleTypeManager typeManager
		)
			throws PersistenceExceptionTypeNotPersistable
		{
			final PersistenceTypeHandler.Initializer<M, T> typeHandlerInitializer = this.lookupInitializer(type);
			if(typeHandlerInitializer == null)
			{
				throw new RuntimeException(); // (30.03.2013)EXCP: proper exception
			}
			
			/* (30.08.2017 TM)FIXME: typehandler must be
			 * - initialized for the passed typeId
			 * - registered at the typehandlerManager
			 */

			final PersistenceTypeHandler<M, T> typeHandler = typeHandlerInitializer.initializeTypeHandler(typeManager);
			if(typeHandler.type() != type)
			{
				// just in case
				throw new RuntimeException(); // (18.10.2013 TM)EXCP: proper exception
			}

			return typeHandler;
		}

		@Override
		public <D extends PersistenceTypeDictionary> D updateTypeDictionary(final D typeDictionary)
		{
			final BulkList<PersistenceTypeDescription<?>> typeDescs = this.mapping.iterate(
				new TypeDescriptionBuilder<M>(this.typeLookup)
			).yield();
			typeDictionary.registerTypes(typeDescs);
			
			return typeDictionary;
		}

		/* (29.04.2017 TM)FIXME: TypeDescriptionBuilder weird
		 * This is all crazy:
		 * - Why does a TypeDescriptionBuilder live exist in the context of a handler registry?
		 * - Why would a customly handled type have no registered typeId? That is a bug or a faulty initializing order
		 * 
		 */
		static final class TypeDescriptionBuilder<M>
		implements Aggregator<
			KeyValue<Class<?>, PersistenceTypeHandler.Initializer<M, ?>>,
			BulkList<PersistenceTypeDescription<?>>
		>
		{
			///////////////////////////////////////////////////////////////////////////
			// instance fields  //
			/////////////////////

			private final SwizzleTypeLookup                       typeLookup      ;
			private final BulkList<PersistenceTypeDescription<?>> typeDescriptions = BulkList.New();



			///////////////////////////////////////////////////////////////////////////
			// constructors     //
			/////////////////////

			public TypeDescriptionBuilder(final SwizzleTypeLookup typeLookup)
			{
				super();
				this.typeLookup = notNull(typeLookup);
			}

			@Override
			public void accept(final KeyValue<Class<?>, PersistenceTypeHandler.Initializer<M, ?>> element)
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
					element.value().initializeTypeHandler(this.typeLookup)
				);
			}

			@Override
			public BulkList<PersistenceTypeDescription<?>> yield()
			{
				return SwizzleTypeIdOwner.sortByTypeIdAscending(this.typeDescriptions);
			}

		}

	}

}

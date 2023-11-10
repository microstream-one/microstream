package one.microstream.persistence.types;

/*-
 * #%L
 * microstream-persistence
 * %%
 * Copyright (C) 2019 - 2022 MicroStream Software
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

import static one.microstream.X.notNull;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.slf4j.Logger;

import one.microstream.collections.EqHashTable;
import one.microstream.collections.HashEnum;
import one.microstream.collections.HashTable;
import one.microstream.collections.XArrays;
import one.microstream.collections.types.XAddingEnum;
import one.microstream.collections.types.XGettingCollection;
import one.microstream.collections.types.XGettingEnum;
import one.microstream.equality.Equalator;
import one.microstream.persistence.exceptions.PersistenceException;
import one.microstream.persistence.exceptions.PersistenceExceptionConsistency;
import one.microstream.persistence.exceptions.PersistenceExceptionTypeConsistency;
import one.microstream.persistence.exceptions.PersistenceExceptionTypeHandlerConsistency;
import one.microstream.reference.Swizzling;
import one.microstream.reflect.XReflect;
import one.microstream.typing.KeyValue;
import one.microstream.util.logging.Logging;


public interface PersistenceTypeHandlerManager<D> extends PersistenceTypeManager, PersistenceTypeHandlerRegistry<D>
{
	@Override
	public <T> PersistenceTypeHandler<D, ? super T> lookupTypeHandler(T instance);

	@Override
	public <T> PersistenceTypeHandler<D, ? super T> lookupTypeHandler(Class<T> type);

	@Override
	public PersistenceTypeHandler<D, ?> lookupTypeHandler(long typeId);

	public <T> PersistenceTypeHandler<D, ? super T> ensureTypeHandler(T instance);

	public <T> PersistenceTypeHandler<D, ? super T> ensureTypeHandler(Class<T> type);
	
	public <T> PersistenceTypeHandler<D, ? super T> ensureTypeHandler(PersistenceTypeDefinition typeDefinition);
	
	public <T> PersistenceLegacyTypeHandler<D, ? super T> ensureLegacyTypeHandler
	(
		final PersistenceTypeDefinition    legacyTypeDefinition,
		final PersistenceTypeHandler<D, ? super T> currentTypeHandler
	);
	
	public void ensureTypeHandlers(XGettingEnum<PersistenceTypeDefinition> typeDefinitions);

	public void ensureTypeHandlersByTypeIds(XGettingEnum<Long> typeIds);

	public PersistenceTypeHandlerManager<D> initialize();

	public void update(PersistenceTypeDictionary typeDictionary, long highestTypeId);

	public default void update(final PersistenceTypeDictionary typeDictionary)
	{
		this.update(typeDictionary, 0);
	}
	
	public PersistenceTypeDictionary typeDictionary();
	
	@Override
	public long ensureTypeId(Class<?> type);

	@Override
	public Class<?> ensureType(long typeId);
	
	public void validateTypeHandler(PersistenceTypeHandler<D, ?> typeHandler);
	
	public default void validateTypeHandlers(final Iterable<? extends PersistenceTypeHandler<D, ?>> typeHandlers)
	{
		for(final PersistenceTypeHandler<D, ?> typeHandler : typeHandlers)
		{
			this.validateTypeHandler(typeHandler);
		}
	}
	
	public void checkForPendingRootInstances();
	
	public void checkForPendingRootsStoring(PersistenceStoring storingCallback);
	
	public void clearStorePendingRoots();
	
	public default String deriveEnumRootIdentifier(final PersistenceTypeHandler<?, ?> typeHandler)
	{
		return Persistence.deriveEnumRootIdentifier(typeHandler);
	}
	
	public default boolean isEnumRootIdentifier(final String enumRootIdentifier)
	{
		return Persistence.isEnumRootIdentifier(enumRootIdentifier);
	}
	
	public default Long parseEnumRootIdentifierTypeId(final String enumRootIdentifier)
	{
		return Persistence.parseEnumRootIdentifierTypeId(enumRootIdentifier);
	}
	
	public default Object[] collectEnumConstants(final PersistenceTypeHandler<?, ?> typeHandler)
	{
		try
		{
			return typeHandler.collectEnumConstants();
		}
		catch(final Exception e)
		{
			throw new PersistenceException(
				"Enum constants collection failed for type handler " + typeHandler.toRuntimeTypeIdentifier()
			);
		}
	}
	
	public static <D> void registerEnumContantRoots(
		final HashTable<Class<?>, PersistenceTypeHandler<D, ?>> pendingEnumConstantRootStoringHandlers,
		final PersistenceTypeHandler<D, ?>                      typeHandler
	)
	{
		/*
		 * #internalEnsureTypeHandler ensures that every enum sub class causes
		 * a handler to be ensured for the actual declared enum class.
		 */
		if(!XReflect.isDeclaredEnum(typeHandler.type()))
		{
			// nothing to do for non-(top-level-)enums.
			return;
		}
				
		pendingEnumConstantRootStoringHandlers.add(
			typeHandler.type(),
			typeHandler
		);
	}

	public static <D> PersistenceTypeHandlerManager.Default<D> New(
		final PersistenceTypeHandlerRegistry<D>           typeHandlerRegistry          ,
		final PersistenceTypeHandlerProvider<D>           typeHandlerProvider          ,
		final PersistenceTypeDictionaryManager            typeDictionaryManager        ,
		final PersistenceTypeMismatchValidator<D>         typeMismatchValidator        ,
		final PersistenceLegacyTypeMapper<D>              legacyTypeMapper             ,
		final PersistenceUnreachableTypeHandlerCreator<D> unreachableTypeHandlerCreator,
		final PersistenceRootsProvider<D>                 rootsProvider
	)
	{
		final HashTable<Class<?>, PersistenceTypeHandler<D, ?>> pendingEnumConstantRootStoringHandlers = HashTable.New();
		
		// must initially register all enum type handlers to keep the internal state consistent.
		typeHandlerProvider.iterateTypeHandlers(th ->
		{
			registerEnumContantRoots(pendingEnumConstantRootStoringHandlers, th);
		});
		
//		final PersistenceRootResolverProvider rootResolverProvider = rootsInitializer.rootsResolverProvider();
//		pendingEnumConstantRootStoringHandlers.values().iterate(eth ->
//		{
//			final String   identifier    = deriveEnumRootIdentifier(eth);
//			final Object[] enumInstances = collectEnumInstances(eth);
//			rootResolverProvider.registerRoot(identifier, enumInstances);
//		});
		
		return new PersistenceTypeHandlerManager.Default<>(
			notNull(typeHandlerRegistry)          ,
			notNull(typeHandlerProvider)          ,
			notNull(typeDictionaryManager)        ,
			notNull(typeMismatchValidator)        ,
			notNull(legacyTypeMapper)             ,
			notNull(unreachableTypeHandlerCreator),
			notNull(rootsProvider)                ,
			pendingEnumConstantRootStoringHandlers
		);
	}

	public final class Default<D> implements PersistenceTypeHandlerManager<D>
	{
		private final static Logger logger = Logging.getLogger(Default.class);
		
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		        final PersistenceTypeHandlerRegistry<D>           typeHandlerRegistry          ;
		private final PersistenceTypeHandlerProvider<D>           typeHandlerProvider          ;
		private final PersistenceTypeDictionaryManager            typeDictionaryManager        ;
		private final PersistenceTypeMismatchValidator<D>         typeMismatchValidator        ;
		private final PersistenceLegacyTypeMapper<D>              legacyTypeMapper             ;
		private final PersistenceUnreachableTypeHandlerCreator<D> unreachableTypeHandlerCreator;
		private final PersistenceRootsProvider<D>                 rootsProvider                ;
		
		private final HashTable<Class<?>, PersistenceTypeHandler<D, ?>> pendingEnumConstantRootStoringHandlers;
		private transient PersistenceRoots pendingStoreRoot;
		
		private boolean initialized;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
			final PersistenceTypeHandlerRegistry<D>                 typeHandlerRegistry                   ,
			final PersistenceTypeHandlerProvider<D>                 typeHandlerProvider                   ,
			final PersistenceTypeDictionaryManager                  typeDictionaryManager                 ,
			final PersistenceTypeMismatchValidator<D>               typeMismatchValidator                 ,
			final PersistenceLegacyTypeMapper<D>                    legacyTypeMapper                      ,
			final PersistenceUnreachableTypeHandlerCreator<D>       unreachableTypeHandlerCreator         ,
			final PersistenceRootsProvider<D>                       rootsProvider                         ,
			final HashTable<Class<?>, PersistenceTypeHandler<D, ?>> pendingEnumConstantRootStoringHandlers
		)
		{
			super();
			this.typeHandlerRegistry           = typeHandlerRegistry          ;
			this.typeHandlerProvider           = typeHandlerProvider          ;
			this.typeDictionaryManager         = typeDictionaryManager        ;
			this.typeMismatchValidator         = typeMismatchValidator        ;
			this.legacyTypeMapper              = legacyTypeMapper             ;
			this.unreachableTypeHandlerCreator = unreachableTypeHandlerCreator;
			this.rootsProvider                 = rootsProvider                ;
			
			this.pendingEnumConstantRootStoringHandlers = pendingEnumConstantRootStoringHandlers;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
				
		private <T> void recursiveEnsureTypeHandlers(final PersistenceTypeHandler<D, T> typeHandler)
		{
			/* (25.07.2019 TM)TODO: priv#122: potential problems with recursively ensured type handlers for field types
			 * See considerations in BinaryTypeHandlerCreator#createTypeHandlerGeneric
			 * At the very least, instance viability would have to be checked/guaranteed here, BEFORE registering
			 * the created type handler.
			 * Adding it here as a makeshift solution does not prevent the registration of an invalid handler.
			 * The proper solution would be to register any type handler not before all its handled fields
			 * have been checked for properly persistable types (i.e. a proper type handler) and then bulk-register
			 * them all at once.
			 */
			/*
			 * Must ensure type handlers for all field types as well to keep type definitions consistent.
			 * If some field's type is "too abstract" to be persisted, is has to be registered to an
			 * appropriate type handler (No-op, etc.) manually beforehand.
			 *
			 * creating new type handlers in the process will eventually end up here again for the new types
			 * until all reachable types are ensured to have type handlers registered.
			 */
			typeHandler.iterateMemberTypes(t ->
			{
				try
				{
					final PersistenceTypeHandler<?, ?> fieldTypeHandler = this.ensureTypeHandler(t);
					
					// (25.07.2019 TM)NOTE: since fields can reference instances, this must be checked. But see above.
					fieldTypeHandler.guaranteeSubTypeInstanceViablity();
				}
				catch(final RuntimeException e)
				{
					throw e; // debug hook
				}
			});
		}
		
		private void validateTypeHandlerTypeId(final PersistenceTypeHandler<D, ?> typeHandler)
		{
			if(Swizzling.isProperId(typeHandler.typeId()))
			{
				return;
			}
			
			throw new PersistenceExceptionTypeHandlerConsistency(
				"Unassigned TypeId " + PersistenceTypeHandler.class.getSimpleName()
				+ " " + typeHandler.typeName()
			);
		}
		
		@Override
		public final void validateTypeHandler(final PersistenceTypeHandler<D, ?> typeHandler)
		{
			this.validateTypeHandlerTypeId(typeHandler);
			
			final PersistenceTypeDefinition registeredTd;
			synchronized(this.typeHandlerRegistry)
			{
				registeredTd = this.typeDictionaryManager
					.provideTypeDictionary()
					.lookupTypeByName(typeHandler.typeName())
				;
			}
			if(registeredTd == null)
			{
				return; // type not yet registered, hence it can't be invalid
			}
			
			if(typeHandler.typeId() != registeredTd.typeId())
			{
				throw new PersistenceExceptionTypeConsistency(
					"TypeId inconsistency for " + typeHandler.typeName()
					+ ": typeDictionary type definition typeId = " + registeredTd.typeId()
					+ ", validated type handler typeId = " + typeHandler.typeId()
				);
			}

			final Equalator<PersistenceTypeDescriptionMember> memberValidator = (m1, m2) ->
			{
				if(m1 == null || m2 == null)
				{
					throw new PersistenceExceptionTypeConsistency(
						"Member count mismatch of type " + typeHandler.typeName()
					);
				}

				// structure is enough since qualifiers are just required for intra-type identification
				if(m1.equalsStructure(m2))
				{
					return true;
				}
				
				throw new PersistenceExceptionTypeConsistency(
					"Inconsistent member in type description for type "
					+ typeHandler.typeName() + ": " + m1 + " != " + m2
				);
			};

			if(!PersistenceTypeDescriptionMember.equalMembers(registeredTd.allMembers(), typeHandler.allMembers(), memberValidator))
			{
				// throw generic exception in case the equalator returns false instead of throwing an exception
				throw new PersistenceExceptionTypeConsistency("Member inconsistency for " + typeHandler.typeName());
			}
		}

		@Override
		public final <T> PersistenceTypeHandler<D, ? super T> ensureTypeHandler(final T instance)
		{
			// standard implementation does not consider actual objects, only their types
			
			final PersistenceTypeHandler<D, ? super T> typeHandler = this.ensureTypeHandler(
				XReflect.getClass(instance)
			);
			typeHandler.guaranteeSpecificInstanceViablity();
			
			return typeHandler;
		}
		
		@Override
		public PersistenceTypeDictionary typeDictionary()
		{
			return this.typeDictionaryManager.provideTypeDictionary();
		}

		@Override
		public final <T> PersistenceTypeHandler<D, ? super T> ensureTypeHandler(final Class<T> type)
		{
//			XDebug.debugln("ensureTypeHandler(" + type + ")");
			final PersistenceTypeHandler<D, ? super T> handler; // quick read-only check for already registered type
			if((handler = this.typeHandlerRegistry.lookupTypeHandler(type)) != null)
			{
				return handler;
			}
			
			return this.internalEnsureTypeHandler(type);
		}
		
		private <T> Class<T> validateExistingType(final PersistenceTypeDefinition typeDefinition)
		{
			@SuppressWarnings("unchecked") // cast safety is ensured by the type itself and the logic handling it.
			final Class<T> runtimeType = (Class<T>)typeDefinition.type();
			if(runtimeType != null)
			{
				return runtimeType;
			}
			
			throw new PersistenceException(
				"Missing runtime type for required type handler for type: " + typeDefinition.runtimeTypeName()
			);
		}
		
		private <T> PersistenceTypeHandler<D, T> checkForUnreachableType(final PersistenceTypeDefinition typeDef)
		{
			if(typeDef.runtimeTypeName() != null)
			{
				return null;
			}
			
			synchronized(this.typeHandlerRegistry)
			{
				// must check for an already existing type handler before creating a new one
				final PersistenceTypeHandler<D, ?> alreadyRegisteredTypeHandler = this.lookupTypeHandler(typeDef.typeId());
				if(alreadyRegisteredTypeHandler != null)
				{
					@SuppressWarnings("unchecked")
					final PersistenceTypeHandler<D, T> casted = (PersistenceTypeHandler<D, T>)alreadyRegisteredTypeHandler;
					return casted;
				}
				
				final PersistenceUnreachableTypeHandler<D, T> newHandler =
					this.unreachableTypeHandlerCreator.createUnreachableTypeHandler(typeDef)
				;
				this.registerLegacyTypeHandler(newHandler);
				
				return newHandler;
			}
		}
		
		@Override
		public <T> PersistenceTypeHandler<D, ? super T> ensureTypeHandler(final PersistenceTypeDefinition typeDefinition)
		{
			/*
			 * This method must make sure that the passed typeDefinition gets a functional type handler,
			 * which means it must have a non-null runtime type.
			 * Refactoring mappings have already been considered at the type definition's creation time,
			 * meaning if it has no runtime type by now, it is an error. Either a missing refactoring mapping
			 * or maybe even a type that has been deleted in the design without replacement that should not have been.
			 */
						
			final PersistenceTypeHandler<D, T> unreachableHandler = this.checkForUnreachableType(typeDefinition);
			if(unreachableHandler != null)
			{
				return unreachableHandler;
			}
			
			// for all types not explicitly marked as unreachable, the runtime type is essential.
			final Class<T>                             runtimeType        = this.validateExistingType(typeDefinition);
			final PersistenceTypeHandler<D, ? super T> runtimeTypeHandler = this.ensureTypeHandler(runtimeType);
			
			// check if the type definition is up to date or if a legacy type handler is needed
			if(runtimeTypeHandler.typeId() == typeDefinition.typeId())
			{
//				XDebug.println("Up to date type handler : " + typeDefinition.runtimeTypeName());
				return runtimeTypeHandler;
			}
			
//			XDebug.println("Requires legacy type handler : " + typeDefinition.typeName());
			
			// for non-up-to-date type definitions, a legacy type handler must be ensured (looked up or created)
			return this.ensureLegacyTypeHandler(typeDefinition, runtimeTypeHandler);
		}
		
		@Override
		public <T> PersistenceLegacyTypeHandler<D, ? super T> ensureLegacyTypeHandler(
			final PersistenceTypeDefinition    legacyTypeDefinition,
			final PersistenceTypeHandler<D, ? super T> currentTypeHandler
		)
		{
			final PersistenceLegacyTypeHandler<D, ? super T> legacyTypeHandler = this.legacyTypeMapper.ensureLegacyTypeHandler(
				legacyTypeDefinition,
				currentTypeHandler
			);
			this.registerLegacyTypeHandler(legacyTypeHandler);
			
			logger.debug("registered legacy type handler for {} with id {}", legacyTypeHandler.typeName(), legacyTypeHandler.typeId());
			
			return legacyTypeHandler;
		}
				
		@Override
		public void ensureTypeHandlersByTypeIds(final XGettingEnum<Long> typeIds)
		{
			final HashEnum<PersistenceTypeDefinition> resolvedTypeDefinitions = HashEnum.New();
			this.typeDictionaryManager.provideTypeDictionary().resolveTypeIds(typeIds, resolvedTypeDefinitions);
			this.ensureTypeHandlers(resolvedTypeDefinitions);
		}
				
		@Override
		public void ensureTypeHandlers(final XGettingEnum<PersistenceTypeDefinition> typeDefinitions)
		{
			synchronized(this.typeHandlerRegistry)
			{
				typeDefinitions.iterate(typeDefinition ->
					this.ensureTypeHandler(typeDefinition) // debug-friendlier
				);
			}
		}

		@Override
		public final <T> PersistenceTypeHandler<D, ? super T> lookupTypeHandler(final Class<T> type)
		{
			// may not rely on typeHandlerRegistry implementation to be self-synchronized
			synchronized(this.typeHandlerRegistry)
			{
				return this.typeHandlerRegistry.lookupTypeHandler(type);
			}
		}

		@Override
		public final PersistenceTypeHandler<D, ?> lookupTypeHandler(final long typeId)
		{
			// may not rely on typeHandlerRegistry implementation to be self-synchronized
			synchronized(this.typeHandlerRegistry)
			{
				return this.typeHandlerRegistry.lookupTypeHandler(typeId);
			}
		}

		@Override
		public final <T> PersistenceTypeHandler<D, ? super T> lookupTypeHandler(final T instance)
		{
			// may not rely on typeHandlerRegistry implementation to be self-synchronized
			synchronized(this.typeHandlerRegistry)
			{
				// standard implementation does not consider actual objects
				return this.typeHandlerRegistry.lookupTypeHandler(XReflect.getClass(instance));
			}
		}

		@Override
		public final long lookupTypeId(final Class<?> type)
		{
			// may not rely on typeHandlerRegistry implementation to be self-synchronized
			synchronized(this.typeHandlerRegistry)
			{
				return this.typeHandlerRegistry.lookupTypeId(type);
			}
		}

		@Override
		public final <T> Class<T> lookupType(final long typeId)
		{
			// may not rely on typeHandlerRegistry implementation to be self-synchronized
			synchronized(this.typeHandlerRegistry)
			{
				return this.typeHandlerRegistry.lookupType(typeId);
			}
		}
		
		@Override
		public boolean validateTypeMapping(final long typeId, final Class<?> type) throws PersistenceExceptionConsistency
		{
			// may not rely on typeHandlerRegistry implementation to be self-synchronized
			synchronized(this.typeHandlerRegistry)
			{
				return this.typeHandlerRegistry.validateTypeMapping(typeId, type);
			}
		}
		
		@Override
		public boolean validateTypeMappings(final Iterable<? extends PersistenceTypeLink> mappings)
			throws PersistenceExceptionConsistency
		{
			// may not rely on typeHandlerRegistry implementation to be self-synchronized
			synchronized(this.typeHandlerRegistry)
			{
				return this.typeHandlerRegistry.validateTypeMappings(mappings);
			}
		}
				
		@Override
		public boolean registerTypes(final Iterable<? extends PersistenceTypeLink> types)
			throws PersistenceExceptionConsistency
		{
			// may not rely on typeHandlerRegistry implementation to be self-synchronized
			synchronized(this.typeHandlerRegistry)
			{
				return this.typeHandlerRegistry.registerTypes(types);
			}
		}
		
		private <T> PersistenceTypeHandler<D, ? super T> internalEnsureTypeHandler(final Class<T> type)
		{
			/*
			 * Note on super classes and the hiararchy of implemented interface:
			 * Since every class is handled isolated from its super class, it is not necessary to
			 * recursively analyze all super classes, here. It might even be wrong, since a super class
			 * can actually be unpersistable and would throw an exception during analyzing, but an unjustified one
			 * if instances of that super classes would never be persisted by the application.
			 * In short: only concrete classes of to be persisted instances are relevant, not super classes.
			 * Interfaces are only analyzed (as stateless and unpersistably abstract types) if encountered directly,
			 * e.g. as a field type. Regarding implemented interfaces, the same rationale applies as with super classes.
			 * 
			 * (06.04.2020 TM)NOTE: Update:
			 * The above rationale is still true for handling classes specifically.
			 * However, cases like "java.nio.file.Path" made it necessary to introduce a new type handling strategy:
			 * "abstract type" type handlers.
			 * WindowsPath is "too specific". It is a system-local implementation of the "actual main type" Path.
			 * On a Linux system, the class would be another one and persisting and loading "WindowsPath" there
			 * would be an error.
			 * To find the "actual main type", a recursive super type analysis has to be included in the type handler
			 * ensuring logic in the type handler provider.
			 */
			
			synchronized(this.typeHandlerRegistry)
			{
				// tricky: must ensure a handler for the declared enum class for enum sub classes.
				if(XReflect.isEnum(type) && !XReflect.isDeclaredEnum(type))
				{
					this.ensureTypeHandler(XReflect.getDeclaredEnumClass(type));
				}
				
				PersistenceTypeHandler<D, ? super T> typeHandler;
				if((typeHandler = this.typeHandlerRegistry.lookupTypeHandler(type)) == null)
				{
					typeHandler = this.typeHandlerProvider.provideTypeHandler(type);
					this.registerTypeHandler(type, typeHandler);
				}
				
				return typeHandler;
			}
		}
		
		@Override
		public <T> boolean registerTypeHandler(
			final Class<T>                             type       ,
			final PersistenceTypeHandler<D, ? super T> typeHandler
		)
		{
			synchronized(this.typeHandlerRegistry)
			{
				this.validateTypeHandler(typeHandler);
				
				// check for "proper" type handler
				if(type == typeHandler.type())
				{
					if(this.synchUnvalidatedRegisterTypeHandler(typeHandler))
					{
						this.registerEnumContantRoots(typeHandler);
						this.recursiveEnsureTypeHandlers(typeHandler);
						return true;
					}
					return false;
				}
				
				// just a simple "abstract type" type handler mapping
				return this.typeHandlerRegistry.registerTypeHandler(type, typeHandler);
			}
		}
		
		@Override
		public long registerTypeHandlers(final Iterable<? extends PersistenceTypeHandler<D, ?>> typeHandlers)
		{
			synchronized(this.typeHandlerRegistry)
			{
				// validate and register under the same lock
				this.typeHandlerRegistry.validateTypeMappings(typeHandlers);
				
				return this.synchUnvalidatedRegisterTypeHandlers(typeHandlers);
			}
		}

		@Override
		public final <T> boolean registerTypeHandler(final PersistenceTypeHandler<D, T> typeHandler)
		{
			return this.registerTypeHandler(typeHandler.type(), typeHandler);
		}
		
		private final long synchUnvalidatedRegisterTypeHandlers(
			final Iterable<? extends PersistenceTypeHandler<D, ?>> typeHandlers
		)
		{
			final long registrationCount;
			if((registrationCount = this.typeHandlerRegistry.registerTypeHandlers(typeHandlers)) > 0)
			{
				// (up to date) handlers are always the runtime type definitions
				this.typeDictionaryManager.registerRuntimeTypeDefinitions(typeHandlers);
			}
			
			return registrationCount;
		}
		
		private final boolean synchUnvalidatedRegisterTypeHandler(
			final PersistenceTypeHandler<D, ?> typeHandler
		)
		{
			if(this.typeHandlerRegistry.registerTypeHandler(typeHandler))
			{
				// a (up to date) handler is always the runtime type definition
				this.typeDictionaryManager.registerRuntimeTypeDefinition(typeHandler);
				return true;
			}
			
			return false;
		}
					
		private void initialRegisterTypeHandlers(
			final XGettingCollection<PersistenceTypeHandler<D, ?>> initializedTypeHandlers
		)
		{
			// First, pure registration without recursive type analysis calls to maintain the type handler order
			this.registerTypeHandlers(initializedTypeHandlers);
			
			// AFTERWARDS additional management logic like resursive ensuring and enum root registration
			
			// constant roots for enum types must be collected
			for(final PersistenceTypeHandler<D, ?> typeHandler : initializedTypeHandlers)
			{
				this.registerEnumContantRoots(typeHandler);
			}
			
			// recursive registration: initialized handlers themselves plus all handlers required for their field types
			for(final PersistenceTypeHandler<D, ?> typeHandler : initializedTypeHandlers)
			{
				this.recursiveEnsureTypeHandlers(typeHandler);
			}

			// (30.08.2019 TM)NOTE: old before enum root constants and overhaul:
			
//			// set initialized handlers as runtime definitions
//			this.typeDictionaryManager.registerRuntimeTypeDefinitions(initializedTypeHandlers);
//
//			// register all type handlers at the registry
//			for(final PersistenceTypeHandler<D, ?> typeHandler : initializedTypeHandlers)
//			{
//				this.typeHandlerRegistry.registerTypeHandler(typeHandler);
//			}
//
//			// recursive registration: initialized handlers themselves plus all handlers required for their field types
//			for(final PersistenceTypeHandler<D, ?> typeHandler : initializedTypeHandlers)
//			{
//				this.recursiveEnsureTypeHandlers(typeHandler);
//			}
		}
		
		@Override
		public void checkForPendingRootInstances()
		{
			if(this.pendingEnumConstantRootStoringHandlers.isEmpty())
			{
				return;
			}
			
			synchronized(this.typeHandlerRegistry)
			{
				// check again after acquiring the lock
				if(this.pendingEnumConstantRootStoringHandlers.isEmpty())
				{
					return;
				}
				
				final PersistenceRoots existingRoots = this.rootsProvider.peekRoots();
				if(existingRoots == null)
				{
					return;
				}
				
				final EqHashTable<String, Object> modifiedRootEntries = EqHashTable.New(existingRoots.entries());
				boolean modified = false;
				
				for(final PersistenceTypeHandler<D, ?> typeHandler : this.pendingEnumConstantRootStoringHandlers.values())
				{
					final String enumRootIdentifier = this.deriveEnumRootIdentifier(typeHandler);
					final Object enumRootEntry      = modifiedRootEntries.get(enumRootIdentifier);
					if(enumRootEntry != null)
					{
						this.validateEnumInstances(enumRootEntry, typeHandler);
						return;
					}
					
					final Object[] enumRootEntries = this.collectEnumConstants(typeHandler);
					if(enumRootEntries == null)
					{
						throw new PersistenceException("Discarded enum constants cannot be registered as roots.");
					}
					
					modifiedRootEntries.add(enumRootIdentifier, enumRootEntries);
					modified = true;
				}
				
				if(modified)
				{
					// No change state modification! Important for initialization!
					existingRoots.reinitializeEntries(modifiedRootEntries);
					this.pendingStoreRoot = existingRoots;
				}
			}
		}
		
		private void validateEnumInstances(
			final Object                       existingEntry,
			final PersistenceTypeHandler<D, ?> typeHandler
		)
		{
			if(!(existingEntry instanceof Object[]))
			{
				throw new PersistenceException(
					"Invalid root instance of type " + existingEntry.getClass().getName()
					+ " for enum type entry " + this.deriveEnumRootIdentifier(typeHandler)
					+ " of type " + typeHandler.type().getName()
				);
			}
			
			// reference/identity comparison is important! Arrays#equals does it wrong.
			if(!XArrays.equals((Object[])existingEntry, typeHandler.type().getEnumConstants()))
			{
				throw new PersistenceException(
					"Root entry already exists with inconsistent enum constants"
					+ " for enum type entry " + this.deriveEnumRootIdentifier(typeHandler)
					+ " of type " + typeHandler.type().getName()
				);
			}
			
			// reaching here (returning) means the entry is valid.
		}
		
		@Override
		public void checkForPendingRootsStoring(final PersistenceStoring storingCallback)
		{
			if(this.pendingStoreRoot == null)
			{
				// nothing pending
				return;
			}
			
			storingCallback.store(this.pendingStoreRoot);
		}
		
		@Override
		public void clearStorePendingRoots()
		{
			// (30.08.2019 TM)NOTE: wasn't this missing?
			this.pendingEnumConstantRootStoringHandlers.clear();
			
			// pendingEnumConstantRootStoringHandlers is stored by synching logic
			this.pendingStoreRoot = null;
		}
				
		private void registerEnumContantRoots(final PersistenceTypeHandler<D, ?> typeHandler)
		{
			synchronized(this.typeHandlerRegistry)
			{
				// might fail if meanwhile already added. Should not happen, but who knows ...
				PersistenceTypeHandlerManager.registerEnumContantRoots(
					this.pendingEnumConstantRootStoringHandlers,
					typeHandler
				);
			}
		}
				
		@Override
		public final boolean registerLegacyTypeHandler(final PersistenceLegacyTypeHandler<D, ?> legacyTypeHandler)
		{
			synchronized(this.typeHandlerRegistry)
			{
				this.validateTypeHandlerTypeId(legacyTypeHandler);
				return this.typeHandlerRegistry.registerLegacyTypeHandler(legacyTypeHandler);
			}
		}

		@Override
		public final long ensureTypeId(final Class<?> type)
		{
			synchronized(this.typeHandlerRegistry)
			{
				/* If the type handler is currently being created, its type<->tid mapping is created in advance
				 * to be available here without calling the handler creation recurringly.
				 */
				final long typeId;
				if(Swizzling.isFoundId(typeId = this.typeHandlerRegistry.lookupTypeId(type)))
				{
					// already present/found typeId is returned.
					return typeId;
				}
				
				// typeId not found, so a new handler is ensured and its typeId returned.
				return this.ensureTypeHandler(type).typeId();
			}
		}

		@Override
		public final long currentTypeId()
		{
			synchronized(this.typeHandlerRegistry)
			{
				return this.typeHandlerProvider.currentTypeId();
			}
		}

		@Override
		public final Class<?> ensureType(final long typeId)
		{
			synchronized(this.typeHandlerRegistry)
			{
				final Class<?> type;
				if((type = this.typeHandlerRegistry.lookupType(typeId)) != null)
				{
					return type;
				}
				return this.ensureTypeHandler(typeId).type();
			}
		}

		@Override
		public final boolean registerType(final long tid, final Class<?> type) throws PersistenceExceptionConsistency
		{
			synchronized(this.typeHandlerRegistry)
			{
				// if the passed type is new, ensure a handler for it as well
				if(this.typeHandlerRegistry.registerType(tid, type))
				{
					this.ensureTypeHandler(type);
					return true;
				}
				return false;
			}
		}

		@Override
		public <C extends Consumer<? super PersistenceTypeHandler<D, ?>>> C iterateTypeHandlers(
			final C iterator
		)
		{
			synchronized(this.typeHandlerRegistry)
			{
				this.typeHandlerRegistry.iterateTypeHandlers(iterator);
			}
			
			return iterator;
		}
		
		@Override
		public <C extends Consumer<? super PersistenceLegacyTypeHandler<D, ?>>> C iterateLegacyTypeHandlers(
			final C iterator
		)
		{
			synchronized(this.typeHandlerRegistry)
			{
				this.typeHandlerRegistry.iterateLegacyTypeHandlers(iterator);
			}
			
			return iterator;
		}

		@Override
		public final synchronized PersistenceTypeHandlerManager<D> initialize()
		{
			logger.info("Initializing type handler manager");
			
			if(this.initialized)
			{
//				XDebug.debugln("already initialized");
				return this;
			}

			synchronized(this.typeHandlerRegistry)
			{
				this.synchInternalInitialize();
			}
			
			return this;
		}

		private void synchInternalInitialize()
		{
			final PersistenceTypeDictionary typeDictionary = this.typeDictionaryManager.provideTypeDictionary();
			
			final HashEnum<PersistenceTypeHandler<D, ?>> newTypeHandlers            = HashEnum.New();
			final HashEnum<PersistenceTypeHandler<D, ?>> typeRegisteredTypeHandlers = HashEnum.New();
			
			// either fill/initialize an empty type dictionary or initialize from a non-empty dictionary.
			if(typeDictionary.isEmpty())
			{
				logger.info("Type dictionary is empty or not existing. Initializing type handler manager with a new default type dictionary");
				this.synchInitializeBlank(newTypeHandlers);
			}
			else
			{
				this.synchInitializeFromDictionary(typeDictionary, newTypeHandlers, typeRegisteredTypeHandlers);
			}
			
			// "new" type Handlers are either generated ones for a blank or misfits for an existing type dictionary.
			this.synchInitializeNewTypeHandlers(newTypeHandlers, typeRegisteredTypeHandlers);
			
			// after all type handler initialization and typeId registration was successful, register all type handlers.
			this.initialRegisterTypeHandlers(typeRegisteredTypeHandlers);
						
			// call to create definedRoots
			this.rootsProvider.provideRoots();
			
			// update definedRoots with pending enum roots. NO store here. See EmbeddedStorageManager#initialize
			this.checkForPendingRootInstances();
			
			/*
			 * Tricky:
			 * Must clear the pending store roots after reinitializing the definedRoots entries.
			 * Using the intrinsiv pending store roots on-demand storing would cause them to get stored twice,
			 * since every story does the check implicitely.
			 * Executing an empty store is not possible.
			 * And storing a dummy just to get the on-demand storing in is dopey.
			 * Cleaner is:
			 * Only use the pending mechanism to update the definedRoots and store explicitely what is required.
			 */
			this.clearStorePendingRoots();
			
			// after that, the initialization is complete and marked accordingly.
			this.initialized = true;
		}
		
		private void synchTypeRegisterInitializedTypeHandlers(
			final XGettingEnum<PersistenceTypeHandler<D, ?>> typeUnregisteredInitializedTypeHandlers,
			final XAddingEnum<PersistenceTypeHandler<D, ?>>  typeRegisteredInitializedTypeHandlers
		)
		{
			// register the matched Type<->TypeId mappings
			this.typeHandlerRegistry.registerTypes(typeUnregisteredInitializedTypeHandlers);
			typeRegisteredInitializedTypeHandlers.addAll(typeUnregisteredInitializedTypeHandlers);
		}
		
		private void synchInitializeBlank(final HashEnum<PersistenceTypeHandler<D, ?>> newTypeHandlers)
		{
			this.typeHandlerProvider.iterateTypeHandlers(newTypeHandlers);
		}
		
		private void synchInitializeFromDictionary(
			final PersistenceTypeDictionary                 typeDictionary            ,
			final HashEnum<PersistenceTypeHandler<D, ?>>    newTypeHandlers           ,
			final XAddingEnum<PersistenceTypeHandler<D, ?>> typeRegisteredTypeHandlers
		)
		{
			final HashEnum<PersistenceTypeHandler<D, ?>> initializedMatchingTypeHandlers = HashEnum.New();
			final HashEnum<PersistenceTypeLineage> runtimeTypeLineages = HashEnum.New();
			
			this.filterRuntimeTypeLineages(typeDictionary, runtimeTypeLineages);
			
			final HashTable<PersistenceTypeDefinition, PersistenceTypeHandler<D, ?>> matches = HashTable.New();

			// derive a type handler for every runtime type lineage and try to match an existing type definition
			for(final PersistenceTypeLineage typeLineage : runtimeTypeLineages)
			{
				this.synchDeriveRuntimeTypeHandler(typeLineage, matches, newTypeHandlers);
			}
			
			// pass all misfits and the typeDictionary to a PersistenceTypeMismatchEvaluator
			this.typeMismatchValidator.validateTypeMismatches(typeDictionary, newTypeHandlers);
			
			// internally update the current hightest type id, including non-runtime types.
			this.internalUpdateCurrentHighestTypeId(typeDictionary);
			
			// initialize all matches to the associated TypeId
			for(final KeyValue<PersistenceTypeDefinition, PersistenceTypeHandler<D, ?>> match : matches)
			{
				final long typeId = match.key().typeId();
				final PersistenceTypeHandler<D, ?> ith = match.value().initialize(typeId);
				initializedMatchingTypeHandlers.add(ith);
			}
			
			/*
			 * must register the matching type handlers' type-mappings BEFORE initializing the new type handlers
			 * As initializing the new type handlers might ensure a super type's typeId.
			 */
			this.synchTypeRegisterInitializedTypeHandlers(initializedMatchingTypeHandlers, typeRegisteredTypeHandlers);
		}
				
		private void synchInitializeNewTypeHandlers(
			final XGettingCollection<PersistenceTypeHandler<D, ?>> newTypeHandlers,
			final HashEnum<PersistenceTypeHandler<D, ?>>           typeRegisteredTypeHandlers
		)
		{
			final HashEnum<PersistenceTypeHandler<D, ?>> initializedNewTypeHandlers = HashEnum.New();
			
			// assign new TypeIds to all misfits (TypeIds of matching type handlers must already be registered!)
			for(final PersistenceTypeHandler<D, ?> newTypeHandler : newTypeHandlers)
			{
				// native handlers (e.g. see in class BinaryPersistence) already have their TypeId, even if "new".
				final PersistenceTypeHandler<D, ?> ith = this.synchEnsureInitializedTypeHandler(newTypeHandler);
				initializedNewTypeHandlers.add(ith);
			}

			// register TypeId mappings of all successfully initialized new type handlers.
			this.synchTypeRegisterInitializedTypeHandlers(initializedNewTypeHandlers, typeRegisteredTypeHandlers);
		}
		
		private PersistenceTypeHandler<D, ?> synchEnsureInitializedTypeHandler(
			final PersistenceTypeHandler<D, ?> typeHandler
		)
		{
			if(Swizzling.isProperId(typeHandler.typeId()))
			{
				return typeHandler;
			}

			// must be the TypeHandlerProvider's ensureTypeId in order to circumvent implicit handler creation.
			final long newTypeId = this.typeHandlerProvider.ensureTypeId(typeHandler.type());
			return typeHandler.initialize(newTypeId);
		}
				
		private void filterRuntimeTypeLineages(
			final PersistenceTypeDictionary        typeDictionary     ,
			final HashEnum<PersistenceTypeLineage> runtimeTypeLineages
		)
		{
			typeDictionary.iterateTypeLineages(td ->
			{
				if(td.type() != null)
				{
					runtimeTypeLineages.add(td);
				}
			});
		}
		
		private <T> void synchDeriveRuntimeTypeHandler(
			final PersistenceTypeLineage                                             typeLineage            ,
			final HashTable<PersistenceTypeDefinition, PersistenceTypeHandler<D, ?>> matchedTypeHandlers    ,
			final HashEnum<PersistenceTypeHandler<D, ?>>                             unmatchableTypeHandlers
		)
		{
			final Class<?> runtimeType = typeLineage.type();
			if(runtimeType == null)
			{
				/*
				 * Type lineage has no runtime type, so there can't be a runtime type handler derived for it.
				 * This does not have to be an error. If the type lineage represents an outdated type that is not
				 * used anymore, it can remain without runtime type. The ensuring of required runtime type handlers
				 * later in the initialization will validate that.
				 */
				return;
			}
			
			final PersistenceTypeHandler<D, ?> handler = this.synchAdvanceEnsureTypeHandler(runtimeType);
						
			for(final PersistenceTypeDefinition typeDefinition : typeLineage.entries().values())
			{
				// exact matching structure, including order, of ALL members, but no qualifier matching.
				final boolean isMatched = PersistenceTypeDescriptionMember.equalStructures(
					handler.allMembers(),
					typeDefinition.allMembers()
				);
				
				if(isMatched)
				{
					// matching definition found, register and abort matching.
					matchedTypeHandlers.add(typeDefinition, handler);
					return;
				}
			}

			// no matching definition found
			unmatchableTypeHandlers.add(handler);
		}
		
		private <T> PersistenceTypeHandler<D, ? super T> synchAdvanceEnsureTypeHandler(final Class<T> type)
		{
			PersistenceTypeHandler<D, ? super T> handler;
			if((handler = this.typeHandlerRegistry.lookupTypeHandler(type)) == null)
			{
				handler = this.typeHandlerProvider.ensureTypeHandler(type);
			}
			
			return handler;
		}

		@Override
		public void updateCurrentHighestTypeId(final long highestTypeId)
		{
			synchronized(this.typeHandlerRegistry)
			{
				this.typeHandlerProvider.updateCurrentHighestTypeId(highestTypeId);
			}
		}
		
		final void internalUpdateCurrentHighestTypeId(final PersistenceTypeDictionary typeDictionary)
		{
			this.internalUpdateCurrentHighestTypeId(typeDictionary.determineHighestTypeId());
		}
		
		final void internalUpdateCurrentHighestTypeId(
			final PersistenceTypeDictionary typeDictionary,
			final long                      highestTypeId
		)
		{
			final long effectiveHighestTypeId = Math.max(typeDictionary.determineHighestTypeId(), highestTypeId);
			this.updateCurrentHighestTypeId(effectiveHighestTypeId);
		}
		
		final void internalUpdateCurrentHighestTypeId(final long highestTypeId)
		{
			// update the highest type id first after validation has been passed successfully to guarantee consistency
			if(this.currentTypeId() < highestTypeId)
			{
				// only update if new value is actually higher. No reason to throw an exception otherwise.
				this.updateCurrentHighestTypeId(highestTypeId);
			}
		}

		@Override
		public void update(final PersistenceTypeDictionary typeDictionary, final long highestTypeId)
		{
			synchronized(this.typeHandlerRegistry)
			{
				this.typeDictionaryManager.validateTypeDefinitions(typeDictionary.allTypeDefinitions().values());

				this.internalUpdateCurrentHighestTypeId(typeDictionary, highestTypeId);

				// finally add the type descriptions
				this.typeDictionaryManager.registerTypeDefinitions(typeDictionary.allTypeDefinitions().values());
			}
		}

		@Override
		public void iteratePerIds(final BiConsumer<Long, ? super Class<?>> consumer)
		{
			this.typeHandlerRegistry.iteratePerIds(consumer);
		}

	}

}

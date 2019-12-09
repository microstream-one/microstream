package one.microstream.persistence.types;

import static one.microstream.X.notNull;

import java.lang.reflect.Field;
import java.util.function.Function;
import java.util.function.Supplier;

import one.microstream.collections.EqConstHashTable;
import one.microstream.collections.EqHashEnum;
import one.microstream.collections.EqHashTable;
import one.microstream.collections.types.XGettingEnum;
import one.microstream.collections.types.XGettingTable;
import one.microstream.collections.types.XTable;
import one.microstream.persistence.exceptions.PersistenceException;
import one.microstream.reference.Reference;
import one.microstream.reflect.XReflect;
import one.microstream.typing.KeyValue;

public interface PersistenceRootResolver
{
	public String defaultRootIdentifier();
	
	public Reference<Object> defaultRoot();
	
	public String customRootIdentifier();
	
	public PersistenceRootEntry customRootEntry();
	
	public PersistenceRootEntry resolveRootInstance(String identifier);
	
	public XGettingTable<String, PersistenceRootEntry> definedEntries();
	
	public default XGettingTable<String, PersistenceRootEntry> resolveRootEntries(
		final XGettingEnum<String> identifiers
	)
	{
		final EqHashTable<String, PersistenceRootEntry> resolvedRoots         = EqHashTable.New();
		final EqHashEnum<String>                        unresolvedIdentifiers = EqHashEnum.New();
		
		synchronized(this)
		{
			for(final String identifier : identifiers)
			{
				final PersistenceRootEntry resolvedRootEntry = this.resolveRootInstance(identifier);
				if(resolvedRootEntry != null)
				{
					resolvedRoots.add(identifier, resolvedRootEntry);
				}
				else
				{
					unresolvedIdentifiers.add(identifier);
				}
			}
			
			if(!unresolvedIdentifiers.isEmpty())
			{
				// (19.04.2018 TM)EXCP: proper exception
				throw new PersistenceException(
					"The following root identifiers cannot be resolved: " + unresolvedIdentifiers
				);
			}
		}
		
		return resolvedRoots;
	}
	
	public default XGettingTable<String, Object> resolveDefinedRootInstances()
	{
		return this.resolveRootInstances(this.definedEntries());
	}
	
	public default XTable<String, Object> resolveRootInstances(
		final XGettingTable<String, PersistenceRootEntry> entries
	)
	{
		final EqHashTable<String, Object> resolvedRoots = EqHashTable.New();
		
		for(final PersistenceRootEntry entry : entries.values())
		{
			// may be null if explicitely removed
			final Object rootInstance = entry.instance();
			resolvedRoots.add(entry.identifier(), rootInstance);
		}
		
		return resolvedRoots;
	}

	
	
	public static XGettingTable<String, Supplier<?>> deriveRoots(final Class<?>... types)
	{
		return deriveRoots(XReflect::deriveFieldIdentifier, types);
	}
	
	public static XGettingTable<String, Supplier<?>> deriveRoots(
		final Function<Field, String> rootIdentifierDeriver,
		final Class<?>...             types
	)
	{
		final EqHashTable<String, Supplier<?>> roots = EqHashTable.New();
		
		addRoots(roots, rootIdentifierDeriver, types);
		
		return roots;
	}
	
	public static void addRoots(
		final EqHashTable<String, Supplier<?>> roots                ,
		final Function<Field, String>          rootIdentifierDeriver,
		final Class<?>...                      types
	)
	{
		for(final Class<?> type : types)
		{
			addRoots(roots, rootIdentifierDeriver, type);
		}
	}
	
	public static void addRoots(
		final EqHashTable<String, Supplier<?>> roots                ,
		final Function<Field, String>          rootIdentifierDeriver,
		final Class<?>                         type
	)
	{
		for(final Field field : type.getDeclaredFields())
		{
			/*
			 * better not trust custom predicates:
			 * - field MUST be static, otherwise no instance can be safely retrieved in a static way.
			 * - field MUST be a reference field, because registering primitives is neither possible nor reasonable.
			 */
			if(!XReflect.isStatic(field) || !XReflect.isReference(field))
			{
				continue;
			}
			
			// (04.05.2018 TM)TODO: proper solution for synthetic hacky fields
			/* (04.05.2018 TM)NOTE:
			 * Quick and dirty hotfix (out of pure anger) for the synthetic switch table thingy
			 * when using enum instances as keys in a switch. Apart from the geniuses not having made
			 * Modifier#isSynthetic public (because hey, why would a user of the JDK want to recognize and filter out
			 * their hacky stuff right? Morons), it wouldn't be wise to simply filter out ALL synthetic fields, since
			 * for example ENUM$VALUES can very well be relevant for persistence.
			 * For now, using the ugly plain string is a quick solution. Let's see when they change something
			 * that breaks this fix.
			 */
			if(field.getName().startsWith("$SWITCH_TABLE"))
			{
				continue;
			}
			
			final String rootIdentifier = rootIdentifierDeriver.apply(field);
			if(rootIdentifier == null)
			{
				// the deriver function also serves as a predicate: if it returns null, the field shall be skipped.
				continue;
			}
			
			field.setAccessible(true);
			
			/*
			 * The static field gets registered for the derived identifier.
			 * The Supplier indirection prevents class initialization loops.
			 * Lambda line break for debuggability.
			 */
			roots.add(rootIdentifier, () ->
				XReflect.getFieldValue(field, null)
			);
		}
	}
	
	/**
	 * Central wrapping method mosty to have a unified and concisely named location for the lambda.
	 * 
	 * @param customRootInstance the instance to be used as the entity graph's root.
	 * 
	 * @return a {@link Supplier} returning the passed {@literal customRootInstance} instance.
	 */
	public static Supplier<?> wrapCustomRoot(final Object customRootInstance)
	{
		notNull(customRootInstance);
		return () ->
			customRootInstance
		;
	}
	
	
	public final class Default implements PersistenceRootResolver
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final String                                                defaultRootIdentifier      ;
		private final Reference<Object>                                     defaultRoot                ;
		private final String                                                customRootIdentifier       ;
		private final EqConstHashTable<String, PersistenceRootEntry>        definedRootEntries         ;
		private final Reference<? extends PersistenceTypeHandlerManager<?>> referenceTypeHandlerManager;
		
		private transient PersistenceTypeHandlerManager<?> cachedTypeHandlerManager;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
			final String                                                defaultRootIdentifier      ,
			final Reference<Object>                                     defaultRoot                ,
			final String                                                customRootIdentifier       ,
			final EqConstHashTable<String, PersistenceRootEntry>        definedRootEntries         ,
			final Reference<? extends PersistenceTypeHandlerManager<?>> referenceTypeHandlerManager
		)
		{
			super();
			this.defaultRootIdentifier       = defaultRootIdentifier      ;
			this.customRootIdentifier        = customRootIdentifier       ;
			this.defaultRoot                 = defaultRoot                ;
			this.definedRootEntries          = definedRootEntries         ;
			this.referenceTypeHandlerManager = referenceTypeHandlerManager;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		private PersistenceTypeHandlerManager<?> typeHandlerManager()
		{
			if(this.cachedTypeHandlerManager == null)
			{
				this.cachedTypeHandlerManager = this.referenceTypeHandlerManager.get();
			}
			
			return this.cachedTypeHandlerManager;
		}
								
		@Override
		public final PersistenceRootEntry resolveRootInstance(final String identifier)
		{
			final PersistenceRootEntry rootEntry = this.definedRootEntries.get(identifier);
			if(rootEntry != null)
			{
				// directly registered / "normal" root entries are returned right away.
				return rootEntry;
			}
			
			return this.resolveRootEnumConstants(identifier);
		}
		
		private PersistenceRootEntry resolveRootEnumConstants(final String identifier)
		{
			final PersistenceTypeHandlerManager<?> typeHandlerManager = this.typeHandlerManager();
			
			final Long enumTypeId = typeHandlerManager.parseEnumRootIdentifierTypeId(identifier);
			if(enumTypeId == null)
			{
				// neither direct nor enum entry. Unrecognizable, return null.
				return null;
			}
			
			final PersistenceTypeHandler<?, ?> enumTypeHandler = typeHandlerManager.lookupTypeHandler(
				enumTypeId.longValue()
			);
			if(enumTypeHandler == null)
			{
				// (13.08.2019 TM)EXCP: proper exception
				throw new PersistenceException("Unknown TypeId: " + enumTypeId);
			}
			
			// Checks for enum type internally. May be null for discarded (i.e. legacy) enums.
			final Object[] enumConstants = typeHandlerManager.collectEnumConstants(enumTypeHandler);
						
			return PersistenceRootEntry.New(identifier, () ->
				enumConstants // debuggability line break, do not remove!
			);
		}

		@Override
		public String defaultRootIdentifier()
		{
			return this.defaultRootIdentifier;
		}
		
		@Override
		public Reference<Object> defaultRoot()
		{
			return this.defaultRoot;
		}
		
		@Override
		public String customRootIdentifier()
		{
			return this.customRootIdentifier;
		}
		
		@Override
		public PersistenceRootEntry customRootEntry()
		{
			return this.definedEntries().get(this.customRootIdentifier);
		}
		

		@Override
		public XGettingTable<String, PersistenceRootEntry> definedEntries()
		{
			return this.definedRootEntries;
		}
		
	}
	
	
	
	public static PersistenceRootResolver Wrap(
		final PersistenceRootResolver                    actualRootResolver        ,
		final PersistenceTypeDescriptionResolverProvider refactoringMappingProvider
	)
	{
		return new MappingWrapper(actualRootResolver, refactoringMappingProvider);
	}
	
	public final class MappingWrapper implements PersistenceRootResolver
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		final PersistenceRootResolver                    actualRootResolver        ;
		final PersistenceTypeDescriptionResolverProvider refactoringMappingProvider;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		MappingWrapper(
			final PersistenceRootResolver                actualRootResolver        ,
			final PersistenceTypeDescriptionResolverProvider refactoringMappingProvider
		)
		{
			super();
			this.actualRootResolver         = actualRootResolver        ;
			this.refactoringMappingProvider = refactoringMappingProvider;
		}


		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public XGettingTable<String, PersistenceRootEntry> definedEntries()
		{
			return this.actualRootResolver.definedEntries();
		}

		@Override
		public PersistenceRootEntry resolveRootInstance(final String identifier)
		{
			/*
			 * Mapping lookups take precedence over the direct resolving attempt.
			 * This is important to enable refactorings that switch names.
			 * E.g.:
			 * A -> B
			 * C -> A
			 * However, this also increases the responsibility of the developer who defines the mapping:
			 * The mapping has to be removed after the first usage, otherwise the new instance under the old name
			 * is mapped to the old name's new name, as well. (In the example: after two executions, both instances
			 * would be mapped to B, which is an error. However, the source of the error is not a bug,
			 * but an outdated mapping rule defined by the using developer).
			 */
			final PersistenceTypeDescriptionResolver resolver = this.refactoringMappingProvider.provideTypeDescriptionResolver();
			
			final String sourceIdentifier = PersistenceMetaIdentifiers.normalizeIdentifier(identifier);
			
			final KeyValue<String, String> mapping = resolver.lookup(sourceIdentifier);
			if(mapping == null)
			{
				// simple case: no mapping found, use (normalized) source identifier directly.
				return this.actualRootResolver.resolveRootInstance(sourceIdentifier);
			}
			
			final String targetIdentifier = PersistenceMetaIdentifiers.normalizeIdentifier(mapping.value());
			
			/*
			 * special case: an explicit mapping entry for the (normalized) sourceIdentifier exists,
			 * but its target is null. This means the sourceIdentifier represents an old root entry
			 * that has been mapped as deleted by the user developer.
			 */
			if(targetIdentifier == null)
			{
				// mark removed entry
				return PersistenceRootEntry.New(sourceIdentifier, null);
			}
			
			// normal case: the sourceIdentifier has been mapped to a non-null targetIdentifier. So resolve it.
			final PersistenceRootEntry mappedEntry = this.actualRootResolver.resolveRootInstance(targetIdentifier);
			
			// but there is a catch: an unresolveable explicitly provided targetIdentifier is an error.
			if(mappedEntry == null)
			{
				// (19.04.2018 TM)EXCP: proper exception
				throw new PersistenceException(
					"Refactoring mapping target identifier cannot be resolved: " + targetIdentifier
				);
			}
			
			return mappedEntry;
		}
		
		@Override
		public String defaultRootIdentifier()
		{
			return this.actualRootResolver.defaultRootIdentifier();
		}

		@Override
		public Reference<Object> defaultRoot()
		{
			return this.actualRootResolver.defaultRoot();
		}

		@Override
		public String customRootIdentifier()
		{
			return this.actualRootResolver.customRootIdentifier();
		}

		@Override
		public PersistenceRootEntry customRootEntry()
		{
			return this.actualRootResolver.customRootEntry();
		}
				
	}
	
}

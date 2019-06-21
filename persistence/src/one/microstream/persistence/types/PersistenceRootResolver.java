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
	
	public XGettingTable<String, PersistenceRootEntry> entries();
	
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
				throw new RuntimeException(
					"The following root identifiers cannot be resolved: " + unresolvedIdentifiers
				);
			}
		}
		
		return resolvedRoots;
	}
	
	public default XTable<String, Object> resolveRootInstances()
	{
		return this.resolveRootInstances(this.entries());
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
	


	
	public static PersistenceRootResolver.Builder Builder(
		final String      customRootIdentifier,
		final Supplier<?> customRootSupplier
	)
	{
		return Builder(PersistenceRootEntry::New, customRootIdentifier, customRootSupplier);
	}
	
	public static PersistenceRootResolver.Builder Builder(
		final PersistenceRootEntry.Provider entryProvider       ,
		final String                        customRootIdentifier,
		final Supplier<?>                   customRootSupplier
	)
	{
		final PersistenceRootResolver.Builder.Default builder = new PersistenceRootResolver.Builder.Default(
			notNull(entryProvider)
		);
		
		return builder.registerCustomRoot(customRootIdentifier, customRootSupplier);
	}
		
	public static PersistenceRootResolver New(
		final String      customRootIdentifier,
		final Supplier<?> customRootSupplier
	)
	{
		return Builder(customRootIdentifier, customRootSupplier)
			.build()
		;
	}
	
	public final class Default implements PersistenceRootResolver
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		

		private final String                                         defaultRootIdentifier;
		private final String                                         customRootIdentifier ;
		private final Reference<Object>                              defaultRoot          ;
		private final EqConstHashTable<String, PersistenceRootEntry> rootEntries          ;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
			final String                                         defaultRootIdentifier,
			final String                                         customRootIdentifier ,
			final Reference<Object>                              defaultRoot          ,
			final EqConstHashTable<String, PersistenceRootEntry> rootEntries
		)
		{
			super();
			this.defaultRootIdentifier = defaultRootIdentifier;
			this.customRootIdentifier  = customRootIdentifier ;
			this.defaultRoot           = defaultRoot          ;
			this.rootEntries           = rootEntries          ;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
								
		@Override
		public final PersistenceRootEntry resolveRootInstance(final String identifier)
		{
			return this.rootEntries.get(identifier);
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
			return this.entries().get(this.customRootIdentifier);
		}
		

		@Override
		public XGettingTable<String, PersistenceRootEntry> entries()
		{
			return this.rootEntries;
		}
		
		// (17.06.2019 TM)FIXME: /!\ MS-139: remove when refactored
//		@Override
//		public final XGettingTable<String, Object> getRootInstances()
//		{
//			final EqHashTable<String, Object> rootInstances = EqHashTable.New();
//
//			for(final PersistenceRootEntry entry : this.rootEntries.values())
//			{
//				rootInstances.add(entry.identifier(), entry.instance());
//			}
//
//			return rootInstances;
//		}

	}
	
	
	public static PersistenceRootResolver Wrap(
		final PersistenceRootResolver                actualRootResolver,
		final PersistenceRefactoringResolverProvider refactoringMappingProvider
	)
	{
		return new MappingWrapper(actualRootResolver, refactoringMappingProvider);
	}
	
	public final class MappingWrapper implements PersistenceRootResolver
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		final PersistenceRootResolver                actualRootResolver        ;
		final PersistenceRefactoringResolverProvider refactoringMappingProvider;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		MappingWrapper(
			final PersistenceRootResolver                actualRootResolver        ,
			final PersistenceRefactoringResolverProvider refactoringMappingProvider
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
		public XGettingTable<String, PersistenceRootEntry> entries()
		{
			return this.actualRootResolver.entries();
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
			final PersistenceRefactoringResolver resolver         = this.refactoringMappingProvider.provideResolver();
			final String                         sourceIdentifier = PersistenceMetaIdentifiers.normalizeIdentifier(
				identifier
			);
			
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
				throw new RuntimeException(
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

	
	
	public interface Builder
	{
		public Builder registerDefaultRoot(String defaultRootIdentifier, Reference<Object> defaultRoot);
		
		public Builder registerCustomRoot(String customRootIdentifier, Supplier<?> instanceSupplier);
		
		public Builder registerRoot(String identifier, Supplier<?> instanceSupplier);
		
		public default Builder registerRoots(final XGettingTable<String, Supplier<?>> roots)
		{
			synchronized(this)
			{
				roots.iterate(kv ->
					this.registerRoot(kv.key(), kv.value())
				);
			}
			return this;
		}
		
		public default Builder registerRoot(final String identifier, final Object instance)
		{
			return this.registerRoot(identifier, () -> instance);
		}
		
		public Builder setRefactoring(PersistenceRefactoringResolverProvider refactoring);
		
		public Builder setRefactoring(PersistenceRefactoringMappingProvider refactoringMapping);
		
				
		public PersistenceRootResolver build();
		
		public final class Default implements PersistenceRootResolver.Builder
		{
			///////////////////////////////////////////////////////////////////////////
			// instance fields //
			////////////////////
			
			private final PersistenceRootEntry.Provider             entryProvider        ;
			private final EqHashTable<String, PersistenceRootEntry> rootEntries          ;
			private       String                                    defaultRootIdentifier;
			private       String                                    customRootIdentifier ;
			private       Reference<Object>                         defaultRoot          ;
			private       PersistenceRefactoringResolverProvider    refactoring          ;
			private       PersistenceRefactoringMappingProvider     refactoringMapping   ;
			
			
			
			///////////////////////////////////////////////////////////////////////////
			// constructors //
			/////////////////
			
			Default(final PersistenceRootEntry.Provider entryProvider)
			{
				super();
				this.entryProvider = entryProvider;
				this.rootEntries   = this.initializeRootEntries();
			}
			
			
			
			///////////////////////////////////////////////////////////////////////////
			// methods //
			////////////
			
			/**
			 * System constants that must be present and may not be replaced by user logic are initially registered.
			 */
			private EqHashTable<String, PersistenceRootEntry> initializeRootEntries()
			{
				final EqHashTable<String, PersistenceRootEntry> entries = EqHashTable.New();
				
				for(final KeyValue<String, Supplier<?>> entry : PersistenceMetaIdentifiers.defineConstantSuppliers())
				{
					entries.add(entry.key(), this.entryProvider.provideRootEntry(entry.key(), entry.value()));
				}
								
				return entries;
			}
			
			

			@Override
			public Builder registerDefaultRoot(
				final String            defaultRootIdentifier,
				final Reference<Object> defaultRoot
			)
			{
				notNull(defaultRootIdentifier);
				
				// current main root identifier must be removed in any case because of adding logic later.
				this.rootEntries.removeFor(defaultRootIdentifier);
				this.addEntry(defaultRootIdentifier, () -> defaultRoot);
				this.defaultRootIdentifier = defaultRootIdentifier;
				this.defaultRoot           = defaultRoot;
				
				return this;
			}
			
			@Override
			public Builder registerCustomRoot(
				final String      customRootIdentifier,
				final Supplier<?> instanceSupplier
			)
			{
				notNull(customRootIdentifier);
				
				// current main root identifier must be removed in any case because of adding logic later.
				this.rootEntries.removeFor(customRootIdentifier);
				this.addEntry(customRootIdentifier, instanceSupplier);
				this.customRootIdentifier = customRootIdentifier;
				
				return this;
			}
			
			@Override
			public final synchronized Builder registerRoot(
				final String      identifier      ,
				final Supplier<?> instanceSupplier
			)
			{
				final PersistenceRootEntry entry = this.entryProvider.provideRootEntry(identifier, instanceSupplier);
				this.addEntry(identifier, entry);
				
				return this;
			}
			
			private void addEntry(
				final String      identifier      ,
				final Supplier<?> instanceSupplier
			)
			{
				final PersistenceRootEntry entry = this.entryProvider.provideRootEntry(identifier, instanceSupplier);
				this.addEntry(identifier, entry);
			}
			
			private void addEntry(final String identifier, final PersistenceRootEntry entry)
			{
				if(this.rootEntries.add(identifier, entry))
				{
					return;
				}
				
				throw new RuntimeException(); // (17.04.2018 TM)EXCP: proper exception
			}
						
			@Override
			public final synchronized PersistenceRootResolver build()
			{
				final PersistenceRootResolver resolver = new PersistenceRootResolver.Default(
					this.defaultRootIdentifier,
					this.customRootIdentifier ,
					this.defaultRoot          ,
					this.rootEntries.immure()
				);
				
				final PersistenceRefactoringResolverProvider refactoring = this.getBuildRefactoring();
				
				return refactoring == null
					? resolver
					: PersistenceRootResolver.Wrap(resolver, refactoring)
				;
			}
			
			protected PersistenceRefactoringResolverProvider getBuildRefactoring()
			{
				if(this.refactoring != null)
				{
					return this.refactoring;
				}
				
				if(this.refactoringMapping != null)
				{
					return PersistenceRefactoringResolverProvider.Caching(this.refactoringMapping);
				}
				
				return null;
			}
			
			@Override
			public final synchronized Builder setRefactoring(
				final PersistenceRefactoringResolverProvider refactoring
			)
			{
				this.refactoring = refactoring;
				return this;
			}
			
			@Override
			public final synchronized Builder setRefactoring(
				final PersistenceRefactoringMappingProvider refactoringMapping
			)
			{
				this.refactoringMapping = refactoringMapping;
				return this;
			}
		}
	}
	
	
	
}

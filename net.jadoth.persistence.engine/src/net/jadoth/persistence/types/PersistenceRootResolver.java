package net.jadoth.persistence.types;

import static net.jadoth.Jadoth.notNull;

import java.util.function.BiFunction;
import java.util.function.Supplier;

import net.jadoth.collections.EqConstHashTable;
import net.jadoth.collections.EqHashEnum;
import net.jadoth.collections.EqHashTable;
import net.jadoth.collections.X;
import net.jadoth.collections.types.XGettingEnum;
import net.jadoth.collections.types.XGettingMap;
import net.jadoth.collections.types.XGettingTable;
import net.jadoth.hash.JadothHash;

public interface PersistenceRootResolver
{
	public PersistenceRootEntry resolveRootInstance(String identifier);
	
	public XGettingTable<String, Object> getRootInstances();
	
	public default XGettingTable<String, PersistenceRootEntry> resolveRootInstances(
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

	
	
	public interface Builder
	{
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
				
		public PersistenceRootResolver build();
		
		public final class Implementation implements PersistenceRootResolver.Builder
		{
			///////////////////////////////////////////////////////////////////////////
			// instance fields //
			////////////////////
			
			private final BiFunction<String, Supplier<?>, PersistenceRootEntry> entryProvider  ;
			private final EqHashTable<String, PersistenceRootEntry>             rootEntries    ;
			
			
			
			///////////////////////////////////////////////////////////////////////////
			// constructors //
			/////////////////
			
			Implementation(final BiFunction<String, Supplier<?>, PersistenceRootEntry> entryProvider)
			{
				super();
				this.entryProvider = entryProvider;
				this.rootEntries   = this.initializeRootEntries();
			}
			
			
			
			///////////////////////////////////////////////////////////////////////////
			// methods //
			////////////
			
			@Override
			public final synchronized Builder registerRoot(final String identifier, final Supplier<?> instanceSupplier)
			{
				final PersistenceRootEntry entry = this.entryProvider.apply(identifier, instanceSupplier);
				this.addEntry(identifier, entry);
				return this;
			}
			
			private void addEntry(final String identifier, final PersistenceRootEntry entry)
			{
				if(this.rootEntries.add(identifier, entry))
				{
					return;
				}
				throw new RuntimeException(); // (17.04.2018 TM)EXCP: proper exception
			}
			
			/**
			 * System constants that must be present and may not be replaced by user logic are initially registered.
			 */
			private EqHashTable<String, PersistenceRootEntry> initializeRootEntries()
			{
				final EqHashTable<String, PersistenceRootEntry> entries = EqHashTable.New();
				
				// arbitrary constant identifiers that decouple constant resolving from class/field names.
				this.register(entries, "XHashEqualator:hashEqualityValue"   , JadothHash::hashEqualityValue   );
				this.register(entries, "XHashEqualator:hashEqualityIdentity", JadothHash::hashEqualityIdentity);
				this.register(entries, "XEmpty:Collection"                  , X::empty                        );
				this.register(entries, "XEmpty:Table"                       , X::emptyTable                   );
				
				return entries;
			}
			
			private void register(
				final EqHashTable<String, PersistenceRootEntry> entries         ,
				final String                                    identifier      ,
				final Supplier<?>                               instanceSupplier
			)
			{
				entries.add(identifier, this.entryProvider.apply(identifier, instanceSupplier));
			}
			
			@Override
			public final synchronized PersistenceRootResolver build()
			{
				return new PersistenceRootResolver.Implementation(
					this.rootEntries.immure()
				);
			}
		}
	}
	
	

	
	public static PersistenceRootResolver.Builder Builder()
	{
		return Builder(PersistenceRootEntry::New);
	}
	
	public static PersistenceRootResolver.Builder Builder(
		final BiFunction<String, Supplier<?>, PersistenceRootEntry> entryProvider
	)
	{
		return new PersistenceRootResolver.Builder.Implementation(
			notNull(entryProvider)
		);
	}
	
	public static PersistenceRootResolver New()
	{
		return Builder().build();
	}
	
	public static PersistenceRootResolver New(final String identifier, final Supplier<?> instanceSupplier)
	{
		return Builder()
			.registerRoot(identifier, instanceSupplier)
			.build()
		;
	}
	
	public final class Implementation implements PersistenceRootResolver
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final EqConstHashTable<String, PersistenceRootEntry> rootEntries;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Implementation(final EqConstHashTable<String, PersistenceRootEntry> rootEntries)
		{
			super();
			this.rootEntries = rootEntries;
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
		public final XGettingTable<String, Object> getRootInstances()
		{
			final EqHashTable<String, Object> rootInstances = EqHashTable.New();
			
			for(final PersistenceRootEntry entry : this.rootEntries.values())
			{
				rootInstances.add(entry.identifier(), entry.instance());
			}
			
			return rootInstances;
		}

	}
	
	
	public static PersistenceRootResolver Wrap(
		final PersistenceRootResolver                actualRootResolver,
		final PersistenceRefactoringMappingProvider refactoringMappingProvider
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
		final PersistenceRefactoringMappingProvider refactoringMappingProvider;
		
		transient XGettingMap<String, String>        refactoringMappings;

		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		MappingWrapper(
			final PersistenceRootResolver actualRootResolver        ,
			final PersistenceRefactoringMappingProvider                refactoringMappingProvider
		)
		{
			super();
			this.actualRootResolver         = actualRootResolver        ;
			this.refactoringMappingProvider = refactoringMappingProvider;
		}


		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		private synchronized XGettingMap<String, String> refactoringMappings()
		{
			if(this.refactoringMappings == null)
			{
				this.refactoringMappings = this.refactoringMappingProvider.provideRefactoringMapping().entries();
			}
			
			return this.refactoringMappings;
		}
		
		@Override
		public final XGettingTable<String, Object> getRootInstances()
		{
			return this.actualRootResolver.getRootInstances();
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
			final XGettingMap<String, String> refactoringMappings = this.refactoringMappings();
			final String                      sourceIdentifier    = normalize(identifier);
			
			if(!refactoringMappings.keys().contains(sourceIdentifier))
			{
				// simple case: no mapping found, use (normalized) source identifier directly.
				return this.actualRootResolver.resolveRootInstance(sourceIdentifier);
			}
			
			final String targetIdentifier = normalize(refactoringMappings.get(sourceIdentifier));
			
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
			
			// but there is a catch: an unresolveable explicitely provided targetIdentifier is an error.
			if(mappedEntry == null)
			{
				// (19.04.2018 TM)EXCP: proper exception
				throw new RuntimeException(
					"Refactoring mapping target identifier cannot be resolved: " + targetIdentifier
				);
			}
			
			return mappedEntry;
		}
		
		/**
		 * Identifiers cannot have whitespaces at the end or the beginning.
		 * A string that only consists of whitespaces is considered no identifier at all.
		 * 
		 * Note that for strings that don't have bordering whitespaces or are already <code>null</code>,
		 * this method takes very little time and does not allocate any new instances. Only
		 * the problematic case is any mentionable effort.
		 * 
		 * @param s the raw string to be normalized.
		 * @return the normalized string, potentially <code>null</code>.
		 */
		private static String normalize(final String s)
		{
			if(s == null)
			{
				return null;
			}
			
			final String normalized = s.trim();
			
			return normalized.isEmpty()
				? null
				: normalized
			;
		}
		
	}

}

package net.jadoth.persistence.types;

import static net.jadoth.Jadoth.notNull;

import java.util.function.BiFunction;
import java.util.function.Supplier;

import net.jadoth.collections.EqConstHashTable;
import net.jadoth.collections.EqHashTable;
import net.jadoth.collections.X;
import net.jadoth.hash.JadothHash;
import net.jadoth.util.KeyValue;

public interface PersistenceRootResolver
{
	public interface Builder
	{
		public Builder registerRoot(String identifier, Supplier<?> instanceSupplier);
		
		public Builder registerMapping(String sourceIdentifier, String targetIdentifier);
		
		public PersistenceRootResolver build();
		
		public final class Implementation implements PersistenceRootResolver.Builder
		{
			///////////////////////////////////////////////////////////////////////////
			// instance fields //
			////////////////////
			
			private final BiFunction<String, Supplier<?>, PersistenceRootEntry> entryProvider     ;
			private final EqHashTable<String, String>                           refactoringMapping;
			private final EqHashTable<String, PersistenceRootEntry>             rootEntries       ;
			
			
			
			///////////////////////////////////////////////////////////////////////////
			// constructors //
			/////////////////
			
			Implementation(final BiFunction<String, Supplier<?>, PersistenceRootEntry> entryProvider)
			{
				super();
				this.entryProvider      = entryProvider;
				this.rootEntries        = this.initializeEffectiveEntries();
				this.refactoringMapping = EqHashTable.New();
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
			
			@Override
			public final synchronized Builder registerMapping(final String sourceIdentifier, final String targetIdentifier)
			{
				if(!this.refactoringMapping.add(sourceIdentifier, targetIdentifier))
				{
					throw new RuntimeException(); // (17.04.2018 TM)EXCP: proper exception
				}
				return this;
			}
			
			private EqHashTable<String, PersistenceRootEntry> initializeEffectiveEntries()
			{
				final EqHashTable<String, PersistenceRootEntry> entries = EqHashTable.New();
				
				// arbitrary constant identifiers that decouple constant resolving from class/field names.
				this.register(entries, "XHashEqualator:hashEqualityIdentity", JadothHash::hashEqualityIdentity);
				this.register(entries, "XHashEqualator:hashEqualityValue"   , JadothHash::hashEqualityValue   );
				this.register(entries, "XHashEqualator:keyValueHashEqualityKeyIdentity", JadothHash::keyValueHashEqualityKeyIdentity);
				this.register(entries, "XEmpty:Collection", X::empty);
				this.register(entries, "XEmpty:Table", X::emptyTable);
				
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
				final EqHashTable<String, PersistenceRootEntry>             effectiveEntries = EqHashTable.New(this.rootEntries);
				final BiFunction<String, Supplier<?>, PersistenceRootEntry> entryProvider    = this.entryProvider;
				
				for(final KeyValue<String, String> kv : this.refactoringMapping)
				{
					final String sourceIdentifier = kv.key();
					if(kv.value() == null)
					{
						effectiveEntries.add(sourceIdentifier, entryProvider.apply(sourceIdentifier, null));
						continue;
					}
					
					final PersistenceRootEntry targetEntry = effectiveEntries.get(kv.value());
					if(targetEntry == null)
					{
						throw new RuntimeException(); // (17.04.2018 TM)EXCP: proper exception
					}
					this.addEntry(sourceIdentifier, targetEntry);
				}
				
				return new PersistenceRootResolver.Implementation(effectiveEntries.immure());
			}
		}
	}
	


	public PersistenceRootEntry resolveRootInstance(String identifier);
	

	
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

	}

}

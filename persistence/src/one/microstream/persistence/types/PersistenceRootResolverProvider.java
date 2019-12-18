package one.microstream.persistence.types;

import static one.microstream.X.notNull;

import java.util.function.Supplier;

import one.microstream.collections.EqHashTable;
import one.microstream.collections.types.XGettingTable;
import one.microstream.persistence.exceptions.PersistenceException;
import one.microstream.reference.Reference;
import one.microstream.typing.KeyValue;


public interface PersistenceRootResolverProvider
{
	public PersistenceRootReferenceProvider<M> rootReference();
	
	public default String rootIdentifier()
	{
		return Persistence.rootIdentifier();
	}
	
	public default boolean hasRootRegistered()
	{
		return this.rootReference().get() != null;
	}

	public PersistenceRootResolverProvider registerRoot(Object root);
	
	public default PersistenceRootResolverProvider registerRoot(
		final String identifier,
		final Object instance
	)
	{
		return this.registerRootSupplier(identifier, () -> instance);
	}
	

	public default PersistenceRootResolverProvider registerRootSupplier(final Supplier<?> instanceSupplier)
	{
		return this.registerRootSupplier(this.rootIdentifier(), instanceSupplier);
	}
	
	public PersistenceRootResolverProvider registerRootSupplier(String identifier, Supplier<?> instanceSupplier);
	
	public default PersistenceRootResolverProvider registerRootSuppliers(
		final XGettingTable<String, Supplier<?>> roots
	)
	{
		synchronized(this)
		{
			roots.iterate(kv ->
				this.registerRootSupplier(kv.key(), kv.value())
			);
		}
		
		return this;
	}
	
	public PersistenceRootResolverProvider setTypeDescriptionResolverProvider(
		PersistenceTypeDescriptionResolverProvider typeDescriptionResolverProvider
	);
	
	public PersistenceRootResolverProvider setRefactoring(PersistenceRefactoringMappingProvider refactoringMapping);
	
	
	public Reference<? extends PersistenceTypeHandlerManager<?>> typeHandlerManager();
	
	public PersistenceRootResolverProvider setTypeHandlerManager(
		Reference<? extends PersistenceTypeHandlerManager<?>> typeHandlerManager
	);
	
			
	public PersistenceRootResolver provideRootResolver();
	
	
	
	public static PersistenceRootResolverProvider New()
	{
		return New(PersistenceTypeResolver.Default());
	}
	
	public static PersistenceRootResolverProvider New(
		final PersistenceTypeResolver typeResolver
	)
	{
		return New(typeResolver, PersistenceRootEntry::New);
	}
	
	public static PersistenceRootResolverProvider New(
		final PersistenceTypeResolver       typeResolver ,
		final PersistenceRootEntry.Provider entryProvider
	)
	{
		final PersistenceRootResolverProvider.Default builder = new PersistenceRootResolverProvider.Default(
			notNull(typeResolver) ,
			notNull(entryProvider)
		);
		
		return builder;
	}
	
	
	public class Default implements PersistenceRootResolverProvider
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final PersistenceRootEntry.Provider                         entryProvider                  ;
		private final PersistenceTypeResolver                               typeResolver                   ;
		private final EqHashTable<String, PersistenceRootEntry>             rootEntries                    ;
		private final PersistenceRootReference                              rootReference                  ;
		private       PersistenceTypeDescriptionResolverProvider            typeDescriptionResolverProvider;
		private       PersistenceRefactoringMappingProvider                 refactoringMapping             ;
		private       Reference<? extends PersistenceTypeHandlerManager<?>> refTypeHandlerManager          ;
				
		private transient PersistenceRootResolver cachedRootResolver;
		
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default(
			final PersistenceTypeResolver       typeResolver ,
			final PersistenceRootEntry.Provider entryProvider
		)
		{
			super();
			this.typeResolver   = typeResolver                ;
			this.entryProvider  = entryProvider               ;
			this.rootEntries    = this.initializeRootEntries();
			this.rootReference  = PersistenceRootReference.New(null);
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final PersistenceRootReference rootReference()
		{
			return this.rootReference;
		}
		
		/**
		 * System constants that must be present and may not be replaced by user logic are initially registered.
		 */
		private EqHashTable<String, PersistenceRootEntry> initializeRootEntries()
		{
			final EqHashTable<String, PersistenceRootEntry> entries = EqHashTable.New();
			
			for(final KeyValue<String, Supplier<?>> entry : PersistenceMetaIdentifiers.defineConstantSuppliers())
			{
				this.initializeEntry(entries, entry.key(), entry.value());
			}

			// gets registered once initially and never modified afterwards
			this.initializeEntry(entries, this.rootIdentifier(), this.rootReference);
							
			return entries;
		}
		
		private void initializeEntry(
			final EqHashTable<String, PersistenceRootEntry> entries   ,
			final String                                    identifier,
			final Supplier<?>                               supplier
		)
		{
			entries.add(identifier, this.entryProvider.provideRootEntry(identifier, supplier));
		}
		
		@Override
		public final synchronized PersistenceRootResolverProvider registerRoot(final Object root)
		{
			// no need to reregister, see #initializeRootEntries
			this.rootReference.setRoot(root);
			
			return this;
		}
		
		@Override
		public final synchronized PersistenceRootResolverProvider registerRootSupplier(
			final String      identifier      ,
			final Supplier<?> instanceSupplier
		)
		{
			this.addEntry(identifier, instanceSupplier);
			
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
			
			// (17.04.2018 TM)EXCP: proper exception
			throw new PersistenceException("Root entry already registered for identifier \"" + identifier + '"');
		}
		
		private PersistenceRootResolver createRootResolver()
		{
			final PersistenceRootResolver resolver = new PersistenceRootResolver.Default(
				this.rootIdentifier()     ,
				this.rootReference        ,
				this.rootEntries.immure() ,
				this.refTypeHandlerManager
			);
			
			final PersistenceTypeDescriptionResolverProvider refactoring = this.getEffectiveTypeDescriptionResolver();
						
			return refactoring == null
				? resolver
				: PersistenceRootResolver.Wrap(resolver, refactoring)
			;
		}
					
		@Override
		public final synchronized PersistenceRootResolver provideRootResolver()
		{
			if(this.cachedRootResolver == null)
			{
				this.cachedRootResolver = this.createRootResolver();
			}
			
			return this.cachedRootResolver;
		}
		
		protected PersistenceTypeDescriptionResolverProvider getEffectiveTypeDescriptionResolver()
		{
			if(this.typeDescriptionResolverProvider != null)
			{
				return this.typeDescriptionResolverProvider;
			}
			
			if(this.refactoringMapping != null)
			{
				return PersistenceTypeDescriptionResolverProvider.Caching(
					this.typeResolver,
					this.refactoringMapping
				);
			}
			
			return null;
		}
		
		@Override
		public final synchronized PersistenceRootResolverProvider setTypeDescriptionResolverProvider(
			final PersistenceTypeDescriptionResolverProvider typeDescriptionResolverProvider
		)
		{
			this.typeDescriptionResolverProvider = typeDescriptionResolverProvider;
			return this;
		}
		
		@Override
		public final synchronized PersistenceRootResolverProvider setRefactoring(
			final PersistenceRefactoringMappingProvider refactoringMapping
		)
		{
			this.refactoringMapping = refactoringMapping;
			return this;
		}
		
		@Override
		public synchronized Reference<? extends PersistenceTypeHandlerManager<?>> typeHandlerManager()
		{
			return this.refTypeHandlerManager;
		}
		
		@Override
		public synchronized PersistenceRootResolverProvider setTypeHandlerManager(
			final Reference<? extends PersistenceTypeHandlerManager<?>> typeHandlerManager
		)
		{
			this.refTypeHandlerManager = typeHandlerManager;
			return this;
		}
		
	}
}
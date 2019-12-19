package one.microstream.persistence.types;

import static one.microstream.X.notNull;

import java.util.function.Supplier;

import one.microstream.collections.EqHashTable;
import one.microstream.collections.types.XGettingTable;
import one.microstream.persistence.exceptions.PersistenceException;
import one.microstream.reference.Reference;
import one.microstream.typing.KeyValue;


public interface PersistenceRootResolverProvider<M>
{
	public PersistenceRootReferenceProvider<M> rootReferenceProvider();
	
	public default PersistenceRootReference rootReference()
	{
		return this.rootReferenceProvider().provideRootReference();
	}
	
	public default String rootIdentifier()
	{
		return Persistence.rootIdentifier();
	}
	
	public default boolean hasRootRegistered()
	{
		return this.rootReference().get() != null;
	}

	public PersistenceRootResolverProvider<M> registerRoot(Object root);
	
	public default PersistenceRootResolverProvider<M> registerRoot(
		final String identifier,
		final Object instance
	)
	{
		return this.registerRootSupplier(identifier, () -> instance);
	}
	

	public default PersistenceRootResolverProvider<M> registerRootSupplier(final Supplier<?> instanceSupplier)
	{
		return this.registerRootSupplier(this.rootIdentifier(), instanceSupplier);
	}
	
	public PersistenceRootResolverProvider<M> registerRootSupplier(String identifier, Supplier<?> instanceSupplier);
	
	public default PersistenceRootResolverProvider<M> registerRootSuppliers(
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
	
	public PersistenceRootResolverProvider<M> setTypeDescriptionResolverProvider(
		PersistenceTypeDescriptionResolverProvider typeDescriptionResolverProvider
	);
	
	public PersistenceRootResolverProvider<M> setRefactoring(PersistenceRefactoringMappingProvider refactoringMapping);
	
	
	public Reference<? extends PersistenceTypeHandlerManager<?>> typeHandlerManager();
	
	public PersistenceRootResolverProvider<M> setTypeHandlerManager(
		Reference<? extends PersistenceTypeHandlerManager<M>> typeHandlerManager
	);
	
			
	public PersistenceRootResolver provideRootResolver();
	
	
	
	public static <M> PersistenceRootResolverProvider<M> New(
		final PersistenceRootReferenceProvider<M> rootReferenceProvider
	)
	{
		return New(rootReferenceProvider, PersistenceTypeResolver.Default());
	}
	
	public static <M> PersistenceRootResolverProvider<M> New(
		final PersistenceRootReferenceProvider<M> rootReferenceProvider,
		final PersistenceTypeResolver             typeResolver
	)
	{
		return New(rootReferenceProvider, typeResolver, PersistenceRootEntry::New);
	}
	
	public static <M> PersistenceRootResolverProvider<M> New(
		final PersistenceRootReferenceProvider<M> rootReferenceProvider,
		final PersistenceTypeResolver             typeResolver         ,
		final PersistenceRootEntry.Provider       entryProvider
	)
	{
		final PersistenceRootResolverProvider.Default<M> builder = new PersistenceRootResolverProvider.Default<>(
			notNull(rootReferenceProvider),
			notNull(typeResolver)         ,
			notNull(entryProvider)
		);
		
		return builder;
	}
	
	
	public class Default<M> implements PersistenceRootResolverProvider<M>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final PersistenceRootEntry.Provider                         entryProvider                  ;
		private final PersistenceTypeResolver                               typeResolver                   ;
		private final EqHashTable<String, PersistenceRootEntry>             rootEntries                    ;
		private final PersistenceRootReferenceProvider<M>                   rootReferenceProvider          ;
		private       PersistenceTypeDescriptionResolverProvider            typeDescriptionResolverProvider;
		private       PersistenceRefactoringMappingProvider                 refactoringMapping             ;
		private       Reference<? extends PersistenceTypeHandlerManager<M>> refTypeHandlerManager          ;
				
		private transient PersistenceRootResolver cachedRootResolver;
		
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default(
			final PersistenceRootReferenceProvider<M> rootReferenceProvider,
			final PersistenceTypeResolver             typeResolver         ,
			final PersistenceRootEntry.Provider       entryProvider
		)
		{
			super();
			this.rootReferenceProvider = rootReferenceProvider;
			this.typeResolver          = typeResolver         ;
			this.entryProvider         = entryProvider        ;
			
			this.rootEntries = this.initializeRootEntries();
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final PersistenceRootReferenceProvider<M> rootReferenceProvider()
		{
			return this.rootReferenceProvider;
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
			this.initializeEntry(entries, this.rootIdentifier(), this.rootReference());
							
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
		public final synchronized PersistenceRootResolverProvider<M> registerRoot(final Object root)
		{
			// no need to reregister, see #initializeRootEntries
			this.rootReference().setRoot(root);
			
			return this;
		}
		
		@Override
		public final synchronized PersistenceRootResolverProvider<M> registerRootSupplier(
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
				this.rootReference()      ,
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
		public final synchronized PersistenceRootResolverProvider<M> setTypeDescriptionResolverProvider(
			final PersistenceTypeDescriptionResolverProvider typeDescriptionResolverProvider
		)
		{
			this.typeDescriptionResolverProvider = typeDescriptionResolverProvider;
			return this;
		}
		
		@Override
		public final synchronized PersistenceRootResolverProvider<M> setRefactoring(
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
		public synchronized PersistenceRootResolverProvider<M> setTypeHandlerManager(
			final Reference<? extends PersistenceTypeHandlerManager<M>> typeHandlerManager
		)
		{
			this.refTypeHandlerManager = typeHandlerManager;
			return this;
		}
		
	}
}
package one.microstream.persistence.types;

import static one.microstream.X.notNull;

import java.util.function.Supplier;

import one.microstream.X;
import one.microstream.collections.EqHashTable;
import one.microstream.collections.types.XGettingTable;
import one.microstream.persistence.exceptions.PersistenceException;
import one.microstream.reference.Reference;
import one.microstream.typing.KeyValue;


public interface PersistenceRootResolverProvider
{
	public String defaultRootIdentifier();
	
	public String customRootIdentifier();
	
	
	/*
	 * A syntax-"default" implementation returning the default value for the default-root's identifier.
	 * Default default default. Not my fault.
	 */
	public default String defaultDefaultRootIdentifier()
	{
		return Persistence.defaultRootIdentifier();
	}

	/*
	 * A syntax-"default" implementation returning the default instance for the default-root.
	 * Default default default. Not my fault.
	 */
	public default Reference<Object> defaultDefaultRoot()
	{
		return X.Reference(null);
	}
	
	public default boolean hasRootRegistered()
	{
		return this.defaultRootIdentifier() != null || this.customRootIdentifier() != null;
	}
	
	public default PersistenceRootResolverProvider registerDefaultRoot(
		final Reference<Object> defaultRoot
	)
	{
		return this.registerDefaultRoot(this.defaultDefaultRootIdentifier(), defaultRoot);
	}
	
	public default PersistenceRootResolverProvider registerCustomRoot(
		final Object customRoot
	)
	{
		return this.registerCustomRootSupplier(PersistenceRootResolver.wrapCustomRoot(customRoot));
	}
	
	public default PersistenceRootResolverProvider registerCustomRootSupplier(
		final Supplier<?> instanceSupplier
	)
	{
		return this.registerCustomRootSupplier(
			Persistence.customRootIdentifier(),
			instanceSupplier
		);
	}
	
	public PersistenceRootResolverProvider registerDefaultRoot(
		String            defaultRootIdentifier,
		Reference<Object> defaultRoot
	);
	
	public PersistenceRootResolverProvider registerCustomRootSupplier(
		String      customRootIdentifier,
		Supplier<?> instanceSupplier
	);
	
	public default PersistenceRootResolverProvider registerCustomRoot(
		final String customRootIdentifier,
		final Object customRoot
	)
	{
		return this.registerCustomRootSupplier(
			customRootIdentifier,
			PersistenceRootResolver.wrapCustomRoot(customRoot)
		);
	}
	
	public PersistenceRootResolverProvider registerRoot(String identifier, Supplier<?> instanceSupplier);
	
	public default PersistenceRootResolverProvider registerRoots(final XGettingTable<String, Supplier<?>> roots)
	{
		synchronized(this)
		{
			roots.iterate(kv ->
				this.registerRoot(kv.key(), kv.value())
			);
		}
		return this;
	}
	
	public default PersistenceRootResolverProvider registerRoot(
		final String identifier,
		final Object instance
	)
	{
		return this.registerRoot(identifier, () -> instance);
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
		private       String                                                defaultRootIdentifier          ;
		private       String                                                customRootIdentifier           ;
		private       Reference<Object>                                     defaultRoot                    ;
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
			this.typeResolver  = typeResolver                ;
			this.entryProvider = entryProvider               ;
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
		public String customRootIdentifier()
		{
			return this.customRootIdentifier;
		}
		
		@Override
		public String defaultRootIdentifier()
		{
			return this.defaultRootIdentifier;
		}

		@Override
		public PersistenceRootResolverProvider registerDefaultRoot(
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
		public PersistenceRootResolverProvider registerCustomRootSupplier(
			final String      customRootIdentifier,
			final Supplier<?> customRootSupplier
		)
		{
			notNull(customRootIdentifier);
			
			// current main root identifier must be removed in any case because of adding logic later.
			this.rootEntries.removeFor(customRootIdentifier);
			this.addEntry(customRootIdentifier, customRootSupplier);
			this.customRootIdentifier = customRootIdentifier;
			
			return this;
		}
		
		@Override
		public final synchronized PersistenceRootResolverProvider registerRoot(
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
			
			// (17.04.2018 TM)EXCP: proper exception
			throw new PersistenceException("Root entry already registered for identifier \"" + identifier + '"');
		}
		
		private PersistenceRootResolver createRootResolver()
		{
			if(!this.hasRootRegistered())
			{
				// (10.12.2019 TM)FIXME: priv#194 overhaul
				this.registerDefaultRoot(
					this.defaultDefaultRoot()
				);
			}

			// can be null if a custom root is registered, insteads
			final String  defaultRootIdentifier = this.defaultRootIdentifier;
			final Reference<Object> defaultRoot = this.defaultRoot;
			
			final PersistenceRootResolver resolver = new PersistenceRootResolver.Default(
				defaultRootIdentifier,
				defaultRoot,
				this.customRootIdentifier,
				this.rootEntries.immure(),
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
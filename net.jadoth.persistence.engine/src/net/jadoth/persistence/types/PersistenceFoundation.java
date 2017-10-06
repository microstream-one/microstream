package net.jadoth.persistence.types;

import static net.jadoth.Jadoth.notNull;

import net.jadoth.functional.Dispatcher;
import net.jadoth.persistence.internal.PersistenceTypeHandlerProviderCreating;
import net.jadoth.swizzling.internal.SwizzleRegistryGrowingRange;
import net.jadoth.swizzling.types.Swizzle;
import net.jadoth.swizzling.types.SwizzleFoundation;
import net.jadoth.swizzling.types.SwizzleObjectIdProvider;
import net.jadoth.swizzling.types.SwizzleObjectManager;
import net.jadoth.swizzling.types.SwizzleRegistry;
import net.jadoth.swizzling.types.SwizzleTypeIdProvider;
import net.jadoth.swizzling.types.SwizzleTypeManager;
import net.jadoth.util.MissingAssemblyPartException;


/**
 * This type serves as a factory instance for buidling {@link PersistenceManager} instances.
 * However, it is more than a mere factory as it keeps track of all component instances used in building
 * a {@link PersistenceManager} instance. For example managing parts of an application can use it
 * to access former set ID providers or dictionary providers even after they have been assembled into (and
 * are intentionally hindden in) a {@link PersistenceManager} instance.*
 * Hence it can be seen as a kind of "master instance" of the built persistence layer or as its "foundation".
 *
 * @author Thomas Muenz
 * @param <M>
 */
public interface PersistenceFoundation<M> extends SwizzleFoundation
{
	///////////////////////////////////////////////////////////////////////////
	// getters          //
	/////////////////////

	public PersistenceTypeSovereignty getTypeSovereignty();

	public SwizzleRegistry getSwizzleRegistry();

	public SwizzleObjectManager getObjectManager();

	public PersistenceTypeHandlerManager<M> getTypeHandlerManager();

	public PersistenceStorer.Creator<M> getStorerCreator();

	public PersistenceRegisterer.Creator getRegistererCreator();

	public PersistenceLoader.Creator<M> getBuilderCreator();

	public PersistenceTarget<M> getPersistenceTarget();

	public PersistenceSource<M> getPersistenceSource();

	public PersistenceTypeHandlerProvider<M> getTypeHandlerProvider();

	public SwizzleTypeManager getTypeManager();

	public PersistenceTypeHandlerRegistry<M> getTypeHandlerRegistry();

	public PersistenceTypeDictionaryManager getTypeDictionaryManager();

	public PersistenceTypeDictionaryProvider getTypeDictionaryProvider();

	public PersistenceTypeDictionaryExporter getTypeDictionaryExporter();

	public PersistenceTypeDictionaryParser getTypeDictionaryParser();

	public PersistenceTypeDictionaryLoader getTypeDictionaryLoader();

	public PersistenceTypeDictionaryAssembler getTypeDictionaryAssembler();

	public PersistenceTypeDictionaryStorer getTypeDictionaryStorer();

	public PersistenceCustomTypeHandlerRegistry<M> getCustomTypeHandlerRegistry();

	public PersistenceTypeAnalyzer getTypeAnalyzer();

	public PersistenceTypeEvaluator getTypeEvaluatorTypeIdMappable();
	
	public PersistenceTypeChangeCallback getTypeChangeCallback();

	public PersistenceTypeResolver getTypeResolver();
	
	public PersistenceTypeDictionaryBuilder getTypeDictionaryBuilder();

	public PersistenceTypeEvaluator getTypeEvaluatorPersistable();

	public PersistenceFieldLengthResolver getFieldFixedLengthResolver();

	public BufferSizeProvider getBufferSizeProvider();

	public PersistenceFieldEvaluator getFieldEvaluator();
	
	public PersistenceTypeDefinitionBuilder getTypeDescriptionBuilder();
	
	public PersistenceTypeDefinitionInitializerProvider<M> getTypeDescriptionInitializerLookup();

	public PersistenceTypeHandlerEnsurer<M> getTypeHandlerEnsurer();
	
	public PersistenceTypeLineageBuilder getTypeLineageBuilder();


	
	///////////////////////////////////////////////////////////////////////////
	// setters          //
	/////////////////////

	public PersistenceFoundation<M> setTypeSovereignty(PersistenceTypeSovereignty typeSovereignty);

	public PersistenceFoundation<M> setSwizzleRegistry(SwizzleRegistry swizzleRegistry);

	public PersistenceFoundation<M> setInstanceDispatcher(Dispatcher instanceDispatcher);

	public PersistenceFoundation<M> setObjectManager(SwizzleObjectManager objectManager);

	public PersistenceFoundation<M> setStorerCreator(PersistenceStorer.Creator<M> persisterCreator);

	public PersistenceFoundation<M> setTypeHandlerManager(PersistenceTypeHandlerManager<M> typeHandlerManager);

	@Override
	public PersistenceFoundation<M> setObjectIdProvider(SwizzleObjectIdProvider oidProvider);

	@Override
	public PersistenceFoundation<M> setTypeIdProvider(
		SwizzleTypeIdProvider tidProvider
	);

	public PersistenceFoundation<M> setTypeManager(
		SwizzleTypeManager typeManager
	);

	public PersistenceFoundation<M> setTypeHandlerRegistry(
		PersistenceTypeHandlerRegistry<M> typeHandlerRegistry);

	public PersistenceFoundation<M> setTypeHandlerProvider(
		PersistenceTypeHandlerProvider<M> typeHandlerProvider);

	public PersistenceFoundation<M> setRegistererCreator(
		PersistenceRegisterer.Creator registererCreator
	);

	public PersistenceFoundation<M> setBuilderCreator(
		PersistenceLoader.Creator<M> builderCreator
	);

	public PersistenceFoundation<M> setPersistenceTarget(
		PersistenceTarget<M> target
	);

	public PersistenceFoundation<M> setPersistenceSource(
		PersistenceSource<M> source
	);

	public PersistenceFoundation<M> setTypeDictionaryManager(
		PersistenceTypeDictionaryManager typeDictionaryManager
	);

	public PersistenceFoundation<M> setTypeDictionaryProvider(
		PersistenceTypeDictionaryProvider typeDictionaryProvider
	);

	public PersistenceFoundation<M> setTypeDictionaryExporter(
		PersistenceTypeDictionaryExporter typeDictionaryExporter
	);

	public PersistenceFoundation<M> setTypeDictionaryParser(
		PersistenceTypeDictionaryParser typeDictionaryParser
	);

	public PersistenceFoundation<M> setTypeDictionaryAssembler(
		PersistenceTypeDictionaryAssembler typeDictionaryAssembler
	);

	public PersistenceFoundation<M> setTypeDictionaryLoader(
		PersistenceTypeDictionaryLoader typeDictionaryLoader
	);

	public PersistenceFoundation<M> setTypeDictionaryStorer(
		PersistenceTypeDictionaryStorer typeDictionaryStorer
	);

	public PersistenceFoundation<M> setTypeEvaluatorTypeIdMappable(
		PersistenceTypeEvaluator typeEvaluatorTypeIdMappable
	);

	public PersistenceFoundation<M> setTypeResolver(
		PersistenceTypeResolver typeResolver
	);
	
	public PersistenceFoundation<M> setTypeDictionaryBuilder(
		PersistenceTypeDictionaryBuilder typeDictionaryBuilder
	);
	
	public PersistenceFoundation<M> setTypeEvaluatorPersistable(PersistenceTypeEvaluator getTypeEvaluatorPersistable);

	public PersistenceFoundation<M> setBufferSizeProvider(BufferSizeProvider bufferSizeProvider);

	public PersistenceFoundation<M> setFieldFixedLengthResolver(PersistenceFieldLengthResolver resolver);

	public PersistenceFoundation<M> setFieldEvaluator(PersistenceFieldEvaluator fieldEvaluator);

	


	@Override
	public <P extends SwizzleTypeIdProvider & SwizzleObjectIdProvider>
	PersistenceFoundation<M> setSwizzleIdProvider(P swizzleTypeIdProvider);

	public <H extends PersistenceTypeDictionaryLoader & PersistenceTypeDictionaryStorer>
	PersistenceFoundation<M> setDictionaryStorage(H typeDictionaryStorageHandler);

	public <S extends PersistenceTarget<M> & PersistenceSource<M>>
	PersistenceFoundation<M> setPersistenceStorage(S persistenceStorage);



	///////////////////////////////////////////////////////////////////////////
	// declared methods //
	/////////////////////

	/*
	 * generic name is intentional as the role of the created instance may change in extended types
	 * (e.g. representing a database connection)
	 */
	public PersistenceManager<M> createPersistenceManager();



	public abstract class AbstractImplementation<M>
	extends SwizzleFoundation.Implementation
	implements PersistenceFoundation<M>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		private PersistenceTypeSovereignty              typeSovereignty             = PersistenceTypeSovereignty.MASTER;

		// first level assembly parts (used directly to build manager instance) \\
		private SwizzleRegistry                         swizzleRegistry            ;
		private SwizzleObjectManager                    objectManager              ;
		private PersistenceTypeHandlerManager<M>        typeHandlerManager         ;
		private PersistenceStorer.Creator<M>            storerCreator              ;
		private PersistenceRegisterer.Creator           registererCreator          ;
		private PersistenceLoader.Creator<M>            builderCreator             ;
		private PersistenceTarget<M>                    target                     ;
		private PersistenceSource<M>                    source                     ;

		// second level assembly parts (used as a fallback to build missing first level parts) \\
		private SwizzleTypeManager                              typeManager                      ;
		private PersistenceTypeHandlerRegistry<M>               typeHandlerRegistry              ;
		private PersistenceTypeHandlerProvider<M>               typeHandlerProvider              ;
		private PersistenceTypeDictionaryManager                typeDictionaryManager            ;
		private PersistenceTypeDictionaryProvider               typeDictionaryProvider           ;
		private PersistenceTypeDictionaryExporter               typeDictionaryExporter           ;
		private PersistenceTypeDictionaryLoader                 typeDictionaryLoader             ;
		private PersistenceTypeDictionaryParser                 typeDictionaryParser             ;
		private PersistenceTypeDictionaryBuilder                typeDictionaryBuilder            ;
		private PersistenceTypeDictionaryAssembler              typeDictionaryAssembler          ;
		private PersistenceTypeDictionaryStorer                 typeDictionaryStorer             ;
		private PersistenceTypeHandlerEnsurer<M>                typeHandlerEnsurer               ;
		private PersistenceCustomTypeHandlerRegistry<M>         customTypeHandlerRegistry        ;
		private PersistenceTypeAnalyzer                         typeAnalyzer                     ;
		private PersistenceTypeEvaluator                        typeEvaluatorTypeIdMappable      ;
		private PersistenceTypeChangeCallback                   typeChangeCallback               ;
		private PersistenceTypeResolver                         typeResolver                     ;
		private PersistenceTypeEvaluator                        typeEvaluatorPersistable         ;
		private PersistenceFieldLengthResolver                  fieldFixedLengthResolver         ;
		private BufferSizeProvider                              bufferSizeProvider               ;
		private PersistenceFieldEvaluator                       fieldEvaluator                   ;
		private PersistenceTypeDefinitionBuilder                typeDescriptionBuilder           ;
		private PersistenceTypeLineageBuilder                   typeLineageBuilder               ;
		private PersistenceTypeDefinitionInitializerProvider<M> typeDefinitionInitializerProvider;



		///////////////////////////////////////////////////////////////////////////
		// getters          //
		/////////////////////

		@Override
		public PersistenceTypeSovereignty getTypeSovereignty()
		{
			return this.typeSovereignty;
		}

		@Override
		public SwizzleRegistry getSwizzleRegistry()
		{
			if(this.swizzleRegistry == null)
			{
				this.swizzleRegistry = this.dispatch(this.createSwizzleRegistry());
			}
			return this.swizzleRegistry;
		}

		@Override
		public SwizzleObjectManager getObjectManager()
		{
			if(this.objectManager == null)
			{
				this.objectManager = this.dispatch(this.createObjectManager());
			}
			return this.objectManager;
		}

		@Override
		public PersistenceTypeHandlerManager<M> getTypeHandlerManager()
		{
			if(this.typeHandlerManager == null)
			{
				this.typeHandlerManager = this.dispatch(this.createTypeHandlerManager());
			}
			return this.typeHandlerManager;
		}

		@Override
		public PersistenceStorer.Creator<M> getStorerCreator()
		{
			if(this.storerCreator == null)
			{
				this.storerCreator = this.dispatch(this.createStorerCreator());
			}
			return this.storerCreator;
		}

		@Override
		public PersistenceRegisterer.Creator getRegistererCreator()
		{
			if(this.registererCreator == null)
			{
				this.registererCreator = this.dispatch(this.createRegistererCreator());
			}
			return this.registererCreator;
		}

		@Override
		public PersistenceLoader.Creator<M> getBuilderCreator()
		{
			if(this.builderCreator == null)
			{
				this.builderCreator = this.dispatch(this.createBuilderCreator());
			}
			return this.builderCreator;
		}

		@Override
		public PersistenceTarget<M> getPersistenceTarget()
		{
			if(this.target == null)
			{
				this.target = this.dispatch(this.createPersistenceTarget());
			}
			return this.target;
		}

		@Override
		public PersistenceSource<M> getPersistenceSource()
		{
			if(this.source == null)
			{
				this.source = this.dispatch(this.createPersistenceSource());
			}
			return this.source;
		}

		@Override
		public PersistenceTypeHandlerRegistry<M> getTypeHandlerRegistry()
		{
			if(this.typeHandlerRegistry == null)
			{
				this.typeHandlerRegistry = this.dispatch(this.createTypeHandlerRegistry());
			}
			return this.typeHandlerRegistry;
		}

		@Override
		public PersistenceTypeHandlerProvider<M> getTypeHandlerProvider()
		{
			if(this.typeHandlerProvider == null)
			{
				this.typeHandlerProvider = this.dispatch(this.createTypeHandlerProvider());
			}
			return this.typeHandlerProvider;
		}

		@Override
		public SwizzleTypeManager getTypeManager()
		{
			/* this is tricky!
			 * (a type slave may not use an actual sovereign typeId-assigning creating master manager,
			 * but only a type-retrieving manager, which is the TypeHandlerManager assembled with the
			 * appropriate type handler provider)
			 */
			if(this.typeSovereignty == PersistenceTypeSovereignty.SLAVE)
			{
				return this.getTypeHandlerManager();
			}

			if(this.typeManager == null)
			{
				this.typeManager = this.dispatch(this.createTypeManager());
			}
			return this.typeManager;
		}
		
		@Override
		public PersistenceTypeDictionaryProvider getTypeDictionaryProvider()
		{
			if(this.typeDictionaryProvider == null)
			{
				this.typeDictionaryProvider = this.dispatch(this.createTypeDictionaryProvider());
			}
			return this.typeDictionaryProvider;
		}

		@Override
		public PersistenceTypeDictionaryManager getTypeDictionaryManager()
		{
			if(this.typeDictionaryManager == null)
			{
				this.typeDictionaryManager = this.dispatch(this.createTypeDictionaryManager());
			}
			return this.typeDictionaryManager;
		}

		@Override
		public PersistenceTypeDictionaryExporter getTypeDictionaryExporter()
		{
			if(this.typeDictionaryExporter == null)
			{
				this.typeDictionaryExporter = this.dispatch(this.createTypeDictionaryExporter());
			}
			return this.typeDictionaryExporter;
		}

		@Override
		public PersistenceTypeDictionaryParser getTypeDictionaryParser()
		{
			if(this.typeDictionaryParser == null)
			{
				this.typeDictionaryParser = this.dispatch(this.createTypeDictionaryParser());
			}
			return this.typeDictionaryParser;
		}

		@Override
		public PersistenceTypeDictionaryLoader getTypeDictionaryLoader()
		{
			if(this.typeDictionaryLoader == null)
			{
				this.typeDictionaryLoader = this.dispatch(this.createTypeDictionaryLoader());
			}
			return this.typeDictionaryLoader;
		}

		@Override
		public PersistenceTypeDictionaryAssembler getTypeDictionaryAssembler()
		{
			if(this.typeDictionaryAssembler == null)
			{
				this.typeDictionaryAssembler = this.dispatch(this.createTypeDictionaryAssembler());
			}
			return this.typeDictionaryAssembler;
		}

		@Override
		public PersistenceTypeDictionaryStorer getTypeDictionaryStorer()
		{
			if(this.typeDictionaryStorer == null)
			{
				this.typeDictionaryStorer = this.dispatch(this.createTypeDictionaryStorer());
			}
			return this.typeDictionaryStorer;
		}

		@Override
		public PersistenceTypeHandlerEnsurer<M> getTypeHandlerEnsurer()
		{
			if(this.typeHandlerEnsurer == null)
			{
				this.typeHandlerEnsurer = this.dispatch(this.createTypeHandlerEnsurer());
			}
			return this.typeHandlerEnsurer;
		}

		@Override
		public PersistenceCustomTypeHandlerRegistry<M> getCustomTypeHandlerRegistry()
		{
			if(this.customTypeHandlerRegistry == null)
			{
				this.customTypeHandlerRegistry = this.dispatch(this.createCustomTypeHandlerRegistry());
			}
			return this.customTypeHandlerRegistry;
		}

		@Override
		public PersistenceTypeAnalyzer getTypeAnalyzer()
		{
			if(this.typeAnalyzer == null)
			{
				this.typeAnalyzer = this.dispatch(this.createTypeAnalyzer());
			}
			return this.typeAnalyzer;
		}

		@Override
		public PersistenceTypeEvaluator getTypeEvaluatorTypeIdMappable()
		{
			if(this.typeEvaluatorTypeIdMappable == null)
			{
				this.typeEvaluatorTypeIdMappable = this.dispatch(this.createTypeEvaluatorTypeIdMappable());
			}
			return this.typeEvaluatorTypeIdMappable;
		}
		
		@Override
		public PersistenceTypeChangeCallback getTypeChangeCallback()
		{
			if(this.typeChangeCallback == null)
			{
				this.typeChangeCallback = this.dispatch(this.createTypeChangeCallback());
			}
			return this.typeChangeCallback;
		}

		@Override
		public PersistenceTypeResolver getTypeResolver()
		{
			if(this.typeResolver == null)
			{
				this.typeResolver = this.dispatch(this.createTypeResolver());
			}
			return this.typeResolver;
		}
		
		
		@Override
		public PersistenceTypeDictionaryBuilder getTypeDictionaryBuilder()
		{
			if(this.typeDictionaryBuilder == null)
			{
				this.typeDictionaryBuilder = this.dispatch(this.createTypeDictionaryBuilder());
			}
			return this.typeDictionaryBuilder;
		}
		
		@Override
		public PersistenceTypeEvaluator getTypeEvaluatorPersistable()
		{
			if(this.typeEvaluatorPersistable == null)
			{
				this.typeEvaluatorPersistable = this.dispatch(this.createTypeEvaluatorPersistable());
			}
			return this.typeEvaluatorPersistable;
		}

		@Override
		public PersistenceFieldLengthResolver getFieldFixedLengthResolver()
		{
			if(this.fieldFixedLengthResolver == null)
			{
				this.fieldFixedLengthResolver = this.dispatch(this.createFieldFixedLengthResolver());
			}
			return this.fieldFixedLengthResolver;
		}

		@Override
		public BufferSizeProvider getBufferSizeProvider()
		{
			if(this.bufferSizeProvider == null)
			{
				this.bufferSizeProvider = this.dispatch(this.createBufferSizeProvider());
			}
			return this.bufferSizeProvider;
		}

		@Override
		public PersistenceFieldEvaluator getFieldEvaluator()
		{
			if(this.fieldEvaluator == null)
			{
				this.fieldEvaluator = this.dispatch(this.createFieldEvaluator());
			}
			return this.fieldEvaluator;
		}
		
		@Override
		public PersistenceTypeDefinitionBuilder getTypeDescriptionBuilder()
		{
			if(this.typeDescriptionBuilder == null)
			{
				this.typeDescriptionBuilder = this.dispatch(this.createTypeDescriptionBuilder());
			}
			return this.typeDescriptionBuilder;
		}
		
		@Override
		public PersistenceTypeLineageBuilder getTypeLineageBuilder()
		{
			if(this.typeLineageBuilder == null)
			{
				this.typeLineageBuilder = this.dispatch(this.createTypeLineageBuilder());
			}
			return this.typeLineageBuilder;
		}
		
		@Override
		public PersistenceTypeDefinitionInitializerProvider<M> getTypeDescriptionInitializerLookup()
		{
			if(this.typeDefinitionInitializerProvider == null)
			{
				this.typeDefinitionInitializerProvider = this.dispatch(this.createTypeDescriptionInitializerLookup());
			}
			return this.typeDefinitionInitializerProvider;
		}
		
		

		///////////////////////////////////////////////////////////////////////////
		// setters          //
		/////////////////////

		protected final void internalSetTypeSovereignty(
			final PersistenceTypeSovereignty typeSovereignty
		)
		{
			this.typeSovereignty = notNull(typeSovereignty);
		}

		protected final void internalSetSwizzleRegistry(
			final SwizzleRegistry swizzleRegistry
		)
		{
			this.swizzleRegistry = swizzleRegistry;
		}

		protected final void internalSetObjectManager(
			final SwizzleObjectManager objectManager
		)
		{
			this.objectManager = objectManager;
		}

		protected final void internalSetTypeManager(
			final PersistenceTypeHandlerManager<M> typeManager
		)
		{
			this.typeHandlerManager = typeManager;
		}

		protected final void internalSetPersisterCreator(
			final PersistenceStorer.Creator<M> persisterCreator
		)
		{
			this.storerCreator = persisterCreator;
		}

		protected final void internalSetTypeHandlerManager(
			final PersistenceTypeHandlerManager<M> typeHandlerManager
		)
		{
			this.typeHandlerManager = typeHandlerManager;
		}

		protected final void internalSetTypeManager(
			final SwizzleTypeManager typeManager)
		{
			this.typeManager = typeManager;
		}

		protected final void internalSetTypeHandlerRegistry(
			final PersistenceTypeHandlerRegistry<M> typeHandlerRegistry
		)
		{
			this.typeHandlerRegistry = typeHandlerRegistry;
		}

		protected final void internalSetTypeHandlerProvider(
			final PersistenceTypeHandlerProvider<M> typeHandlerProvider
		)
		{
			this.typeHandlerProvider = typeHandlerProvider;
		}

		protected final void internalSetRegistererCreator(
			final PersistenceRegisterer.Creator registererCreator
		)
		{
			this.registererCreator = registererCreator;
		}

		protected final void internalSetBuilderCreator(
			final PersistenceLoader.Creator<M> builderCreator
		)
		{
			this.builderCreator = builderCreator;
		}

		protected final void internalSetTarget(
			final PersistenceTarget<M> target
		)
		{
			this.target = target;
		}

		protected final void internalSetSource(
			final PersistenceSource<M> source
		)
		{
			this.source = source;
		}

		protected final void internalSetTypeDictionaryManager(
			final PersistenceTypeDictionaryManager typeDictionaryManager
		)
		{
			this.typeDictionaryManager = typeDictionaryManager;
		}

		protected final void internalSetTypeDictionaryProvider(
			final PersistenceTypeDictionaryProvider typeDictionaryProvider
		)
		{
			this.typeDictionaryProvider = typeDictionaryProvider;
		}

		protected final void internalSetTypeDictionaryExporter(
			final PersistenceTypeDictionaryExporter typeDictionaryExporter
		)
		{
			this.typeDictionaryExporter = typeDictionaryExporter;
		}

		protected final void internalSetTypeDictionaryParser(final PersistenceTypeDictionaryParser typeDictionaryParser)
		{
			this.typeDictionaryParser = typeDictionaryParser;
		}

		protected final void internalSetTypeDictionaryAssembler(
			final PersistenceTypeDictionaryAssembler typeDictionaryAssembler
		)
		{
			this.typeDictionaryAssembler = typeDictionaryAssembler;
		}

		protected final void internalSetTypeDictionaryLoader(
			final PersistenceTypeDictionaryLoader typeDictionaryLoader
		)
		{
			this.typeDictionaryLoader = typeDictionaryLoader;
		}

		protected final void internalSetTypeDictionaryStorer(
			final PersistenceTypeDictionaryStorer typeDictionaryStorer
		)
		{
			this.typeDictionaryStorer = typeDictionaryStorer;
		}

		protected final void internalSetTypeEvaluatorTypeIdMappable(
			final PersistenceTypeEvaluator typeEvaluatorTypeIdMappable
		)
		{
			this.typeEvaluatorTypeIdMappable = typeEvaluatorTypeIdMappable;
		}

		protected final void internalsetTypeResolver(
			final PersistenceTypeResolver typeResolver
		)
		{
			this.typeResolver = typeResolver;
		}
		
		protected final void internalsetTypeDictionaryBuilder(
			final PersistenceTypeDictionaryBuilder typeDictionaryBuilder
		)
		{
			this.typeDictionaryBuilder = typeDictionaryBuilder;
		}

		protected final void internalSetTypeEvaluatorPersistable(
			final PersistenceTypeEvaluator typeEvaluatorPersistable
		)
		{
			this.typeEvaluatorPersistable = typeEvaluatorPersistable;
		}

		protected final void internalSetBufferSizeProvider(
			final BufferSizeProvider bufferSizeProvider
		)
		{
			this.bufferSizeProvider = bufferSizeProvider;
		}

		protected final void internalSetFieldFixedLengthResolver(
			final PersistenceFieldLengthResolver fieldFixedLengthResolver
		)
		{
			this.fieldFixedLengthResolver = fieldFixedLengthResolver;
		}

		@Override
		public PersistenceFoundation.AbstractImplementation<M> setTypeSovereignty(
			final PersistenceTypeSovereignty typeSovereignty
		)
		{
			this.internalSetTypeSovereignty(typeSovereignty);
			return this;
		}

		@Override
		public PersistenceFoundation.AbstractImplementation<M> setInstanceDispatcher(
			final Dispatcher instanceDispatcher
		)
		{
			this.internalSetDispatcher(instanceDispatcher);
			return this;
		}

		@Override
		public PersistenceFoundation.AbstractImplementation<M> setObjectManager(
			final SwizzleObjectManager objectManager
		)
		{
			this.internalSetObjectManager(objectManager);
			return this;
		}

		@Override
		public PersistenceFoundation.AbstractImplementation<M> setStorerCreator(
			final PersistenceStorer.Creator<M> persisterCreator
		)
		{
			this.internalSetPersisterCreator(persisterCreator);
			return this;
		}

		@Override
		public PersistenceFoundation.AbstractImplementation<M> setTypeHandlerManager(
			final PersistenceTypeHandlerManager<M> typeHandlerManager
		)
		{
			this.internalSetTypeHandlerManager(typeHandlerManager);
			return this;
		}

		@Override
		public PersistenceFoundation.AbstractImplementation<M> setSwizzleRegistry(
			final SwizzleRegistry swizzleRegistry
		)
		{
			this.internalSetSwizzleRegistry(swizzleRegistry);
			return this;
		}

		@Override
		public PersistenceFoundation.AbstractImplementation<M> setObjectIdProvider(
			final SwizzleObjectIdProvider oidProvider
		)
		{
			this.internalSetOidProvider(oidProvider);
			return this;
		}

		@Override
		public PersistenceFoundation.AbstractImplementation<M> setTypeIdProvider(
			final SwizzleTypeIdProvider tidProvider
		)
		{
			this.internalSetTidProvider(tidProvider);
			return this;
		}

		@Override
		public <P extends SwizzleTypeIdProvider & SwizzleObjectIdProvider>
		PersistenceFoundation.AbstractImplementation<M> setSwizzleIdProvider(
			final P swizzleTypeIdProvider
		)
		{
			this.internalSetOidProvider(swizzleTypeIdProvider);
			this.internalSetTidProvider(swizzleTypeIdProvider);
			return this;
		}

		@Override
		public PersistenceFoundation.AbstractImplementation<M> setTypeManager(
			final SwizzleTypeManager typeManager
		)
		{
			this.internalSetTypeManager(typeManager);
			return this;
		}

		@Override
		public PersistenceFoundation.AbstractImplementation<M> setTypeHandlerRegistry(
			final PersistenceTypeHandlerRegistry<M> typeHandlerRegistry
		)
		{
			this.internalSetTypeHandlerRegistry(typeHandlerRegistry);
			return this;
		}

		@Override
		public PersistenceFoundation.AbstractImplementation<M> setTypeHandlerProvider(
			final PersistenceTypeHandlerProvider<M> typeHandlerProvider
		)
		{
			this.internalSetTypeHandlerProvider(typeHandlerProvider);
			return this;
		}

		@Override
		public PersistenceFoundation.AbstractImplementation<M> setRegistererCreator(
			final PersistenceRegisterer.Creator registererCreator
		)
		{
			this.internalSetRegistererCreator(registererCreator);
			return this;
		}

		@Override
		public PersistenceFoundation.AbstractImplementation<M> setBuilderCreator(
			final PersistenceLoader.Creator<M> builderCreator
		)
		{
			this.internalSetBuilderCreator(builderCreator);
			return this;
		}

		@Override
		public PersistenceFoundation.AbstractImplementation<M> setPersistenceTarget(
			final PersistenceTarget<M> target
		)
		{
			this.internalSetTarget(target);
			return this;
		}

		@Override
		public PersistenceFoundation.AbstractImplementation<M> setPersistenceSource(
			final PersistenceSource<M> source
		)
		{
			this.internalSetSource(source);
			return this;
		}

		@Override
		public PersistenceFoundation.AbstractImplementation<M> setTypeDictionaryAssembler(
			final PersistenceTypeDictionaryAssembler typeDictionaryAssembler
		)
		{
			this.internalSetTypeDictionaryAssembler(typeDictionaryAssembler);
			return this;
		}

		@Override
		public PersistenceFoundation.AbstractImplementation<M> setTypeDictionaryProvider(
			final PersistenceTypeDictionaryProvider typeDictionaryReader
		)
		{
			this.internalSetTypeDictionaryProvider(typeDictionaryReader);
			return this;
		}

		@Override
		public PersistenceFoundation.AbstractImplementation<M> setTypeEvaluatorTypeIdMappable(
			final PersistenceTypeEvaluator typeEvaluatorTypeIdMappable
		)
		{
			this.internalSetTypeEvaluatorTypeIdMappable(typeEvaluatorTypeIdMappable);
			return this;
		}

		@Override
		public PersistenceFoundation.AbstractImplementation<M> setTypeResolver(
			final PersistenceTypeResolver typeResolver
		)
		{
			this.internalsetTypeResolver(typeResolver);
			return this;
		}
		
		@Override
		public PersistenceFoundation.AbstractImplementation<M> setTypeDictionaryBuilder(
			final PersistenceTypeDictionaryBuilder typeDictionaryBuilder
		)
		{
			this.internalsetTypeDictionaryBuilder(typeDictionaryBuilder);
			return this;
		}
		
		@Override
		public PersistenceFoundation.AbstractImplementation<M> setTypeEvaluatorPersistable(
			final PersistenceTypeEvaluator typeEvaluatorPersistable
		)
		{
			this.internalSetTypeEvaluatorPersistable(typeEvaluatorPersistable);
			return this;
		}

		@Override
		public PersistenceFoundation.AbstractImplementation<M> setBufferSizeProvider(
			final BufferSizeProvider bufferSizeProvider
		)
		{
			this.internalSetBufferSizeProvider(bufferSizeProvider);
			return this;
		}

		@Override
		public PersistenceFoundation<M> setFieldFixedLengthResolver(
			final PersistenceFieldLengthResolver resolver
		)
		{
			this.internalSetFieldFixedLengthResolver(resolver);
			return this;
		}

		@Override
		public PersistenceFoundation<M> setFieldEvaluator(
			final PersistenceFieldEvaluator fieldEvaluator
		)
		{
			this.fieldEvaluator = fieldEvaluator;
			return this;
		}
		


		///////////////////////////////////////////////////////////////////////////
		// creators         //
		/////////////////////

		protected SwizzleRegistry createSwizzleRegistry()
		{
			final SwizzleRegistryGrowingRange newSwizzleRegistry = new SwizzleRegistryGrowingRange();
			Swizzle.registerDefaults(newSwizzleRegistry);
			return newSwizzleRegistry;
		}

		protected SwizzleObjectManager createObjectManager()
		{
			final SwizzleObjectManager newObjectManager =
				new SwizzleObjectManager.Implementation(
					this.getSwizzleRegistry(),
					this.getObjectIdProvider(),
					this.getTypeManager()
				)
			;
			return newObjectManager;
		}

		protected SwizzleTypeManager createTypeManager()
		{
			final SwizzleTypeManager newTypeManager = new SwizzleTypeManager.Implementation(
				this.getSwizzleRegistry(),
				this.getTypeIdProvider()
			);
			return newTypeManager;
		}

		protected PersistenceTypeHandlerManager<M> createTypeHandlerManager()
		{
			final PersistenceTypeHandlerManager<M> newTypeHandlerManager =
				PersistenceTypeHandlerManager.New(
					this.getTypeHandlerRegistry(),
					this.getTypeHandlerProvider(),
					this.getTypeDictionaryManager(),
					this.getTypeEvaluatorTypeIdMappable(),
					this.getTypeChangeCallback()
				)
			;
			return newTypeHandlerManager;
		}

		protected PersistenceRegisterer.Creator createRegistererCreator()
		{
			return new PersistenceRegisterer.Implementation.Creator();
		}

		protected PersistenceTypeHandlerRegistry<M> createTypeHandlerRegistry()
		{
			// note: sub class should/must register native type handlers in an overridden version of this method
			final PersistenceTypeHandlerRegistry<M> newTypeHandlerRegistry =
				new PersistenceTypeHandlerRegistry.Implementation<>(this.getSwizzleRegistry())
			;
			return newTypeHandlerRegistry;
		}

		protected PersistenceTypeHandlerProvider<M> createTypeHandlerProvider()
		{
			return new PersistenceTypeHandlerProviderCreating<>(
				this.getTypeManager(),
				this.getTypeHandlerEnsurer()
			);
		}

		protected PersistenceTypeDictionaryManager createTypeDictionaryManager()
		{
			final PersistenceTypeDictionaryManager newTypeDictionaryManager =
				new PersistenceTypeDictionaryManager.Implementation(
					this.getTypeDictionaryProvider(),
					this.getTypeDictionaryExporter()
				)
			;
			return newTypeDictionaryManager;
		}

		protected PersistenceTypeDictionaryProvider createTypeDictionaryProvider()
		{
			final PersistenceTypeDictionaryProvider newTypeDictionaryProvider =
				PersistenceTypeDictionaryProvider.New(
					this.getTypeDictionaryLoader() ,
					this.getTypeDictionaryParser() ,
					this.getTypeDictionaryBuilder()
				)
			;
			return new PersistenceTypeDictionaryProvider.Caching(newTypeDictionaryProvider);
		}

		protected PersistenceTypeDictionaryExporter createTypeDictionaryExporter()
		{
			final PersistenceTypeDictionaryExporter newTypeDictionaryExporter =
				new PersistenceTypeDictionaryExporter.Implementation(
					this.getTypeDictionaryAssembler(),
					this.getTypeDictionaryStorer()
				)
			;
			return newTypeDictionaryExporter;
		}

		protected PersistenceTypeDictionaryParser createTypeDictionaryParser()
		{
			final PersistenceTypeDictionaryParser newTypeDictionaryParser =
				new PersistenceTypeDictionaryParser.Implementation(
					this.getFieldFixedLengthResolver()
				)
			;
			return newTypeDictionaryParser;
		}

		protected PersistenceTypeDictionaryAssembler createTypeDictionaryAssembler()
		{
			final PersistenceTypeDictionaryAssembler newTypeDictionaryAssembler =
				new PersistenceTypeDictionaryAssembler.Implementation()
			;
			return newTypeDictionaryAssembler;
		}

		protected PersistenceTypeAnalyzer createTypeAnalyzer()
		{
			return new PersistenceTypeAnalyzer.Implementation(
				this.getTypeEvaluatorPersistable(),
				this.getFieldEvaluator()
			);
		}



		///////////////////////////////////////////////////////////////////////////
		// pseudo-abstract creators //
		/////////////////////////////

		/* Explanation:
		 * These methods are not actually abstract because it is not necessaryly required
		 * to create new instances of these types. Instead, apropriate instances can be set.
		 * These methods exist in order to allow sub classes to implement them optionally
		 * and throw an exception if neither implementation nor set instance is available.
		 */

		protected PersistenceStorer.Creator<M> createStorerCreator()
		{
			throw new MissingAssemblyPartException(PersistenceStorer.Creator.class);
		}

		protected PersistenceLoader.Creator<M> createBuilderCreator()
		{
			throw new MissingAssemblyPartException(PersistenceLoader.Creator.class);
		}

		protected PersistenceTarget<M> createPersistenceTarget()
		{
			throw new MissingAssemblyPartException(PersistenceTarget.class);
		}

		protected PersistenceSource<M> createPersistenceSource()
		{
			throw new MissingAssemblyPartException(PersistenceSource.class);
		}

		protected PersistenceTypeDictionaryLoader createTypeDictionaryLoader()
		{
			throw new MissingAssemblyPartException(PersistenceTypeDictionaryLoader.class);
		}

		protected PersistenceTypeDictionaryStorer createTypeDictionaryStorer()
		{
			throw new MissingAssemblyPartException(PersistenceTypeDictionaryStorer.class);
		}

		protected PersistenceTypeHandlerEnsurer<M> createTypeHandlerEnsurer()
		{
			throw new MissingAssemblyPartException(PersistenceTypeHandlerEnsurer.class);
		}

		protected PersistenceCustomTypeHandlerRegistry<M> createCustomTypeHandlerRegistry()
		{
			throw new MissingAssemblyPartException(PersistenceCustomTypeHandlerRegistry.class);
		}

		protected PersistenceTypeEvaluator createTypeEvaluatorTypeIdMappable()
		{
			return Persistence.defaultTypeEvaluatorTypeIdMappable();
		}
		
		protected PersistenceTypeChangeCallback createTypeChangeCallback()
		{
			// (06.10.2017 TM)NOTE: by default, type changes cause the initialization to abort with an exception
			return PersistenceTypeChangeCallback.Aborting();
		}

		protected PersistenceTypeResolver createTypeResolver()
		{
			return PersistenceTypeResolver.Failing();
		}
				
		protected PersistenceTypeDictionaryBuilder createTypeDictionaryBuilder()
		{
			return PersistenceTypeDictionaryBuilder.New(
				this.getTypeLineageBuilder()
			);
		}

		protected PersistenceTypeEvaluator createTypeEvaluatorPersistable()
		{
			return Persistence.defaultTypeEvaluatorPersistable();
		}

		protected PersistenceFieldLengthResolver createFieldFixedLengthResolver()
		{
			throw new MissingAssemblyPartException(PersistenceFieldLengthResolver.class);
		}

		protected BufferSizeProvider createBufferSizeProvider()
		{
			return new BufferSizeProvider.PageSize();
		}

		protected PersistenceFieldEvaluator createFieldEvaluator()
		{
			return Persistence.defaultFieldEvaluator();
		}
				
		protected PersistenceTypeDefinitionBuilder createTypeDescriptionBuilder()
		{
			throw new net.jadoth.meta.NotImplementedYetError(); // FIXME PersistenceFoundation.AbstractImplementation#createTypeDescriptionBuilder()
		}
		
		protected PersistenceTypeLineageBuilder createTypeLineageBuilder()
		{
			throw new net.jadoth.meta.NotImplementedYetError(); // FIXME PersistenceFoundation.AbstractImplementation#createTypeDescriptionBuilder()
		}

		protected PersistenceTypeDefinitionInitializerProvider<M> createTypeDescriptionInitializerLookup()
		{
			return PersistenceTypeDefinitionInitializerProvider.New(
				this.getTypeHandlerProvider(),
				this.getTypeHandlerManager()
			);
		}



		///////////////////////////////////////////////////////////////////////////
		// override methods //
		/////////////////////

		@Override
		public PersistenceManager<M> createPersistenceManager()
		{
			final PersistenceTypeHandlerManager<M> typeHandlerManager = this.getTypeHandlerManager();
			typeHandlerManager.initialize(); // initialize type handlers (i.e. import/validate type dictionary)

			final PersistenceManager<M> newPersistenceManager =
				new PersistenceManager.Implementation<>(
					this.getSwizzleRegistry(),
					this.getObjectManager(),
					typeHandlerManager,
					this.getStorerCreator(),
					this.getBuilderCreator(),
					this.getRegistererCreator(),
					this.getPersistenceTarget(),
					this.getPersistenceSource(),
					this.getBufferSizeProvider()
				)
			;
			return newPersistenceManager;
		}

	}

}

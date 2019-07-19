package one.microstream.persistence.types;

import java.nio.ByteOrder;

import one.microstream.X;
import one.microstream.collections.types.XEnum;
import one.microstream.exceptions.MissingFoundationPartException;
import one.microstream.functional.InstanceDispatcherLogic;
import one.microstream.persistence.internal.PersistenceTypeHandlerProviderCreating;
import one.microstream.persistence.types.PersistenceRootResolver.Builder;
import one.microstream.reference.Reference;
import one.microstream.typing.LambdaTypeRecognizer;
import one.microstream.typing.TypeMapping;
import one.microstream.typing.XTypes;
import one.microstream.util.BufferSizeProviderIncremental;
import one.microstream.util.Cloneable;
import one.microstream.util.InstanceDispatcher;


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
public interface PersistenceFoundation<M, F extends PersistenceFoundation<M, ?>>
extends Cloneable<PersistenceFoundation<M, F>>, ByteOrderTargeting.Mutable<F>
{
	// the pseudo-self-type F is to avoid having to override every setter in every sub class (it was really tedious)
	
	@Override
	public PersistenceFoundation<M, F> Clone();

	public InstanceDispatcherLogic getInstanceDispatcherLogic(); // (14.04.2013)XXX: move dispatching aspect to separate super type


	public PersistenceObjectIdProvider getObjectIdProvider();

	public PersistenceTypeIdProvider getTypeIdProvider();


	public F setObjectIdProvider(PersistenceObjectIdProvider oidProvider);

	public F setTypeIdProvider(PersistenceTypeIdProvider tidProvider);

	public <P extends PersistenceTypeIdProvider & PersistenceObjectIdProvider>
	F setIdProvider(P idProvider);
	

	public PersistenceStorer.Creator<M> getStorerCreator();

	public PersistenceRegisterer.Creator getRegistererCreator();

	public PersistenceLoader.Creator<M> getBuilderCreator();

	public PersistenceTarget<M> getPersistenceTarget();

	public PersistenceSource<M> getPersistenceSource();
	
	
	public PersistenceObjectRegistry getObjectRegistry();

	public PersistenceObjectManager getObjectManager();
	
	public PersistenceTypeRegistry getTypeRegistry();
	
	public PersistenceTypeManager getTypeManager();

	public PersistenceTypeHandlerManager<M> getTypeHandlerManager();
	
	public PersistenceContextDispatcher<M> getContextDispatcher();

	public PersistenceTypeHandlerProvider<M> getTypeHandlerProvider();

	public PersistenceTypeHandlerEnsurer<M> getTypeHandlerEnsurer();

	public PersistenceTypeHandlerRegistry<M> getTypeHandlerRegistry();

	public PersistenceTypeDictionaryManager getTypeDictionaryManager();
	
	public PersistenceTypeDictionaryCreator getTypeDictionaryCreator();

	public PersistenceTypeDictionaryProvider getTypeDictionaryProvider();

	public PersistenceTypeDictionaryExporter getTypeDictionaryExporter();

	public PersistenceTypeDictionaryParser getTypeDictionaryParser();

	public PersistenceTypeDictionaryLoader getTypeDictionaryLoader();
	
	public PersistenceTypeDictionaryBuilder getTypeDictionaryBuilder();
	
	public PersistenceTypeDictionaryCompiler getTypeDictionaryCompiler();

	public PersistenceTypeDictionaryAssembler getTypeDictionaryAssembler();

	public PersistenceTypeDictionaryStorer getTypeDictionaryStorer();
	
	public PersistenceTypeLineageCreator getTypeLineageCreator();

	public PersistenceTypeHandlerCreator<M> getTypeHandlerCreator();

	public PersistenceCustomTypeHandlerRegistry<M> getCustomTypeHandlerRegistry();

	public PersistenceTypeAnalyzer getTypeAnalyzer();

	public PersistenceTypeEvaluator getTypeEvaluatorTypeIdMappable();
	
	public PersistenceTypeMismatchValidator<M> getTypeMismatchValidator();
	
	public PersistenceTypeDefinitionCreator getTypeDefinitionCreator();

	public PersistenceTypeEvaluator getTypeEvaluatorPersistable();

	public PersistenceFieldLengthResolver getFieldFixedLengthResolver();
	
	public PersistenceEagerStoringFieldEvaluator getReferenceFieldMandatoryEvaluator();

	public BufferSizeProviderIncremental getBufferSizeProvider();

	public PersistenceFieldEvaluator getFieldEvaluatorPersistable();
	
	public PersistenceFieldEvaluator getFieldEvaluatorCollection();
	
	public PersistenceRootResolver.Builder getRootResolverBuilder();
	
	public PersistenceRootResolver getRootResolver();
	
	public PersistenceRootsProvider<M> getRootsProvider();
	
	public PersistenceUnreachableTypeHandlerCreator<M> getUnreachableTypeHandlerCreator();
	
	public PersistenceLegacyTypeMapper<M> getLegacyTypeMapper();

	public PersistenceRefactoringMappingProvider getRefactoringMappingProvider();

	public PersistenceRefactoringResolverProvider getRefactoringResolverProvider();

	public XEnum<? extends PersistenceRefactoringTypeIdentifierBuilder>  getRefactoringLegacyTypeIdentifierBuilders();
	
	public XEnum<? extends PersistenceRefactoringMemberIdentifierBuilder> getRefactoringLegacyMemberIdentifierBuilders();
	
	public XEnum<? extends PersistenceRefactoringMemberIdentifierBuilder> getRefactoringCurrentMemberIdentifierBuilders();
	
	public TypeMapping<Float> getTypeSimilarity();
	
	public PersistenceMemberMatchingProvider getLegacyMemberMatchingProvider();
	
	public PersistenceLegacyTypeMappingResultor<M> getLegacyTypeMappingResultor();
	
	public PersistenceLegacyTypeHandlerCreator<M> getLegacyTypeHandlerCreator();
	
	public PersistenceLegacyTypeHandlingListener<M> getLegacyTypeHandlingListener();
	
	public PersistenceSizedArrayLengthController getSizedArrayLengthController();

	public PersistenceRootResolver rootResolver();
	
	public LambdaTypeRecognizer getLambdaTypeRecognizer();
	
	
	
	public F setObjectRegistry(PersistenceObjectRegistry objectRegistry);
	
	public F setTypeRegistry(PersistenceTypeRegistry typeRegistry);

	public F setInstanceDispatcher(InstanceDispatcherLogic instanceDispatcher);

	public F setObjectManager(PersistenceObjectManager objectManager);

	public F setStorerCreator(PersistenceStorer.Creator<M> storerCreator);

	public F setTypeHandlerManager(PersistenceTypeHandlerManager<M> typeHandlerManager);
	
	public F setContextDispatcher(PersistenceContextDispatcher<M> contextDispatcher);

	public F setTypeManager(PersistenceTypeManager typeManager);

	public F setTypeHandlerCreatorLookup(PersistenceTypeHandlerEnsurer<M> typeHandlerCreatorLookup);
	
	public F setTypeHandlerCreator(PersistenceTypeHandlerCreator<M> typeHandlerCreator);

	public F setTypeHandlerRegistry(PersistenceTypeHandlerRegistry<M> typeHandlerRegistry);

	public F setTypeHandlerProvider(PersistenceTypeHandlerProvider<M> typeHandlerProvider);

	public F setRegistererCreator(PersistenceRegisterer.Creator registererCreator);

	public F setBuilderCreator(PersistenceLoader.Creator<M> builderCreator);

	public F setPersistenceTarget(PersistenceTarget<M> target);

	public F setPersistenceSource(PersistenceSource<M> source);

	public F setTypeDictionaryManager(PersistenceTypeDictionaryManager typeDictionaryManager);
	
	public F setTypeDictionaryCreator(PersistenceTypeDictionaryCreator typeDictionaryCreator);
		
	public F setTypeDictionaryProvider(PersistenceTypeDictionaryProvider typeDictionaryProvider);
	
	public F setTypeDictionaryExporter(PersistenceTypeDictionaryExporter typeDictionaryExporter);
	
	public F setTypeDictionaryParser(PersistenceTypeDictionaryParser typeDictionaryParser);
	
	public F setTypeDictionaryAssembler(PersistenceTypeDictionaryAssembler typeDictionaryAssembler);
	
	public F setTypeDictionaryLoader(PersistenceTypeDictionaryLoader typeDictionaryLoader);
	
	public F setTypeDictionaryBuilder(PersistenceTypeDictionaryBuilder typeDictionaryBuilder);

	public F setTypeDictionaryCompiler(PersistenceTypeDictionaryCompiler typeDictionaryCompiler);
	
	public F setTypeDictionaryStorer(PersistenceTypeDictionaryStorer typeDictionaryStorer);

	public <H extends PersistenceTypeDictionaryLoader & PersistenceTypeDictionaryStorer> F setTypeDictionaryIoHandling(
		H typeDictionaryStorage
	);
	
	public default F setTypeDictionaryIoHandler(
		final PersistenceTypeDictionaryIoHandler typeDictionaryIoHandler
	)
	{
		return this.setTypeDictionaryIoHandling(typeDictionaryIoHandler);
	}
	
	public F setTypeLineageCreator(PersistenceTypeLineageCreator typeLineageCreator);

	public F setTypeEvaluatorTypeIdMappable(PersistenceTypeEvaluator typeEvaluatorTypeIdMappable);
	
	public F setTypeMismatchValidator(PersistenceTypeMismatchValidator<M> typeMismatchValidator);
	
	public F setTypeDescriptionBuilder(PersistenceTypeDefinitionCreator typeDefinitionCreator);

	/* (29.10.2013 TM)TODO: rename to "TypeEvaluatorAnalyzable" and keep comment
	 * rationale (keep as documentation afterwards)
	 *
	 * The difference between not persistable and not analyzable is:
	 * Many implementations are very well persistable in a generic way (created by the analyzer), however that
	 * way would be very inefficient (e.g. double linked list, storing every entry as an instance is a total overkill).
	 * So there has to a way to only throw an exception of no custom handler was found before (this mechanism already
	 * works that way). As a consequence, this means that the logik behind the mechanism does not "just" evaluate
	 * persistabilty, but generic analyzability.
	 * Unpersistable types can still end up there and still throw an exception.
	 * Or they might even get assigned a custom handler and cause no problem at all (e.g. if someone implements
	 * a handler for java.lang.Thread that is sufficient for a specific project, then why not).
	 */
	public F setTypeEvaluatorPersistable(PersistenceTypeEvaluator typeEvaluatorPersistable);

	public F setBufferSizeProvider(BufferSizeProviderIncremental bufferSizeProvider);

	public F setFieldFixedLengthResolver(PersistenceFieldLengthResolver fieldFixedLengthResolver);

	public F setFieldEvaluatorPersistable(PersistenceFieldEvaluator fieldEvaluator);
	
	public F setFieldEvaluatorCollection(PersistenceFieldEvaluator fieldEvaluator);

	public F setReferenceFieldMandatoryEvaluator(PersistenceEagerStoringFieldEvaluator evaluator);

	public F setRootResolver(PersistenceRootResolver.Builder rootResolverBuilder);
	
	public F setRootResolver(PersistenceRootResolver rootResolver);
	
	public F setLambdaTypeRecognizer(LambdaTypeRecognizer lambdaTypeRecognizer);

	public F setRootsProvider(PersistenceRootsProvider<M> rootsProvider);
	
	public F setUnreachableTypeHandlerCreator(PersistenceUnreachableTypeHandlerCreator<M> unreachableTypeHandlerCreator);
	
	public F setLegacyTypeMapper(PersistenceLegacyTypeMapper<M> legacyTypeMapper);
	
	public F setTypeSimilarity(TypeMapping<Float> typeSimilarity);
	
	public F setRefactoringMappingProvider(PersistenceRefactoringMappingProvider refactoringMappingProvider);
	
	public F setRefactoringResolverProvider(PersistenceRefactoringResolverProvider refactoringResolverProvider);
	
	public F setRefactoringLegacyTypeIdentifierBuilders(
		XEnum<? extends PersistenceRefactoringTypeIdentifierBuilder> typeIdentifierBuilders
	);
	
	public F setRefactoringLegacyMemberIdentifierBuilders(
		XEnum<? extends PersistenceRefactoringMemberIdentifierBuilder> memberIdentifierBuilders
	);
	
	public F setRefactoringCurrentMemberIdentifierBuilders(
		XEnum<? extends PersistenceRefactoringMemberIdentifierBuilder> memberIdentifierBuilders
	);
		
	public F setLegacyMemberMatchingProvider(PersistenceMemberMatchingProvider legacyMemberMatchingProvider);
		
	public F setLegacyTypeMappingResultor(PersistenceLegacyTypeMappingResultor<M> legacyTypeMappingResultor);
	
	public F setLegacyTypeHandlerCreator(PersistenceLegacyTypeHandlerCreator<M> legacyTypeHandlerCreator);
	
	public F setLegacyTypeHandlingListener(PersistenceLegacyTypeHandlingListener<M> legacyTypeHandlingListener);

	public F setPersistenceChannel(PersistenceChannel<M> persistenceChannel);
	
	public F setSizedArrayLengthController(PersistenceSizedArrayLengthController sizedArrayLengthController);

	
	/**
	 * Executes the passed {@link PersistenceTypeHandlerRegistration} logic while supplying this instance's
	 * {@link PersistenceCustomTypeHandlerRegistry} and {@link PersistenceSizedArrayLengthController} instances.
	 * The passed instance itself will not be referenced after the method exits.
	 * 
	 * @param typeHandlerRegistration the {@link PersistenceTypeHandlerRegistration} to be executed.
	 * 
	 * @return {@literal this} to allow method chaining.
	 */
	public F executeTypeHandlerRegistration(PersistenceTypeHandlerRegistration<M> typeHandlerRegistration);
	

	/*
	 * generic name is intentional as the role of the created instance may change in extended types
	 * (e.g. representing a database connection)
	 */
	public PersistenceManager<M> createPersistenceManager();



	public static <M> PersistenceFoundation<M, ?> New()
	{
		return new PersistenceFoundation.Default<>();
	}
	
	public class Default<M, F extends PersistenceFoundation.Default<M, ?>>
	extends InstanceDispatcher.Default
	implements PersistenceFoundation<M, F>, Unpersistable
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		// required to resolve a TypeHandlerManager dependancy loop
		private final Reference<PersistenceTypeHandlerManager<M>> referenceTypeHandlerManager = X.Reference(null);
		
		private PersistenceObjectIdProvider oidProvider;
		private PersistenceTypeIdProvider   tidProvider;

		// first level assembly parts (used directly to build manager instance) \\
		private PersistenceTypeRegistry          typeRegistry      ;
		private PersistenceObjectRegistry        objectRegistry    ;
		private PersistenceTypeHandlerManager<M> typeHandlerManager;
		private PersistenceContextDispatcher<M>  contextDispatcher ;
		private PersistenceStorer.Creator<M>     storerCreator     ;
		private PersistenceRegisterer.Creator    registererCreator ;
		private PersistenceLoader.Creator<M>     builderCreator    ;
		private PersistenceTarget<M>             target            ;
		private PersistenceSource<M>             source            ;
		

		// second level assembly parts (used as a fallback to build missing first level parts) \\
		private PersistenceTypeManager                  typeManager                ;
		private PersistenceObjectManager                objectManager              ;
		private PersistenceTypeHandlerEnsurer<M>        typeHandlerEnsurer         ;
		private PersistenceTypeHandlerRegistry<M>       typeHandlerRegistry        ;
		private PersistenceTypeHandlerProvider<M>       typeHandlerProvider        ;
		private PersistenceTypeDictionaryManager        typeDictionaryManager      ;
		private PersistenceTypeDictionaryCreator        typeDictionaryCreator      ;
		private PersistenceTypeDictionaryProvider       typeDictionaryProvider     ;
		private PersistenceTypeDictionaryExporter       typeDictionaryExporter     ;
		private PersistenceTypeDictionaryLoader         typeDictionaryLoader       ;
		private PersistenceTypeDictionaryParser         typeDictionaryParser       ;
		private PersistenceTypeDictionaryBuilder        typeDictionaryBuilder      ;
		private PersistenceTypeDictionaryCompiler       typeDictionaryCompiler     ;
		private PersistenceTypeDictionaryAssembler      typeDictionaryAssembler    ;
		private PersistenceTypeDictionaryStorer         typeDictionaryStorer       ;
		private PersistenceTypeLineageCreator           typeLineageCreator         ;
		private PersistenceTypeHandlerCreator<M>        typeHandlerCreator         ;
		private PersistenceCustomTypeHandlerRegistry<M> customTypeHandlerRegistry  ;
		private PersistenceTypeAnalyzer                 typeAnalyzer               ;
		private PersistenceTypeEvaluator                typeEvaluatorTypeIdMappable;
		private PersistenceTypeMismatchValidator<M>     typeMismatchValidator      ;
		private PersistenceTypeDefinitionCreator        typeDefinitionCreator      ;
		private PersistenceTypeEvaluator                typeEvaluatorPersistable   ;
		private PersistenceFieldLengthResolver          fieldFixedLengthResolver   ;
		private BufferSizeProviderIncremental           bufferSizeProvider         ;
		private PersistenceFieldEvaluator               fieldEvaluatorPersistable  ;
		private PersistenceFieldEvaluator               fieldEvaluatorCollection   ;
		private PersistenceEagerStoringFieldEvaluator   eagerStoringFieldEvaluator ;
		private PersistenceRootResolver.Builder         rootResolverBuilder        ;
		private PersistenceRootResolver                 rootResolver               ;
		private PersistenceRootsProvider<M>             rootsProvider              ;
		private PersistenceSizedArrayLengthController   sizedArrayLengthController ;
		private LambdaTypeRecognizer                    lambdaTypeRecognizer       ;
		
		// (14.09.2018 TM)NOTE: that legacy mapping stuff grows to a size where it could use its own foundation.
		private PersistenceUnreachableTypeHandlerCreator<M> unreachableTypeHandlerCreator;
		private PersistenceLegacyTypeMapper<M>              legacyTypeMapper             ;
		private PersistenceRefactoringMappingProvider       refactoringMappingProvider   ;
		private PersistenceRefactoringResolverProvider      refactoringResolverProvider  ;

		private XEnum<? extends PersistenceRefactoringTypeIdentifierBuilder>   refactoringLegacyTypeIdentifierBuilders   ;
		private XEnum<? extends PersistenceRefactoringMemberIdentifierBuilder> refactoringLegacyMemberIdentifierBuilders ;
		private XEnum<? extends PersistenceRefactoringMemberIdentifierBuilder> refactoringCurrentMemberIdentifierBuilders;
		
		private TypeMapping<Float>                       typeSimilarity              ;
		private PersistenceMemberMatchingProvider        legacyMemberMatchingProvider;
		private PersistenceLegacyTypeMappingResultor<M>  legacyTypeMappingResultor   ;
		private PersistenceLegacyTypeHandlerCreator<M>   legacyTypeHandlerCreator    ;
		private PersistenceLegacyTypeHandlingListener<M> legacyTypeHandlingListener  ;
		
		private ByteOrder targetByteOrder;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		protected Default()
		{
			super();
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@SuppressWarnings("unchecked") // magic self-type.
		protected final F $()
		{
			return (F)this;
		}
		
		@Override
		public PersistenceFoundation.Default<M, F> Clone()
		{
			return new PersistenceFoundation.Default<>();
		}

		
		
		///////////////////////////////////////////////////////////////////////////
		// getters //
		////////////
		
		@Override
		public InstanceDispatcherLogic getInstanceDispatcherLogic()
		{
			return this.getInstanceDispatcherLogic();
		}
		
		protected Reference<PersistenceTypeHandlerManager<M>> referenceTypeHandlerManager()
		{
			return this.referenceTypeHandlerManager;
		}

		@Override
		public PersistenceObjectIdProvider getObjectIdProvider()
		{
			if(this.oidProvider == null)
			{
				this.oidProvider = this.dispatch(this.ensureObjectIdProvider());
			}
			return this.oidProvider;
		}

		@Override
		public PersistenceTypeIdProvider getTypeIdProvider()
		{
			if(this.tidProvider == null)
			{
				this.tidProvider = this.dispatch(this.ensureTypeIdProvider());
			}
			return this.tidProvider;
		}
		
		@Override
		public PersistenceTypeLineageCreator getTypeLineageCreator()
		{
			if(this.typeLineageCreator == null)
			{
				this.typeLineageCreator = this.dispatch(this.ensureTypeLineageCreator());
			}
			
			return this.typeLineageCreator;
		}

		@Override
		public PersistenceObjectRegistry getObjectRegistry()
		{
			if(this.objectRegistry == null)
			{
				this.objectRegistry = this.dispatch(this.ensureObjectRegistry());
			}
			
			return this.objectRegistry;
		}
		
		@Override
		public PersistenceTypeRegistry getTypeRegistry()
		{
			if(this.typeRegistry == null)
			{
				this.typeRegistry = this.dispatch(this.ensureTypeRegistry());
			}
			
			return this.typeRegistry;
		}

		@Override
		public PersistenceObjectManager getObjectManager()
		{
			if(this.objectManager == null)
			{
				this.objectManager = this.dispatch(this.ensureObjectManager());
			}
			
			return this.objectManager;
		}

		@Override
		public PersistenceTypeHandlerManager<M> getTypeHandlerManager()
		{
			if(this.typeHandlerManager == null)
			{
				this.internalSetTypeHandlerManager(
					this.dispatch(this.ensureTypeHandlerManager())
				);
			}
			
			return this.typeHandlerManager;
		}
		
		@Override
		public PersistenceContextDispatcher<M> getContextDispatcher()
		{
			if(this.contextDispatcher == null)
			{
				this.contextDispatcher = this.dispatch(this.ensureContextDispatcher());
			}
			
			return this.contextDispatcher;
		}

		@Override
		public PersistenceStorer.Creator<M> getStorerCreator()
		{
			if(this.storerCreator == null)
			{
				this.storerCreator = this.dispatch(this.ensureStorerCreator());
			}
			
			return this.storerCreator;
		}

		@Override
		public PersistenceRegisterer.Creator getRegistererCreator()
		{
			if(this.registererCreator == null)
			{
				this.registererCreator = this.dispatch(this.ensureRegistererCreator());
			}
			
			return this.registererCreator;
		}

		@Override
		public PersistenceLoader.Creator<M> getBuilderCreator()
		{
			if(this.builderCreator == null)
			{
				this.builderCreator = this.dispatch(this.ensureBuilderCreator());
			}
			
			return this.builderCreator;
		}

		@Override
		public PersistenceTarget<M> getPersistenceTarget()
		{
			if(this.target == null)
			{
				this.target = this.dispatch(this.ensurePersistenceTarget());
			}
			
			return this.target;
		}

		@Override
		public PersistenceSource<M> getPersistenceSource()
		{
			if(this.source == null)
			{
				this.source = this.dispatch(this.ensurePersistenceSource());
			}
			
			return this.source;
		}

		@Override
		public PersistenceTypeHandlerRegistry<M> getTypeHandlerRegistry()
		{
			if(this.typeHandlerRegistry == null)
			{
				this.typeHandlerRegistry = this.dispatch(this.ensureTypeHandlerRegistry());
			}
			
			return this.typeHandlerRegistry;
		}

		@Override
		public PersistenceTypeHandlerProvider<M> getTypeHandlerProvider()
		{
			if(this.typeHandlerProvider == null)
			{
				this.typeHandlerProvider = this.dispatch(this.ensureTypeHandlerProvider());
			}
			
			return this.typeHandlerProvider;
		}

		@Override
		public PersistenceTypeManager getTypeManager()
		{
			if(this.typeManager == null)
			{
				this.typeManager = this.dispatch(this.ensureTypeManager());
			}
			
			return this.typeManager;
		}

		@Override
		public PersistenceTypeHandlerEnsurer<M> getTypeHandlerEnsurer()
		{
			if(this.typeHandlerEnsurer == null)
			{
				this.typeHandlerEnsurer = this.dispatch(this.ensureTypeHandlerEnsurer());
			}
			
			return this.typeHandlerEnsurer;
		}

		@Override
		public PersistenceTypeDictionaryProvider getTypeDictionaryProvider()
		{
			if(this.typeDictionaryProvider == null)
			{
				this.typeDictionaryProvider = this.dispatch(this.ensureTypeDictionaryProvider());
			}
			
			return this.typeDictionaryProvider;
		}

		@Override
		public PersistenceTypeDictionaryManager getTypeDictionaryManager()
		{
			if(this.typeDictionaryManager == null)
			{
				this.typeDictionaryManager = this.dispatch(this.ensureTypeDictionaryManager());
			}
			
			return this.typeDictionaryManager;
		}
		
		@Override
		public PersistenceTypeDictionaryCreator getTypeDictionaryCreator()
		{
			if(this.typeDictionaryCreator == null)
			{
				this.typeDictionaryCreator = this.dispatch(this.ensureTypeDictionaryCreator());
			}
			
			return this.typeDictionaryCreator;
		}
		
		@Override
		public PersistenceTypeDictionaryExporter getTypeDictionaryExporter()
		{
			if(this.typeDictionaryExporter == null)
			{
				this.typeDictionaryExporter = this.dispatch(this.ensureTypeDictionaryExporter());
			}
			
			return this.typeDictionaryExporter;
		}

		@Override
		public PersistenceTypeDictionaryParser getTypeDictionaryParser()
		{
			if(this.typeDictionaryParser == null)
			{
				this.typeDictionaryParser = this.dispatch(this.ensureTypeDictionaryParser());
			}
			
			return this.typeDictionaryParser;
		}

		@Override
		public PersistenceTypeDictionaryLoader getTypeDictionaryLoader()
		{
			if(this.typeDictionaryLoader == null)
			{
				this.typeDictionaryLoader = this.dispatch(this.ensureTypeDictionaryLoader());
			}
			
			return this.typeDictionaryLoader;
		}
		
		@Override
		public PersistenceTypeDictionaryBuilder getTypeDictionaryBuilder()
		{
			if(this.typeDictionaryBuilder == null)
			{
				this.typeDictionaryBuilder = this.dispatch(this.ensureTypeDictionaryBuilder());
			}
			
			return this.typeDictionaryBuilder;
		}
		
		@Override
		public PersistenceTypeDictionaryCompiler getTypeDictionaryCompiler()
		{
			if(this.typeDictionaryCompiler == null)
			{
				this.typeDictionaryCompiler = this.dispatch(this.ensureTypeDictionaryCompiler());
			}
			
			return this.typeDictionaryCompiler;
		}

		@Override
		public PersistenceTypeDictionaryAssembler getTypeDictionaryAssembler()
		{
			if(this.typeDictionaryAssembler == null)
			{
				this.typeDictionaryAssembler = this.dispatch(this.ensureTypeDictionaryAssembler());
			}
			
			return this.typeDictionaryAssembler;
		}

		@Override
		public PersistenceTypeDictionaryStorer getTypeDictionaryStorer()
		{
			if(this.typeDictionaryStorer == null)
			{
				this.typeDictionaryStorer = this.dispatch(this.ensureTypeDictionaryStorer());
			}
			
			return this.typeDictionaryStorer;
		}

		@Override
		public PersistenceTypeHandlerCreator<M> getTypeHandlerCreator()
		{
			if(this.typeHandlerCreator == null)
			{
				this.typeHandlerCreator = this.dispatch(this.ensureTypeHandlerCreator());
			}
			
			return this.typeHandlerCreator;
		}

		@Override
		public PersistenceCustomTypeHandlerRegistry<M> getCustomTypeHandlerRegistry()
		{
			if(this.customTypeHandlerRegistry == null)
			{
				this.customTypeHandlerRegistry = this.dispatch(this.ensureCustomTypeHandlerRegistry());
			}
			
			return this.customTypeHandlerRegistry;
		}

		@Override
		public PersistenceTypeAnalyzer getTypeAnalyzer()
		{
			if(this.typeAnalyzer == null)
			{
				this.typeAnalyzer = this.dispatch(this.ensureTypeAnalyzer());
			}
			
			return this.typeAnalyzer;
		}

		@Override
		public PersistenceTypeEvaluator getTypeEvaluatorTypeIdMappable()
		{
			if(this.typeEvaluatorTypeIdMappable == null)
			{
				this.typeEvaluatorTypeIdMappable = this.dispatch(this.ensureTypeEvaluatorTypeIdMappable());
			}
			
			return this.typeEvaluatorTypeIdMappable;
		}
		
		@Override
		public PersistenceTypeMismatchValidator<M> getTypeMismatchValidator()
		{
			if(this.typeMismatchValidator == null)
			{
				this.typeMismatchValidator = this.dispatch(this.ensureTypeMismatchValidator());
			}
			
			return this.typeMismatchValidator;
		}
		
		@Override
		public PersistenceTypeDefinitionCreator getTypeDefinitionCreator()
		{
			if(this.typeDefinitionCreator == null)
			{
				this.typeDefinitionCreator = this.dispatch(this.ensureTypeDefinitionCreator());
			}
			
			return this.typeDefinitionCreator;
		}

		@Override
		public PersistenceTypeEvaluator getTypeEvaluatorPersistable()
		{
			if(this.typeEvaluatorPersistable == null)
			{
				this.typeEvaluatorPersistable = this.dispatch(this.ensureTypeEvaluatorPersistable());
			}
			
			return this.typeEvaluatorPersistable;
		}

		@Override
		public PersistenceFieldLengthResolver getFieldFixedLengthResolver()
		{
			if(this.fieldFixedLengthResolver == null)
			{
				this.fieldFixedLengthResolver = this.dispatch(this.ensureFieldFixedLengthResolver());
			}
			
			return this.fieldFixedLengthResolver;
		}

		@Override
		public BufferSizeProviderIncremental getBufferSizeProvider()
		{
			if(this.bufferSizeProvider == null)
			{
				this.bufferSizeProvider = this.dispatch(this.ensureBufferSizeProvider());
			}
			
			return this.bufferSizeProvider;
		}

		@Override
		public PersistenceFieldEvaluator getFieldEvaluatorPersistable()
		{
			if(this.fieldEvaluatorPersistable == null)
			{
				this.fieldEvaluatorPersistable = this.dispatch(this.ensureFieldEvaluatorPersistable());
			}
			
			return this.fieldEvaluatorPersistable;
		}
		
		@Override
		public PersistenceFieldEvaluator getFieldEvaluatorCollection()
		{
			if(this.fieldEvaluatorCollection == null)
			{
				this.fieldEvaluatorCollection = this.dispatch(this.ensureFieldEvaluatorCollection());
			}
			
			return this.fieldEvaluatorCollection;
		}
		
		@Override
		public PersistenceEagerStoringFieldEvaluator getReferenceFieldMandatoryEvaluator()
		{
			if(this.eagerStoringFieldEvaluator == null)
			{
				this.eagerStoringFieldEvaluator = this.dispatch(this.ensureReferenceFieldMandatoryEvaluator());
			}
			
			return this.eagerStoringFieldEvaluator;
		}
		
		@Override
		public PersistenceRootResolver.Builder getRootResolverBuilder()
		{

			if(this.rootResolverBuilder == null)
			{
				this.rootResolverBuilder = this.dispatch(this.ensureRootResolverBuilder());
			}
			
			return this.rootResolverBuilder;
		}

		@Override
		public PersistenceRootResolver getRootResolver()
		{
			if(this.rootResolver == null)
			{
				this.rootResolver = this.dispatch(this.ensureRootResolver());
			}
			
			return this.rootResolver;
		}
		
		@Override
		public PersistenceUnreachableTypeHandlerCreator<M> getUnreachableTypeHandlerCreator()
		{
			if(this.unreachableTypeHandlerCreator == null)
			{
				this.unreachableTypeHandlerCreator = this.dispatch(this.ensureUnreachableTypeHandlerCreator());
			}
			
			return this.unreachableTypeHandlerCreator;
		}
		
		@Override
		public PersistenceLegacyTypeMapper<M> getLegacyTypeMapper()
		{
			if(this.legacyTypeMapper == null)
			{
				this.legacyTypeMapper = this.dispatch(this.ensureLegacyTypeMapper());
			}
			
			return this.legacyTypeMapper;
		}
		
		@Override
		public PersistenceRefactoringMappingProvider getRefactoringMappingProvider()
		{
			if(this.refactoringMappingProvider == null)
			{
				this.refactoringMappingProvider = this.dispatch(this.ensureRefactoringMappingProvider());
			}
			
			return this.refactoringMappingProvider;
		}
		
		@Override
		public PersistenceRefactoringResolverProvider getRefactoringResolverProvider()
		{
			if(this.refactoringResolverProvider == null)
			{
				this.refactoringResolverProvider = this.dispatch(this.ensureRefactoringResolverProvider());
			}
			
			return this.refactoringResolverProvider;
		}
		
		@Override
		public XEnum<? extends PersistenceRefactoringTypeIdentifierBuilder> getRefactoringLegacyTypeIdentifierBuilders()
		{
			if(this.refactoringLegacyTypeIdentifierBuilders == null)
			{
				this.refactoringLegacyTypeIdentifierBuilders = this.dispatch(this.ensureRefactoringLegacyTypeIdentifierBuilders());
			}
			
			return this.refactoringLegacyTypeIdentifierBuilders;
		}
		
		@Override
		public XEnum<? extends PersistenceRefactoringMemberIdentifierBuilder> getRefactoringLegacyMemberIdentifierBuilders()
		{
			if(this.refactoringLegacyMemberIdentifierBuilders == null)
			{
				this.refactoringLegacyMemberIdentifierBuilders = this.dispatch(this.ensureRefactoringLegacyMemberIdentifierBuilders());
			}
			
			return this.refactoringLegacyMemberIdentifierBuilders;
		}
		
		@Override
		public XEnum<? extends PersistenceRefactoringMemberIdentifierBuilder> getRefactoringCurrentMemberIdentifierBuilders()
		{
			if(this.refactoringCurrentMemberIdentifierBuilders == null)
			{
				this.refactoringCurrentMemberIdentifierBuilders = this.dispatch(this.ensureRefactoringCurrentMemberIdentifierBuilders());
			}
			
			return this.refactoringCurrentMemberIdentifierBuilders;
		}
		
		@Override
		public TypeMapping<Float> getTypeSimilarity()
		{
			if(this.typeSimilarity == null)
			{
				this.typeSimilarity = this.dispatch(this.ensureTypeSimilarity());
			}
			
			return this.typeSimilarity;
		}
		
		@Override
		public PersistenceMemberMatchingProvider getLegacyMemberMatchingProvider()
		{
			if(this.legacyMemberMatchingProvider == null)
			{
				this.legacyMemberMatchingProvider = this.dispatch(this.ensureLegacyMemberMatchingProvider());
			}
			
			return this.legacyMemberMatchingProvider;
		}
				
		@Override
		public PersistenceLegacyTypeMappingResultor<M> getLegacyTypeMappingResultor()
		{
			if(this.legacyTypeMappingResultor == null)
			{
				this.legacyTypeMappingResultor = this.dispatch(this.ensureLegacyTypeMappingResultor());
			}
			
			return this.legacyTypeMappingResultor;
		}
		
		@Override
		public PersistenceLegacyTypeHandlerCreator<M> getLegacyTypeHandlerCreator()
		{
			if(this.legacyTypeHandlerCreator == null)
			{
				this.legacyTypeHandlerCreator = this.dispatch(this.ensureLegacyTypeHandlerCreator());
			}
			
			return this.legacyTypeHandlerCreator;
		}
		
		@Override
		public PersistenceLegacyTypeHandlingListener<M> getLegacyTypeHandlingListener()
		{
			if(this.legacyTypeHandlingListener == null)
			{
				this.legacyTypeHandlingListener = this.dispatch(this.ensureLegacyTypeHandlingListener());
			}
			
			return this.legacyTypeHandlingListener;
		}
		
		@Override
		public PersistenceSizedArrayLengthController getSizedArrayLengthController()
		{
			if(this.sizedArrayLengthController == null)
			{
				this.sizedArrayLengthController = this.dispatch(this.ensureSizedArrayLengthController());
			}
			
			return this.sizedArrayLengthController;
		}
		
		@Override
		public PersistenceRootResolver rootResolver()
		{
			return this.rootResolver;
		}
		
		@Override
		public LambdaTypeRecognizer getLambdaTypeRecognizer()
		{
			if(this.lambdaTypeRecognizer == null)
			{
				this.lambdaTypeRecognizer = this.dispatch(this.ensureLambdaTypeRecognizer());
			}
			return this.lambdaTypeRecognizer;
		}

		@Override
		public PersistenceRootsProvider<M> getRootsProvider()
		{
			if(this.rootsProvider == null)
			{
				this.rootsProvider = this.dispatch(this.ensureRootsProvider());
			}
			return this.rootsProvider;
		}
		
		@Override
		public ByteOrder getTargetByteOrder()
		{
			if(this.targetByteOrder == null)
			{
				this.targetByteOrder = this.dispatch(this.ensureTargetByteOrder());
			}
			
			return this.targetByteOrder;
		}
		


		///////////////////////////////////////////////////////////////////////////
		// setters //
		////////////

		@Override
		public F setInstanceDispatcher(
			final InstanceDispatcherLogic instanceDispatcher
		)
		{
			super.setInstanceDispatcherLogic(instanceDispatcher);
			return this.$();
		}
		
		@Override
		public F setObjectIdProvider(final PersistenceObjectIdProvider oidProvider)
		{
			this.oidProvider = oidProvider;
			return this.$();
		}

		@Override
		public F setTypeIdProvider(final PersistenceTypeIdProvider tidProvider)
		{
			this.tidProvider = tidProvider;
			return this.$();
		}

		@Override
		public <P extends PersistenceTypeIdProvider & PersistenceObjectIdProvider>
		F setIdProvider(final P typeIdProvider)
		{
			this.setObjectIdProvider(typeIdProvider);
			this.setTypeIdProvider(typeIdProvider);
			return this.$();
		}

		@Override
		public F setObjectManager(
			final PersistenceObjectManager objectManager
		)
		{
			this.objectManager = objectManager;
			return this.$();
		}

		@Override
		public F setStorerCreator(
			final PersistenceStorer.Creator<M> storerCreator
		)
		{
			this.storerCreator = storerCreator;
			return this.$();
		}

		@Override
		public F setTypeHandlerCreatorLookup(
			final PersistenceTypeHandlerEnsurer<M> typeHandlerCreatorLookup
		)
		{
			this.typeHandlerEnsurer = typeHandlerCreatorLookup;
			return this.$();
		}
		
		@Override
		public F setTypeHandlerCreator(
			final PersistenceTypeHandlerCreator<M> typeHandlerCreator
		)
		{
			this.typeHandlerCreator = typeHandlerCreator;
			return this.$();
		}

		@Override
		public F setTypeHandlerManager(
			final PersistenceTypeHandlerManager<M> typeHandlerManager
		)
		{
			this.internalSetTypeHandlerManager(typeHandlerManager);
			return this.$();
		}
		
		private void internalSetTypeHandlerManager(
			final PersistenceTypeHandlerManager<M> typeHandlerManager
		)
		{
			synchronized(this.referenceTypeHandlerManager)
			{
				this.typeHandlerManager = typeHandlerManager;
				this.referenceTypeHandlerManager.set(typeHandlerManager);
			}
		}
		
		@Override
		public F setContextDispatcher(
			final PersistenceContextDispatcher<M> contextDispatcher
		)
		{
			this.contextDispatcher = contextDispatcher;
			return this.$();
		}

		@Override
		public F setObjectRegistry(
			final PersistenceObjectRegistry objectRegistry
		)
		{
			this.objectRegistry = objectRegistry;
			return this.$();
		}
		
		@Override
		public F setTypeRegistry(final PersistenceTypeRegistry typeRegistry)
		{
			this.typeRegistry = typeRegistry;
			return this.$();
		}

		@Override
		public F setTypeManager(
			final PersistenceTypeManager typeManager
		)
		{
			this.typeManager = typeManager;
			return this.$();
		}

		@Override
		public F setTypeHandlerRegistry(
			final PersistenceTypeHandlerRegistry<M> typeHandlerRegistry
		)
		{
			this.typeHandlerRegistry = typeHandlerRegistry;
			return this.$();
		}

		@Override
		public F setTypeHandlerProvider(
			final PersistenceTypeHandlerProvider<M> typeHandlerProvider
		)
		{
			this.typeHandlerProvider = typeHandlerProvider;
			return this.$();
		}

		@Override
		public F setRegistererCreator(
			final PersistenceRegisterer.Creator registererCreator
		)
		{
			this.registererCreator = registererCreator;
			return this.$();
		}

		@Override
		public F setBuilderCreator(
			final PersistenceLoader.Creator<M> builderCreator
		)
		{
			this.builderCreator = builderCreator;
			return this.$();
		}

		@Override
		public F setPersistenceTarget(
			final PersistenceTarget<M> target
		)
		{
			this.target = target;
			return this.$();
		}

		@Override
		public F setPersistenceSource(
			final PersistenceSource<M> source
		)
		{
			this.source = source;
			return this.$();
		}
		
		@Override
		public F setPersistenceChannel(final PersistenceChannel<M> persistenceChannel)
		{
			this.setPersistenceSource(persistenceChannel);
			this.setPersistenceTarget(persistenceChannel);
			return this.$();
		}
		
		@Override
		public F setSizedArrayLengthController(final PersistenceSizedArrayLengthController sizedArrayLengthController)
		{
			this.sizedArrayLengthController = sizedArrayLengthController;
			return this.$();
		}
		
		@Override
		public F setTypeDictionaryManager(
			final PersistenceTypeDictionaryManager typeDictionaryManager
		)
		{
			this.typeDictionaryManager = typeDictionaryManager;
			return this.$();
		}
				
		@Override
		public F setTypeDictionaryCreator(
			final PersistenceTypeDictionaryCreator typeDictionaryCreator
		)
		{
			this.typeDictionaryCreator = typeDictionaryCreator;
			return this.$();
		}

		@Override
		public F setTypeDictionaryProvider(
			final PersistenceTypeDictionaryProvider typeDictionaryProvider
		)
		{
			this.typeDictionaryProvider = typeDictionaryProvider;
			return this.$();
		}

		@Override
		public F setTypeDictionaryExporter(
			final PersistenceTypeDictionaryExporter typeDictionaryExporter
		)
		{
			this.typeDictionaryExporter = typeDictionaryExporter;
			return this.$();
		}

		@Override
		public F setTypeDictionaryParser(
			final PersistenceTypeDictionaryParser typeDictionaryParser
		)
		{
			this.typeDictionaryParser = typeDictionaryParser;
			return this.$();
		}

		@Override
		public F setTypeDictionaryAssembler(
			final PersistenceTypeDictionaryAssembler typeDictionaryAssembler
		)
		{
			this.typeDictionaryAssembler = typeDictionaryAssembler;
			return this.$();
		}

		@Override
		public F setTypeDictionaryLoader(
			final PersistenceTypeDictionaryLoader typeDictionaryLoader
		)
		{
			this.typeDictionaryLoader = typeDictionaryLoader;
			return this.$();
		}
		
		@Override
		public F setTypeDictionaryBuilder(
			final PersistenceTypeDictionaryBuilder typeDictionaryBuilder
		)
		{
			this.typeDictionaryBuilder = typeDictionaryBuilder;
			return this.$();
		}
		
		@Override
		public F setTypeDictionaryCompiler(
			final PersistenceTypeDictionaryCompiler typeDictionaryCompiler
		)
		{
			this.typeDictionaryCompiler = typeDictionaryCompiler;
			return this.$();
		}
		
		@Override
		public F setTypeDictionaryStorer(
			final PersistenceTypeDictionaryStorer typeDictionaryStorer
		)
		{
			this.typeDictionaryStorer = typeDictionaryStorer;
			return this.$();
		}
		
		@Override
		public <H extends PersistenceTypeDictionaryLoader & PersistenceTypeDictionaryStorer> F setTypeDictionaryIoHandling(
			final H typeDictionaryStorageHandler
		)
		{
			this.setTypeDictionaryLoader(typeDictionaryStorageHandler);
			this.setTypeDictionaryStorer(typeDictionaryStorageHandler);
			return this.$();
		}
		
		@Override
		public F setTypeLineageCreator(
			final PersistenceTypeLineageCreator typeLineageCreator
		)
		{
			this.typeLineageCreator = typeLineageCreator;
			return this.$();
		}

		@Override
		public F setTypeEvaluatorTypeIdMappable(
			final PersistenceTypeEvaluator typeEvaluatorTypeIdMappable
		)
		{
			this.typeEvaluatorTypeIdMappable = typeEvaluatorTypeIdMappable;
			return this.$();
		}

		@Override
		public F setTypeMismatchValidator(
			final PersistenceTypeMismatchValidator<M> typeMismatchValidator
		)
		{
			this.typeMismatchValidator = typeMismatchValidator;
			return this.$();
		}
		
		@Override
		public F setTypeDescriptionBuilder(
			final PersistenceTypeDefinitionCreator typeDefinitionCreator
		)
		{
			this.typeDefinitionCreator = typeDefinitionCreator;
			return this.$();
		}
		
		@Override
		public F setTypeEvaluatorPersistable(
			final PersistenceTypeEvaluator typeEvaluatorPersistable
		)
		{
			this.typeEvaluatorPersistable = typeEvaluatorPersistable;
			return this.$();
		}

		@Override
		public F setBufferSizeProvider(
			final BufferSizeProviderIncremental bufferSizeProvider
		)
		{
			this.bufferSizeProvider = bufferSizeProvider;
			return this.$();
		}

		@Override
		public F setFieldFixedLengthResolver(
			final PersistenceFieldLengthResolver fieldFixedLengthResolver
		)
		{
			this.fieldFixedLengthResolver = fieldFixedLengthResolver;
			return this.$();
		}

		@Override
		public F setFieldEvaluatorPersistable(
			final PersistenceFieldEvaluator fieldEvaluator
		)
		{
			this.fieldEvaluatorPersistable = fieldEvaluator;
			return this.$();
		}

		@Override
		public F setFieldEvaluatorCollection(
			final PersistenceFieldEvaluator fieldEvaluator
		)
		{
			this.fieldEvaluatorCollection = fieldEvaluator;
			return this.$();
		}
		
		@Override
		public F setReferenceFieldMandatoryEvaluator(
			final PersistenceEagerStoringFieldEvaluator evaluator
		)
		{
			this.eagerStoringFieldEvaluator = evaluator;
			return this.$();
		}
		
		@Override
		public F setRootResolver(final Builder rootResolverBuilder)
		{
			this.rootResolverBuilder = rootResolverBuilder;
			return this.$();
		}

		@Override
		public F setRootResolver(
			final PersistenceRootResolver rootResolver
		)
		{
			this.rootResolver = rootResolver;
			return this.$();
		}
		
		@Override
		public F setLambdaTypeRecognizer(
			final LambdaTypeRecognizer lambdaTypeRecognizer
		)
		{
			this.lambdaTypeRecognizer = lambdaTypeRecognizer;
			return this.$();
		}

		@Override
		public F setRootsProvider(
			final PersistenceRootsProvider<M> rootsProvider
		)
		{
			this.rootsProvider = rootsProvider;
			return this.$();
		}
		
		@Override
		public F setUnreachableTypeHandlerCreator(
			final PersistenceUnreachableTypeHandlerCreator<M> unreachableTypeHandlerCreator
		)
		{
			this.unreachableTypeHandlerCreator = unreachableTypeHandlerCreator;
			return this.$();
		}
		
		@Override
		public F setLegacyTypeMapper(
			final PersistenceLegacyTypeMapper<M> legacyTypeMapper
		)
		{
			this.legacyTypeMapper = legacyTypeMapper;
			return this.$();
		}
		
		@Override
		public F setRefactoringMappingProvider(
			final PersistenceRefactoringMappingProvider refactoringMappingProvider
		)
		{
			this.refactoringMappingProvider = refactoringMappingProvider;
			return this.$();
		}
		
		@Override
		public F setRefactoringResolverProvider(
			final PersistenceRefactoringResolverProvider refactoringResolverProvider
		)
		{
			this.refactoringResolverProvider = refactoringResolverProvider;
			return this.$();
		}
		
		@Override
		public F setRefactoringLegacyTypeIdentifierBuilders(
			final XEnum<? extends PersistenceRefactoringTypeIdentifierBuilder> refactoringTypeIdentifierBuilders
		)
		{
			this.refactoringLegacyTypeIdentifierBuilders = refactoringTypeIdentifierBuilders;
			return this.$();
		}
		
		@Override
		public F setRefactoringLegacyMemberIdentifierBuilders(
			final XEnum<? extends PersistenceRefactoringMemberIdentifierBuilder> refactoringMemberIdentifierBuilders
		)
		{
			this.refactoringLegacyMemberIdentifierBuilders = refactoringMemberIdentifierBuilders;
			return this.$();
		}
		
		@Override
		public F setRefactoringCurrentMemberIdentifierBuilders(
			final XEnum<? extends PersistenceRefactoringMemberIdentifierBuilder> refactoringMemberIdentifierBuilders
		)
		{
			this.refactoringCurrentMemberIdentifierBuilders = refactoringMemberIdentifierBuilders;
			return this.$();
		}
		
		@Override
		public F setTypeSimilarity(final TypeMapping<Float> typeSimilarity)
		{
			this.typeSimilarity = typeSimilarity;
			return this.$();
		}
		
		@Override
		public F setLegacyMemberMatchingProvider(
			final PersistenceMemberMatchingProvider legacyMemberMatchingProvider
		)
		{
			this.legacyMemberMatchingProvider = legacyMemberMatchingProvider;
			return this.$();
		}
				
		@Override
		public F setLegacyTypeMappingResultor(
			final PersistenceLegacyTypeMappingResultor<M> legacyTypeMappingResultor
		)
		{
			this.legacyTypeMappingResultor = legacyTypeMappingResultor;
			return this.$();
		}
		
		@Override
		public F setLegacyTypeHandlerCreator(
			final PersistenceLegacyTypeHandlerCreator<M> legacyTypeHandlerCreator
		)
		{
			this.legacyTypeHandlerCreator = legacyTypeHandlerCreator;
			return this.$();
		}
		
		@Override
		public F setLegacyTypeHandlingListener(
			final PersistenceLegacyTypeHandlingListener<M> legacyTypeHandlingListener
		)
		{
			this.legacyTypeHandlingListener = legacyTypeHandlingListener;
			return this.$();
		}
		
		@Override
		public F setTargetByteOrder(final ByteOrder targetByteOrder)
		{
			this.targetByteOrder = targetByteOrder;
			return this.$();
		}


		
		///////////////////////////////////////////////////////////////////////////
		// ensuring //
		/////////////
		
		/* Explanation:
		 * These methods are not actually abstract because it is not necessaryly required
		 * to create new instances of these types. Instead, appropriate instances can be set.
		 * These methods exist in order to allow sub classes to implement them optionally
		 * and throw an exception if neither implementation nor set instance is available.
		 */

		protected PersistenceObjectIdProvider ensureObjectIdProvider()
		{
			return PersistenceObjectIdProvider.Transient();
		}

		protected PersistenceTypeIdProvider ensureTypeIdProvider()
		{
			return PersistenceTypeIdProvider.Transient();
		}

		protected PersistenceObjectRegistry ensureObjectRegistry()
		{
			final PersistenceObjectRegistry registry = PersistenceObjectRegistry.New();
			Persistence.registerJavaConstants(registry);
			
			return registry;
		}

		protected PersistenceTypeRegistry ensureTypeRegistry()
		{
			final PersistenceTypeRegistry registry = PersistenceTypeRegistry.New();
			Persistence.registerJavaBasicTypes(registry);
			
			return registry;
		}
		
		protected PersistenceTypeLineageCreator ensureTypeLineageCreator()
		{
			final PersistenceTypeLineageCreator newTypeLineageCreator =
				PersistenceTypeLineageCreator.New()
			;
			return newTypeLineageCreator;
		}

		protected PersistenceObjectManager ensureObjectManager()
		{
			final PersistenceObjectManager newObjectManager = PersistenceObjectManager.New(
				this.getObjectRegistry(),
				this.getObjectIdProvider()
			);
			
			return newObjectManager;
		}

		protected PersistenceTypeManager ensureTypeManager()
		{
			final PersistenceTypeManager newTypeManager = PersistenceTypeManager.New(
				this.getTypeRegistry(),
				this.getTypeIdProvider()
			);
			return newTypeManager;
		}

		protected PersistenceTypeHandlerManager<M> ensureTypeHandlerManager()
		{
			final PersistenceTypeHandlerManager<M> newTypeHandlerManager =
				PersistenceTypeHandlerManager.New(
					this.getTypeHandlerRegistry()        , // holds actually used (potentially generically created) handlers
					this.getTypeHandlerProvider()        , // knows/contains the Custom~Registry w. default handlers/definitions
					this.getTypeDictionaryManager()      , // only manages loading, storing and registering
					this.getTypeEvaluatorTypeIdMappable(),
					this.getTypeMismatchValidator()      ,
					this.getLegacyTypeMapper()           ,
					this.getUnreachableTypeHandlerCreator()
				)
			;
			return newTypeHandlerManager;
		}
		
		protected PersistenceContextDispatcher<M> ensureContextDispatcher()
		{
			return PersistenceContextDispatcher.PassThrough();
		}

		protected PersistenceRegisterer.Creator ensureRegistererCreator()
		{
			return new PersistenceRegisterer.Default.Creator();
		}

		protected PersistenceTypeHandlerRegistry<M> ensureTypeHandlerRegistry()
		{
			// note: sub class should/must register native type handlers in an overridden version of this method
			final PersistenceTypeHandlerRegistry<M> newTypeHandlerRegistry =
				new PersistenceTypeHandlerRegistry.Default<>(this.getTypeRegistry())
			;
			return newTypeHandlerRegistry;
		}

		protected PersistenceTypeHandlerProvider<M> ensureTypeHandlerProvider()
		{
			return PersistenceTypeHandlerProviderCreating.New(
				this.getTypeManager(),
				this.getTypeHandlerEnsurer()
			);
		}

		protected PersistenceTypeDictionaryManager ensureTypeDictionaryManager()
		{
			final PersistenceTypeDictionaryManager newTypeDictionaryManager =
				PersistenceTypeDictionaryManager.Exporting(
					this.getTypeDictionaryProvider(),
					this.getTypeDictionaryExporter()
				)
			;
			return newTypeDictionaryManager;
		}
		
		protected PersistenceTypeDictionaryCreator ensureTypeDictionaryCreator()
		{
			final PersistenceTypeDictionaryCreator newTypeDictionaryCreator =
				PersistenceTypeDictionaryCreator.New(
					this.getTypeLineageCreator()
				)
			;
			return newTypeDictionaryCreator;
		}

		protected PersistenceTypeDictionaryProvider ensureTypeDictionaryProvider()
		{
			final PersistenceTypeDictionaryProvider newTypeDictionaryProvider =
				PersistenceTypeDictionaryProvider.New(
					this.getTypeDictionaryLoader(),
					this.getTypeDictionaryCompiler()
				)
			;
			return new PersistenceTypeDictionaryProvider.Caching(newTypeDictionaryProvider);
		}

		protected PersistenceTypeDictionaryExporter ensureTypeDictionaryExporter()
		{
			final PersistenceTypeDictionaryExporter newTypeDictionaryExporter =
				new PersistenceTypeDictionaryExporter.Default(
					this.getTypeDictionaryAssembler(),
					this.getTypeDictionaryStorer()
				)
			;
			return newTypeDictionaryExporter;
		}

		protected PersistenceTypeDictionaryParser ensureTypeDictionaryParser()
		{
			final PersistenceTypeDictionaryParser newTypeDictionaryParser =
				new PersistenceTypeDictionaryParser.Default(
					this.getFieldFixedLengthResolver()
				)
			;
			return newTypeDictionaryParser;
		}

		protected PersistenceTypeDictionaryAssembler ensureTypeDictionaryAssembler()
		{
			final PersistenceTypeDictionaryAssembler newTypeDictionaryAssembler =
				PersistenceTypeDictionaryAssembler.New()
			;
			return newTypeDictionaryAssembler;
		}

		protected PersistenceTypeAnalyzer ensureTypeAnalyzer()
		{
			return new PersistenceTypeAnalyzer.Default(
				this.getTypeEvaluatorPersistable(),
				this.getFieldEvaluatorPersistable(),
				this.getFieldEvaluatorCollection()
			);
		}

		protected PersistenceTypeHandlerEnsurer<M> ensureTypeHandlerEnsurer()
		{
			return PersistenceTypeHandlerEnsurer.New(
				this.getCustomTypeHandlerRegistry(),
				this.getTypeHandlerCreator()
			);
		}

		protected PersistenceTypeDictionaryBuilder ensureTypeDictionaryBuilder()
		{
			return PersistenceTypeDictionaryBuilder.New(
				this.getTypeDictionaryCreator(),
				this.getTypeDefinitionCreator(),
				this.getRefactoringResolverProvider()
			);
		}
		
		protected PersistenceTypeDictionaryCompiler ensureTypeDictionaryCompiler()
		{
			return PersistenceTypeDictionaryCompiler.New(
				this.getTypeDictionaryParser() ,
				this.getTypeDictionaryBuilder()
			);
		}

		protected PersistenceTypeEvaluator ensureTypeEvaluatorTypeIdMappable()
		{
			return Persistence.defaultTypeEvaluatorTypeIdMappable();
		}
		
		protected PersistenceTypeMismatchValidator<M> ensureTypeMismatchValidator()
		{
			// (13.09.2018 TM)NOTE: changed for Legacy Type Mapping. Still a valid callback for monitoring purposes.
			return Persistence.typeMismatchValidatorNoOp();
//			return Persistence.typeMismatchValidatorFailing();
		}
		
		protected PersistenceTypeDefinitionCreator ensureTypeDefinitionCreator()
		{
			return PersistenceTypeDefinitionCreator.New();
		}

		protected PersistenceTypeEvaluator ensureTypeEvaluatorPersistable()
		{
			return Persistence.defaultTypeEvaluatorPersistable();
		}

		protected BufferSizeProviderIncremental ensureBufferSizeProvider()
		{
			return new BufferSizeProviderIncremental.Default();
		}

		protected PersistenceFieldEvaluator ensureFieldEvaluatorPersistable()
		{
			return Persistence.defaultFieldEvaluatorPersistable();
		}

		protected PersistenceFieldEvaluator ensureFieldEvaluatorCollection()
		{
			return Persistence.defaultFieldEvaluatorCollection();
		}
		
		protected PersistenceEagerStoringFieldEvaluator ensureReferenceFieldMandatoryEvaluator()
		{
			return Persistence.defaultReferenceFieldMandatoryEvaluator();
		}
		
		protected PersistenceUnreachableTypeHandlerCreator<M> ensureUnreachableTypeHandlerCreator()
		{
			return PersistenceUnreachableTypeHandlerCreator.New();
		}
		
		protected PersistenceLegacyTypeMapper<M> ensureLegacyTypeMapper()
		{
			return PersistenceLegacyTypeMapper.New(
				this.getRefactoringResolverProvider() ,
				this.getTypeSimilarity()              ,
				this.getCustomTypeHandlerRegistry()   ,
				this.getLegacyMemberMatchingProvider(),
				this.getLegacyTypeMappingResultor()   ,
				this.getLegacyTypeHandlerCreator()
			);
		}
				
		protected PersistenceRefactoringMappingProvider ensureRefactoringMappingProvider()
		{
			// empty (= dummy) mapping by default
			return PersistenceRefactoringMappingProvider.NewEmpty();
		}
		
		protected PersistenceRefactoringResolverProvider ensureRefactoringResolverProvider()
		{
			return PersistenceRefactoringResolverProvider.Caching(
				this.getRefactoringMappingProvider()                ,
				this.getRefactoringLegacyTypeIdentifierBuilders()   ,
				this.getRefactoringLegacyMemberIdentifierBuilders() ,
				this.getRefactoringCurrentMemberIdentifierBuilders()
			);
		}
		
		protected XEnum<? extends PersistenceRefactoringTypeIdentifierBuilder> ensureRefactoringLegacyTypeIdentifierBuilders()
		{
			return PersistenceRefactoringTypeIdentifierBuilder.createDefaultRefactoringLegacyTypeIdentifierBuilders();
		}
		
		protected XEnum<? extends PersistenceRefactoringMemberIdentifierBuilder> ensureRefactoringLegacyMemberIdentifierBuilders()
		{
			return PersistenceRefactoringMemberIdentifierBuilder.createDefaultRefactoringLegacyMemberIdentifierBuilders();
		}
		
		protected XEnum<? extends PersistenceRefactoringMemberIdentifierBuilder> ensureRefactoringCurrentMemberIdentifierBuilders()
		{
			return PersistenceRefactoringMemberIdentifierBuilder.createDefaultRefactoringCurrentMemberIdentifierBuilders();
		}
		
		protected TypeMapping<Float> ensureTypeSimilarity()
		{
			return XTypes.createDefaultTypeSimilarity();
		}
				
		protected PersistenceMemberMatchingProvider ensureLegacyMemberMatchingProvider()
		{
			return PersistenceMemberMatchingProvider.New();
		}
				
		protected PersistenceLegacyTypeMappingResultor<M> ensureLegacyTypeMappingResultor()
		{
			// default is silent, which is dangerous when heuristics are in play. Should be wrapped by the user.
			return PersistenceLegacyTypeMappingResultor.New();
		}
		
		protected PersistenceLegacyTypeHandlerCreator<M> ensureLegacyTypeHandlerCreator()
		{
			throw new MissingFoundationPartException(PersistenceLegacyTypeHandlerCreator.class);
		}
		
		protected PersistenceLegacyTypeHandlingListener<M> ensureLegacyTypeHandlingListener()
		{
			/*
			 * this listener is purely optional, so by default, nothing is created.
			 * This method is just a stub for sub classes to override.
			 */
			return null;
		}
		
		protected PersistenceSizedArrayLengthController ensureSizedArrayLengthController()
		{
			// unlimited by default to not change program behavior
			return PersistenceSizedArrayLengthController.Unrestricted();
		}

		/* Explanation:
		 * These methods are not actually abstract because it is not necessarily required to
		 * create new instances of these types here. Instead, appropriate instances can be set.
		 * These methods exist in order to allow sub classes to implement them optionally and
		 * throw an exception if neither implementation nor set instance is available.
		 */

		protected PersistenceStorer.Creator<M> ensureStorerCreator()
		{
			throw new MissingFoundationPartException(PersistenceStorer.Creator.class);
		}

		protected PersistenceLoader.Creator<M> ensureBuilderCreator()
		{
			throw new MissingFoundationPartException(PersistenceLoader.Creator.class);
		}

		protected PersistenceTarget<M> ensurePersistenceTarget()
		{
			throw new MissingFoundationPartException(PersistenceTarget.class);
		}

		protected PersistenceSource<M> ensurePersistenceSource()
		{
			throw new MissingFoundationPartException(PersistenceSource.class);
		}

		protected PersistenceTypeDictionaryLoader ensureTypeDictionaryLoader()
		{
			throw new MissingFoundationPartException(PersistenceTypeDictionaryLoader.class);
		}
		
		protected PersistenceTypeDictionaryStorer ensureTypeDictionaryStorer()
		{
			throw new MissingFoundationPartException(PersistenceTypeDictionaryStorer.class);
		}

		protected PersistenceTypeHandlerCreator<M> ensureTypeHandlerCreator()
		{
			throw new MissingFoundationPartException(PersistenceTypeHandlerCreator.class);
		}

		protected PersistenceCustomTypeHandlerRegistry<M> ensureCustomTypeHandlerRegistry()
		{
			throw new MissingFoundationPartException(PersistenceCustomTypeHandlerRegistry.class);
		}

		protected PersistenceFieldLengthResolver ensureFieldFixedLengthResolver()
		{
			throw new MissingFoundationPartException(PersistenceFieldLengthResolver.class);
		}
		
		protected PersistenceRootResolver.Builder ensureRootResolverBuilder()
		{
			return PersistenceRootResolver.Builder();
		}
		
		protected PersistenceRootResolver ensureRootResolver()
		{
			throw new MissingFoundationPartException(PersistenceRootResolver.class);
		}
		
		protected PersistenceRootsProvider<M> ensureRootsProviderInternal()
		{
			throw new MissingFoundationPartException(PersistenceRootsProvider.class);
		}

		protected PersistenceRootsProvider<M> ensureRootsProvider()
		{
			final PersistenceRootsProvider<M> rootsProvider = this.ensureRootsProviderInternal();
			rootsProvider.registerRootsTypeHandlerCreator(
				this.getCustomTypeHandlerRegistry(),
				this.getObjectRegistry()
			);
			
			return rootsProvider;
		}
		
		protected LambdaTypeRecognizer ensureLambdaTypeRecognizer()
		{
			return LambdaTypeRecognizer.New();
		}
		
		protected ByteOrder ensureTargetByteOrder()
		{
			return ByteOrder.nativeOrder();
		}



		///////////////////////////////////////////////////////////////////////////
		// methods // (with logic worth mentioning)
		////////////

		@Override
		public F executeTypeHandlerRegistration(final PersistenceTypeHandlerRegistration<M> typeHandlerRegistration)
		{
			typeHandlerRegistration.registerTypeHandlers(
				this.getCustomTypeHandlerRegistry(),
				this.getSizedArrayLengthController()
			);
			
			return this.$();
		}
		
		@Override
		public PersistenceManager<M> createPersistenceManager()
		{
			final PersistenceTypeHandlerManager<M> typeHandlerManager = this.getTypeHandlerManager();
			typeHandlerManager.initialize(); // initialize type handlers (i.e. import/validate type dictionary)

			final PersistenceManager<M> newPersistenceManager =
				new PersistenceManager.Default<>(
					this.getObjectRegistry(),
					this.getObjectManager(),
					typeHandlerManager,
					this.getContextDispatcher(),
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

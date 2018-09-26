package net.jadoth.persistence.types;

import net.jadoth.exceptions.MissingFoundationPartException;
import net.jadoth.functional.InstanceDispatcherLogic;
import net.jadoth.persistence.internal.PersistenceTypeHandlerProviderCreating;
import net.jadoth.reflect.XReflect;
import net.jadoth.swizzling.internal.SwizzleRegistryGrowingRange;
import net.jadoth.swizzling.types.Swizzle;
import net.jadoth.swizzling.types.SwizzleFoundation;
import net.jadoth.swizzling.types.SwizzleObjectIdProvider;
import net.jadoth.swizzling.types.SwizzleObjectManager;
import net.jadoth.swizzling.types.SwizzleRegistry;
import net.jadoth.swizzling.types.SwizzleTypeIdProvider;
import net.jadoth.swizzling.types.SwizzleTypeManager;
import net.jadoth.typing.TypeMapping;
import net.jadoth.typing.XTypes;


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
	// getters //
	/////////////
	
	// (31.05.2018 TM)TODO: rename ALL get~ methods in ALL foundations to provide~? Because that is what they do.

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

	public PersistenceTypeHandlerEnsurer<M> getTypeHandlerEnsurer();

	public PersistenceTypeHandlerRegistry<M> getTypeHandlerRegistry();

	public PersistenceTypeDictionaryManager getTypeDictionaryManager();
	
	public PersistenceTypeDictionaryCreator getTypeDictionaryCreator();

	public PersistenceTypeDictionaryProvider getTypeDictionaryProvider();

	public PersistenceTypeDictionaryExporter getTypeDictionaryExporter();

	public PersistenceTypeDictionaryParser getTypeDictionaryParser();

	public PersistenceTypeDictionaryLoader getTypeDictionaryLoader();
	
	public PersistenceTypeDictionaryBuilder getTypeDictionaryBuilder();

	public PersistenceTypeDictionaryAssembler getTypeDictionaryAssembler();

	public PersistenceTypeDictionaryStorer getTypeDictionaryStorer();
	
	public PersistenceTypeLineageCreator getTypeLineageCreator();

	public PersistenceTypeHandlerCreator<M> getTypeHandlerCreator();

	public PersistenceCustomTypeHandlerRegistry<M> getCustomTypeHandlerRegistry();

	public PersistenceTypeAnalyzer getTypeAnalyzer();

	public PersistenceTypeEvaluator getTypeEvaluatorTypeIdMappable();
	
	public PersistenceTypeMismatchValidator<M> getTypeMismatchValidator();

	public PersistenceTypeResolver getTypeResolver();
	
	public PersistenceTypeDefinitionCreator getTypeDefinitionCreator();

	public PersistenceTypeEvaluator getTypeEvaluatorPersistable();

	public PersistenceFieldLengthResolver getFieldFixedLengthResolver();
	
	public PersistenceEagerStoringFieldEvaluator getReferenceFieldMandatoryEvaluator();

	public BufferSizeProviderIncremental getBufferSizeProvider();

	public PersistenceFieldEvaluator getFieldEvaluator();
	
	public PersistenceRootResolver getRootResolver();
	
	public PersistenceRootResolver getEffectiveRootResolver();

	public PersistenceRootsProvider<M> getRootsProvider();
	
	public PersistenceLegacyTypeMapper<M> getLegacyTypeMapper();

	public PersistenceRefactoringMappingProvider getRefactoringMappingProvider();
	
	public TypeMapping<Float> getTypeSimilarity();
	
	public PersistenceDeletedTypeHandlerCreator<M> getDeletedTypeHandlerCreator();

	public PersistenceMemberMatchingProvider getLegacyMemberMatchingProvider();
	
	public PersistenceLegacyTypeMappingResultor<M> getLegacyTypeMappingResultor();
	
	public PersistenceLegacyTypeHandlerCreator<M> getLegacyTypeHandlerCreator();
	
	public PersistenceLegacyTypeHandlingListener<M> getLegacyTypeHandlingListener();
	
	



	///////////////////////////////////////////////////////////////////////////
	// setters          //
	/////////////////////

	public PersistenceFoundation<M> setSwizzleRegistry(SwizzleRegistry swizzleRegistry);

	public PersistenceFoundation<M> setInstanceDispatcher(InstanceDispatcherLogic instanceDispatcher);

	public PersistenceFoundation<M> setObjectManager(SwizzleObjectManager objectManager);

	public PersistenceFoundation<M> setStorerCreator(PersistenceStorer.Creator<M> storerCreator);

	public PersistenceFoundation<M> setTypeHandlerManager(PersistenceTypeHandlerManager<M> typeHandlerManager);

	@Override
	public PersistenceFoundation<M> setObjectIdProvider(SwizzleObjectIdProvider oidProvider);

	@Override
	public PersistenceFoundation<M> setTypeIdProvider(SwizzleTypeIdProvider tidProvider);

	public PersistenceFoundation<M> setTypeManager(SwizzleTypeManager typeManager);

	public PersistenceFoundation<M> setTypeHandlerCreatorLookup(PersistenceTypeHandlerEnsurer<M> typeHandlerCreatorLookup);
	
	public PersistenceFoundation<M> setTypeHandlerCreator(PersistenceTypeHandlerCreator<M> typeHandlerCreator);

	public PersistenceFoundation<M> setTypeHandlerRegistry(PersistenceTypeHandlerRegistry<M> typeHandlerRegistry);

	public PersistenceFoundation<M> setTypeHandlerProvider(PersistenceTypeHandlerProvider<M> typeHandlerProvider);

	public PersistenceFoundation<M> setRegistererCreator(PersistenceRegisterer.Creator registererCreator);

	public PersistenceFoundation<M> setBuilderCreator(PersistenceLoader.Creator<M> builderCreator);

	public PersistenceFoundation<M> setPersistenceTarget(PersistenceTarget<M> target);

	public PersistenceFoundation<M> setPersistenceSource(PersistenceSource<M> source);

	public PersistenceFoundation<M> setTypeDictionaryManager(PersistenceTypeDictionaryManager typeDictionaryManager);
	
	public PersistenceFoundation<M> setTypeDictionaryCreator(PersistenceTypeDictionaryCreator typeDictionaryCreator);
		
	public PersistenceFoundation<M> setTypeDictionaryProvider(PersistenceTypeDictionaryProvider typeDictionaryProvider);
	
	public PersistenceFoundation<M> setTypeDictionaryExporter(PersistenceTypeDictionaryExporter typeDictionaryExporter);
	
	public PersistenceFoundation<M> setTypeDictionaryParser(PersistenceTypeDictionaryParser typeDictionaryParser);
	
	public PersistenceFoundation<M> setTypeDictionaryAssembler(PersistenceTypeDictionaryAssembler typeDictionaryAssembler);
	
	public PersistenceFoundation<M> setTypeDictionaryLoader(PersistenceTypeDictionaryLoader typeDictionaryLoader);
	
	public PersistenceFoundation<M> setTypeDictionaryBuilder(PersistenceTypeDictionaryBuilder typeDictionaryBuilder);
	
	public PersistenceFoundation<M> setTypeDictionaryStorer(PersistenceTypeDictionaryStorer typeDictionaryStorer);
	
	public PersistenceFoundation<M> setTypeLineageCreator(PersistenceTypeLineageCreator typeLineageCreator);

	public PersistenceFoundation<M> setTypeEvaluatorTypeIdMappable(PersistenceTypeEvaluator typeEvaluatorTypeIdMappable);
	
	public PersistenceFoundation<M> setTypeMismatchValidator(PersistenceTypeMismatchValidator<M> typeMismatchValidator);

	public PersistenceFoundation<M> setTypeResolver(PersistenceTypeResolver typeResolver);
	
	public PersistenceFoundation<M> setTypeDescriptionBuilder(PersistenceTypeDefinitionCreator typeDefinitionCreator);

	/* (29.10.2013 TM)TODO: rename to "TypeEvaluatorAnalyzable" & keep comment
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
	public PersistenceFoundation<M> setTypeEvaluatorPersistable(PersistenceTypeEvaluator getTypeEvaluatorPersistable);

	public PersistenceFoundation<M> setBufferSizeProvider(BufferSizeProviderIncremental bufferSizeProvider);

	public PersistenceFoundation<M> setFieldFixedLengthResolver(PersistenceFieldLengthResolver fieldFixedLengthResolver);

	public PersistenceFoundation<M> setFieldEvaluator(PersistenceFieldEvaluator fieldEvaluator);

	public PersistenceFoundation<M> setReferenceFieldMandatoryEvaluator(PersistenceEagerStoringFieldEvaluator evaluator);

	public PersistenceFoundation<M> setRootResolver(PersistenceRootResolver rootResolver);

	public PersistenceFoundation<M> setRootsProvider(PersistenceRootsProvider<M> rootsProvider);
	
	public PersistenceFoundation<M> setLegacyTypeMapper(PersistenceLegacyTypeMapper<M> legacyTypeMapper);
	
	public PersistenceFoundation<M> setTypeSimilarity(TypeMapping<Float> typeSimilarity);
	
	public PersistenceFoundation<M> setRefactoringMappingProvider(
		PersistenceRefactoringMappingProvider refactoringMappingProvider
	);
	
	public PersistenceFoundation<M> setDeletedTypeHandlerCreator(
		PersistenceDeletedTypeHandlerCreator<M> deletedTypeHandlerCreator
	);
		
	public PersistenceFoundation<M> setLegacyMemberMatchingProvider(
		PersistenceMemberMatchingProvider legacyMemberMatchingProvider
	);
		
	public PersistenceFoundation<M> setLegacyTypeMappingResultor(
		PersistenceLegacyTypeMappingResultor<M> legacyTypeMappingResultor
	);
	
	public PersistenceFoundation<M> setLegacyTypeHandlerCreator(
		PersistenceLegacyTypeHandlerCreator<M> legacyTypeHandlerCreator
	);
	
	public PersistenceFoundation<M> setLegacyTypeHandlingListener(
		PersistenceLegacyTypeHandlingListener<M> legacyTypeHandlingListener
	);
	
	

	


	@Override
	public <P extends SwizzleTypeIdProvider & SwizzleObjectIdProvider>
	PersistenceFoundation<M> setSwizzleIdProvider(P swizzleTypeIdProvider);

	public <H extends PersistenceTypeDictionaryLoader & PersistenceTypeDictionaryStorer>
	PersistenceFoundation<M> setDictionaryStorage(H typeDictionaryStorageHandler);

	public PersistenceFoundation<M> setPersistenceChannel(PersistenceChannel<M> persistenceChannel);



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
	implements PersistenceFoundation<M>, Unpersistable
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

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
		private SwizzleTypeManager                      typeManager                ;
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
		private PersistenceTypeDictionaryAssembler      typeDictionaryAssembler    ;
		private PersistenceTypeDictionaryStorer         typeDictionaryStorer       ;
		private PersistenceTypeLineageCreator           typeLineageCreator         ;
		private PersistenceTypeHandlerCreator<M>        typeHandlerCreator         ;
		private PersistenceCustomTypeHandlerRegistry<M> customTypeHandlerRegistry  ;
		private PersistenceTypeAnalyzer                 typeAnalyzer               ;
		private PersistenceTypeEvaluator                typeEvaluatorTypeIdMappable;
		private PersistenceTypeMismatchValidator<M>     typeMismatchValidator      ;
		private PersistenceTypeResolver                 typeResolver               ;
		private PersistenceTypeDefinitionCreator        typeDefinitionCreator      ;
		private PersistenceTypeEvaluator                typeEvaluatorPersistable   ;
		private PersistenceFieldLengthResolver          fieldFixedLengthResolver   ;
		private BufferSizeProviderIncremental           bufferSizeProvider         ;
		private PersistenceFieldEvaluator               fieldEvaluator             ;
		private PersistenceEagerStoringFieldEvaluator   eagerStoringFieldEvaluator ;
		private PersistenceRootResolver                 rootResolver               ;
		private PersistenceRootsProvider<M>             rootsProvider              ;
		
		// (14.09.2018 TM)NOTE: that legacy mapping stuff grows to a size where it could use its own foundation.
		private PersistenceLegacyTypeMapper<M>           legacyTypeMapper            ;
		private PersistenceRefactoringMappingProvider    refactoringMappingProvider  ;
		private TypeMapping<Float>                       typeSimilarity              ;
		private PersistenceDeletedTypeHandlerCreator<M>  deletedTypeHandlerCreator   ;
		private PersistenceMemberMatchingProvider        legacyMemberMatchingProvider;
		private PersistenceLegacyTypeMappingResultor<M>  legacyTypeMappingResultor   ;
		private PersistenceLegacyTypeHandlerCreator<M>   legacyTypeHandlerCreator    ;
		private PersistenceLegacyTypeHandlingListener<M> legacyTypeHandlingListener  ;
		

		
		///////////////////////////////////////////////////////////////////////////
		// getters          //
		/////////////////////
		
		@Override
		public PersistenceTypeLineageCreator getTypeLineageCreator()
		{
			if(this.typeLineageCreator == null)
			{
				this.typeLineageCreator = this.dispatch(this.createTypeLineageCreator());
			}
			
			return this.typeLineageCreator;
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
			if(this.typeManager == null)
			{
				this.typeManager = this.dispatch(this.createTypeManager());
			}
			
			return this.typeManager;
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
		public PersistenceTypeDictionaryCreator getTypeDictionaryCreator()
		{
			if(this.typeDictionaryCreator == null)
			{
				this.typeDictionaryCreator = this.dispatch(this.createTypeDictionaryCreator());
			}
			
			return this.typeDictionaryCreator;
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
		public PersistenceTypeDictionaryBuilder getTypeDictionaryBuilder()
		{
			if(this.typeDictionaryBuilder == null)
			{
				this.typeDictionaryBuilder = this.dispatch(this.createTypeDictionaryBuilder());
			}
			
			return this.typeDictionaryBuilder;
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
		public PersistenceTypeHandlerCreator<M> getTypeHandlerCreator()
		{
			if(this.typeHandlerCreator == null)
			{
				this.typeHandlerCreator = this.dispatch(this.createTypeHandlerCreator());
			}
			
			return this.typeHandlerCreator;
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
		public PersistenceTypeMismatchValidator<M> getTypeMismatchValidator()
		{
			if(this.typeMismatchValidator == null)
			{
				this.typeMismatchValidator = this.dispatch(this.createTypeMismatchValidator());
			}
			
			return this.typeMismatchValidator;
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
		public PersistenceTypeDefinitionCreator getTypeDefinitionCreator()
		{
			if(this.typeDefinitionCreator == null)
			{
				this.typeDefinitionCreator = this.dispatch(this.createTypeDefinitionCreator());
			}
			
			return this.typeDefinitionCreator;
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
		public BufferSizeProviderIncremental getBufferSizeProvider()
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
		public PersistenceEagerStoringFieldEvaluator getReferenceFieldMandatoryEvaluator()
		{
			if(this.eagerStoringFieldEvaluator == null)
			{
				this.eagerStoringFieldEvaluator = this.dispatch(this.createReferenceFieldMandatoryEvaluator());
			}
			
			return this.eagerStoringFieldEvaluator;
		}



		@Override
		public PersistenceRootResolver getRootResolver()
		{
			if(this.rootResolver == null)
			{
				this.rootResolver = this.dispatch(this.createRootResolver());
			}
			
			return this.rootResolver;
		}
		
		@Override
		public PersistenceLegacyTypeMapper<M> getLegacyTypeMapper()
		{
			if(this.legacyTypeMapper == null)
			{
				this.legacyTypeMapper = this.dispatch(this.createLegacyTypeMapper());
			}
			
			return this.legacyTypeMapper;
		}
		
		@Override
		public PersistenceRefactoringMappingProvider getRefactoringMappingProvider()
		{
			if(this.refactoringMappingProvider == null)
			{
				this.refactoringMappingProvider = this.dispatch(this.createRefactoringMappingProvider());
			}
			
			return this.refactoringMappingProvider;
		}
		
		@Override
		public TypeMapping<Float> getTypeSimilarity()
		{
			if(this.typeSimilarity == null)
			{
				this.typeSimilarity = this.dispatch(this.createTypeSimilarity());
			}
			
			return this.typeSimilarity;
		}
		
		@Override
		public PersistenceDeletedTypeHandlerCreator<M> getDeletedTypeHandlerCreator()
		{
			if(this.deletedTypeHandlerCreator == null)
			{
				this.deletedTypeHandlerCreator = this.dispatch(this.createDeletedTypeHandlerCreator());
			}
			
			return this.deletedTypeHandlerCreator;
		}
		
		@Override
		public PersistenceMemberMatchingProvider getLegacyMemberMatchingProvider()
		{
			if(this.legacyMemberMatchingProvider == null)
			{
				this.legacyMemberMatchingProvider = this.dispatch(this.createLegacyMemberMatchingProvider());
			}
			
			return this.legacyMemberMatchingProvider;
		}
				
		@Override
		public PersistenceLegacyTypeMappingResultor<M> getLegacyTypeMappingResultor()
		{
			if(this.legacyTypeMappingResultor == null)
			{
				this.legacyTypeMappingResultor = this.dispatch(this.createLegacyTypeMappingResultor());
			}
			
			return this.legacyTypeMappingResultor;
		}
		
		@Override
		public PersistenceLegacyTypeHandlerCreator<M> getLegacyTypeHandlerCreator()
		{
			if(this.legacyTypeHandlerCreator == null)
			{
				this.legacyTypeHandlerCreator = this.dispatch(this.createLegacyTypeHandlerCreator());
			}
			
			return this.legacyTypeHandlerCreator;
		}
		
		@Override
		public PersistenceLegacyTypeHandlingListener<M> getLegacyTypeHandlingListener()
		{
			if(this.legacyTypeHandlingListener == null)
			{
				this.legacyTypeHandlingListener = this.dispatch(this.createLegacyTypeHandlingListener());
			}
			
			return this.legacyTypeHandlingListener;
		}

		@Override
		public PersistenceRootsProvider<M> getRootsProvider()
		{
			if(this.rootsProvider == null)
			{
				this.rootsProvider = this.dispatch(this.createRootsProvider());
			}
			return this.rootsProvider;
		}
		


		///////////////////////////////////////////////////////////////////////////
		// setters          //
		/////////////////////

		@Override
		public PersistenceFoundation.AbstractImplementation<M> setInstanceDispatcher(
			final InstanceDispatcherLogic instanceDispatcher
		)
		{
			super.setInstanceDispatcherLogic(instanceDispatcher);
			return this;
		}

		@Override
		public PersistenceFoundation.AbstractImplementation<M> setObjectManager(
			final SwizzleObjectManager objectManager
		)
		{
			this.objectManager = objectManager;
			return this;
		}

		@Override
		public PersistenceFoundation.AbstractImplementation<M> setStorerCreator(
			final PersistenceStorer.Creator<M> storerCreator
		)
		{
			this.storerCreator = storerCreator;
			return this;
		}

		@Override
		public PersistenceFoundation.AbstractImplementation<M> setTypeHandlerCreatorLookup(
			final PersistenceTypeHandlerEnsurer<M> typeHandlerCreatorLookup
		)
		{
			this.typeHandlerEnsurer = typeHandlerCreatorLookup;
			return this;
		}
		
		@Override
		public PersistenceFoundation.AbstractImplementation<M> setTypeHandlerCreator(
			final PersistenceTypeHandlerCreator<M> typeHandlerCreator
		)
		{
			this.typeHandlerCreator = typeHandlerCreator;
			return this;
		}

		@Override
		public PersistenceFoundation.AbstractImplementation<M> setTypeHandlerManager(
			final PersistenceTypeHandlerManager<M> typeHandlerManager
		)
		{
			this.typeHandlerManager = typeHandlerManager;
			return this;
		}

		@Override
		public PersistenceFoundation.AbstractImplementation<M> setSwizzleRegistry(
			final SwizzleRegistry swizzleRegistry
		)
		{
			this.swizzleRegistry = swizzleRegistry;
			return this;
		}

		@Override
		public PersistenceFoundation.AbstractImplementation<M> setObjectIdProvider(
			final SwizzleObjectIdProvider oidProvider
		)
		{
			super.setObjectIdProvider(oidProvider);
			return this;
		}

		@Override
		public PersistenceFoundation.AbstractImplementation<M> setTypeIdProvider(
			final SwizzleTypeIdProvider tidProvider
		)
		{
			super.setTypeIdProvider(tidProvider);
			return this;
		}

		@Override
		public <P extends SwizzleTypeIdProvider & SwizzleObjectIdProvider>
		PersistenceFoundation.AbstractImplementation<M> setSwizzleIdProvider(
			final P swizzleTypeIdProvider
		)
		{
			this.setObjectIdProvider(swizzleTypeIdProvider);
			this.setTypeIdProvider  (swizzleTypeIdProvider);
			return this;
		}

		@Override
		public PersistenceFoundation.AbstractImplementation<M> setTypeManager(
			final SwizzleTypeManager typeManager
		)
		{
			this.typeManager = typeManager;
			return this;
		}

		@Override
		public PersistenceFoundation.AbstractImplementation<M> setTypeHandlerRegistry(
			final PersistenceTypeHandlerRegistry<M> typeHandlerRegistry
		)
		{
			this.typeHandlerRegistry = typeHandlerRegistry;
			return this;
		}

		@Override
		public PersistenceFoundation.AbstractImplementation<M> setTypeHandlerProvider(
			final PersistenceTypeHandlerProvider<M> typeHandlerProvider
		)
		{
			this.typeHandlerProvider = typeHandlerProvider;
			return this;
		}

		@Override
		public PersistenceFoundation.AbstractImplementation<M> setRegistererCreator(
			final PersistenceRegisterer.Creator registererCreator
		)
		{
			this.registererCreator = registererCreator;
			return this;
		}

		@Override
		public PersistenceFoundation.AbstractImplementation<M> setBuilderCreator(
			final PersistenceLoader.Creator<M> builderCreator
		)
		{
			this.builderCreator = builderCreator;
			return this;
		}

		@Override
		public PersistenceFoundation.AbstractImplementation<M> setPersistenceTarget(
			final PersistenceTarget<M> target
		)
		{
			this.target = target;
			return this;
		}

		@Override
		public PersistenceFoundation.AbstractImplementation<M> setPersistenceSource(
			final PersistenceSource<M> source
		)
		{
			this.source = source;
			return this;
		}
		
		@Override
		public PersistenceFoundation<M> setTypeDictionaryManager(
			final PersistenceTypeDictionaryManager typeDictionaryManager
		)
		{
			this.typeDictionaryManager = typeDictionaryManager;
			return this;
		}
				
		@Override
		public PersistenceFoundation<M> setTypeDictionaryCreator(
			final PersistenceTypeDictionaryCreator typeDictionaryCreator
		)
		{
			this.typeDictionaryCreator = typeDictionaryCreator;
			return this;
		}

		@Override
		public PersistenceFoundation.AbstractImplementation<M> setTypeDictionaryProvider(
			final PersistenceTypeDictionaryProvider typeDictionaryProvider
		)
		{
			this.typeDictionaryProvider = typeDictionaryProvider;
			return this;
		}

		@Override
		public PersistenceFoundation<M> setTypeDictionaryExporter(
			final PersistenceTypeDictionaryExporter typeDictionaryExporter
		)
		{
			this.typeDictionaryExporter = typeDictionaryExporter;
			return this;
		}

		@Override
		public PersistenceFoundation<M> setTypeDictionaryParser(
			final PersistenceTypeDictionaryParser typeDictionaryParser
		)
		{
			this.typeDictionaryParser = typeDictionaryParser;
			return this;
		}

		@Override
		public PersistenceFoundation.AbstractImplementation<M> setTypeDictionaryAssembler(
			final PersistenceTypeDictionaryAssembler typeDictionaryAssembler
		)
		{
			this.typeDictionaryAssembler = typeDictionaryAssembler;
			return this;
		}

		@Override
		public PersistenceFoundation.AbstractImplementation<M> setTypeDictionaryLoader(
			final PersistenceTypeDictionaryLoader typeDictionaryLoader
		)
		{
			this.typeDictionaryLoader = typeDictionaryLoader;
			return this;
		}
		
		@Override
		public PersistenceFoundation.AbstractImplementation<M> setTypeDictionaryBuilder(
			final PersistenceTypeDictionaryBuilder typeDictionaryBuilder
		)
		{
			this.typeDictionaryBuilder = typeDictionaryBuilder;
			return this;
		}

		@Override
		public PersistenceFoundation.AbstractImplementation<M> setTypeDictionaryStorer(
			final PersistenceTypeDictionaryStorer typeDictionaryStorer
		)
		{
			this.typeDictionaryStorer = typeDictionaryStorer;
			return this;
		}
		
		@Override
		public PersistenceFoundation.AbstractImplementation<M> setTypeLineageCreator(
			final PersistenceTypeLineageCreator typeLineageCreator
		)
		{
			this.typeLineageCreator = typeLineageCreator;
			return this;
		}

		@Override
		public PersistenceFoundation.AbstractImplementation<M> setTypeEvaluatorTypeIdMappable(
			final PersistenceTypeEvaluator typeEvaluatorTypeIdMappable
		)
		{
			this.typeEvaluatorTypeIdMappable = typeEvaluatorTypeIdMappable;
			return this;
		}

		@Override
		public PersistenceFoundation.AbstractImplementation<M> setTypeMismatchValidator(
			final PersistenceTypeMismatchValidator<M> typeMismatchValidator
		)
		{
			this.typeMismatchValidator = typeMismatchValidator;
			return this;
		}

		@Override
		public PersistenceFoundation.AbstractImplementation<M> setTypeResolver(
			final PersistenceTypeResolver typeResolver
		)
		{
			this.typeResolver = typeResolver;
			return this;
		}
		
		@Override
		public PersistenceFoundation.AbstractImplementation<M> setTypeDescriptionBuilder(
			final PersistenceTypeDefinitionCreator typeDefinitionCreator
		)
		{
			this.typeDefinitionCreator = typeDefinitionCreator;
			return this;
		}
		
		@Override
		public PersistenceFoundation.AbstractImplementation<M> setTypeEvaluatorPersistable(
			final PersistenceTypeEvaluator typeEvaluatorPersistable
		)
		{
			this.typeEvaluatorPersistable = typeEvaluatorPersistable;
			return this;
		}

		@Override
		public PersistenceFoundation.AbstractImplementation<M> setBufferSizeProvider(
			final BufferSizeProviderIncremental bufferSizeProvider
		)
		{
			this.bufferSizeProvider = bufferSizeProvider;
			return this;
		}

		@Override
		public PersistenceFoundation.AbstractImplementation<M> setFieldFixedLengthResolver(
			final PersistenceFieldLengthResolver fieldFixedLengthResolver
		)
		{
			this.fieldFixedLengthResolver = fieldFixedLengthResolver;
			return this;
		}

		@Override
		public PersistenceFoundation.AbstractImplementation<M> setFieldEvaluator(
			final PersistenceFieldEvaluator fieldEvaluator
		)
		{
			this.fieldEvaluator = fieldEvaluator;
			return this;
		}
		
		@Override
		public PersistenceFoundation.AbstractImplementation<M> setReferenceFieldMandatoryEvaluator(
			final PersistenceEagerStoringFieldEvaluator evaluator
		)
		{
			this.eagerStoringFieldEvaluator = evaluator;
			return this;
		}

		@Override
		public PersistenceFoundation.AbstractImplementation<M> setRootResolver(
			final PersistenceRootResolver rootResolver
		)
		{
			this.rootResolver = rootResolver;
			return this;
		}

		@Override
		public PersistenceFoundation.AbstractImplementation<M> setRootsProvider(
			final PersistenceRootsProvider<M> rootsProvider
		)
		{
			this.rootsProvider = rootsProvider;
			return this;
		}
		
		@Override
		public PersistenceFoundation<M> setLegacyTypeMapper(
			final PersistenceLegacyTypeMapper<M> legacyTypeMapper
		)
		{
			this.legacyTypeMapper = legacyTypeMapper;
			return this;
		}
		
		@Override
		public PersistenceFoundation.AbstractImplementation<M> setRefactoringMappingProvider(
			final PersistenceRefactoringMappingProvider refactoringMappingProvider
		)
		{
			this.refactoringMappingProvider = refactoringMappingProvider;
			return this;
		}
		
		@Override
		public PersistenceFoundation<M> setTypeSimilarity(final TypeMapping<Float> typeSimilarity)
		{
			this.typeSimilarity = typeSimilarity;
			return this;
		}
		
		@Override
		public PersistenceFoundation.AbstractImplementation<M> setDeletedTypeHandlerCreator(
			final PersistenceDeletedTypeHandlerCreator<M> deletedTypeHandlerCreator
		)
		{
			this.deletedTypeHandlerCreator = deletedTypeHandlerCreator;
			return this;
		}
		
		@Override
		public PersistenceFoundation<M> setLegacyMemberMatchingProvider(
			final PersistenceMemberMatchingProvider legacyMemberMatchingProvider
		)
		{
			this.legacyMemberMatchingProvider = legacyMemberMatchingProvider;
			return this;
		}
				
		@Override
		public PersistenceFoundation<M> setLegacyTypeMappingResultor(
			final PersistenceLegacyTypeMappingResultor<M> legacyTypeMappingResultor
		)
		{
			this.legacyTypeMappingResultor = legacyTypeMappingResultor;
			return this;
		}
		
		@Override
		public PersistenceFoundation<M> setLegacyTypeHandlerCreator(
			final PersistenceLegacyTypeHandlerCreator<M> legacyTypeHandlerCreator
		)
		{
			this.legacyTypeHandlerCreator = legacyTypeHandlerCreator;
			return this;
		}
		
		@Override
		public PersistenceFoundation<M> setLegacyTypeHandlingListener(
			final PersistenceLegacyTypeHandlingListener<M> legacyTypeHandlingListener
		)
		{
			this.legacyTypeHandlingListener = legacyTypeHandlingListener;
			return this;
		}



		
		///////////////////////////////////////////////////////////////////////////
		// creators         //
		/////////////////////

		protected SwizzleRegistry createSwizzleRegistry()
		{
			final SwizzleRegistryGrowingRange newSwizzleRegistry = new SwizzleRegistryGrowingRange();
			Swizzle.registerJavaBasicTypes(newSwizzleRegistry);
			Swizzle.registerJavaConstants(newSwizzleRegistry);
			return newSwizzleRegistry;
		}
		
		protected PersistenceTypeLineageCreator createTypeLineageCreator()
		{
			final PersistenceTypeLineageCreator newTypeLineageCreator =
				PersistenceTypeLineageCreator.New()
			;
			return newTypeLineageCreator;
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
					this.getTypeMismatchValidator(),
					this.getLegacyTypeMapper()
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
			return PersistenceTypeHandlerProviderCreating.New(
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
		
		protected PersistenceTypeDictionaryCreator createTypeDictionaryCreator()
		{
			final PersistenceTypeDictionaryCreator newTypeDictionaryCreator =
				PersistenceTypeDictionaryCreator.New(
					this.getTypeLineageCreator()
				)
			;
			return newTypeDictionaryCreator;
		}

		protected PersistenceTypeDictionaryProvider createTypeDictionaryProvider()
		{
			final PersistenceTypeDictionaryProvider newTypeDictionaryProvider =
				PersistenceTypeDictionaryProvider.New(
					this.getTypeDictionaryLoader(),
					this.getTypeDictionaryParser(),
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

		protected PersistenceTypeHandlerEnsurer<M> createTypeHandlerEnsurer()
		{
			return PersistenceTypeHandlerEnsurer.New(
				this.getCustomTypeHandlerRegistry(),
				this.getTypeHandlerCreator()
			);
		}

		protected PersistenceTypeDictionaryBuilder createTypeDictionaryBuilder()
		{
			return PersistenceTypeDictionaryBuilder.New(
				this.getTypeDictionaryCreator(),
				this.getTypeDefinitionCreator()
			);
		}

		protected PersistenceTypeEvaluator createTypeEvaluatorTypeIdMappable()
		{
			return Persistence.defaultTypeEvaluatorTypeIdMappable();
		}
		
		protected PersistenceTypeMismatchValidator<M> createTypeMismatchValidator()
		{
			// (13.09.2018 TM)NOTE: changed for legacy type mapping. Still a valid callback for monitoring purposes.
			return Persistence.typeMismatchValidatorNoOp();
//			return Persistence.typeMismatchValidatorFailing();
		}

		protected PersistenceTypeResolver createTypeResolver()
		{
			return PersistenceTypeResolver.Failing();
		}
		
		protected PersistenceTypeDefinitionCreator createTypeDefinitionCreator()
		{
			return PersistenceTypeDefinitionCreator.New();
		}

		protected PersistenceTypeEvaluator createTypeEvaluatorPersistable()
		{
			return Persistence.defaultTypeEvaluatorPersistable();
		}

		protected BufferSizeProviderIncremental createBufferSizeProvider()
		{
			return new BufferSizeProviderIncremental.Default();
		}

		protected PersistenceFieldEvaluator createFieldEvaluator()
		{
			return Persistence.defaultFieldEvaluator();
		}
		
		protected PersistenceEagerStoringFieldEvaluator createReferenceFieldMandatoryEvaluator()
		{
			return Persistence.defaultReferenceFieldMandatoryEvaluator();
		}
		
		protected PersistenceLegacyTypeMapper<M> createLegacyTypeMapper()
		{
			return PersistenceLegacyTypeMapper.New(
				this.getRefactoringMappingProvider()  ,
				this.getTypeSimilarity()              ,
				this.getCustomTypeHandlerRegistry()   ,
				this.getDeletedTypeHandlerCreator()   ,
				this.getLegacyMemberMatchingProvider(),
				this.getLegacyTypeMappingResultor()   ,
				this.getLegacyTypeHandlerCreator()    ,
				XReflect.fieldIdentifierDelimiter()
			);
		}
				
		protected PersistenceRefactoringMappingProvider createRefactoringMappingProvider()
		{
			// empty (= dummy) mapping by default
			return PersistenceRefactoringMappingProvider.NewEmpty();
		}
		
		protected TypeMapping<Float> createTypeSimilarity()
		{
			return XTypes.createDefaultTypeSimilarity();
		}
		
		protected PersistenceDeletedTypeHandlerCreator<M> createDeletedTypeHandlerCreator()
		{
			return PersistenceDeletedTypeHandlerCreator.New();
		}
				
		protected PersistenceMemberMatchingProvider createLegacyMemberMatchingProvider()
		{
			return PersistenceMemberMatchingProvider.New();
		}
				
		protected PersistenceLegacyTypeMappingResultor<M> createLegacyTypeMappingResultor()
		{
			return PersistenceLegacyTypeMappingResultor.New();
		}
		
		protected PersistenceLegacyTypeHandlerCreator<M> createLegacyTypeHandlerCreator()
		{
			throw new MissingFoundationPartException(PersistenceLegacyTypeHandlerCreator.class);
		}
		
		protected PersistenceLegacyTypeHandlingListener<M> createLegacyTypeHandlingListener()
		{
			/*
			 * this listener is purely optional, so by default, nothing is created.
			 * This method is just a stub for sub classes to override.
			 */
			return null;
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
			throw new MissingFoundationPartException(PersistenceStorer.Creator.class);
		}

		protected PersistenceLoader.Creator<M> createBuilderCreator()
		{
			throw new MissingFoundationPartException(PersistenceLoader.Creator.class);
		}

		protected PersistenceTarget<M> createPersistenceTarget()
		{
			throw new MissingFoundationPartException(PersistenceTarget.class);
		}

		protected PersistenceSource<M> createPersistenceSource()
		{
			throw new MissingFoundationPartException(PersistenceSource.class);
		}

		protected PersistenceTypeDictionaryLoader createTypeDictionaryLoader()
		{
			throw new MissingFoundationPartException(PersistenceTypeDictionaryLoader.class);
		}
		protected PersistenceTypeDictionaryStorer createTypeDictionaryStorer()
		{
			throw new MissingFoundationPartException(PersistenceTypeDictionaryStorer.class);
		}

		protected PersistenceTypeHandlerCreator<M> createTypeHandlerCreator()
		{
			throw new MissingFoundationPartException(PersistenceTypeHandlerCreator.class);
		}

		protected PersistenceCustomTypeHandlerRegistry<M> createCustomTypeHandlerRegistry()
		{
			throw new MissingFoundationPartException(PersistenceCustomTypeHandlerRegistry.class);
		}

		protected PersistenceFieldLengthResolver createFieldFixedLengthResolver()
		{
			throw new MissingFoundationPartException(PersistenceFieldLengthResolver.class);
		}
		
		protected PersistenceRootResolver createRootResolver()
		{
			throw new MissingFoundationPartException(PersistenceRootResolver.class);
		}
		
		protected PersistenceRootsProvider<M> createRootsProviderInternal()
		{
			throw new MissingFoundationPartException(PersistenceRootsProvider.class);
		}



		///////////////////////////////////////////////////////////////////////////
		// methods // (with logic worth mentioning)
		////////////

		protected PersistenceRootsProvider<M> createRootsProvider()
		{
			final PersistenceRootsProvider<M> rootsProvider = this.createRootsProviderInternal();
			rootsProvider.registerRootsTypeHandlerCreator(
				this.getCustomTypeHandlerRegistry(),
				this.getSwizzleRegistry()
			);
			
			return rootsProvider;
		}

		@Override
		public PersistenceRootResolver getEffectiveRootResolver()
		{
			final PersistenceRootResolver               definedRootResolver = this.getRootResolver();
			final PersistenceRefactoringMappingProvider mappingProvider     = this.getRefactoringMappingProvider();
			
			return mappingProvider == null
				? definedRootResolver
				: PersistenceRootResolver.Wrap(definedRootResolver, mappingProvider)
			;
		}
		
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

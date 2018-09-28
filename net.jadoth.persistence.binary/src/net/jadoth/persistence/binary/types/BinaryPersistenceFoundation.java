package net.jadoth.persistence.binary.types;

import net.jadoth.functional.InstanceDispatcherLogic;
import net.jadoth.persistence.types.BufferSizeProviderIncremental;
import net.jadoth.persistence.types.PersistenceChannel;
import net.jadoth.persistence.types.PersistenceCustomTypeHandlerRegistry;
import net.jadoth.persistence.types.PersistenceDeletedTypeHandlerCreator;
import net.jadoth.persistence.types.PersistenceEagerStoringFieldEvaluator;
import net.jadoth.persistence.types.PersistenceFieldEvaluator;
import net.jadoth.persistence.types.PersistenceFieldLengthResolver;
import net.jadoth.persistence.types.PersistenceFoundation;
import net.jadoth.persistence.types.PersistenceLegacyTypeHandlerCreator;
import net.jadoth.persistence.types.PersistenceLegacyTypeHandlingListener;
import net.jadoth.persistence.types.PersistenceLegacyTypeMapper;
import net.jadoth.persistence.types.PersistenceLegacyTypeMappingResultor;
import net.jadoth.persistence.types.PersistenceLoader;
import net.jadoth.persistence.types.PersistenceManager;
import net.jadoth.persistence.types.PersistenceMemberMatchingProvider;
import net.jadoth.persistence.types.PersistenceRefactoringMappingProvider;
import net.jadoth.persistence.types.PersistenceRegisterer;
import net.jadoth.persistence.types.PersistenceRootResolver;
import net.jadoth.persistence.types.PersistenceRootsProvider;
import net.jadoth.persistence.types.PersistenceSource;
import net.jadoth.persistence.types.PersistenceStorer;
import net.jadoth.persistence.types.PersistenceTarget;
import net.jadoth.persistence.types.PersistenceTypeDefinitionCreator;
import net.jadoth.persistence.types.PersistenceTypeDictionaryAssembler;
import net.jadoth.persistence.types.PersistenceTypeDictionaryBuilder;
import net.jadoth.persistence.types.PersistenceTypeDictionaryCreator;
import net.jadoth.persistence.types.PersistenceTypeDictionaryExporter;
import net.jadoth.persistence.types.PersistenceTypeDictionaryLoader;
import net.jadoth.persistence.types.PersistenceTypeDictionaryManager;
import net.jadoth.persistence.types.PersistenceTypeDictionaryParser;
import net.jadoth.persistence.types.PersistenceTypeDictionaryProvider;
import net.jadoth.persistence.types.PersistenceTypeDictionaryStorer;
import net.jadoth.persistence.types.PersistenceTypeEvaluator;
import net.jadoth.persistence.types.PersistenceTypeHandlerCreator;
import net.jadoth.persistence.types.PersistenceTypeHandlerEnsurer;
import net.jadoth.persistence.types.PersistenceTypeHandlerManager;
import net.jadoth.persistence.types.PersistenceTypeHandlerProvider;
import net.jadoth.persistence.types.PersistenceTypeHandlerRegistry;
import net.jadoth.persistence.types.PersistenceTypeLineageCreator;
import net.jadoth.persistence.types.PersistenceTypeMismatchValidator;
import net.jadoth.persistence.types.PersistenceTypeResolver;
import net.jadoth.swizzling.types.SwizzleObjectIdProvider;
import net.jadoth.swizzling.types.SwizzleObjectManager;
import net.jadoth.swizzling.types.SwizzleRegistry;
import net.jadoth.swizzling.types.SwizzleTypeIdProvider;
import net.jadoth.swizzling.types.SwizzleTypeManager;
import net.jadoth.typing.TypeMapping;



/**
 * Factory and master instance type for assembling and binary persistence layer.
 *
 * @author Thomas Muenz
 */
public interface BinaryPersistenceFoundation extends PersistenceFoundation<Binary>
{
	///////////////////////////////////////////////////////////////////////////
	// getters //
	////////////
	
	public BinaryValueTranslatorMappingProvider getValueTranslatorMappingProvider();
	
	public BinaryValueTranslatorProvider getValueTranslatorProvider();
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// setters  //
	/////////////
	
	// (overridden from Persistencefoundation to specify the return type)
	
	@Override
	public BinaryPersistenceFoundation setSwizzleRegistry(SwizzleRegistry swizzleRegistry);

	@Override
	public BinaryPersistenceFoundation setInstanceDispatcher(InstanceDispatcherLogic instanceDispatcher);

	@Override
	public BinaryPersistenceFoundation setObjectManager(SwizzleObjectManager objectManager);

	@Override
	public BinaryPersistenceFoundation setStorerCreator(PersistenceStorer.Creator<Binary> storerCreator);

	@Override
	public BinaryPersistenceFoundation setTypeHandlerManager(PersistenceTypeHandlerManager<Binary> typeHandlerManager);

	@Override
	public BinaryPersistenceFoundation setObjectIdProvider(SwizzleObjectIdProvider oidProvider);

	@Override
	public BinaryPersistenceFoundation setTypeIdProvider(SwizzleTypeIdProvider tidProvider);

	@Override
	public BinaryPersistenceFoundation setTypeManager(SwizzleTypeManager typeManager);

	@Override
	public BinaryPersistenceFoundation setTypeHandlerCreatorLookup(PersistenceTypeHandlerEnsurer<Binary> typeHandlerCreatorLookup);
	
	@Override
	public BinaryPersistenceFoundation setTypeHandlerCreator(PersistenceTypeHandlerCreator<Binary> typeHandlerCreator);

	@Override
	public BinaryPersistenceFoundation setTypeHandlerRegistry(PersistenceTypeHandlerRegistry<Binary> typeHandlerRegistry);

	@Override
	public BinaryPersistenceFoundation setTypeHandlerProvider(PersistenceTypeHandlerProvider<Binary> typeHandlerProvider);

	@Override
	public BinaryPersistenceFoundation setRegistererCreator(PersistenceRegisterer.Creator registererCreator);

	@Override
	public BinaryPersistenceFoundation setBuilderCreator(PersistenceLoader.Creator<Binary> builderCreator);

	@Override
	public BinaryPersistenceFoundation setPersistenceTarget(PersistenceTarget<Binary> target);

	@Override
	public BinaryPersistenceFoundation setPersistenceSource(PersistenceSource<Binary> source);

	@Override
	public BinaryPersistenceFoundation setTypeDictionaryManager(PersistenceTypeDictionaryManager typeDictionaryManager);
	
	@Override
	public BinaryPersistenceFoundation setTypeDictionaryCreator(PersistenceTypeDictionaryCreator typeDictionaryCreator);
		
	@Override
	public BinaryPersistenceFoundation setTypeDictionaryProvider(PersistenceTypeDictionaryProvider typeDictionaryProvider);
	
	@Override
	public BinaryPersistenceFoundation setTypeDictionaryExporter(PersistenceTypeDictionaryExporter typeDictionaryExporter);
	
	@Override
	public BinaryPersistenceFoundation setTypeDictionaryParser(PersistenceTypeDictionaryParser typeDictionaryParser);
	
	@Override
	public BinaryPersistenceFoundation setTypeDictionaryAssembler(PersistenceTypeDictionaryAssembler typeDictionaryAssembler);
	
	@Override
	public BinaryPersistenceFoundation setTypeDictionaryLoader(PersistenceTypeDictionaryLoader typeDictionaryLoader);
	
	@Override
	public BinaryPersistenceFoundation setTypeDictionaryBuilder(PersistenceTypeDictionaryBuilder typeDictionaryBuilder);
	
	@Override
	public BinaryPersistenceFoundation setTypeDictionaryStorer(PersistenceTypeDictionaryStorer typeDictionaryStorer);
	
	@Override
	public BinaryPersistenceFoundation setTypeLineageCreator(PersistenceTypeLineageCreator typeLineageCreator);

	@Override
	public BinaryPersistenceFoundation setTypeEvaluatorTypeIdMappable(PersistenceTypeEvaluator typeEvaluatorTypeIdMappable);
	
	@Override
	public BinaryPersistenceFoundation setTypeMismatchValidator(PersistenceTypeMismatchValidator<Binary> typeMismatchValidator);

	@Override
	public BinaryPersistenceFoundation setTypeResolver(PersistenceTypeResolver typeResolver);
	
	@Override
	public BinaryPersistenceFoundation setTypeDescriptionBuilder(PersistenceTypeDefinitionCreator typeDefinitionCreator);

	@Override
	public BinaryPersistenceFoundation setTypeEvaluatorPersistable(PersistenceTypeEvaluator getTypeEvaluatorPersistable);

	@Override
	public BinaryPersistenceFoundation setBufferSizeProvider(BufferSizeProviderIncremental bufferSizeProvider);

	@Override
	public BinaryPersistenceFoundation setFieldFixedLengthResolver(PersistenceFieldLengthResolver fieldFixedLengthResolver);

	@Override
	public BinaryPersistenceFoundation setFieldEvaluator(PersistenceFieldEvaluator fieldEvaluator);

	@Override
	public BinaryPersistenceFoundation setReferenceFieldMandatoryEvaluator(PersistenceEagerStoringFieldEvaluator evaluator);

	@Override
	public BinaryPersistenceFoundation setRootResolver(PersistenceRootResolver rootResolver);

	@Override
	public BinaryPersistenceFoundation setRootsProvider(PersistenceRootsProvider<Binary> rootsProvider);
	
	@Override
	public BinaryPersistenceFoundation setLegacyTypeMapper(PersistenceLegacyTypeMapper<Binary> legacyTypeMapper);
	
	@Override
	public BinaryPersistenceFoundation setTypeSimilarity(TypeMapping<Float> typeSimilarity);
	
	@Override
	public BinaryPersistenceFoundation setRefactoringMappingProvider(
		PersistenceRefactoringMappingProvider refactoringMappingProvider
	);
	
	@Override
	public BinaryPersistenceFoundation setDeletedTypeHandlerCreator(
		PersistenceDeletedTypeHandlerCreator<Binary> deletedTypeHandlerCreator
	);
		
	@Override
	public BinaryPersistenceFoundation setLegacyMemberMatchingProvider(
		PersistenceMemberMatchingProvider legacyMemberMatchingProvider
	);
		
	@Override
	public BinaryPersistenceFoundation setLegacyTypeMappingResultor(
		PersistenceLegacyTypeMappingResultor<Binary> legacyTypeMappingResultor
	);
	
	@Override
	public BinaryPersistenceFoundation setLegacyTypeHandlerCreator(
		PersistenceLegacyTypeHandlerCreator<Binary> legacyTypeHandlerCreator
	);
	
	@Override
	public BinaryPersistenceFoundation setLegacyTypeHandlingListener(
		PersistenceLegacyTypeHandlingListener<Binary> legacyTypeHandlingListener
	);
	
	@Override
	public <H extends PersistenceTypeDictionaryLoader & PersistenceTypeDictionaryStorer>
	BinaryPersistenceFoundation setDictionaryStorage(H typeDictionaryStorageHandler);

	@Override
	public <P extends SwizzleTypeIdProvider & SwizzleObjectIdProvider>
	BinaryPersistenceFoundation setSwizzleIdProvider(P swizzleTypeIdProvider);

	@Override
	public BinaryPersistenceFoundation setPersistenceChannel(PersistenceChannel<Binary> persistenceChannel);
	

	
	///////////////////////////////////////////////////////////////////////////
	// specific setters //
	/////////////////////

	public BinaryPersistenceFoundation setValueTranslatorProvider(
		BinaryValueTranslatorProvider valueTranslatorProvider
	);
	
	public BinaryPersistenceFoundation setValueTranslatorMappingProvider(
		BinaryValueTranslatorMappingProvider valueTranslatorMappingProvider
	);


	
	@Override
	public PersistenceManager<Binary> createPersistenceManager();


	
	public static BinaryPersistenceFoundation.Implementation New()
	{
		return new BinaryPersistenceFoundation.Implementation();
	}

	public class Implementation
	extends PersistenceFoundation.AbstractImplementation<Binary>
	implements BinaryPersistenceFoundation
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private BinaryValueTranslatorMappingProvider valueTranslatorMapping ;
		private BinaryValueTranslatorProvider        valueTranslatorProvider;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		protected Implementation()
		{
			super();
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// getters          //
		/////////////////////
		
		@Override
		public BinaryValueTranslatorMappingProvider getValueTranslatorMappingProvider()
		{
			if(this.valueTranslatorMapping == null)
			{
				this.valueTranslatorMapping = this.dispatch(this.createValueTranslatorMappingProvider());
			}
			
			return this.valueTranslatorMapping;
		}
		
		@Override
		public BinaryValueTranslatorProvider getValueTranslatorProvider()
		{
			if(this.valueTranslatorProvider == null)
			{
				this.valueTranslatorProvider = this.dispatch(this.createValueTranslatorProvider());
			}
			
			return this.valueTranslatorProvider;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// setters          //
		/////////////////////

		@Override
		public BinaryPersistenceFoundation.Implementation setTypeDictionaryAssembler(
			final PersistenceTypeDictionaryAssembler typeDictionaryAssembler
		)
		{
			super.setTypeDictionaryAssembler(typeDictionaryAssembler);
			return this;
		}

		@Override
		public BinaryPersistenceFoundation.Implementation setTypeDictionaryStorer(
			final PersistenceTypeDictionaryStorer typeDictionaryStorer
		)
		{
			super.setTypeDictionaryStorer(typeDictionaryStorer);
			return this;
		}

		@Override
		public BinaryPersistenceFoundation.Implementation setTypeDictionaryProvider(
			final PersistenceTypeDictionaryProvider typeDictionaryProvider
		)
		{
			super.setTypeDictionaryProvider(typeDictionaryProvider);
			return this;
		}

		@Override
		public BinaryPersistenceFoundation.Implementation setTypeDictionaryManager(
			final PersistenceTypeDictionaryManager typeDictionaryManager
		)
		{
			super.setTypeDictionaryManager(typeDictionaryManager);
			return this;
		}

		@Override
		public BinaryPersistenceFoundation.Implementation setInstanceDispatcher(final InstanceDispatcherLogic instanceDispatcher)
		{
			super.setInstanceDispatcher(instanceDispatcher);
			return this;
		}

		@Override
		public BinaryPersistenceFoundation.Implementation setObjectManager(final SwizzleObjectManager objectManager)
		{
			super.setObjectManager(objectManager);
			return this;
		}

		@Override
		public BinaryPersistenceFoundation.Implementation setSwizzleRegistry(final SwizzleRegistry swizzleRegistry)
		{
			super.setSwizzleRegistry(swizzleRegistry);
			return this;
		}

		@Override
		public BinaryPersistenceFoundation.Implementation setStorerCreator(
			final PersistenceStorer.Creator<Binary> storerCreator
		)
		{
			super.setStorerCreator(storerCreator);
			return this;
		}

		@Override
		public BinaryPersistenceFoundation.Implementation setTypeHandlerManager(
			final PersistenceTypeHandlerManager<Binary> typeHandlerManager
		)
		{
			super.setTypeHandlerManager(typeHandlerManager);
			return this;
		}

		@Override
		public BinaryPersistenceFoundation.Implementation setObjectIdProvider(final SwizzleObjectIdProvider oidProvider)
		{
			super.setObjectIdProvider(oidProvider);
			return this;
		}

		@Override
		public BinaryPersistenceFoundation.Implementation setTypeIdProvider(final SwizzleTypeIdProvider tidProvider)
		{
			super.setTypeIdProvider(tidProvider);
			return this;
		}

		@Override
		public BinaryPersistenceFoundation.Implementation setTypeManager(final SwizzleTypeManager typeManager)
		{
			super.setTypeManager(typeManager);
			return this;
		}

		@Override
		public BinaryPersistenceFoundation.Implementation setTypeHandlerCreatorLookup(
			final PersistenceTypeHandlerEnsurer<Binary> typeHandlerCreatorLookup
		)
		{
			super.setTypeHandlerCreatorLookup(typeHandlerCreatorLookup);
			return this;
		}
		
		@Override
		public BinaryPersistenceFoundation.Implementation setTypeHandlerCreator(
			final PersistenceTypeHandlerCreator<Binary> typeHandlerCreator
		)
		{
			super.setTypeHandlerCreator(typeHandlerCreator);
			return this;
		}

		@Override
		public BinaryPersistenceFoundation.Implementation setTypeHandlerRegistry(
			final PersistenceTypeHandlerRegistry<Binary> typeHandlerRegistry
		)
		{
			super.setTypeHandlerRegistry(typeHandlerRegistry);
			return this;
		}

		@Override
		public BinaryPersistenceFoundation.Implementation setTypeHandlerProvider(
			final PersistenceTypeHandlerProvider<Binary> typeHandlerProvider
		)
		{
			super.setTypeHandlerProvider(typeHandlerProvider);
			return this;
		}

		@Override
		public BinaryPersistenceFoundation.Implementation setRegistererCreator(
			final PersistenceRegisterer.Creator registererCreator
		)
		{
			super.setRegistererCreator(registererCreator);
			return this;
		}

		@Override
		public BinaryPersistenceFoundation.Implementation setBuilderCreator(
			final PersistenceLoader.Creator<Binary> builderCreator
		)
		{
			super.setBuilderCreator(builderCreator);
			return this;
		}

		@Override
		public BinaryPersistenceFoundation.Implementation setPersistenceTarget(
			final PersistenceTarget<Binary> target
		)
		{
			super.setPersistenceTarget(target);
			return this;
		}

		@Override
		public BinaryPersistenceFoundation.Implementation setPersistenceSource(
			final PersistenceSource<Binary> source
		)
		{
			super.setPersistenceSource(source);
			return this;
		}

		@Override
		public BinaryPersistenceFoundation.Implementation setTypeDictionaryExporter(
			final PersistenceTypeDictionaryExporter typeDictionaryExporter
		)
		{
			super.setTypeDictionaryExporter(typeDictionaryExporter);
			return this;
		}

		@Override
		public BinaryPersistenceFoundation.Implementation setTypeDictionaryParser(
			final PersistenceTypeDictionaryParser typeDictionaryParser
		)
		{
			super.setTypeDictionaryParser(typeDictionaryParser);
			return this;
		}

		@Override
		public BinaryPersistenceFoundation.Implementation setTypeDictionaryLoader(
			final PersistenceTypeDictionaryLoader typeDictionaryLoader
		)
		{
			super.setTypeDictionaryLoader(typeDictionaryLoader);
			return this;
		}

		@Override
		public <H extends PersistenceTypeDictionaryLoader & PersistenceTypeDictionaryStorer>
		BinaryPersistenceFoundation.Implementation setDictionaryStorage(final H typeDictionaryStorageHandler)
		{
			super.setTypeDictionaryLoader(typeDictionaryStorageHandler);
			super.setTypeDictionaryStorer(typeDictionaryStorageHandler);
			return this;
		}

		@Override
		public BinaryPersistenceFoundation.Implementation setPersistenceChannel(final PersistenceChannel<Binary> persistenceChannel)
		{
			super.setPersistenceSource(persistenceChannel);
			super.setPersistenceTarget(persistenceChannel);
			return this;
		}

		@Override
		public <P extends SwizzleTypeIdProvider & SwizzleObjectIdProvider>
		BinaryPersistenceFoundation.Implementation setSwizzleIdProvider(final P swizzleTypeIdProvider)
		{
			this.setTypeIdProvider  (swizzleTypeIdProvider);
			this.setObjectIdProvider(swizzleTypeIdProvider);
			return this;
		}

		@Override
		public BinaryPersistenceFoundation.Implementation setBufferSizeProvider(
			final BufferSizeProviderIncremental bufferSizeProvider
		)
		{
			super.setBufferSizeProvider(bufferSizeProvider);
			return this;
		}

		@Override
		public BinaryPersistenceFoundation.Implementation setTypeEvaluatorPersistable(
			final PersistenceTypeEvaluator getTypeEvaluatorPersistable
		)
		{
			super.setTypeEvaluatorPersistable(getTypeEvaluatorPersistable);
			return this;
		}

		@Override
		public BinaryPersistenceFoundation.Implementation setTypeEvaluatorTypeIdMappable(
			final PersistenceTypeEvaluator getTypeEvaluatorTypeIdMappable
		)
		{
			super.setTypeEvaluatorTypeIdMappable(getTypeEvaluatorTypeIdMappable);
			return this;
		}

		@Override
		public BinaryPersistenceFoundation.Implementation setFieldFixedLengthResolver(
			final PersistenceFieldLengthResolver resolver
		)
		{
			super.setFieldFixedLengthResolver(resolver);
			return this;
		}
		
		@Override
		public BinaryPersistenceFoundation.Implementation setTypeDictionaryCreator(
			final PersistenceTypeDictionaryCreator typeDictionaryCreator
		)
		{
			super.setTypeDictionaryCreator(typeDictionaryCreator);
			return this;
		}
		
		@Override
		public BinaryPersistenceFoundation.Implementation setLegacyMemberMatchingProvider(
			final PersistenceMemberMatchingProvider legacyMemberMatchingProvider
		)
		{
			super.setLegacyMemberMatchingProvider(legacyMemberMatchingProvider);
			return this;
		}
		
		@Override
		public BinaryPersistenceFoundation.Implementation setLegacyTypeHandlerCreator(
			final PersistenceLegacyTypeHandlerCreator<Binary> legacyTypeHandlerCreator
		)
		{
			super.setLegacyTypeHandlerCreator(legacyTypeHandlerCreator);
			return this;
		}
		
		@Override
		public BinaryPersistenceFoundation.Implementation setLegacyTypeHandlingListener(
			final PersistenceLegacyTypeHandlingListener<Binary> legacyTypeHandlingListener
		)
		{
			super.setLegacyTypeHandlingListener(legacyTypeHandlingListener);
			return this;
		}
		
		@Override
		public BinaryPersistenceFoundation.Implementation setLegacyTypeMapper(
			final PersistenceLegacyTypeMapper<Binary> legacyTypeMapper
		)
		{
			super.setLegacyTypeMapper(legacyTypeMapper);
			return this;
		}
		
		@Override
		public BinaryPersistenceFoundation.Implementation setLegacyTypeMappingResultor(
			final PersistenceLegacyTypeMappingResultor<Binary> legacyTypeMappingResultor
		)
		{
			super.setLegacyTypeMappingResultor(legacyTypeMappingResultor);
			return this;
		}
		
		@Override
		public BinaryPersistenceFoundation.Implementation setTypeSimilarity(
			final TypeMapping<Float> typeSimilarity
		)
		{
			super.setTypeSimilarity(typeSimilarity);
			return this;
		}
		
		@Override
		public BinaryPersistenceFoundation.Implementation setDeletedTypeHandlerCreator(
			final PersistenceDeletedTypeHandlerCreator<Binary> deletedTypeHandlerCreator
		)
		{
			super.setDeletedTypeHandlerCreator(deletedTypeHandlerCreator);
			return this;
		}
		
		@Override
		public BinaryPersistenceFoundation.Implementation setFieldEvaluator(
			final PersistenceFieldEvaluator fieldEvaluator
		)
		{
			super.setFieldEvaluator(fieldEvaluator);
			return this;
		}
		
		@Override
		public BinaryPersistenceFoundation.Implementation setRefactoringMappingProvider(
			final PersistenceRefactoringMappingProvider refactoringMappingProvider
		)
		{
			super.setRefactoringMappingProvider(refactoringMappingProvider);
			return this;
		}
		
		@Override
		public BinaryPersistenceFoundation.Implementation setReferenceFieldMandatoryEvaluator(
			final PersistenceEagerStoringFieldEvaluator evaluator
		)
		{
			super.setReferenceFieldMandatoryEvaluator(evaluator);
			return this;
		}
		
		@Override
		public BinaryPersistenceFoundation.Implementation setRootResolver(
			final PersistenceRootResolver rootResolver
		)
		{
			super.setRootResolver(rootResolver);
			return this;
		}
		
		@Override
		public BinaryPersistenceFoundation.Implementation setRootsProvider(
			final PersistenceRootsProvider<Binary> rootsProvider
		)
		{
			super.setRootsProvider(rootsProvider);
			return this;
		}
		
		@Override
		public BinaryPersistenceFoundation.Implementation setTypeDescriptionBuilder(
			final PersistenceTypeDefinitionCreator typeDefinitionCreator
		)
		{
			super.setTypeDescriptionBuilder(typeDefinitionCreator);
			return this;
		}
		
		@Override
		public BinaryPersistenceFoundation.Implementation setTypeDictionaryBuilder(
			final PersistenceTypeDictionaryBuilder typeDictionaryBuilder
		)
		{
			super.setTypeDictionaryBuilder(typeDictionaryBuilder);
			return this;
		}
		
		@Override
		public BinaryPersistenceFoundation.Implementation setTypeLineageCreator(
			final PersistenceTypeLineageCreator typeLineageCreator
		)
		{
			super.setTypeLineageCreator(typeLineageCreator);
			return this;
		}
		
		@Override
		public BinaryPersistenceFoundation.Implementation setTypeMismatchValidator(
			final PersistenceTypeMismatchValidator<Binary> typeMismatchValidator
		)
		{
			super.setTypeMismatchValidator(typeMismatchValidator);
			return this;
		}
		
		@Override
		public BinaryPersistenceFoundation.Implementation setTypeResolver(
			final PersistenceTypeResolver typeResolver
		)
		{
			super.setTypeResolver(typeResolver);
			return this;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// specific setters //
		/////////////////////
		
		@Override
		public BinaryPersistenceFoundation setValueTranslatorProvider(
			final BinaryValueTranslatorProvider valueTranslatorProvider
		)
		{
			this.valueTranslatorProvider = valueTranslatorProvider;
			return this;
		}
		
		@Override
		public BinaryPersistenceFoundation setValueTranslatorMappingProvider(
			final BinaryValueTranslatorMappingProvider valueTranslatorMapping
		)
		{
			this.valueTranslatorMapping = valueTranslatorMapping;
			return this;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		protected BinaryStorer.Creator createStorerCreator()
		{
			return BinaryStorer.Creator(() -> 1);
		}

		@Override
		protected BinaryLoader.Creator createBuilderCreator()
		{
			return new BinaryLoader.CreatorSimple();
		}

		@Override
		protected PersistenceTypeHandlerCreator<Binary> createTypeHandlerCreator()
		{
			return new BinaryTypeHandlerCreator.Implementation(
				this.getTypeAnalyzer(),
				this.getFieldFixedLengthResolver(),
				this.getReferenceFieldMandatoryEvaluator()
			);
		}

		@Override
		protected PersistenceCustomTypeHandlerRegistry<Binary> createCustomTypeHandlerRegistry()
		{
			return BinaryPersistence.createDefaultCustomTypeHandlerRegistry();
		}

		@Override
		protected BinaryFieldLengthResolver createFieldFixedLengthResolver()
		{
			return BinaryPersistence.createFieldLengthResolver();
		}
		
		@Override
		protected PersistenceRootsProvider<Binary> createRootsProviderInternal()
		{
			return BinaryPersistenceRootsProvider.New(
				this.getRootResolver()
			);
		}
		
		@Override
		protected PersistenceLegacyTypeHandlerCreator<Binary> createLegacyTypeHandlerCreator()
		{
			return BinaryLegacyTypeHandlerCreator.New(
				this.createValueTranslatorProvider(),
				this.getLegacyTypeHandlingListener()
			);
		}
		
		protected BinaryValueTranslatorMappingProvider createValueTranslatorMappingProvider()
		{
			return BinaryValueTranslatorMappingProvider.New();
		}
		
		protected BinaryValueTranslatorProvider createValueTranslatorProvider()
		{
			return BinaryValueTranslatorProvider.New(
				this.getValueTranslatorMappingProvider()
			);
		}
		
	}

}

package one.microstream.storage.embedded.types;

import java.nio.ByteOrder;
import java.util.function.Supplier;

import one.microstream.collections.HashTable;
import one.microstream.collections.types.XEnum;
import one.microstream.collections.types.XMap;
import one.microstream.collections.types.XTable;
import one.microstream.functional.InstanceDispatcherLogic;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.binary.types.BinaryPersistenceFoundation;
import one.microstream.persistence.binary.types.BinaryValueSetter;
import one.microstream.persistence.binary.types.BinaryValueTranslatorKeyBuilder;
import one.microstream.persistence.binary.types.BinaryValueTranslatorMappingProvider;
import one.microstream.persistence.binary.types.BinaryValueTranslatorProvider;
import one.microstream.persistence.types.PersistenceAbstractTypeHandlerSearcher;
import one.microstream.persistence.types.PersistenceChannel;
import one.microstream.persistence.types.PersistenceContextDispatcher;
import one.microstream.persistence.types.PersistenceCustomTypeHandlerRegistry;
import one.microstream.persistence.types.PersistenceCustomTypeHandlerRegistryEnsurer;
import one.microstream.persistence.types.PersistenceEagerStoringFieldEvaluator;
import one.microstream.persistence.types.PersistenceFieldEvaluator;
import one.microstream.persistence.types.PersistenceFieldLengthResolver;
import one.microstream.persistence.types.PersistenceInstantiator;
import one.microstream.persistence.types.PersistenceLegacyTypeHandlerCreator;
import one.microstream.persistence.types.PersistenceLegacyTypeHandlingListener;
import one.microstream.persistence.types.PersistenceLegacyTypeMapper;
import one.microstream.persistence.types.PersistenceLegacyTypeMappingResultor;
import one.microstream.persistence.types.PersistenceManager;
import one.microstream.persistence.types.PersistenceMemberMatchingProvider;
import one.microstream.persistence.types.PersistenceObjectIdProvider;
import one.microstream.persistence.types.PersistenceObjectManager;
import one.microstream.persistence.types.PersistenceObjectRegistry;
import one.microstream.persistence.types.PersistenceRefactoringMappingProvider;
import one.microstream.persistence.types.PersistenceRefactoringMemberIdentifierBuilder;
import one.microstream.persistence.types.PersistenceRefactoringTypeIdentifierBuilder;
import one.microstream.persistence.types.PersistenceRootReferenceProvider;
import one.microstream.persistence.types.PersistenceRootResolverProvider;
import one.microstream.persistence.types.PersistenceRootsProvider;
import one.microstream.persistence.types.PersistenceSizedArrayLengthController;
import one.microstream.persistence.types.PersistenceSource;
import one.microstream.persistence.types.PersistenceStorer.Creator;
import one.microstream.persistence.types.PersistenceTarget;
import one.microstream.persistence.types.PersistenceTypeAnalyzer;
import one.microstream.persistence.types.PersistenceTypeDefinitionCreator;
import one.microstream.persistence.types.PersistenceTypeDescriptionResolverProvider;
import one.microstream.persistence.types.PersistenceTypeDictionaryAssembler;
import one.microstream.persistence.types.PersistenceTypeDictionaryBuilder;
import one.microstream.persistence.types.PersistenceTypeDictionaryCompiler;
import one.microstream.persistence.types.PersistenceTypeDictionaryCreator;
import one.microstream.persistence.types.PersistenceTypeDictionaryExporter;
import one.microstream.persistence.types.PersistenceTypeDictionaryIoHandler;
import one.microstream.persistence.types.PersistenceTypeDictionaryLoader;
import one.microstream.persistence.types.PersistenceTypeDictionaryManager;
import one.microstream.persistence.types.PersistenceTypeDictionaryParser;
import one.microstream.persistence.types.PersistenceTypeDictionaryProvider;
import one.microstream.persistence.types.PersistenceTypeDictionaryStorer;
import one.microstream.persistence.types.PersistenceTypeEvaluator;
import one.microstream.persistence.types.PersistenceTypeHandler;
import one.microstream.persistence.types.PersistenceTypeHandlerCreator;
import one.microstream.persistence.types.PersistenceTypeHandlerEnsurer;
import one.microstream.persistence.types.PersistenceTypeHandlerManager;
import one.microstream.persistence.types.PersistenceTypeHandlerProvider;
import one.microstream.persistence.types.PersistenceTypeHandlerRegistration;
import one.microstream.persistence.types.PersistenceTypeHandlerRegistry;
import one.microstream.persistence.types.PersistenceTypeIdProvider;
import one.microstream.persistence.types.PersistenceTypeInstantiator;
import one.microstream.persistence.types.PersistenceTypeInstantiatorProvider;
import one.microstream.persistence.types.PersistenceTypeLineageCreator;
import one.microstream.persistence.types.PersistenceTypeManager;
import one.microstream.persistence.types.PersistenceTypeMismatchValidator;
import one.microstream.persistence.types.PersistenceTypeNameMapper;
import one.microstream.persistence.types.PersistenceTypeRegistry;
import one.microstream.persistence.types.PersistenceTypeResolver;
import one.microstream.persistence.types.PersistenceUnreachableTypeHandlerCreator;
import one.microstream.persistence.types.Persister;
import one.microstream.reflect.ClassLoaderProvider;
import one.microstream.storage.types.StorageConnection;
import one.microstream.storage.types.StorageLoggingWrapper;
import one.microstream.storage.types.StorageSystem;
import one.microstream.storage.types.StorageWriteController;
import one.microstream.typing.LambdaTypeRecognizer;
import one.microstream.typing.TypeMapping;
import one.microstream.util.BufferSizeProviderIncremental;

public interface EmbeddedStorageConnectionFoundationLogging<F extends EmbeddedStorageConnectionFoundation<?>>
	extends EmbeddedStorageConnectionFoundation<F>, StorageLoggingWrapper<EmbeddedStorageConnectionFoundation<F>>
{
	
	public static <F extends EmbeddedStorageConnectionFoundation<?>> EmbeddedStorageConnectionFoundationLogging<F> New(
		final EmbeddedStorageConnectionFoundation<F> wrapped
	)
	{
		return new Default<>(wrapped);
	}
	
	public static class Default<F extends EmbeddedStorageConnectionFoundation<?>>
	extends StorageLoggingWrapper.Abstract<EmbeddedStorageConnectionFoundation<F>>
	implements EmbeddedStorageConnectionFoundationLogging<F>
	{
		protected Default(
			final EmbeddedStorageConnectionFoundation<F> wrapped
		)
		{
			super(wrapped);
		}
						
		@Override
		public Class<Binary> dataType()
		{
			return this.wrapped().dataType();
		}

		@Override
		public ByteOrder getTargetByteOrder()
		{
			return this.wrapped().getTargetByteOrder();
		}

		@Override
		public boolean isByteOrderMismatch()
		{
			return this.wrapped().isByteOrderMismatch();
		}

		@Override
		public Supplier<? extends StorageSystem> storageSystemSupplier()
		{
			return this.wrapped().storageSystemSupplier();
		}

		@Override
		public F setTargetByteOrder(final ByteOrder targetByteOrder)
		{
			return this.wrapped().setTargetByteOrder(targetByteOrder);
		}

		@Override
		public StorageSystem getStorageSystem()
		{
			return this.wrapped().getStorageSystem();
		}

		@Override
		public F setStorageSystem(final StorageSystem storageSystem)
		{
			return this.wrapped().setStorageSystem(storageSystem);
		}

		@Override
		public F setStorageSystemSupplier(final Supplier<? extends StorageSystem> storageSystemSupplier)
		{
			return this.wrapped().setStorageSystemSupplier(storageSystemSupplier);
		}

		@Override
		public StorageConnection createStorageConnection()
		{
			this.logger().embeddedStorageConnectionFoundation_beforeCreateConnection();
			
			final StorageConnection storageConnection = this.wrapped().createStorageConnection();
			
			this.logger().embeddedStorageConnectionFoundation_afterCreateConnection(storageConnection);
			
			return storageConnection;
		}

		@Override
		public BinaryPersistenceFoundation<F> Clone()
		{
			return this.wrapped().Clone();
		}

		@Override
		public XTable<String, BinaryValueSetter> getCustomTranslatorLookup()
		{
			return this.wrapped().getCustomTranslatorLookup();
		}

		@Override
		public XEnum<BinaryValueTranslatorKeyBuilder> getTranslatorKeyBuilders()
		{
			return this.wrapped().getTranslatorKeyBuilders();
		}

		@Override
		public BinaryValueTranslatorMappingProvider getValueTranslatorMappingProvider()
		{
			return this.wrapped().getValueTranslatorMappingProvider();
		}

		@Override
		public BinaryValueTranslatorProvider getValueTranslatorProvider()
		{
			return this.wrapped().getValueTranslatorProvider();
		}

		@Override
		public F setCustomTranslatorLookup(final XTable<String, BinaryValueSetter> customTranslatorLookup)
		{
			return this.wrapped().setCustomTranslatorLookup(customTranslatorLookup);
		}

		@Override
		public F setTranslatorKeyBuilders(final XEnum<BinaryValueTranslatorKeyBuilder> translatorKeyBuilders)
		{
			return this.wrapped().setTranslatorKeyBuilders(translatorKeyBuilders);
		}

		@Override
		public F setValueTranslatorProvider(final BinaryValueTranslatorProvider valueTranslatorProvider)
		{
			return this.wrapped().setValueTranslatorProvider(valueTranslatorProvider);
		}

		@Override
		public F setValueTranslatorMappingProvider(final BinaryValueTranslatorMappingProvider valueTranslatorMappingProvider)
		{
			return this.wrapped().setValueTranslatorMappingProvider(valueTranslatorMappingProvider);
		}

		@Override
		public InstanceDispatcherLogic getInstanceDispatcherLogic()
		{
			return this.wrapped().getInstanceDispatcherLogic();
		}

		@Override
		public PersistenceManager<Binary> createPersistenceManager()
		{
			this.logger().embeddedStorageConnectionFoundation_beforeCreatePersistenceManager();
			
			final PersistenceManager<Binary> persistenceManager = this.wrapped().createPersistenceManager();
			
			this.logger().embeddedStorageConnectionFoundation_afterPersistenceManager(persistenceManager);
			
			return persistenceManager;
		}

		@Override
		public XMap<Class<?>, PersistenceTypeHandler<Binary, ?>> customTypeHandlers()
		{
			return this.wrapped().customTypeHandlers();
		}

		@Override
		public XMap<Class<?>, PersistenceTypeInstantiator<Binary, ?>> customTypeInstantiators()
		{
			return this.wrapped().customTypeInstantiators();
		}

		@Override
		public F registerCustomTypeHandlers(final HashTable<Class<?>, PersistenceTypeHandler<Binary, ?>> customTypeHandlers)
		{
			return this.wrapped().registerCustomTypeHandlers(customTypeHandlers);
		}

		@Override
		@SuppressWarnings("unchecked")
		public F registerCustomTypeHandlers(final PersistenceTypeHandler<Binary, ?>... customTypeHandlers)
		{
			return this.wrapped().registerCustomTypeHandlers(customTypeHandlers);
		}

		@Override
		public F registerCustomTypeHandlers(final Iterable<? extends PersistenceTypeHandler<Binary, ?>> customTypeHandlers)
		{
			return this.wrapped().registerCustomTypeHandlers(customTypeHandlers);
		}

		@Override
		public F registerCustomTypeHandler(final PersistenceTypeHandler<Binary, ?> customTypeHandler)
		{
			return this.wrapped().registerCustomTypeHandler(customTypeHandler);
		}

		@Override
		public <T> F registerCustomInstantiator(final Class<T> type, final PersistenceTypeInstantiator<Binary, T> typeInstantiator)
		{
			return this.wrapped().registerCustomInstantiator(type, typeInstantiator);
		}

		@Override
		public PersistenceObjectIdProvider getObjectIdProvider()
		{
			return this.wrapped().getObjectIdProvider();
		}

		@Override
		public PersistenceTypeIdProvider getTypeIdProvider()
		{
			return this.wrapped().getTypeIdProvider();
		}

		@Override
		public Creator<Binary> getStorerCreator()
		{
			return this.wrapped().getStorerCreator();
		}

		@Override
		public one.microstream.persistence.types.PersistenceRegisterer.Creator getRegistererCreator()
		{
			return this.wrapped().getRegistererCreator();
		}

		@Override
		public one.microstream.persistence.types.PersistenceLoader.Creator<Binary> getBuilderCreator()
		{
			return this.wrapped().getBuilderCreator();
		}

		@Override
		public Persister getPersister()
		{
			return this.wrapped().getPersister();
		}

		@Override
		public PersistenceTarget<Binary> getPersistenceTarget()
		{
			return this.wrapped().getPersistenceTarget();
		}

		@Override
		public PersistenceSource<Binary> getPersistenceSource()
		{
			return this.wrapped().getPersistenceSource();
		}

		@Override
		public PersistenceObjectRegistry getObjectRegistry()
		{
			return this.wrapped().getObjectRegistry();
		}

		@Override
		public PersistenceObjectManager<Binary> getObjectManager()
		{
			return this.wrapped().getObjectManager();
		}

		@Override
		public PersistenceTypeRegistry getTypeRegistry()
		{
			return this.wrapped().getTypeRegistry();
		}

		@Override
		public PersistenceTypeManager getTypeManager()
		{
			return this.wrapped().getTypeManager();
		}

		@Override
		public PersistenceTypeHandlerManager<Binary> getTypeHandlerManager()
		{
			return this.wrapped().getTypeHandlerManager();
		}

		@Override
		public PersistenceContextDispatcher<Binary> getContextDispatcher()
		{
			return this.wrapped().getContextDispatcher();
		}

		@Override
		public PersistenceTypeHandlerProvider<Binary> getTypeHandlerProvider()
		{
			return this.wrapped().getTypeHandlerProvider();
		}

		@Override
		public PersistenceTypeHandlerEnsurer<Binary> getTypeHandlerEnsurer()
		{
			return this.wrapped().getTypeHandlerEnsurer();
		}

		@Override
		public PersistenceTypeHandlerRegistry<Binary> getTypeHandlerRegistry()
		{
			return this.wrapped().getTypeHandlerRegistry();
		}

		@Override
		public PersistenceTypeDictionaryManager getTypeDictionaryManager()
		{
			return this.wrapped().getTypeDictionaryManager();
		}

		@Override
		public PersistenceTypeDictionaryCreator getTypeDictionaryCreator()
		{
			return this.wrapped().getTypeDictionaryCreator();
		}

		@Override
		public PersistenceTypeDictionaryProvider getTypeDictionaryProvider()
		{
			return this.wrapped().getTypeDictionaryProvider();
		}

		@Override
		public PersistenceTypeDictionaryExporter getTypeDictionaryExporter()
		{
			return this.wrapped().getTypeDictionaryExporter();
		}

		@Override
		public PersistenceTypeDictionaryParser getTypeDictionaryParser()
		{
			return this.wrapped().getTypeDictionaryParser();
		}

		@Override
		public PersistenceTypeDictionaryLoader getTypeDictionaryLoader()
		{
			return this.wrapped().getTypeDictionaryLoader();
		}

		@Override
		public PersistenceTypeDictionaryBuilder getTypeDictionaryBuilder()
		{
			return this.wrapped().getTypeDictionaryBuilder();
		}

		@Override
		public PersistenceTypeDictionaryCompiler getTypeDictionaryCompiler()
		{
			return this.wrapped().getTypeDictionaryCompiler();
		}

		@Override
		public PersistenceTypeDictionaryAssembler getTypeDictionaryAssembler()
		{
			return this.wrapped().getTypeDictionaryAssembler();
		}

		@Override
		public PersistenceTypeDictionaryStorer getTypeDictionaryStorer()
		{
			return this.wrapped().getTypeDictionaryStorer();
		}

		@Override
		public PersistenceTypeLineageCreator getTypeLineageCreator()
		{
			return this.wrapped().getTypeLineageCreator();
		}

		@Override
		public PersistenceTypeHandlerCreator<Binary> getTypeHandlerCreator()
		{
			return this.wrapped().getTypeHandlerCreator();
		}

		@Override
		public PersistenceCustomTypeHandlerRegistry<Binary> getCustomTypeHandlerRegistry()
		{
			return this.wrapped().getCustomTypeHandlerRegistry();
		}

		@Override
		public PersistenceCustomTypeHandlerRegistryEnsurer<Binary> customTypeHandlerRegistryEnsurer()
		{
			return this.wrapped().customTypeHandlerRegistryEnsurer();
		}

		@Override
		public PersistenceCustomTypeHandlerRegistryEnsurer<Binary> getCustomTypeHandlerRegistryEnsurer()
		{
			return this.wrapped().getCustomTypeHandlerRegistryEnsurer();
		}

		@Override
		public PersistenceTypeAnalyzer getTypeAnalyzer()
		{
			return this.wrapped().getTypeAnalyzer();
		}

		@Override
		public PersistenceTypeResolver getTypeResolver()
		{
			return this.wrapped().getTypeResolver();
		}

		@Override
		public ClassLoaderProvider getClassLoaderProvider()
		{
			return this.wrapped().getClassLoaderProvider();
		}

		@Override
		public PersistenceTypeMismatchValidator<Binary> getTypeMismatchValidator()
		{
			return this.wrapped().getTypeMismatchValidator();
		}

		@Override
		public PersistenceTypeDefinitionCreator getTypeDefinitionCreator()
		{
			return this.wrapped().getTypeDefinitionCreator();
		}

		@Override
		public PersistenceTypeEvaluator getTypeEvaluatorPersistable()
		{
			return this.wrapped().getTypeEvaluatorPersistable();
		}

		@Override
		public PersistenceFieldLengthResolver getFieldFixedLengthResolver()
		{
			return this.wrapped().getFieldFixedLengthResolver();
		}

		@Override
		public PersistenceTypeNameMapper getTypeNameMapper()
		{
			return this.wrapped().getTypeNameMapper();
		}

		@Override
		public PersistenceEagerStoringFieldEvaluator getReferenceFieldEagerEvaluator()
		{
			return this.wrapped().getReferenceFieldEagerEvaluator();
		}

		@Override
		public BufferSizeProviderIncremental getBufferSizeProvider()
		{
			return this.wrapped().getBufferSizeProvider();
		}

		@Override
		public PersistenceFieldEvaluator getFieldEvaluatorPersistable()
		{
			return this.wrapped().getFieldEvaluatorPersistable();
		}

		@Override
		public PersistenceFieldEvaluator getFieldEvaluatorPersister()
		{
			return this.wrapped().getFieldEvaluatorPersister();
		}

		@Override
		public PersistenceFieldEvaluator getFieldEvaluatorEnum()
		{
			return this.wrapped().getFieldEvaluatorEnum();
		}

		@Override
		public PersistenceFieldEvaluator getFieldEvaluatorCollection()
		{
			return this.wrapped().getFieldEvaluatorCollection();
		}

		@Override
		public PersistenceRootResolverProvider getRootResolverProvider()
		{
			return this.wrapped().getRootResolverProvider();
		}

		@Override
		public PersistenceRootReferenceProvider<Binary> getRootReferenceProvider()
		{
			return this.wrapped().getRootReferenceProvider();
		}

		@Override
		public PersistenceRootsProvider<Binary> getRootsProvider()
		{
			return this.wrapped().getRootsProvider();
		}

		@Override
		public PersistenceUnreachableTypeHandlerCreator<Binary> getUnreachableTypeHandlerCreator()
		{
			return this.wrapped().getUnreachableTypeHandlerCreator();
		}

		@Override
		public PersistenceLegacyTypeMapper<Binary> getLegacyTypeMapper()
		{
			return this.wrapped().getLegacyTypeMapper();
		}

		@Override
		public PersistenceRefactoringMappingProvider getRefactoringMappingProvider()
		{
			return this.wrapped().getRefactoringMappingProvider();
		}

		@Override
		public PersistenceTypeDescriptionResolverProvider getTypeDescriptionResolverProvider()
		{
			return this.wrapped().getTypeDescriptionResolverProvider();
		}

		@Override
		public XEnum<? extends PersistenceRefactoringTypeIdentifierBuilder> getRefactoringLegacyTypeIdentifierBuilders()
		{
			return this.wrapped().getRefactoringLegacyTypeIdentifierBuilders();
		}

		@Override
		public XEnum<? extends PersistenceRefactoringMemberIdentifierBuilder> getRefactoringLegacyMemberIdentifierBuilders()
		{
			return this.wrapped().getRefactoringLegacyMemberIdentifierBuilders();
		}

		@Override
		public XEnum<? extends PersistenceRefactoringMemberIdentifierBuilder> getRefactoringCurrentMemberIdentifierBuilders()
		{
			return this.wrapped().getRefactoringCurrentMemberIdentifierBuilders();
		}

		@Override
		public TypeMapping<Float> getTypeSimilarity()
		{
			return this.wrapped().getTypeSimilarity();
		}

		@Override
		public PersistenceMemberMatchingProvider getLegacyMemberMatchingProvider()
		{
			return this.wrapped().getLegacyMemberMatchingProvider();
		}

		@Override
		public PersistenceLegacyTypeMappingResultor<Binary> getLegacyTypeMappingResultor()
		{
			return this.wrapped().getLegacyTypeMappingResultor();
		}

		@Override
		public PersistenceLegacyTypeHandlerCreator<Binary> getLegacyTypeHandlerCreator()
		{
			return this.wrapped().getLegacyTypeHandlerCreator();
		}

		@Override
		public PersistenceLegacyTypeHandlingListener<Binary> getLegacyTypeHandlingListener()
		{
			return this.wrapped().getLegacyTypeHandlingListener();
		}

		@Override
		public PersistenceSizedArrayLengthController getSizedArrayLengthController()
		{
			return this.wrapped().getSizedArrayLengthController();
		}

		@Override
		public LambdaTypeRecognizer getLambdaTypeRecognizer()
		{
			return this.wrapped().getLambdaTypeRecognizer();
		}

		@Override
		public PersistenceAbstractTypeHandlerSearcher<Binary> getAbstractTypeHandlerSearcher()
		{
			return this.wrapped().getAbstractTypeHandlerSearcher();
		}

		@Override
		public PersistenceInstantiator<Binary> getInstantiator()
		{
			return this.wrapped().getInstantiator();
		}

		@Override
		public PersistenceTypeInstantiatorProvider<Binary> getInstantiatorProvider()
		{
			return this.wrapped().getInstantiatorProvider();
		}

		@Override
		public F setObjectRegistry(final PersistenceObjectRegistry objectRegistry)
		{
			return this.wrapped().setObjectRegistry(objectRegistry);
		}

		@Override
		public F setTypeRegistry(final PersistenceTypeRegistry typeRegistry)
		{
			return this.wrapped().setTypeRegistry(typeRegistry);
		}

		@Override
		public F setInstanceDispatcher(final InstanceDispatcherLogic instanceDispatcher)
		{
			return this.wrapped().setInstanceDispatcher(instanceDispatcher);
		}

		@Override
		public F setObjectManager(final PersistenceObjectManager<Binary> objectManager)
		{
			return this.wrapped().setObjectManager(objectManager);
		}

		@Override
		public F setStorerCreator(final Creator<Binary> storerCreator)
		{
			return this.wrapped().setStorerCreator(storerCreator);
		}

		@Override
		public F setTypeHandlerManager(final PersistenceTypeHandlerManager<Binary> typeHandlerManager)
		{
			return this.wrapped().setTypeHandlerManager(typeHandlerManager);
		}

		@Override
		public F setContextDispatcher(final PersistenceContextDispatcher<Binary> contextDispatcher)
		{
			return this.wrapped().setContextDispatcher(contextDispatcher);
		}

		@Override
		public F setTypeManager(final PersistenceTypeManager typeManager)
		{
			return this.wrapped().setTypeManager(typeManager);
		}

		@Override
		public F setTypeHandlerCreatorLookup(final PersistenceTypeHandlerEnsurer<Binary> typeHandlerCreatorLookup)
		{
			return this.wrapped().setTypeHandlerCreatorLookup(typeHandlerCreatorLookup);
		}

		@Override
		public F setTypeHandlerCreator(final PersistenceTypeHandlerCreator<Binary> typeHandlerCreator)
		{
			return this.wrapped().setTypeHandlerCreator(typeHandlerCreator);
		}

		@Override
		public F setTypeAnalyzer(final PersistenceTypeAnalyzer typeAnalyzer)
		{
			return this.wrapped().setTypeAnalyzer(typeAnalyzer);
		}

		@Override
		public F setTypeResolver(final PersistenceTypeResolver typeResolver)
		{
			return this.wrapped().setTypeResolver(typeResolver);
		}

		@Override
		public F setClassLoaderProvider(final ClassLoaderProvider classLoaderProvider)
		{
			return this.wrapped().setClassLoaderProvider(classLoaderProvider);
		}

		@Override
		public F setTypeHandlerRegistry(final PersistenceTypeHandlerRegistry<Binary> typeHandlerRegistry)
		{
			return this.wrapped().setTypeHandlerRegistry(typeHandlerRegistry);
		}

		@Override
		public F setTypeHandlerProvider(final PersistenceTypeHandlerProvider<Binary> typeHandlerProvider)
		{
			return this.wrapped().setTypeHandlerProvider(typeHandlerProvider);
		}

		@Override
		public F setRegistererCreator(final one.microstream.persistence.types.PersistenceRegisterer.Creator registererCreator)
		{
			return this.wrapped().setRegistererCreator(registererCreator);
		}

		@Override
		public F setBuilderCreator(final one.microstream.persistence.types.PersistenceLoader.Creator<Binary> builderCreator)
		{
			return this.wrapped().setBuilderCreator(builderCreator);
		}

		@Override
		public F setPersister(final Persister persister)
		{
			return this.wrapped().setPersister(persister);
		}

		@Override
		public F setPersistenceTarget(final PersistenceTarget<Binary> target)
		{
			return this.wrapped().setPersistenceTarget(target);
		}

		@Override
		public F setPersistenceSource(final PersistenceSource<Binary> source)
		{
			return this.wrapped().setPersistenceSource(source);
		}

		@Override
		public F setTypeDictionaryManager(final PersistenceTypeDictionaryManager typeDictionaryManager)
		{
			return this.wrapped().setTypeDictionaryManager(typeDictionaryManager);
		}

		@Override
		public F setTypeDictionaryCreator(final PersistenceTypeDictionaryCreator typeDictionaryCreator)
		{
			return this.wrapped().setTypeDictionaryCreator(typeDictionaryCreator);
		}

		@Override
		public F setTypeDictionaryProvider(final PersistenceTypeDictionaryProvider typeDictionaryProvider)
		{
			return this.wrapped().setTypeDictionaryProvider(typeDictionaryProvider);
		}

		@Override
		public F setTypeDictionaryExporter(final PersistenceTypeDictionaryExporter typeDictionaryExporter)
		{
			return this.wrapped().setTypeDictionaryExporter(typeDictionaryExporter);
		}

		@Override
		public F setTypeDictionaryParser(final PersistenceTypeDictionaryParser typeDictionaryParser)
		{
			return this.wrapped().setTypeDictionaryParser(typeDictionaryParser);
		}

		@Override
		public F setTypeDictionaryAssembler(final PersistenceTypeDictionaryAssembler typeDictionaryAssembler)
		{
			return this.wrapped().setTypeDictionaryAssembler(typeDictionaryAssembler);
		}

		@Override
		public F setTypeDictionaryLoader(final PersistenceTypeDictionaryLoader typeDictionaryLoader)
		{
			return this.wrapped().setTypeDictionaryLoader(typeDictionaryLoader);
		}

		@Override
		public F setTypeDictionaryBuilder(final PersistenceTypeDictionaryBuilder typeDictionaryBuilder)
		{
			return this.wrapped().setTypeDictionaryBuilder(typeDictionaryBuilder);
		}

		@Override
		public F setTypeDictionaryCompiler(final PersistenceTypeDictionaryCompiler typeDictionaryCompiler)
		{
			return this.wrapped().setTypeDictionaryCompiler(typeDictionaryCompiler);
		}

		@Override
		public F setTypeDictionaryStorer(final PersistenceTypeDictionaryStorer typeDictionaryStorer)
		{
			return this.wrapped().setTypeDictionaryStorer(typeDictionaryStorer);
		}

		@Override
		public <H extends PersistenceTypeDictionaryLoader & PersistenceTypeDictionaryStorer> F setTypeDictionaryIoHandling(
			final H typeDictionaryStorage)
		{
			return this.wrapped().setTypeDictionaryIoHandling(typeDictionaryStorage);
		}

		@Override
		public F setTypeDictionaryIoHandler(final PersistenceTypeDictionaryIoHandler typeDictionaryIoHandler)
		{
			return this.wrapped().setTypeDictionaryIoHandler(typeDictionaryIoHandler);
		}

		@Override
		public F setTypeLineageCreator(final PersistenceTypeLineageCreator typeLineageCreator)
		{
			return this.wrapped().setTypeLineageCreator(typeLineageCreator);
		}

		@Override
		public F setTypeMismatchValidator(final PersistenceTypeMismatchValidator<Binary> typeMismatchValidator)
		{
			return this.wrapped().setTypeMismatchValidator(typeMismatchValidator);
		}

		@Override
		public F setTypeDescriptionBuilder(final PersistenceTypeDefinitionCreator typeDefinitionCreator)
		{
			return this.wrapped().setTypeDescriptionBuilder(typeDefinitionCreator);
		}

		@Override
		public F setTypeEvaluatorPersistable(final PersistenceTypeEvaluator typeEvaluatorPersistable)
		{
			return this.wrapped().setTypeEvaluatorPersistable(typeEvaluatorPersistable);
		}

		@Override
		public F setBufferSizeProvider(final BufferSizeProviderIncremental bufferSizeProvider)
		{
			return this.wrapped().setBufferSizeProvider(bufferSizeProvider);
		}

		@Override
		public F setFieldFixedLengthResolver(final PersistenceFieldLengthResolver fieldFixedLengthResolver)
		{
			return this.wrapped().setFieldFixedLengthResolver(fieldFixedLengthResolver);
		}

		@Override
		public F setTypeNameMapper(final PersistenceTypeNameMapper typeNameMapper)
		{
			return this.wrapped().setTypeNameMapper(typeNameMapper);
		}

		@Override
		public F setFieldEvaluatorPersistable(final PersistenceFieldEvaluator fieldEvaluator)
		{
			return this.wrapped().setFieldEvaluatorPersistable(fieldEvaluator);
		}

		@Override
		public F setFieldEvaluatorPersister(final PersistenceFieldEvaluator fieldEvaluator)
		{
			return this.wrapped().setFieldEvaluatorPersister(fieldEvaluator);
		}

		@Override
		public F setFieldEvaluatorEnum(final PersistenceFieldEvaluator fieldEvaluator)
		{
			return this.wrapped().setFieldEvaluatorEnum(fieldEvaluator);
		}

		@Override
		public F setFieldEvaluatorCollection(final PersistenceFieldEvaluator fieldEvaluator)
		{
			return this.wrapped().setFieldEvaluatorCollection(fieldEvaluator);
		}

		@Override
		public F setReferenceFieldEagerEvaluator(final PersistenceEagerStoringFieldEvaluator evaluator)
		{
			return this.wrapped().setReferenceFieldEagerEvaluator(evaluator);
		}

		@Override
		public F setRootResolverProvider(final PersistenceRootResolverProvider rootResolverProvider)
		{
			return this.wrapped().setRootResolverProvider(rootResolverProvider);
		}

		@Override
		public F setRootReferenceProvider(final PersistenceRootReferenceProvider<Binary> rootReferenceProvider)
		{
			return this.wrapped().setRootReferenceProvider(rootReferenceProvider);
		}

		@Override
		public F setLambdaTypeRecognizer(final LambdaTypeRecognizer lambdaTypeRecognizer)
		{
			return this.wrapped().setLambdaTypeRecognizer(lambdaTypeRecognizer);
		}

		@Override
		public F setAbstractTypeHandlerSearcher(
			final PersistenceAbstractTypeHandlerSearcher<Binary> abstractTypeHandlerSearcher)
		{
			return this.wrapped().setAbstractTypeHandlerSearcher(abstractTypeHandlerSearcher);
		}

		@Override
		public F setRootsProvider(final PersistenceRootsProvider<Binary> rootsProvider)
		{
			return this.wrapped().setRootsProvider(rootsProvider);
		}

		@Override
		public F setUnreachableTypeHandlerCreator(
			final PersistenceUnreachableTypeHandlerCreator<Binary> unreachableTypeHandlerCreator)
		{
			return this.wrapped().setUnreachableTypeHandlerCreator(unreachableTypeHandlerCreator);
		}

		@Override
		public F setLegacyTypeMapper(final PersistenceLegacyTypeMapper<Binary> legacyTypeMapper)
		{
			return this.wrapped().setLegacyTypeMapper(legacyTypeMapper);
		}

		@Override
		public F setTypeSimilarity(final TypeMapping<Float> typeSimilarity)
		{
			return this.wrapped().setTypeSimilarity(typeSimilarity);
		}

		@Override
		public F setRefactoringMappingProvider(final PersistenceRefactoringMappingProvider refactoringMappingProvider)
		{
			return this.wrapped().setRefactoringMappingProvider(refactoringMappingProvider);
		}

		@Override
		public F setTypeDescriptionResolverProvider(
			final PersistenceTypeDescriptionResolverProvider typeDescriptionResolverProvider)
		{
			return this.wrapped().setTypeDescriptionResolverProvider(typeDescriptionResolverProvider);
		}

		@Override
		public F setRefactoringLegacyTypeIdentifierBuilders(
			final XEnum<? extends PersistenceRefactoringTypeIdentifierBuilder> typeIdentifierBuilders)
		{
			return this.wrapped().setRefactoringLegacyTypeIdentifierBuilders(typeIdentifierBuilders);
		}

		@Override
		public F setRefactoringLegacyMemberIdentifierBuilders(
			final XEnum<? extends PersistenceRefactoringMemberIdentifierBuilder> memberIdentifierBuilders)
		{
			return this.wrapped().setRefactoringLegacyMemberIdentifierBuilders(memberIdentifierBuilders);
		}

		@Override
		public F setRefactoringCurrentMemberIdentifierBuilders(
			final XEnum<? extends PersistenceRefactoringMemberIdentifierBuilder> memberIdentifierBuilders)
		{
			return this.wrapped().setRefactoringCurrentMemberIdentifierBuilders(memberIdentifierBuilders);
		}

		@Override
		public F setLegacyMemberMatchingProvider(final PersistenceMemberMatchingProvider legacyMemberMatchingProvider)
		{
			return this.wrapped().setLegacyMemberMatchingProvider(legacyMemberMatchingProvider);
		}

		@Override
		public F setLegacyTypeMappingResultor(final PersistenceLegacyTypeMappingResultor<Binary> legacyTypeMappingResultor)
		{
			return this.wrapped().setLegacyTypeMappingResultor(legacyTypeMappingResultor);
		}

		@Override
		public F setLegacyTypeHandlerCreator(final PersistenceLegacyTypeHandlerCreator<Binary> legacyTypeHandlerCreator)
		{
			return this.wrapped().setLegacyTypeHandlerCreator(legacyTypeHandlerCreator);
		}

		@Override
		public F setLegacyTypeHandlingListener(final PersistenceLegacyTypeHandlingListener<Binary> legacyTypeHandlingListener)
		{
			return this.wrapped().setLegacyTypeHandlingListener(legacyTypeHandlingListener);
		}

		@Override
		public F setPersistenceChannel(final PersistenceChannel<Binary> persistenceChannel)
		{
			return this.wrapped().setPersistenceChannel(persistenceChannel);
		}

		@Override
		public F setSizedArrayLengthController(final PersistenceSizedArrayLengthController sizedArrayLengthController)
		{
			return this.wrapped().setSizedArrayLengthController(sizedArrayLengthController);
		}

		@Override
		public F setObjectIdProvider(final PersistenceObjectIdProvider oidProvider)
		{
			return this.wrapped().setObjectIdProvider(oidProvider);
		}

		@Override
		public F setTypeIdProvider(final PersistenceTypeIdProvider tidProvider)
		{
			return this.wrapped().setTypeIdProvider(tidProvider);
		}

		@Override
		public <P extends PersistenceTypeIdProvider & PersistenceObjectIdProvider> F setIdProvider(final P idProvider)
		{
			return this.wrapped().setIdProvider(idProvider);
		}

		@Override
		public F setInstantiator(final PersistenceInstantiator<Binary> instantiator)
		{
			return this.wrapped().setInstantiator(instantiator);
		}

		@Override
		public F setInstantiatorProvider(final PersistenceTypeInstantiatorProvider<Binary> instantiatorProvider)
		{
			return this.wrapped().setInstantiatorProvider(instantiatorProvider);
		}

		@Override
		public F executeTypeHandlerRegistration(final PersistenceTypeHandlerRegistration<Binary> typeHandlerRegistration)
		{
			return this.wrapped().executeTypeHandlerRegistration(typeHandlerRegistration);
		}

		@Override
		public F setCustomTypeHandlerRegistryEnsurer(
			final PersistenceCustomTypeHandlerRegistryEnsurer<Binary> customTypeHandlerRegistryEnsurer)
		{
			return this.wrapped().setCustomTypeHandlerRegistryEnsurer(customTypeHandlerRegistryEnsurer);
		}

		@Override
		public StorageWriteController writeController()
		{
			return this.wrapped().getWriteController();
		}

		@Override
		public StorageWriteController getWriteController()
		{
			return this.wrapped().getWriteController();
		}

		@Override
		public F setWriteController(final StorageWriteController writeController)
		{
			return this.wrapped().setWriteController(writeController);
		}
		
	}

}

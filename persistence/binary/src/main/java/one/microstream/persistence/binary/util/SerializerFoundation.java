package one.microstream.persistence.binary.util;

/*-
 * #%L
 * microstream-persistence-binary
 * %%
 * Copyright (C) 2019 - 2022 MicroStream Software
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

import java.nio.ByteOrder;

import one.microstream.X;
import one.microstream.collections.EqHashEnum;
import one.microstream.collections.EqHashTable;
import one.microstream.collections.HashEnum;
import one.microstream.collections.HashTable;
import one.microstream.collections.types.XEnum;
import one.microstream.collections.types.XMap;
import one.microstream.collections.types.XTable;
import one.microstream.functional.InstanceDispatcherLogic;
import one.microstream.persistence.binary.one.microstream.persistence.types.BinaryRootReferenceProvider;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.binary.types.BinaryFieldLengthResolver;
import one.microstream.persistence.binary.types.BinaryLegacyTypeHandlerCreator;
import one.microstream.persistence.binary.types.BinaryLoader;
import one.microstream.persistence.binary.types.BinaryPersistence;
import one.microstream.persistence.binary.types.BinaryPersistenceRootsProvider;
import one.microstream.persistence.binary.types.BinaryStorer;
import one.microstream.persistence.binary.types.BinaryTypeHandlerCreator;
import one.microstream.persistence.binary.types.BinaryValueSetter;
import one.microstream.persistence.binary.types.BinaryValueTranslatorKeyBuilder;
import one.microstream.persistence.binary.types.BinaryValueTranslatorMappingProvider;
import one.microstream.persistence.binary.types.BinaryValueTranslatorProvider;
import one.microstream.persistence.internal.LoggingLegacyTypeMappingResultor;
import one.microstream.persistence.internal.PersistenceTypeHandlerProviderCreating;
import one.microstream.persistence.types.ByteOrderTargeting;
import one.microstream.persistence.types.Persistence;
import one.microstream.persistence.types.PersistenceAbstractTypeHandlerSearcher;
import one.microstream.persistence.types.PersistenceContextDispatcher;
import one.microstream.persistence.types.PersistenceCustomTypeHandlerRegistry;
import one.microstream.persistence.types.PersistenceDataTypeHolder;
import one.microstream.persistence.types.PersistenceEagerStoringFieldEvaluator;
import one.microstream.persistence.types.PersistenceFieldEvaluator;
import one.microstream.persistence.types.PersistenceFieldLengthResolver;
import one.microstream.persistence.types.PersistenceInstantiator;
import one.microstream.persistence.types.PersistenceLegacyTypeHandlerCreator;
import one.microstream.persistence.types.PersistenceLegacyTypeHandlingListener;
import one.microstream.persistence.types.PersistenceLegacyTypeMapper;
import one.microstream.persistence.types.PersistenceLegacyTypeMappingResultor;
import one.microstream.persistence.types.PersistenceLoader;
import one.microstream.persistence.types.PersistenceManager;
import one.microstream.persistence.types.PersistenceMemberMatchingProvider;
import one.microstream.persistence.types.PersistenceObjectIdProvider;
import one.microstream.persistence.types.PersistenceObjectManager;
import one.microstream.persistence.types.PersistenceObjectRegistry;
import one.microstream.persistence.types.PersistenceRefactoringMappingProvider;
import one.microstream.persistence.types.PersistenceRefactoringMemberIdentifierBuilder;
import one.microstream.persistence.types.PersistenceRefactoringTypeIdentifierBuilder;
import one.microstream.persistence.types.PersistenceRegisterer;
import one.microstream.persistence.types.PersistenceRootReference;
import one.microstream.persistence.types.PersistenceRootReferenceProvider;
import one.microstream.persistence.types.PersistenceRootResolverProvider;
import one.microstream.persistence.types.PersistenceRootsProvider;
import one.microstream.persistence.types.PersistenceSizedArrayLengthController;
import one.microstream.persistence.types.PersistenceSource;
import one.microstream.persistence.types.PersistenceStorer;
import one.microstream.persistence.types.PersistenceTarget;
import one.microstream.persistence.types.PersistenceTypeAnalyzer;
import one.microstream.persistence.types.PersistenceTypeDefinitionCreator;
import one.microstream.persistence.types.PersistenceTypeDescriptionResolverProvider;
import one.microstream.persistence.types.PersistenceTypeDictionaryCreator;
import one.microstream.persistence.types.PersistenceTypeDictionaryManager;
import one.microstream.persistence.types.PersistenceTypeDictionaryParser;
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
import one.microstream.persistence.types.Unpersistable;
import one.microstream.reference.Reference;
import one.microstream.reflect.ClassLoaderProvider;
import one.microstream.typing.LambdaTypeRecognizer;
import one.microstream.typing.TypeMapping;
import one.microstream.typing.XTypes;
import one.microstream.util.BufferSizeProviderIncremental;
import one.microstream.util.InstanceDispatcher;

/**
 * This type serves as a factory instance for building {@link Serializer} instances.
 * However, it is more than a mere factory as it keeps track of all component instances used in building
 * a {@link Serializer} instance. For example managing parts of an application can use it
 * to access former set ID providers or dictionary providers even after they have been assembled into (and
 * are intentionally hidden in) a {@link PersistenceManager} instance.*
 * Hence it can be seen as a kind of "master instance" of the built persistence layer or as its "foundation".
 *
 * @param <F> the foundation type
 */
public interface SerializerFoundation<F extends SerializerFoundation<?>>
extends ByteOrderTargeting.Mutable<F>,
        PersistenceDataTypeHolder<Binary>,
        PersistenceTypeHandlerRegistration.Executor<Binary>,
        InstanceDispatcher
{
	public XMap<Class<?>, PersistenceTypeHandler<Binary, ?>> customTypeHandlers();

	public XMap<Class<?>, PersistenceTypeInstantiator<Binary, ?>> customTypeInstantiators();
	
	public F registerCustomTypeHandlers(HashTable<Class<?>, PersistenceTypeHandler<Binary, ?>> customTypeHandlers);
	
	@SuppressWarnings("unchecked")
	public F registerCustomTypeHandlers(PersistenceTypeHandler<Binary, ?>... customTypeHandlers);
	
	public F registerCustomTypeHandlers(Iterable<? extends PersistenceTypeHandler<Binary, ?>> customTypeHandlers);
	
	public F registerCustomTypeHandler(PersistenceTypeHandler<Binary, ?> customTypeHandler);
	
	public <T> F registerCustomInstantiator(Class<T> type, PersistenceTypeInstantiator<Binary, T> typeInstantiator);
	
	public PersistenceObjectIdProvider getObjectIdProvider();

	public PersistenceTypeIdProvider getTypeIdProvider();


	public PersistenceStorer.Creator<Binary> getStorerCreator();

	public PersistenceRegisterer.Creator getRegistererCreator();

	public PersistenceLoader.Creator<Binary> getBuilderCreator();
		
	public Persister getPersister();
	
	public PersistenceObjectRegistry getObjectRegistry();

	public PersistenceObjectManager<Binary> getObjectManager();
	
	public PersistenceTypeRegistry getTypeRegistry();
	
	public PersistenceTypeManager getTypeManager();

	public PersistenceTypeHandlerManager<Binary> getTypeHandlerManager();
	
	public PersistenceContextDispatcher<Binary> getContextDispatcher();
	
	public PersistenceTypeHandlerProvider<Binary> getTypeHandlerProvider();

	public PersistenceTypeHandlerEnsurer<Binary> getTypeHandlerEnsurer();

	public PersistenceTypeHandlerRegistry<Binary> getTypeHandlerRegistry();

	public PersistenceTypeDictionaryManager getTypeDictionaryManager();
	
	public PersistenceTypeDictionaryCreator getTypeDictionaryCreator();
	
	public PersistenceTypeDictionaryParser getTypeDictionaryParser();
	
	public PersistenceTypeLineageCreator getTypeLineageCreator();

	public PersistenceTypeHandlerCreator<Binary> getTypeHandlerCreator();

	/**
	 * Creates if required and returns the {@link PersistenceCustomTypeHandlerRegistry} containing all custom tailored
	 * {@link PersistenceTypeHandler} instances for specialized handling of instances.<p>
	 * To avoid order problems caused by internal implicit dependencies (e.g. creating the default custom type handlers
	 * required {@link #getReferenceFieldEagerEvaluator()}), use {@link #customTypeHandlers()}
	 * or one of the {@code registerCustomTypeHandler~} methods.
	 * 
	 * @return the (on-demand created) {@link PersistenceCustomTypeHandlerRegistry} instance.
	 */
	public PersistenceCustomTypeHandlerRegistry<Binary> getCustomTypeHandlerRegistry();

	public PersistenceTypeAnalyzer getTypeAnalyzer();
	
	public PersistenceTypeResolver getTypeResolver();
	
	public ClassLoaderProvider getClassLoaderProvider();
	
	public PersistenceTypeMismatchValidator<Binary> getTypeMismatchValidator();
	
	public PersistenceTypeDefinitionCreator getTypeDefinitionCreator();

	public PersistenceTypeEvaluator getTypeEvaluatorPersistable();

	public PersistenceFieldLengthResolver getFieldFixedLengthResolver();
	
	public PersistenceTypeNameMapper getTypeNameMapper();
	
	public PersistenceEagerStoringFieldEvaluator getReferenceFieldEagerEvaluator();

	public BufferSizeProviderIncremental getBufferSizeProvider();

	public PersistenceFieldEvaluator getFieldEvaluatorPersistable();
	
	public PersistenceFieldEvaluator getFieldEvaluatorPersister();
	
	public PersistenceFieldEvaluator getFieldEvaluatorEnum();
	
	public PersistenceFieldEvaluator getFieldEvaluatorCollection();
	
	public PersistenceRootResolverProvider getRootResolverProvider();
	
	public PersistenceRootReferenceProvider<Binary> getRootReferenceProvider();
	
	public PersistenceRootsProvider<Binary> getRootsProvider();
	
	public PersistenceUnreachableTypeHandlerCreator<Binary> getUnreachableTypeHandlerCreator();
	
	public PersistenceLegacyTypeMapper<Binary> getLegacyTypeMapper();

	public PersistenceRefactoringMappingProvider getRefactoringMappingProvider();

	public PersistenceTypeDescriptionResolverProvider getTypeDescriptionResolverProvider();

	public XEnum<? extends PersistenceRefactoringTypeIdentifierBuilder>  getRefactoringLegacyTypeIdentifierBuilders();
	
	public XEnum<? extends PersistenceRefactoringMemberIdentifierBuilder> getRefactoringLegacyMemberIdentifierBuilders();
	
	public XEnum<? extends PersistenceRefactoringMemberIdentifierBuilder> getRefactoringCurrentMemberIdentifierBuilders();
	
	public TypeMapping<Float> getTypeSimilarity();
	
	public PersistenceMemberMatchingProvider getLegacyMemberMatchingProvider();
	
	public PersistenceLegacyTypeMappingResultor<Binary> getLegacyTypeMappingResultor();
	
	public PersistenceLegacyTypeHandlerCreator<Binary> getLegacyTypeHandlerCreator();
	
	public PersistenceLegacyTypeHandlingListener<Binary> getLegacyTypeHandlingListener();
	
	public PersistenceSizedArrayLengthController getSizedArrayLengthController();
	
	public LambdaTypeRecognizer getLambdaTypeRecognizer();
	
	public PersistenceAbstractTypeHandlerSearcher<Binary> getAbstractTypeHandlerSearcher();

	public PersistenceInstantiator<Binary> getInstantiator();
	
	public PersistenceTypeInstantiatorProvider<Binary> getInstantiatorProvider();
	
	public XEnum<Class<?>> getEntityTypes();
	
	public SerializerTypeInfoStrategyCreator getSerializerTypeInfoStrategyCreator();
	
	
	
	public F setObjectRegistry(PersistenceObjectRegistry objectRegistry);
	
	public F setTypeRegistry(PersistenceTypeRegistry typeRegistry);

	public F setInstanceDispatcher(InstanceDispatcherLogic instanceDispatcher);

	public F setObjectManager(PersistenceObjectManager<Binary> objectManager);

	public F setStorerCreator(PersistenceStorer.Creator<Binary> storerCreator);

	public F setTypeHandlerManager(PersistenceTypeHandlerManager<Binary> typeHandlerManager);

	public F setTypeManager(PersistenceTypeManager typeManager);

	public F setTypeHandlerCreatorLookup(PersistenceTypeHandlerEnsurer<Binary> typeHandlerCreatorLookup);
	
	public F setTypeHandlerCreator(PersistenceTypeHandlerCreator<Binary> typeHandlerCreator);
	
	public F setTypeAnalyzer(PersistenceTypeAnalyzer typeAnalyzer);
	
	public F setTypeResolver(PersistenceTypeResolver typeResolver);
	
	public F setClassLoaderProvider(ClassLoaderProvider classLoaderProvider);

	public F setTypeHandlerRegistry(PersistenceTypeHandlerRegistry<Binary> typeHandlerRegistry);

	public F setTypeHandlerProvider(PersistenceTypeHandlerProvider<Binary> typeHandlerProvider);

	public F setRegistererCreator(PersistenceRegisterer.Creator registererCreator);

	public F setBuilderCreator(PersistenceLoader.Creator<Binary> builderCreator);
	
	public F setPersister(Persister persister);
	
	public F setTypeDictionaryCreator(PersistenceTypeDictionaryCreator typeDictionaryCreator);
	
	public F setTypeDictionaryParser(PersistenceTypeDictionaryParser typeDictionaryParser);
	
	public F setTypeLineageCreator(PersistenceTypeLineageCreator typeLineageCreator);
	
	public F setTypeMismatchValidator(PersistenceTypeMismatchValidator<Binary> typeMismatchValidator);
	
	public F setTypeDescriptionBuilder(PersistenceTypeDefinitionCreator typeDefinitionCreator);

	public F setTypeEvaluatorPersistable(PersistenceTypeEvaluator typeEvaluatorPersistable);

	public F setBufferSizeProvider(BufferSizeProviderIncremental bufferSizeProvider);

	public F setFieldFixedLengthResolver(PersistenceFieldLengthResolver fieldFixedLengthResolver);

	public F setTypeNameMapper(PersistenceTypeNameMapper typeNameMapper);

	public F setFieldEvaluatorPersistable(PersistenceFieldEvaluator fieldEvaluator);
	
	public F setFieldEvaluatorPersister(PersistenceFieldEvaluator fieldEvaluator);

	public F setFieldEvaluatorEnum(PersistenceFieldEvaluator fieldEvaluator);
	
	public F setFieldEvaluatorCollection(PersistenceFieldEvaluator fieldEvaluator);

	public F setReferenceFieldEagerEvaluator(PersistenceEagerStoringFieldEvaluator evaluator);

	public F setRootResolverProvider(PersistenceRootResolverProvider rootResolverProvider);
	
	public F setRootReferenceProvider(PersistenceRootReferenceProvider<Binary> rootReferenceProvider);
		
	public F setLambdaTypeRecognizer(LambdaTypeRecognizer lambdaTypeRecognizer);
	
	public F setAbstractTypeHandlerSearcher(PersistenceAbstractTypeHandlerSearcher<Binary> abstractTypeHandlerSearcher);
	
	public F setUnreachableTypeHandlerCreator(
		PersistenceUnreachableTypeHandlerCreator<Binary> unreachableTypeHandlerCreator
	);
	
	public F setLegacyTypeMapper(
		PersistenceLegacyTypeMapper<Binary> legacyTypeMapper
	);
	
	public F setTypeSimilarity(
		TypeMapping<Float> typeSimilarity
	);
	
	public F setRefactoringMappingProvider(
		PersistenceRefactoringMappingProvider refactoringMappingProvider
	);
	
	public F setTypeDescriptionResolverProvider(
		PersistenceTypeDescriptionResolverProvider typeDescriptionResolverProvider
	);
	
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
		
	public F setLegacyTypeMappingResultor(PersistenceLegacyTypeMappingResultor<Binary> legacyTypeMappingResultor);
	
	public F setLegacyTypeHandlerCreator(PersistenceLegacyTypeHandlerCreator<Binary> legacyTypeHandlerCreator);
	
	public F setLegacyTypeHandlingListener(PersistenceLegacyTypeHandlingListener<Binary> legacyTypeHandlingListener);
	
	public F setSizedArrayLengthController(PersistenceSizedArrayLengthController sizedArrayLengthController);

	public F setInstantiator(PersistenceInstantiator<Binary> instantiator);
	
	public F setInstantiatorProvider(PersistenceTypeInstantiatorProvider<Binary> instantiatorProvider);

	public F setSerializerTypeInfoStrategyCreator(SerializerTypeInfoStrategyCreator serializerTypeInfoStrategyCreator);


	
	public XTable<String, BinaryValueSetter> getCustomTranslatorLookup();
	
	public XEnum<BinaryValueTranslatorKeyBuilder> getTranslatorKeyBuilders();
	
	public BinaryValueTranslatorMappingProvider getValueTranslatorMappingProvider();
	
	public BinaryValueTranslatorProvider getValueTranslatorProvider();
	
	public F setEntityTypes(XEnum<Class<?>> entityTypes);
	
	public boolean registerEntityType(Class<?> entityType);
	
	public F registerEntityTypes(Class<?>... entityTypes);
	
	public F registerEntityTypes(final Iterable<Class<?>> entityTypes);
			
	
	
	public F setCustomTranslatorLookup(
		XTable<String, BinaryValueSetter> customTranslatorLookup
	);
	
	public F setTranslatorKeyBuilders(
		XEnum<BinaryValueTranslatorKeyBuilder> translatorKeyBuilders
	);
	
	public F setValueTranslatorProvider(
		BinaryValueTranslatorProvider valueTranslatorProvider
	);
	
	public F setValueTranslatorMappingProvider(
		BinaryValueTranslatorMappingProvider valueTranslatorMappingProvider
	);
	
	
	public PersistenceManager<Binary> createPersistenceManager(
		PersistenceSource<Binary> source,
		PersistenceTarget<Binary> target
	);
	
	
	public static SerializerFoundation<?> New()
	{
		return new SerializerFoundation.Default<>();
	}

	
	public class Default<F extends SerializerFoundation.Default<?>>
	extends InstanceDispatcher.Default
	implements SerializerFoundation<F>, Unpersistable
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final Reference<PersistenceTypeHandlerManager<Binary>> referenceTypeHandlerManager = X.Reference(null);
		
		private final HashTable<Class<?>, PersistenceTypeHandler<Binary, ?>> customTypeHandlers = HashTable.New();
		
		private final HashTable<Class<?>, PersistenceTypeInstantiator<Binary, ?>> customTypeInstantiators = HashTable.New();

		private final PersistenceObjectIdProvider           oidProvider       = PersistenceObjectIdProvider.Transient();
		private final PersistenceTypeIdProvider             tidProvider       = PersistenceTypeIdProvider.Transient();
		private final PersistenceContextDispatcher<Binary>  contextDispatcher = PersistenceContextDispatcher.LocalObjectRegistration();
		private final PersistenceRootsProvider<Binary>      rootsProvider     = PersistenceRootsProvider.Empty();
		
		private PersistenceObjectManager<Binary>               objectManager                   ;
		private PersistenceTypeRegistry                        typeRegistry                    ;
		private PersistenceObjectRegistry                      objectRegistry                  ;
		private PersistenceTypeHandlerManager<Binary>          typeHandlerManager              ;
		private PersistenceStorer.Creator<Binary>              storerCreator                   ;
		private PersistenceRegisterer.Creator                  registererCreator               ;
		private PersistenceLoader.Creator<Binary>              builderCreator                  ;
		private Persister                                      persister                       ;
		private PersistenceFieldLengthResolver                 fieldFixedLengthResolver        ;
		private PersistenceTypeNameMapper                      typeNameMapper                  ;
		private PersistenceFieldEvaluator                      fieldEvaluatorPersistable       ;
		private PersistenceFieldEvaluator                      fieldEvaluatorPersister         ;
		private PersistenceFieldEvaluator                      fieldEvaluatorEnum              ;
		private PersistenceFieldEvaluator                      fieldEvaluatorCollection        ;
		private PersistenceEagerStoringFieldEvaluator          eagerStoringFieldEvaluator      ;

		private PersistenceTypeManager                         typeManager                     ;
		private PersistenceTypeHandlerEnsurer<Binary>          typeHandlerEnsurer              ;
		private PersistenceTypeHandlerRegistry<Binary>         typeHandlerRegistry             ;
		private PersistenceTypeHandlerProvider<Binary>         typeHandlerProvider             ;
		private PersistenceTypeDictionaryManager               typeDictionaryManager           ;
		private PersistenceTypeDictionaryCreator               typeDictionaryCreator           ;
		private PersistenceTypeDictionaryParser                typeDictionaryParser            ;
		private PersistenceTypeLineageCreator                  typeLineageCreator              ;
		private PersistenceTypeHandlerCreator<Binary>          typeHandlerCreator              ;
		private PersistenceTypeAnalyzer                        typeAnalyzer                    ;
		private PersistenceTypeResolver                        typeResolver                    ;
		private ClassLoaderProvider                            classLoaderProvider             ;
		private PersistenceTypeMismatchValidator<Binary>       typeMismatchValidator           ;
		private PersistenceTypeDefinitionCreator               typeDefinitionCreator           ;
		private PersistenceTypeEvaluator                       typeEvaluatorPersistable        ;
		private LambdaTypeRecognizer                           lambdaTypeRecognizer            ;
		private PersistenceAbstractTypeHandlerSearcher<Binary> abstractTypeHandlerSearcher     ;
		private PersistenceSizedArrayLengthController          sizedArrayLengthController      ;
		private PersistenceInstantiator<Binary>                instantiator                    ;
		private PersistenceTypeInstantiatorProvider<Binary>    instantiatorProvider            ;
		private PersistenceCustomTypeHandlerRegistry<Binary>   customTypeHandlerRegistry       ;
		private BufferSizeProviderIncremental                  bufferSizeProvider              ;
		private PersistenceRootResolverProvider                rootResolverProvider            ;
		private PersistenceRootReferenceProvider<Binary>       rootReferenceProvider           ;
		private ByteOrder                                      targetByteOrder                 ;
		
		private PersistenceUnreachableTypeHandlerCreator<Binary> unreachableTypeHandlerCreator   ;
		private PersistenceLegacyTypeMapper<Binary>              legacyTypeMapper                ;
		private PersistenceRefactoringMappingProvider            refactoringMappingProvider      ;
		private PersistenceTypeDescriptionResolverProvider       typeDescriptionResolverProvider ;
		private TypeMapping<Float>                               typeSimilarity                  ;
		private PersistenceMemberMatchingProvider                legacyMemberMatchingProvider    ;
		private PersistenceLegacyTypeMappingResultor<Binary>     legacyTypeMappingResultor       ;
		private PersistenceLegacyTypeHandlerCreator<Binary>      legacyTypeHandlerCreator        ;
		private PersistenceLegacyTypeHandlingListener<Binary>    legacyTypeHandlingListener      ;

		private XEnum<? extends PersistenceRefactoringTypeIdentifierBuilder>   refactoringLegacyTypeIdentifierBuilders   ;
		private XEnum<? extends PersistenceRefactoringMemberIdentifierBuilder> refactoringLegacyMemberIdentifierBuilders ;
		private XEnum<? extends PersistenceRefactoringMemberIdentifierBuilder> refactoringCurrentMemberIdentifierBuilders;

		private XTable<String, BinaryValueSetter>      customTranslatorLookup ;
		private XEnum<BinaryValueTranslatorKeyBuilder> translatorKeyBuilders  ;
		private BinaryValueTranslatorMappingProvider   valueTranslatorMapping ;
		private BinaryValueTranslatorProvider          valueTranslatorProvider;

		private XEnum<Class<?>> entityTypes;
		private SerializerTypeInfoStrategyCreator serializerTypeInfoStrategyCreator;
		
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
		public Class<Binary> dataType()
		{
			return Binary.class;
		}

		
		///////////////////////////////////////////////////////////////////////////
		// property getters and related convenience methods //
		/////////////////////////////////////////////////////
		
		@Override
		public XMap<Class<?>, PersistenceTypeHandler<Binary, ?>> customTypeHandlers()
		{
			return this.customTypeHandlers;
		}
		
		@Override
		public synchronized F registerCustomTypeHandlers(
			final HashTable<Class<?>, PersistenceTypeHandler<Binary, ?>> customTypeHandlers
		)
		{
			this.customTypeHandlers.putAll(customTypeHandlers);
			return this.$();
		}
		
		@Override
		@SuppressWarnings("unchecked")
		public synchronized F registerCustomTypeHandlers(
			 final PersistenceTypeHandler<Binary, ?>... customTypeHandlers
		)
		{
			for(final PersistenceTypeHandler<Binary, ?> customTypeHandler : customTypeHandlers)
			{
				this.registerCustomTypeHandler(customTypeHandler);
			}
			
			return this.$();
		}
		
		@Override
		public synchronized F registerCustomTypeHandlers(
			 final Iterable<? extends PersistenceTypeHandler<Binary, ?>> customTypeHandlers
		)
		{
			for(final PersistenceTypeHandler<Binary, ?> customTypeHandler : customTypeHandlers)
			{
				this.registerCustomTypeHandler(customTypeHandler);
			}
			
			return this.$();
		}
		
		@Override
		public synchronized F registerCustomTypeHandler(
			final PersistenceTypeHandler<Binary, ?> customTypeHandler
		)
		{
			this.customTypeHandlers.put(customTypeHandler.type(), customTypeHandler);
			return this.$();
		}
		
		@Override
		public XMap<Class<?>, PersistenceTypeInstantiator<Binary, ?>> customTypeInstantiators()
		{
			return this.customTypeInstantiators;
		}
		
		@Override
		public synchronized <T> F registerCustomInstantiator(
			final Class<T>                          type            ,
			final PersistenceTypeInstantiator<Binary, T> typeInstantiator
		)
		{
			this.customTypeInstantiators.put(type, typeInstantiator);
			return this.$();
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// getters //
		////////////
		
		@Override
		public InstanceDispatcherLogic getInstanceDispatcherLogic()
		{
			return this.getInstanceDispatcherLogic();
		}
		
		protected Reference<PersistenceTypeHandlerManager<Binary>> referenceTypeHandlerManager()
		{
			return this.referenceTypeHandlerManager;
		}

		@Override
		public PersistenceObjectIdProvider getObjectIdProvider()
		{
			return this.oidProvider;
		}

		@Override
		public PersistenceTypeIdProvider getTypeIdProvider()
		{
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
		public PersistenceObjectManager<Binary> getObjectManager()
		{
			if(this.objectManager == null)
			{
				this.objectManager = this.dispatch(this.ensureObjectManager());
			}
			
			return this.objectManager;
		}

		@Override
		public PersistenceTypeHandlerManager<Binary> getTypeHandlerManager()
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
		public PersistenceContextDispatcher<Binary> getContextDispatcher()
		{
			return this.contextDispatcher;
		}

		@Override
		public PersistenceStorer.Creator<Binary> getStorerCreator()
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
		public PersistenceLoader.Creator<Binary> getBuilderCreator()
		{
			if(this.builderCreator == null)
			{
				this.builderCreator = this.dispatch(this.ensureBuilderCreator());
			}
			
			return this.builderCreator;
		}

		@Override
		public Persister getPersister()
		{
			if(this.persister == null)
			{
				this.persister = this.dispatch(this.ensurePersister());
			}
			
			return this.persister;
		}

		@Override
		public PersistenceTypeHandlerRegistry<Binary> getTypeHandlerRegistry()
		{
			if(this.typeHandlerRegistry == null)
			{
				this.typeHandlerRegistry = this.dispatch(this.ensureTypeHandlerRegistry());
			}
			
			return this.typeHandlerRegistry;
		}

		@Override
		public PersistenceTypeHandlerProvider<Binary> getTypeHandlerProvider()
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
		public PersistenceTypeHandlerEnsurer<Binary> getTypeHandlerEnsurer()
		{
			if(this.typeHandlerEnsurer == null)
			{
				this.typeHandlerEnsurer = this.dispatch(this.ensureTypeHandlerEnsurer());
			}
			
			return this.typeHandlerEnsurer;
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
		public PersistenceTypeDictionaryParser getTypeDictionaryParser()
		{
			if(this.typeDictionaryParser == null)
			{
				this.typeDictionaryParser = this.dispatch(this.ensureTypeDictionaryParser());
			}
			
			return this.typeDictionaryParser;
		}
		
		@Override
		public PersistenceTypeHandlerCreator<Binary> getTypeHandlerCreator()
		{
			if(this.typeHandlerCreator == null)
			{
				this.typeHandlerCreator = this.dispatch(this.ensureTypeHandlerCreator());
			}
			
			return this.typeHandlerCreator;
		}

		@Override
		public PersistenceCustomTypeHandlerRegistry<Binary> getCustomTypeHandlerRegistry()
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
		public PersistenceTypeResolver getTypeResolver()
		{
			if(this.typeResolver == null)
			{
				this.typeResolver = this.dispatch(this.ensureTypeResolver());
			}
			
			return this.typeResolver;
		}
		
		@Override
		public ClassLoaderProvider getClassLoaderProvider()
		{
			if(this.classLoaderProvider == null)
			{
				this.classLoaderProvider = this.dispatch(this.ensureClassLoaderProvider());
			}
			
			return this.classLoaderProvider;
		}
		
		@Override
		public PersistenceTypeMismatchValidator<Binary> getTypeMismatchValidator()
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
		public PersistenceTypeNameMapper getTypeNameMapper()
		{
			if(this.typeNameMapper == null)
			{
				this.typeNameMapper = this.dispatch(this.ensureTypeNameMapper());
			}
			
			return this.typeNameMapper;
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
		public PersistenceFieldEvaluator getFieldEvaluatorPersister()
		{
			if(this.fieldEvaluatorPersister == null)
			{
				this.fieldEvaluatorPersister = this.dispatch(this.ensureFieldEvaluatorPersister());
			}
			
			return this.fieldEvaluatorPersister;
		}

		@Override
		public PersistenceFieldEvaluator getFieldEvaluatorEnum()
		{
			if(this.fieldEvaluatorEnum == null)
			{
				this.fieldEvaluatorEnum = this.dispatch(this.ensureFieldEvaluatorEnum());
			}
			
			return this.fieldEvaluatorEnum;
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
		public PersistenceEagerStoringFieldEvaluator getReferenceFieldEagerEvaluator()
		{
			if(this.eagerStoringFieldEvaluator == null)
			{
				this.eagerStoringFieldEvaluator = this.dispatch(this.ensureReferenceFieldEagerEvaluator());
			}
			
			return this.eagerStoringFieldEvaluator;
		}
		
		@Override
		public PersistenceRootResolverProvider getRootResolverProvider()
		{
			if(this.rootResolverProvider == null)
			{
				this.rootResolverProvider = this.dispatch(this.ensureRootResolverProvider());
			}
			
			return this.rootResolverProvider;
		}
		
		@Override
		public PersistenceRootReferenceProvider<Binary> getRootReferenceProvider()
		{
			if(this.rootReferenceProvider == null)
			{
				this.rootReferenceProvider = this.dispatch(this.ensureRootReferenceProvider());
			}
			
			return this.rootReferenceProvider;
		}
		
		@Override
		public PersistenceUnreachableTypeHandlerCreator<Binary> getUnreachableTypeHandlerCreator()
		{
			if(this.unreachableTypeHandlerCreator == null)
			{
				this.unreachableTypeHandlerCreator = this.dispatch(this.ensureUnreachableTypeHandlerCreator());
			}
			
			return this.unreachableTypeHandlerCreator;
		}
		
		@Override
		public PersistenceLegacyTypeMapper<Binary> getLegacyTypeMapper()
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
		public PersistenceTypeDescriptionResolverProvider getTypeDescriptionResolverProvider()
		{
			if(this.typeDescriptionResolverProvider == null)
			{
				this.typeDescriptionResolverProvider = this.dispatch(this.ensureTypeDescriptionResolverProvider());
			}
			
			return this.typeDescriptionResolverProvider;
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
		public PersistenceLegacyTypeMappingResultor<Binary> getLegacyTypeMappingResultor()
		{
			if(this.legacyTypeMappingResultor == null)
			{
				this.legacyTypeMappingResultor = this.dispatch(this.ensureLegacyTypeMappingResultor());
			}
			
			return this.legacyTypeMappingResultor;
		}
		
		@Override
		public PersistenceLegacyTypeHandlerCreator<Binary> getLegacyTypeHandlerCreator()
		{
			if(this.legacyTypeHandlerCreator == null)
			{
				this.legacyTypeHandlerCreator = this.dispatch(this.ensureLegacyTypeHandlerCreator());
			}
			
			return this.legacyTypeHandlerCreator;
		}
		
		@Override
		public PersistenceLegacyTypeHandlingListener<Binary> getLegacyTypeHandlingListener()
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
		public LambdaTypeRecognizer getLambdaTypeRecognizer()
		{
			if(this.lambdaTypeRecognizer == null)
			{
				this.lambdaTypeRecognizer = this.dispatch(this.ensureLambdaTypeRecognizer());
			}
			
			return this.lambdaTypeRecognizer;
		}

		@Override
		public PersistenceAbstractTypeHandlerSearcher<Binary> getAbstractTypeHandlerSearcher()
		{
			if(this.abstractTypeHandlerSearcher == null)
			{
				this.abstractTypeHandlerSearcher = this.dispatch(this.ensureAbstractTypeHandlerSearcher());
			}
			
			return this.abstractTypeHandlerSearcher;
		}

		@Override
		public PersistenceRootsProvider<Binary> getRootsProvider()
		{
			return this.rootsProvider;
		}
		
		@Override
		public PersistenceInstantiator<Binary> getInstantiator()
		{
			// this is just a getter, not an on demand provider method. See #getInstantiatorProvider for that.
			return this.instantiator;
		}
		
		@Override
		public PersistenceTypeInstantiatorProvider<Binary> getInstantiatorProvider()
		{
			if(this.instantiatorProvider == null)
			{
				this.instantiatorProvider = this.dispatch(this.ensureInstantiatorProvider());
			}
			
			return this.instantiatorProvider;
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
		
		@Override
		public XTable<String, BinaryValueSetter> getCustomTranslatorLookup()
		{
			if(this.customTranslatorLookup == null)
			{
				this.customTranslatorLookup = this.dispatch(this.ensureCustomTranslatorLookup());
			}
			
			return this.customTranslatorLookup;
		}
		
		@Override
		public XEnum<BinaryValueTranslatorKeyBuilder> getTranslatorKeyBuilders()
		{
			if(this.translatorKeyBuilders == null)
			{
				this.translatorKeyBuilders = this.dispatch(this.ensureTranslatorKeyBuilders());
			}
			
			return this.translatorKeyBuilders;
		}
		
		@Override
		public BinaryValueTranslatorMappingProvider getValueTranslatorMappingProvider()
		{
			if(this.valueTranslatorMapping == null)
			{
				this.valueTranslatorMapping = this.dispatch(this.ensureValueTranslatorMappingProvider());
			}
			
			return this.valueTranslatorMapping;
		}
		
		@Override
		public BinaryValueTranslatorProvider getValueTranslatorProvider()
		{
			if(this.valueTranslatorProvider == null)
			{
				this.valueTranslatorProvider = this.dispatch(this.ensureValueTranslatorProvider());
			}
			
			return this.valueTranslatorProvider;
		}
		
		@Override
		public XEnum<Class<?>> getEntityTypes()
		{
			if(this.entityTypes == null)
			{
				this.entityTypes = this.ensureEntityTypes();
			}

			return this.entityTypes;
		}
		
		@Override
		public SerializerTypeInfoStrategyCreator getSerializerTypeInfoStrategyCreator()
		{
			if(this.serializerTypeInfoStrategyCreator == null)
			{
				this.serializerTypeInfoStrategyCreator = this.ensureSerializerTypeInfoStrategyCreator();
			}
			return this.serializerTypeInfoStrategyCreator;
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
		public F setObjectManager(
			final PersistenceObjectManager<Binary> objectManager
		)
		{
			this.objectManager = objectManager;
			return this.$();
		}

		@Override
		public F setStorerCreator(
			final PersistenceStorer.Creator<Binary> storerCreator
		)
		{
			this.storerCreator = storerCreator;
			return this.$();
		}

		@Override
		public F setTypeHandlerCreatorLookup(
			final PersistenceTypeHandlerEnsurer<Binary> typeHandlerCreatorLookup
		)
		{
			this.typeHandlerEnsurer = typeHandlerCreatorLookup;
			return this.$();
		}
		
		@Override
		public F setTypeHandlerCreator(
			final PersistenceTypeHandlerCreator<Binary> typeHandlerCreator
		)
		{
			this.typeHandlerCreator = typeHandlerCreator;
			return this.$();
		}
		
		@Override
		public F setTypeAnalyzer(final PersistenceTypeAnalyzer typeAnalyzer)
		{
			this.typeAnalyzer = typeAnalyzer;
			return this.$();
		}
		
		@Override
		public F setTypeResolver(final PersistenceTypeResolver typeResolver)
		{
			this.typeResolver = typeResolver;
			return this.$();
		}
		
		@Override
		public F setClassLoaderProvider(final ClassLoaderProvider classLoaderProvider)
		{
			this.classLoaderProvider = classLoaderProvider;
			return this.$();
		}

		@Override
		public F setTypeHandlerManager(
			final PersistenceTypeHandlerManager<Binary> typeHandlerManager
		)
		{
			this.internalSetTypeHandlerManager(typeHandlerManager);
			return this.$();
		}
		
		private void internalSetTypeHandlerManager(
			final PersistenceTypeHandlerManager<Binary> typeHandlerManager
		)
		{
			synchronized(this.referenceTypeHandlerManager)
			{
				this.typeHandlerManager = typeHandlerManager;
				this.referenceTypeHandlerManager.set(typeHandlerManager);
			}
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
			final PersistenceTypeHandlerRegistry<Binary> typeHandlerRegistry
		)
		{
			this.typeHandlerRegistry = typeHandlerRegistry;
			return this.$();
		}

		@Override
		public F setTypeHandlerProvider(
			final PersistenceTypeHandlerProvider<Binary> typeHandlerProvider
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
			final PersistenceLoader.Creator<Binary> builderCreator
		)
		{
			this.builderCreator = builderCreator;
			return this.$();
		}
		
		@Override
		public F setPersister(final Persister persister)
		{
			this.persister = persister;
			return this.$();
		}
		
		@Override
		public F setSizedArrayLengthController(final PersistenceSizedArrayLengthController sizedArrayLengthController)
		{
			this.sizedArrayLengthController = sizedArrayLengthController;
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
		public F setTypeDictionaryParser(
			final PersistenceTypeDictionaryParser typeDictionaryParser
		)
		{
			this.typeDictionaryParser = typeDictionaryParser;
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
		public F setTypeMismatchValidator(
			final PersistenceTypeMismatchValidator<Binary> typeMismatchValidator
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
		public F setTypeNameMapper(final PersistenceTypeNameMapper typeNameMapper)
		{
			this.typeNameMapper = typeNameMapper;
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
		public F setFieldEvaluatorPersister(
			final PersistenceFieldEvaluator fieldEvaluator
		)
		{
			this.fieldEvaluatorPersister = fieldEvaluator;
			return this.$();
		}

		@Override
		public F setFieldEvaluatorEnum(
			final PersistenceFieldEvaluator fieldEvaluator
		)
		{
			this.fieldEvaluatorEnum = fieldEvaluator;
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
		public F setReferenceFieldEagerEvaluator(
			final PersistenceEagerStoringFieldEvaluator evaluator
		)
		{
			this.eagerStoringFieldEvaluator = evaluator;
			return this.$();
		}
		
		@Override
		public F setRootResolverProvider(final PersistenceRootResolverProvider rootResolverProvider)
		{
			this.rootResolverProvider = rootResolverProvider;
			return this.$();
		}
		
		@Override
		public F setRootReferenceProvider(final PersistenceRootReferenceProvider<Binary> rootReferenceProvider)
		{
			this.rootReferenceProvider = rootReferenceProvider;
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
		public F setAbstractTypeHandlerSearcher(
			final PersistenceAbstractTypeHandlerSearcher<Binary> abstractTypeHandlerSearcher
		)
		{
			this.abstractTypeHandlerSearcher = abstractTypeHandlerSearcher;
			return this.$();
		}

		@Override
		public F setUnreachableTypeHandlerCreator(
			final PersistenceUnreachableTypeHandlerCreator<Binary> unreachableTypeHandlerCreator
		)
		{
			this.unreachableTypeHandlerCreator = unreachableTypeHandlerCreator;
			return this.$();
		}
		
		@Override
		public F setLegacyTypeMapper(
			final PersistenceLegacyTypeMapper<Binary> legacyTypeMapper
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
		public F setTypeDescriptionResolverProvider(
			final PersistenceTypeDescriptionResolverProvider typeDescriptionResolverProvi
		)
		{
			this.typeDescriptionResolverProvider = typeDescriptionResolverProvi;
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
			final PersistenceLegacyTypeMappingResultor<Binary> legacyTypeMappingResultor
		)
		{
			this.legacyTypeMappingResultor = legacyTypeMappingResultor;
			return this.$();
		}
		
		@Override
		public F setLegacyTypeHandlerCreator(
			final PersistenceLegacyTypeHandlerCreator<Binary> legacyTypeHandlerCreator
		)
		{
			this.legacyTypeHandlerCreator = legacyTypeHandlerCreator;
			return this.$();
		}
		
		@Override
		public F setLegacyTypeHandlingListener(
			final PersistenceLegacyTypeHandlingListener<Binary> legacyTypeHandlingListener
		)
		{
			this.legacyTypeHandlingListener = legacyTypeHandlingListener;
			return this.$();
		}
		
		@Override
		public F setInstantiator(final PersistenceInstantiator<Binary> instantiator)
		{
			this.instantiator = instantiator;
			return this.$();
		}
		
		@Override
		public F setInstantiatorProvider(final PersistenceTypeInstantiatorProvider<Binary> instantiatorProvider)
		{
			this.instantiatorProvider = instantiatorProvider;
			return this.$();
		}
		
		@Override
		public F setTargetByteOrder(final ByteOrder targetByteOrder)
		{
			this.targetByteOrder = targetByteOrder;
			return this.$();
		}
		
		@Override
		public F setCustomTranslatorLookup(final XTable<String, BinaryValueSetter> customTranslatorLookup)
		{
			this.customTranslatorLookup = customTranslatorLookup;
			return this.$();
		}
		
		@Override
		public F setTranslatorKeyBuilders(final XEnum<BinaryValueTranslatorKeyBuilder> translatorKeyBuilders)
		{
			this.translatorKeyBuilders = translatorKeyBuilders;
			return this.$();
		}
		
		@Override
		public F setValueTranslatorProvider(final BinaryValueTranslatorProvider valueTranslatorProvider)
		{
			this.valueTranslatorProvider = valueTranslatorProvider;
			return this.$();
		}
		
		@Override
		public F setValueTranslatorMappingProvider(final BinaryValueTranslatorMappingProvider valueTranslatorMapping)
		{
			this.valueTranslatorMapping = valueTranslatorMapping;
			return this.$();
		}
		
		@Override
		public F setEntityTypes(final XEnum<Class<?>> entityTypes)
		{
			this.entityTypes = entityTypes;
			return this.$();
		}
		
		@Override
		public F setSerializerTypeInfoStrategyCreator(final SerializerTypeInfoStrategyCreator serializerTypeInfoStrategyCreator)
		{
			this.serializerTypeInfoStrategyCreator = serializerTypeInfoStrategyCreator;
			return this.$();
		}
		
		@Override
		public boolean registerEntityType(final Class<?> entityType)
		{
			return this.getEntityTypes().add(entityType);
		}
		
		@Override
		public F registerEntityTypes(final Class<?>... entityTypes)
		{
			this.getEntityTypes().addAll(entityTypes);
			
			return this.$();
		}
		
		@Override
		public F registerEntityTypes(final Iterable<Class<?>> entityTypes)
		{
			final XEnum<Class<?>> registeredEntityTypes = this.getEntityTypes();
			
			for(final Class<?> entityType : entityTypes)
			{
				registeredEntityTypes.add(entityType);
			}
			
			return this.$();
		}


		
		///////////////////////////////////////////////////////////////////////////
		// ensuring //
		/////////////
		
		/* Explanation:
		 * These methods are not actually abstract because it is not necessarily required
		 * to create new instances of these types. Instead, appropriate instances can be set.
		 * These methods exist in order to allow sub classes to implement them optionally
		 * and throw an exception if neither implementation nor set instance is available.
		 */


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

		protected PersistenceObjectManager<Binary> ensureObjectManager()
		{
			final PersistenceObjectManager<Binary> newObjectManager = PersistenceObjectManager.New(
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

		protected PersistenceTypeHandlerManager<Binary> ensureTypeHandlerManager()
		{
			final PersistenceTypeHandlerManager<Binary> newTypeHandlerManager =
				PersistenceTypeHandlerManager.New(
					this.getTypeHandlerRegistry()          , // holds actually used (potentially generically created) handlers
					this.getTypeHandlerProvider()          , // knows/contains the Custom~Registry w. default handlers/definitions
					this.getTypeDictionaryManager()        , // only manages loading, storing and registering
					this.getTypeMismatchValidator()        ,
					this.getLegacyTypeMapper()             ,
					this.getUnreachableTypeHandlerCreator(),
					this.getRootsProvider()                  // only required for dynamically registering enum constant roots
				)
			;
			return newTypeHandlerManager;
		}

		protected PersistenceRegisterer.Creator ensureRegistererCreator()
		{
			return new PersistenceRegisterer.Default.Creator();
		}

		protected PersistenceTypeHandlerRegistry<Binary> ensureTypeHandlerRegistry()
		{
			// note: sub class should/must register native type handlers in an overridden version of this method
			final PersistenceTypeHandlerRegistry<Binary> newTypeHandlerRegistry =
				PersistenceTypeHandlerRegistry.New(this.getTypeRegistry())
			;
			return newTypeHandlerRegistry;
		}

		protected PersistenceTypeHandlerProvider<Binary> ensureTypeHandlerProvider()
		{
			return PersistenceTypeHandlerProviderCreating.New(
				this.dataType(),
				this.getTypeManager(),
				this.getTypeHandlerEnsurer()
			);
		}

		protected PersistenceTypeDictionaryManager ensureTypeDictionaryManager()
		{
			final PersistenceTypeDictionaryManager newTypeDictionaryManager =
				PersistenceTypeDictionaryManager.Transient(
					this.getTypeDictionaryCreator()
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

		protected PersistenceTypeDictionaryParser ensureTypeDictionaryParser()
		{
			final PersistenceTypeDictionaryParser newTypeDictionaryParser =
				PersistenceTypeDictionaryParser.New(
					this.getTypeResolver()            ,
					this.getFieldFixedLengthResolver(),
					this.getTypeNameMapper()
				)
			;
			return newTypeDictionaryParser;
		}

		protected PersistenceTypeAnalyzer ensureTypeAnalyzer()
		{
			return PersistenceTypeAnalyzer.New(
				this.getTypeEvaluatorPersistable() ,
				this.getFieldEvaluatorPersistable(),
				this.getFieldEvaluatorPersister()  ,
				this.getFieldEvaluatorEnum()       ,
				this.getFieldEvaluatorCollection()
			);
		}
		
		protected PersistenceTypeResolver ensureTypeResolver()
		{
			return PersistenceTypeResolver.New(
				this.getClassLoaderProvider()
			);
		}
		
		protected ClassLoaderProvider ensureClassLoaderProvider()
		{
			// must always resolve types using that ClassLoader with which the initialization happened (this class's).
			return ClassLoaderProvider.New(
				this.getClass().getClassLoader()
			);
		}

		protected PersistenceTypeHandlerEnsurer<Binary> ensureTypeHandlerEnsurer()
		{
			return PersistenceTypeHandlerEnsurer.New(
				this.dataType(),
				this.getCustomTypeHandlerRegistry(),
				this.getTypeAnalyzer(),
				this.getLambdaTypeRecognizer(),
				this.getAbstractTypeHandlerSearcher(),
				this.getTypeHandlerCreator()
			);
		}
		
		protected PersistenceTypeMismatchValidator<Binary> ensureTypeMismatchValidator()
		{
			// (13.09.2018 TM)NOTE: changed for Legacy Type Mapping. Still a valid callback for monitoring purposes.
			return Persistence.typeMismatchValidatorNoOp();
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

		protected PersistenceFieldEvaluator ensureFieldEvaluatorPersister()
		{
			return Persistence.defaultFieldEvaluatorPersister();
		}

		protected PersistenceFieldEvaluator ensureFieldEvaluatorEnum()
		{
			return Persistence.defaultFieldEvaluatorEnum();
		}

		protected PersistenceFieldEvaluator ensureFieldEvaluatorCollection()
		{
			return Persistence.defaultFieldEvaluatorCollection();
		}
		
		protected PersistenceEagerStoringFieldEvaluator ensureReferenceFieldEagerEvaluator()
		{
			return Persistence.defaultReferenceFieldEagerEvaluator();
		}
		
		protected PersistenceUnreachableTypeHandlerCreator<Binary> ensureUnreachableTypeHandlerCreator()
		{
			return PersistenceUnreachableTypeHandlerCreator.New();
		}
		
		protected PersistenceLegacyTypeMapper<Binary> ensureLegacyTypeMapper()
		{
			return PersistenceLegacyTypeMapper.New(
				this.getTypeDescriptionResolverProvider() ,
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
		
		protected PersistenceTypeDescriptionResolverProvider ensureTypeDescriptionResolverProvider()
		{
			return PersistenceTypeDescriptionResolverProvider.Caching(
				this.getTypeResolver()                              ,
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
				
		protected PersistenceLegacyTypeMappingResultor<Binary> ensureLegacyTypeMappingResultor()
		{
			return LoggingLegacyTypeMappingResultor.New(
				PersistenceLegacyTypeMappingResultor.New()
			);
		}
		
		protected PersistenceLegacyTypeHandlerCreator<Binary> ensureLegacyTypeHandlerCreator()
		{
			return BinaryLegacyTypeHandlerCreator.New(
				this.ensureValueTranslatorProvider(),
				this.getLegacyTypeHandlingListener(),
				this.isByteOrderMismatch()
			);
		}
		
		protected PersistenceLegacyTypeHandlingListener<Binary> ensureLegacyTypeHandlingListener()
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

		protected BinaryStorer.Creator ensureStorerCreator()
		{
			return BinaryStorer.Creator(
				() -> 1,
				this.isByteOrderMismatch()
			);
		}

		protected BinaryLoader.Creator ensureBuilderCreator()
		{
			return BinaryLoader.CreatorSimple(
				this.isByteOrderMismatch()
			);
		}

		protected BinaryTypeHandlerCreator ensureTypeHandlerCreator()
		{
			return BinaryTypeHandlerCreator.New(
				this.getTypeAnalyzer(),
				this.getTypeResolver(),
				this.getFieldFixedLengthResolver(),
				this.getReferenceFieldEagerEvaluator(),
				this.getInstantiatorProvider(),
				this.referenceTypeHandlerManager(),
				this.isByteOrderMismatch()
			);
		}

		protected synchronized PersistenceCustomTypeHandlerRegistry<Binary> ensureCustomTypeHandlerRegistry()
		{
			return BinaryPersistence.createDefaultCustomTypeHandlerRegistry(
				this.referenceTypeHandlerManager(),
				this.getSizedArrayLengthController(),
				this.getTypeHandlerCreator(),
				this.customTypeHandlers().values()
			);
		}

		protected BinaryFieldLengthResolver ensureFieldFixedLengthResolver()
		{
			return BinaryPersistence.createFieldLengthResolver();
		}
		
		protected PersistenceTypeNameMapper ensureTypeNameMapper()
		{
			return PersistenceTypeNameMapper.New();
		}
		
		protected PersistenceRootResolverProvider ensureRootResolverProvider()
		{
			final PersistenceTypeDescriptionResolverProvider tDReslvProvdr = this.getTypeDescriptionResolverProvider();
			final PersistenceTypeResolver                    typeResolver  = this.getTypeResolver();
			final PersistenceRootReferenceProvider<?>        rootRefProvdr = this.getRootReferenceProvider();
			final PersistenceRootReference                   rootReference = rootRefProvdr.provideRootReference();
						
			// must use the foundation's type resolver instance instead of creating a new default one internally.
			final PersistenceRootResolverProvider resolverProvider = PersistenceRootResolverProvider
				.New(rootReference, typeResolver)
				.setTypeDescriptionResolverProvider(tDReslvProvdr)
				.setTypeHandlerManager(this.referenceTypeHandlerManager)
			;
			
			return resolverProvider;
		}
		
		protected PersistenceRootReferenceProvider<Binary> ensureRootReferenceProvider()
		{
			return BinaryRootReferenceProvider.New();
		}
		
		protected PersistenceRootsProvider<Binary> ensureRootsProviderInternal()
		{
			return BinaryPersistenceRootsProvider.New(
				this.getRootResolverProvider(),
				this.getRootReferenceProvider()
			);
		}

		protected PersistenceRootsProvider<Binary> ensureRootsProvider()
		{
			final PersistenceRootsProvider<Binary> rootsProvider = this.ensureRootsProviderInternal();
			rootsProvider.registerRootsTypeHandlerCreator(
				this.getCustomTypeHandlerRegistry(),
				this.getObjectRegistry()
			);
			
			return rootsProvider;
		}
		
		protected PersistenceInstantiator<Binary> ensureInstantiator()
		{
			return this.instantiator != null
				? this.instantiator
				: PersistenceInstantiator.New()
			;
		}
		
		protected PersistenceTypeInstantiatorProvider<Binary> ensureInstantiatorProvider()
		{
			// empty table check done inside (constructor method concern)
			return PersistenceTypeInstantiatorProvider.New(
				this.customTypeInstantiators,
				this.ensureInstantiator()
			);
		}
		
		protected LambdaTypeRecognizer ensureLambdaTypeRecognizer()
		{
			return LambdaTypeRecognizer.New();
		}
		
		protected PersistenceAbstractTypeHandlerSearcher<Binary> ensureAbstractTypeHandlerSearcher()
		{
			return PersistenceAbstractTypeHandlerSearcher.New();
		}
		
		protected ByteOrder ensureTargetByteOrder()
		{
			return ByteOrder.nativeOrder();
		}
		
		protected Persister ensurePersister()
		{
			// null by default, then the persistenceManager itself is the persister.
			return null;
		}
				
		protected XTable<String, BinaryValueSetter> ensureCustomTranslatorLookup()
		{
			return EqHashTable.New();
		}
		
		protected XEnum<BinaryValueTranslatorKeyBuilder> ensureTranslatorKeyBuilders()
		{
			return EqHashEnum.New();
		}
		
		protected BinaryValueTranslatorMappingProvider ensureValueTranslatorMappingProvider()
		{
			return BinaryValueTranslatorMappingProvider.New();
		}
		
		protected BinaryValueTranslatorProvider ensureValueTranslatorProvider()
		{
			return BinaryValueTranslatorProvider.New(
				this.getCustomTranslatorLookup(),
				this.getTranslatorKeyBuilders(),
				this.getValueTranslatorMappingProvider(),
				this.isByteOrderMismatch()
			);
		}
		
		protected XEnum<Class<?>> ensureEntityTypes()
		{
			return HashEnum.New();
		}

		protected SerializerTypeInfoStrategyCreator ensureSerializerTypeInfoStrategyCreator()
		{
			return new SerializerTypeInfoStrategyCreator.TypeDictionary(false);
		}


		///////////////////////////////////////////////////////////////////////////
		// methods // (with logic worth mentioning)
		////////////

		@Override
		public void executeTypeHandlerRegistration(final PersistenceTypeHandlerRegistration<Binary> typeHandlerRegistration)
		{
			typeHandlerRegistration.registerTypeHandlers(
				this.getCustomTypeHandlerRegistry(),
				this.getSizedArrayLengthController()
			);
		}
		
		
		@Override
		public PersistenceManager<Binary> createPersistenceManager(
			final PersistenceSource<Binary> source,
			final PersistenceTarget<Binary> target
		)
		{
			final PersistenceTypeHandlerManager<Binary> typeHandlerManager = this.getTypeHandlerManager();
			typeHandlerManager.initialize(); // initialize type handlers (i.e. import/validate type dictionary)
			this.getEntityTypes().forEach(typeHandlerManager::ensureTypeHandler);
			
			return PersistenceManager.New(
				this.getObjectRegistry(),
				this.getObjectManager(),
				typeHandlerManager,
				this.getContextDispatcher(),
				this.getStorerCreator(),
				this.getBuilderCreator(),
				this.getRegistererCreator(),
				this.getPersister(),
				target,
				source,
				PersistenceStorer.CreationObserver::noOp, // only relevant for storage
				this.getBufferSizeProvider(),
				this.getTargetByteOrder()
			);
		}

	}
	
}

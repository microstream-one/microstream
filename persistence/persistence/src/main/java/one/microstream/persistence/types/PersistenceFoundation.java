package one.microstream.persistence.types;

/*-
 * #%L
 * microstream-persistence
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

import static one.microstream.X.notNull;

import java.nio.ByteOrder;

import one.microstream.X;
import one.microstream.collections.HashTable;
import one.microstream.collections.types.XEnum;
import one.microstream.collections.types.XMap;
import one.microstream.exceptions.MissingFoundationPartException;
import one.microstream.functional.InstanceDispatcherLogic;
import one.microstream.persistence.internal.LoggingLegacyTypeMappingResultor;
import one.microstream.persistence.internal.PersistenceTypeHandlerProviderCreating;
import one.microstream.reference.ObjectSwizzling;
import one.microstream.reference.Reference;
import one.microstream.reflect.ClassLoaderProvider;
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
 * 
 * @param <D> the data type
 * @param <F> the foundation type
 */
public interface PersistenceFoundation<D, F extends PersistenceFoundation<D, ?>>
extends Cloneable<PersistenceFoundation<D, F>>,
        ByteOrderTargeting.Mutable<F>,
        PersistenceDataTypeHolder<D>,
        PersistenceTypeHandlerRegistration.Executor<D>,
        InstanceDispatcher
{
	// the pseudo-self-type F is to avoid having to override every setter in every sub class (it was really tedious)
	
	@Override
	public PersistenceFoundation<D, F> Clone();
	
	public XMap<Class<?>, PersistenceTypeHandler<D, ?>> customTypeHandlers();

	public XMap<Class<?>, PersistenceTypeInstantiator<D, ?>> customTypeInstantiators();
	
	public F registerCustomTypeHandlers(HashTable<Class<?>, PersistenceTypeHandler<D, ?>> customTypeHandlers);
	
	@SuppressWarnings("unchecked")
	public F registerCustomTypeHandlers(PersistenceTypeHandler<D, ?>... customTypeHandlers);
	
	public F registerCustomTypeHandlers(Iterable<? extends PersistenceTypeHandler<D, ?>> customTypeHandlers);
	
	public F registerCustomTypeHandler(PersistenceTypeHandler<D, ?> customTypeHandler);
	
	public <T> F registerCustomInstantiator(Class<T> type, PersistenceTypeInstantiator<D, T> typeInstantiator);
	
	public PersistenceObjectIdProvider getObjectIdProvider();

	public PersistenceTypeIdProvider getTypeIdProvider();


	public PersistenceStorer.Creator<D> getStorerCreator();

	public PersistenceRegisterer.Creator getRegistererCreator();

	public PersistenceLoader.Creator<D> getBuilderCreator();
	
//	public ObjectSwizzling getObjectRetriever();
	
	public Persister getPersister();

	public PersistenceTarget<D> getPersistenceTarget();

	public PersistenceSource<D> getPersistenceSource();
	
	public PersistenceObjectRegistry getObjectRegistry();

	public PersistenceObjectManager<D> getObjectManager();
	
	public PersistenceTypeRegistry getTypeRegistry();
	
	public PersistenceTypeManager getTypeManager();

	public PersistenceTypeHandlerManager<D> getTypeHandlerManager();
	
	public PersistenceContextDispatcher<D> getContextDispatcher();

	public PersistenceTypeHandlerProvider<D> getTypeHandlerProvider();

	public PersistenceTypeHandlerEnsurer<D> getTypeHandlerEnsurer();

	public PersistenceTypeHandlerRegistry<D> getTypeHandlerRegistry();

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

	public PersistenceTypeHandlerCreator<D> getTypeHandlerCreator();

	/**
	 * Creates if required and returns the {@link PersistenceCustomTypeHandlerRegistry} containing all custom tailored
	 * {@link PersistenceTypeHandler} instances for specialized handling of instances.<p>
	 * To avoid order problems caused by internal implicit dependencies (e.g. creating the default custom type handlers
	 * required {@link #getReferenceFieldEagerEvaluator()}), use {@link #customTypeHandlers()}
	 * or one of the {@code registerCustomTypeHandler~} methods.
	 * 
	 * @return the (on-demand created) {@link PersistenceCustomTypeHandlerRegistry} instance.
	 */
	public PersistenceCustomTypeHandlerRegistry<D> getCustomTypeHandlerRegistry();
	
	public PersistenceCustomTypeHandlerRegistryEnsurer<D> customTypeHandlerRegistryEnsurer();
	
	public PersistenceCustomTypeHandlerRegistryEnsurer<D> getCustomTypeHandlerRegistryEnsurer();

	public PersistenceTypeAnalyzer getTypeAnalyzer();
	
	public PersistenceTypeResolver getTypeResolver();
	
	public ClassLoaderProvider getClassLoaderProvider();
	
	public PersistenceTypeMismatchValidator<D> getTypeMismatchValidator();
	
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
	
	public PersistenceRootReferenceProvider<D> getRootReferenceProvider();
	
	public PersistenceRootsProvider<D> getRootsProvider();
	
	public PersistenceUnreachableTypeHandlerCreator<D> getUnreachableTypeHandlerCreator();
	
	public PersistenceLegacyTypeMapper<D> getLegacyTypeMapper();

	public PersistenceRefactoringMappingProvider getRefactoringMappingProvider();

	public PersistenceTypeDescriptionResolverProvider getTypeDescriptionResolverProvider();

	public XEnum<? extends PersistenceRefactoringTypeIdentifierBuilder>  getRefactoringLegacyTypeIdentifierBuilders();
	
	public XEnum<? extends PersistenceRefactoringMemberIdentifierBuilder> getRefactoringLegacyMemberIdentifierBuilders();
	
	public XEnum<? extends PersistenceRefactoringMemberIdentifierBuilder> getRefactoringCurrentMemberIdentifierBuilders();
	
	public TypeMapping<Float> getTypeSimilarity();
	
	public PersistenceMemberMatchingProvider getLegacyMemberMatchingProvider();
	
	public PersistenceLegacyTypeMappingResultor<D> getLegacyTypeMappingResultor();
	
	public PersistenceLegacyTypeHandlerCreator<D> getLegacyTypeHandlerCreator();
	
	public PersistenceLegacyTypeHandlingListener<D> getLegacyTypeHandlingListener();
	
	public PersistenceSizedArrayLengthController getSizedArrayLengthController();
	
	public LambdaTypeRecognizer getLambdaTypeRecognizer();
	
	public PersistenceAbstractTypeHandlerSearcher<D> getAbstractTypeHandlerSearcher();

	public PersistenceInstantiator<D> getInstantiator();
	
	public PersistenceTypeInstantiatorProvider<D> getInstantiatorProvider();
	
	public PersistenceStorer.CreationObserver getStorerCreationObserver();
	
	public F setObjectRegistry(PersistenceObjectRegistry objectRegistry);
	
	public F setTypeRegistry(PersistenceTypeRegistry typeRegistry);

	public F setInstanceDispatcher(InstanceDispatcherLogic instanceDispatcher);

	public F setObjectManager(PersistenceObjectManager<D> objectManager);

	public F setStorerCreator(PersistenceStorer.Creator<D> storerCreator);

	public F setTypeHandlerManager(PersistenceTypeHandlerManager<D> typeHandlerManager);
	
	public F setContextDispatcher(PersistenceContextDispatcher<D> contextDispatcher);

	public F setTypeManager(PersistenceTypeManager typeManager);

	public F setTypeHandlerCreatorLookup(PersistenceTypeHandlerEnsurer<D> typeHandlerCreatorLookup);
	
	public F setTypeHandlerCreator(PersistenceTypeHandlerCreator<D> typeHandlerCreator);
	
	public F setTypeAnalyzer(PersistenceTypeAnalyzer typeAnalyzer);
	
	public F setTypeResolver(PersistenceTypeResolver typeResolver);
	
	public F setClassLoaderProvider(ClassLoaderProvider classLoaderProvider);

	public F setTypeHandlerRegistry(PersistenceTypeHandlerRegistry<D> typeHandlerRegistry);

	public F setTypeHandlerProvider(PersistenceTypeHandlerProvider<D> typeHandlerProvider);

	public F setRegistererCreator(PersistenceRegisterer.Creator registererCreator);

	public F setBuilderCreator(PersistenceLoader.Creator<D> builderCreator);
	
//	public F setObjectRetriever(ObjectSwizzling objectRetriever);
	
	public F setPersister(Persister persister);

	public F setPersistenceTarget(PersistenceTarget<D> target);

	public F setPersistenceSource(PersistenceSource<D> source);

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
	
	public F setTypeMismatchValidator(PersistenceTypeMismatchValidator<D> typeMismatchValidator);
	
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

	public F setTypeNameMapper(PersistenceTypeNameMapper typeNameMapper);

	public F setFieldEvaluatorPersistable(PersistenceFieldEvaluator fieldEvaluator);
	
	public F setFieldEvaluatorPersister(PersistenceFieldEvaluator fieldEvaluator);

	public F setFieldEvaluatorEnum(PersistenceFieldEvaluator fieldEvaluator);
	
	public F setFieldEvaluatorCollection(PersistenceFieldEvaluator fieldEvaluator);

	public F setReferenceFieldEagerEvaluator(PersistenceEagerStoringFieldEvaluator evaluator);

	public F setRootResolverProvider(PersistenceRootResolverProvider rootResolverProvider);
	
	public F setRootReferenceProvider(PersistenceRootReferenceProvider<D> rootReferenceProvider);
		
	public F setLambdaTypeRecognizer(LambdaTypeRecognizer lambdaTypeRecognizer);
	
	public F setAbstractTypeHandlerSearcher(PersistenceAbstractTypeHandlerSearcher<D> abstractTypeHandlerSearcher);

	public F setRootsProvider(PersistenceRootsProvider<D> rootsProvider);
	
	public F setUnreachableTypeHandlerCreator(
		PersistenceUnreachableTypeHandlerCreator<D> unreachableTypeHandlerCreator
	);
	
	public F setLegacyTypeMapper(
		PersistenceLegacyTypeMapper<D> legacyTypeMapper
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
		
	public F setLegacyTypeMappingResultor(PersistenceLegacyTypeMappingResultor<D> legacyTypeMappingResultor);
	
	public F setLegacyTypeHandlerCreator(PersistenceLegacyTypeHandlerCreator<D> legacyTypeHandlerCreator);
	
	public F setLegacyTypeHandlingListener(PersistenceLegacyTypeHandlingListener<D> legacyTypeHandlingListener);

	public F setPersistenceChannel(PersistenceChannel<D> persistenceChannel);
	
	public F setSizedArrayLengthController(PersistenceSizedArrayLengthController sizedArrayLengthController);
	
	public F setObjectIdProvider(PersistenceObjectIdProvider oidProvider);

	public F setTypeIdProvider(PersistenceTypeIdProvider tidProvider);

	public <P extends PersistenceTypeIdProvider & PersistenceObjectIdProvider>
	F setIdProvider(P idProvider);
		
	public F setInstantiator(PersistenceInstantiator<D> instantiator);
	
	public F setInstantiatorProvider(PersistenceTypeInstantiatorProvider<D> instantiatorProvider);
	
	public F setStorerCreationObserver(PersistenceStorer.CreationObserver liveStorerRegistry);
		
	public F setCustomTypeHandlerRegistryEnsurer(
		PersistenceCustomTypeHandlerRegistryEnsurer<D> customTypeHandlerRegistryEnsurer
	);
	

	/*
	 * generic name is intentional as the role of the created instance may change in extended types
	 * (e.g. representing a database connection)
	 */
	public PersistenceManager<D> createPersistenceManager();



	public static <D> PersistenceFoundation<D, ?> New(final Class<D> dataType)
	{
		return new PersistenceFoundation.Default<>(
			notNull(dataType)
		);
	}
	
	public class Default<D, F extends PersistenceFoundation.Default<D, ?>>
	extends InstanceDispatcher.Default
	implements PersistenceFoundation<D, F>, Unpersistable
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final Class<D> dataType;

		// required to resolve a TypeHandlerManager dependancy loop. Must be created anew to a
		private final Reference<PersistenceTypeHandlerManager<D>> referenceTypeHandlerManager = X.Reference(null);
		
		private final HashTable<Class<?>, PersistenceTypeHandler<D, ?>> customTypeHandlers = HashTable.New();
		
		private final HashTable<Class<?>, PersistenceTypeInstantiator<D, ?>> customTypeInstantiators = HashTable.New();

		private PersistenceObjectManager<D>                    objectManager                   ;
		private PersistenceObjectIdProvider                    oidProvider                     ;
		private PersistenceTypeIdProvider                      tidProvider                     ;
		private PersistenceTypeRegistry                        typeRegistry                    ;
		private PersistenceObjectRegistry                      objectRegistry                  ;
		private PersistenceTypeHandlerManager<D>               typeHandlerManager              ;
		private PersistenceContextDispatcher<D>                contextDispatcher               ;
		private PersistenceStorer.Creator<D>                   storerCreator                   ;
		private PersistenceRegisterer.Creator                  registererCreator               ;
		private PersistenceLoader.Creator<D>                   builderCreator                  ;
//		private ObjectSwizzling                                objectRetriever                 ;
		private Persister                                      persister                       ;
		private PersistenceTarget<D>                           target                          ;
		private PersistenceSource<D>                           source                          ;
		private PersistenceFieldLengthResolver                 fieldFixedLengthResolver        ;
		private PersistenceTypeNameMapper                      typeNameMapper                  ;
		private PersistenceFieldEvaluator                      fieldEvaluatorPersistable       ;
		private PersistenceFieldEvaluator                      fieldEvaluatorPersister         ;
		private PersistenceFieldEvaluator                      fieldEvaluatorEnum              ;
		private PersistenceFieldEvaluator                      fieldEvaluatorCollection        ;
		private PersistenceEagerStoringFieldEvaluator          eagerStoringFieldEvaluator      ;

		// (14.09.2018 TM)NOTE: that type handling stuff grows to a size where it could use its own foundation.
		private PersistenceTypeManager                         typeManager                     ;
		private PersistenceTypeHandlerEnsurer<D>               typeHandlerEnsurer              ;
		private PersistenceTypeHandlerRegistry<D>              typeHandlerRegistry             ;
		private PersistenceTypeHandlerProvider<D>              typeHandlerProvider             ;
		private PersistenceTypeDictionaryManager               typeDictionaryManager           ;
		private PersistenceTypeDictionaryCreator               typeDictionaryCreator           ;
		private PersistenceTypeDictionaryProvider              typeDictionaryProvider          ;
		private PersistenceTypeDictionaryExporter              typeDictionaryExporter          ;
		private PersistenceTypeDictionaryLoader                typeDictionaryLoader            ;
		private PersistenceTypeDictionaryParser                typeDictionaryParser            ;
		private PersistenceTypeDictionaryBuilder               typeDictionaryBuilder           ;
		private PersistenceTypeDictionaryCompiler              typeDictionaryCompiler          ;
		private PersistenceTypeDictionaryAssembler             typeDictionaryAssembler         ;
		private PersistenceTypeDictionaryStorer                typeDictionaryStorer            ;
		private PersistenceTypeLineageCreator                  typeLineageCreator              ;
		private PersistenceTypeHandlerCreator<D>               typeHandlerCreator              ;
		private PersistenceTypeAnalyzer                        typeAnalyzer                    ;
		private PersistenceTypeResolver                        typeResolver                    ;
		private ClassLoaderProvider                            classLoaderProvider             ;
		private PersistenceTypeMismatchValidator<D>            typeMismatchValidator           ;
		private PersistenceTypeDefinitionCreator               typeDefinitionCreator           ;
		private PersistenceTypeEvaluator                       typeEvaluatorPersistable        ;
		private LambdaTypeRecognizer                           lambdaTypeRecognizer            ;
		private PersistenceAbstractTypeHandlerSearcher<D>      abstractTypeHandlerSearcher     ;
		private PersistenceSizedArrayLengthController          sizedArrayLengthController      ;
		private PersistenceInstantiator<D>                     instantiator                    ;
		private PersistenceTypeInstantiatorProvider<D>         instantiatorProvider            ;
		private PersistenceStorer.CreationObserver             liveStorerRegistry              ;
		private PersistenceCustomTypeHandlerRegistry<D>        customTypeHandlerRegistry       ;
		private PersistenceCustomTypeHandlerRegistryEnsurer<D> customTypeHandlerRegistryEnsurer;
		private BufferSizeProviderIncremental                  bufferSizeProvider              ;
		private PersistenceRootResolverProvider                rootResolverProvider            ;
		private PersistenceRootsProvider<D>                    rootsProvider                   ;
		private PersistenceRootReferenceProvider<D>            rootReferenceProvider           ;
		private ByteOrder                                      targetByteOrder                 ;
		
		// (14.09.2018 TM)NOTE: that legacy mapping stuff grows to a size where it could use its own foundation.
		private PersistenceUnreachableTypeHandlerCreator<D>    unreachableTypeHandlerCreator   ;
		private PersistenceLegacyTypeMapper<D>                 legacyTypeMapper                ;
		private PersistenceRefactoringMappingProvider          refactoringMappingProvider      ;
		private PersistenceTypeDescriptionResolverProvider     typeDescriptionResolverProvider ;
		private TypeMapping<Float>                             typeSimilarity                  ;
		private PersistenceMemberMatchingProvider              legacyMemberMatchingProvider    ;
		private PersistenceLegacyTypeMappingResultor<D>        legacyTypeMappingResultor       ;
		private PersistenceLegacyTypeHandlerCreator<D>         legacyTypeHandlerCreator        ;
		private PersistenceLegacyTypeHandlingListener<D>       legacyTypeHandlingListener      ;

		private XEnum<? extends PersistenceRefactoringTypeIdentifierBuilder>   refactoringLegacyTypeIdentifierBuilders   ;
		private XEnum<? extends PersistenceRefactoringMemberIdentifierBuilder> refactoringLegacyMemberIdentifierBuilders ;
		private XEnum<? extends PersistenceRefactoringMemberIdentifierBuilder> refactoringCurrentMemberIdentifierBuilders;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		protected Default(final Class<D> dataType)
		{
			super();
			this.dataType = dataType;
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
		public Class<D> dataType()
		{
			return this.dataType;
		}
		
		@Override
		public PersistenceFoundation.Default<D, F> Clone()
		{
			return new PersistenceFoundation.Default<>(this.dataType);
		}

		
		///////////////////////////////////////////////////////////////////////////
		// property getters and related convenience methods //
		/////////////////////////////////////////////////////
		
		@Override
		public XMap<Class<?>, PersistenceTypeHandler<D, ?>> customTypeHandlers()
		{
			return this.customTypeHandlers;
		}

		@Override
		public PersistenceCustomTypeHandlerRegistryEnsurer<D> customTypeHandlerRegistryEnsurer()
		{
			return this.customTypeHandlerRegistryEnsurer;
		}
		
		@Override
		public synchronized F registerCustomTypeHandlers(
			final HashTable<Class<?>, PersistenceTypeHandler<D, ?>> customTypeHandlers
		)
		{
			this.customTypeHandlers.putAll(customTypeHandlers);
			return this.$();
		}
		
		@Override
		@SuppressWarnings("unchecked")
		public synchronized F registerCustomTypeHandlers(
			 final PersistenceTypeHandler<D, ?>... customTypeHandlers
		)
		{
			for(final PersistenceTypeHandler<D, ?> customTypeHandler : customTypeHandlers)
			{
				this.registerCustomTypeHandler(customTypeHandler);
			}
			
			return this.$();
		}
		
		@Override
		public synchronized F registerCustomTypeHandlers(
			 final Iterable<? extends PersistenceTypeHandler<D, ?>> customTypeHandlers
		)
		{
			for(final PersistenceTypeHandler<D, ?> customTypeHandler : customTypeHandlers)
			{
				this.registerCustomTypeHandler(customTypeHandler);
			}
			
			return this.$();
		}
		
		@Override
		public synchronized F registerCustomTypeHandler(
			final PersistenceTypeHandler<D, ?> customTypeHandler
		)
		{
			this.customTypeHandlers.put(customTypeHandler.type(), customTypeHandler);
			return this.$();
		}
		
		@Override
		public XMap<Class<?>, PersistenceTypeInstantiator<D, ?>> customTypeInstantiators()
		{
			return this.customTypeInstantiators;
		}
		
		@Override
		public synchronized <T> F registerCustomInstantiator(
			final Class<T>                          type            ,
			final PersistenceTypeInstantiator<D, T> typeInstantiator
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
		
		protected Reference<PersistenceTypeHandlerManager<D>> referenceTypeHandlerManager()
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
		public PersistenceObjectManager<D> getObjectManager()
		{
			if(this.objectManager == null)
			{
				this.objectManager = this.dispatch(this.ensureObjectManager());
			}
			
			return this.objectManager;
		}

		@Override
		public PersistenceTypeHandlerManager<D> getTypeHandlerManager()
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
		public PersistenceContextDispatcher<D> getContextDispatcher()
		{
			if(this.contextDispatcher == null)
			{
				this.contextDispatcher = this.dispatch(this.ensureContextDispatcher());
			}
			
			return this.contextDispatcher;
		}

		@Override
		public PersistenceStorer.Creator<D> getStorerCreator()
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
		public PersistenceLoader.Creator<D> getBuilderCreator()
		{
			if(this.builderCreator == null)
			{
				this.builderCreator = this.dispatch(this.ensureBuilderCreator());
			}
			
			return this.builderCreator;
		}
		
//		@Override
//		public ObjectSwizzling getObjectRetriever()
//		{
//			if(this.objectRetriever == null)
//			{
//				this.objectRetriever = this.dispatch(this.ensureObjectRetriever());
//			}
//
//			return this.objectRetriever;
//		}

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
		public PersistenceTarget<D> getPersistenceTarget()
		{
			if(this.target == null)
			{
				this.target = this.dispatch(this.ensurePersistenceTarget());
			}
			
			return this.target;
		}

		@Override
		public PersistenceSource<D> getPersistenceSource()
		{
			if(this.source == null)
			{
				this.source = this.dispatch(this.ensurePersistenceSource());
			}
			
			return this.source;
		}

		@Override
		public PersistenceTypeHandlerRegistry<D> getTypeHandlerRegistry()
		{
			if(this.typeHandlerRegistry == null)
			{
				this.typeHandlerRegistry = this.dispatch(this.ensureTypeHandlerRegistry());
			}
			
			return this.typeHandlerRegistry;
		}

		@Override
		public PersistenceTypeHandlerProvider<D> getTypeHandlerProvider()
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
		public PersistenceTypeHandlerEnsurer<D> getTypeHandlerEnsurer()
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
		public PersistenceTypeHandlerCreator<D> getTypeHandlerCreator()
		{
			if(this.typeHandlerCreator == null)
			{
				this.typeHandlerCreator = this.dispatch(this.ensureTypeHandlerCreator());
			}
			
			return this.typeHandlerCreator;
		}

		@Override
		public PersistenceCustomTypeHandlerRegistry<D> getCustomTypeHandlerRegistry()
		{
			if(this.customTypeHandlerRegistry == null)
			{
				this.customTypeHandlerRegistry = this.dispatch(this.ensureCustomTypeHandlerRegistry());
			}
			
			return this.customTypeHandlerRegistry;
		}
		
		@Override
		public PersistenceCustomTypeHandlerRegistryEnsurer<D> getCustomTypeHandlerRegistryEnsurer()
		{
			if(this.customTypeHandlerRegistryEnsurer == null)
			{
				this.customTypeHandlerRegistryEnsurer = this.dispatch(
					this.ensureCustomTypeHandlerRegistryEnsurer(this.$())
				);
			}
			
			return this.customTypeHandlerRegistryEnsurer;
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
		public PersistenceTypeMismatchValidator<D> getTypeMismatchValidator()
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
		public PersistenceRootReferenceProvider<D> getRootReferenceProvider()
		{
			if(this.rootReferenceProvider == null)
			{
				this.rootReferenceProvider = this.dispatch(this.ensureRootReferenceProvider());
			}
			
			return this.rootReferenceProvider;
		}
		
		@Override
		public PersistenceUnreachableTypeHandlerCreator<D> getUnreachableTypeHandlerCreator()
		{
			if(this.unreachableTypeHandlerCreator == null)
			{
				this.unreachableTypeHandlerCreator = this.dispatch(this.ensureUnreachableTypeHandlerCreator());
			}
			
			return this.unreachableTypeHandlerCreator;
		}
		
		@Override
		public PersistenceLegacyTypeMapper<D> getLegacyTypeMapper()
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
		public PersistenceLegacyTypeMappingResultor<D> getLegacyTypeMappingResultor()
		{
			if(this.legacyTypeMappingResultor == null)
			{
				this.legacyTypeMappingResultor = this.dispatch(this.ensureLegacyTypeMappingResultor());
			}
			
			return this.legacyTypeMappingResultor;
		}
		
		@Override
		public PersistenceLegacyTypeHandlerCreator<D> getLegacyTypeHandlerCreator()
		{
			if(this.legacyTypeHandlerCreator == null)
			{
				this.legacyTypeHandlerCreator = this.dispatch(this.ensureLegacyTypeHandlerCreator());
			}
			
			return this.legacyTypeHandlerCreator;
		}
		
		@Override
		public PersistenceLegacyTypeHandlingListener<D> getLegacyTypeHandlingListener()
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
		public PersistenceAbstractTypeHandlerSearcher<D> getAbstractTypeHandlerSearcher()
		{
			if(this.abstractTypeHandlerSearcher == null)
			{
				this.abstractTypeHandlerSearcher = this.dispatch(this.ensureAbstractTypeHandlerSearcher());
			}
			
			return this.abstractTypeHandlerSearcher;
		}

		@Override
		public PersistenceRootsProvider<D> getRootsProvider()
		{
			if(this.rootsProvider == null)
			{
				this.rootsProvider = this.dispatch(this.ensureRootsProvider());
			}
			return this.rootsProvider;
		}
		
		@Override
		public PersistenceInstantiator<D> getInstantiator()
		{
			// this is just a getter, not an on demand provider method. See #getInstantiatorProvider for that.
			return this.instantiator;
		}
		
		@Override
		public PersistenceTypeInstantiatorProvider<D> getInstantiatorProvider()
		{
			if(this.instantiatorProvider == null)
			{
				this.instantiatorProvider = this.dispatch(this.ensureInstantiatorProvider());
			}
			
			return this.instantiatorProvider;
		}
		
		@Override
		public final PersistenceStorer.CreationObserver getStorerCreationObserver()
		{
			if(this.liveStorerRegistry == null)
			{
				this.liveStorerRegistry = this.dispatch(this.ensureStorerCreationObserver());
			}

			return this.liveStorerRegistry;
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
			final PersistenceObjectManager<D> objectManager
		)
		{
			this.objectManager = objectManager;
			return this.$();
		}

		@Override
		public F setStorerCreator(
			final PersistenceStorer.Creator<D> storerCreator
		)
		{
			this.storerCreator = storerCreator;
			return this.$();
		}

		@Override
		public F setTypeHandlerCreatorLookup(
			final PersistenceTypeHandlerEnsurer<D> typeHandlerCreatorLookup
		)
		{
			this.typeHandlerEnsurer = typeHandlerCreatorLookup;
			return this.$();
		}
		
		@Override
		public F setTypeHandlerCreator(
			final PersistenceTypeHandlerCreator<D> typeHandlerCreator
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
			final PersistenceTypeHandlerManager<D> typeHandlerManager
		)
		{
			this.internalSetTypeHandlerManager(typeHandlerManager);
			return this.$();
		}
		
		private void internalSetTypeHandlerManager(
			final PersistenceTypeHandlerManager<D> typeHandlerManager
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
			final PersistenceContextDispatcher<D> contextDispatcher
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
			final PersistenceTypeHandlerRegistry<D> typeHandlerRegistry
		)
		{
			this.typeHandlerRegistry = typeHandlerRegistry;
			return this.$();
		}

		@Override
		public F setTypeHandlerProvider(
			final PersistenceTypeHandlerProvider<D> typeHandlerProvider
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
			final PersistenceLoader.Creator<D> builderCreator
		)
		{
			this.builderCreator = builderCreator;
			return this.$();
		}

//		@Override
//		public F setObjectRetriever(final ObjectSwizzling objectRetriever)
//		{
//			this.objectRetriever = objectRetriever;
//			return this.$();
//		}
		
		@Override
		public F setPersister(final Persister persister)
		{
			this.persister = persister;
			return this.$();
		}

		@Override
		public F setPersistenceTarget(
			final PersistenceTarget<D> target
		)
		{
			this.target = target;
			return this.$();
		}

		@Override
		public F setPersistenceSource(
			final PersistenceSource<D> source
		)
		{
			this.source = source;
			return this.$();
		}
		
		@Override
		public F setPersistenceChannel(final PersistenceChannel<D> persistenceChannel)
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
		public F setTypeMismatchValidator(
			final PersistenceTypeMismatchValidator<D> typeMismatchValidator
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
		public F setRootReferenceProvider(final PersistenceRootReferenceProvider<D> rootReferenceProvider)
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
			final PersistenceAbstractTypeHandlerSearcher<D> abstractTypeHandlerSearcher
		)
		{
			this.abstractTypeHandlerSearcher = abstractTypeHandlerSearcher;
			return this.$();
		}

		@Override
		public F setRootsProvider(
			final PersistenceRootsProvider<D> rootsProvider
		)
		{
			this.rootsProvider = rootsProvider;
			return this.$();
		}
		
		@Override
		public F setUnreachableTypeHandlerCreator(
			final PersistenceUnreachableTypeHandlerCreator<D> unreachableTypeHandlerCreator
		)
		{
			this.unreachableTypeHandlerCreator = unreachableTypeHandlerCreator;
			return this.$();
		}
		
		@Override
		public F setLegacyTypeMapper(
			final PersistenceLegacyTypeMapper<D> legacyTypeMapper
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
			final PersistenceLegacyTypeMappingResultor<D> legacyTypeMappingResultor
		)
		{
			this.legacyTypeMappingResultor = legacyTypeMappingResultor;
			return this.$();
		}
		
		@Override
		public F setLegacyTypeHandlerCreator(
			final PersistenceLegacyTypeHandlerCreator<D> legacyTypeHandlerCreator
		)
		{
			this.legacyTypeHandlerCreator = legacyTypeHandlerCreator;
			return this.$();
		}
		
		@Override
		public F setLegacyTypeHandlingListener(
			final PersistenceLegacyTypeHandlingListener<D> legacyTypeHandlingListener
		)
		{
			this.legacyTypeHandlingListener = legacyTypeHandlingListener;
			return this.$();
		}
		
		@Override
		public F setInstantiator(final PersistenceInstantiator<D> instantiator)
		{
			this.instantiator = instantiator;
			return this.$();
		}
		
		@Override
		public F setInstantiatorProvider(final PersistenceTypeInstantiatorProvider<D> instantiatorProvider)
		{
			this.instantiatorProvider = instantiatorProvider;
			return this.$();
		}
		
		@Override
		public F setStorerCreationObserver(final PersistenceStorer.CreationObserver liveStorerRegistry)
		{
			this.liveStorerRegistry = liveStorerRegistry;
			return this.$();
		}
		
		@Override
		public F setTargetByteOrder(final ByteOrder targetByteOrder)
		{
			this.targetByteOrder = targetByteOrder;
			return this.$();
		}
				
		@Override
		public F setCustomTypeHandlerRegistryEnsurer(
			final PersistenceCustomTypeHandlerRegistryEnsurer<D> customTypeHandlerRegistryEnsurer
		)
		{
			this.customTypeHandlerRegistryEnsurer = customTypeHandlerRegistryEnsurer;
			
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

		protected PersistenceObjectManager<D> ensureObjectManager()
		{
			final PersistenceObjectManager<D> newObjectManager = PersistenceObjectManager.New(
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

		protected PersistenceTypeHandlerManager<D> ensureTypeHandlerManager()
		{
			final PersistenceTypeHandlerManager<D> newTypeHandlerManager =
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
		
		protected PersistenceContextDispatcher<D> ensureContextDispatcher()
		{
			return PersistenceContextDispatcher.PassThrough();
		}

		protected PersistenceRegisterer.Creator ensureRegistererCreator()
		{
			return new PersistenceRegisterer.Default.Creator();
		}

		protected PersistenceTypeHandlerRegistry<D> ensureTypeHandlerRegistry()
		{
			// note: sub class should/must register native type handlers in an overridden version of this method
			final PersistenceTypeHandlerRegistry<D> newTypeHandlerRegistry =
				new PersistenceTypeHandlerRegistry.Default<>(this.getTypeRegistry())
			;
			return newTypeHandlerRegistry;
		}

		protected PersistenceTypeHandlerProvider<D> ensureTypeHandlerProvider()
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
				PersistenceTypeDictionaryExporter.New(
					this.getTypeDictionaryAssembler(),
					this.getTypeDictionaryStorer()
				)
			;
			return newTypeDictionaryExporter;
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
			// must always resolve types using that ClassCloader with which the initialization happned (this class's).
			return ClassLoaderProvider.New(
				this.getClass().getClassLoader()
			);
		}

		protected PersistenceTypeHandlerEnsurer<D> ensureTypeHandlerEnsurer()
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

		protected PersistenceTypeDictionaryBuilder ensureTypeDictionaryBuilder()
		{
			return PersistenceTypeDictionaryBuilder.New(
				this.getTypeDictionaryCreator(),
				this.getTypeDefinitionCreator(),
				this.getTypeDescriptionResolverProvider()
			);
		}
		
		protected PersistenceTypeDictionaryCompiler ensureTypeDictionaryCompiler()
		{
			return PersistenceTypeDictionaryCompiler.New(
				this.getTypeDictionaryParser() ,
				this.getTypeDictionaryBuilder()
			);
		}
		
		protected PersistenceTypeMismatchValidator<D> ensureTypeMismatchValidator()
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
		
		protected PersistenceUnreachableTypeHandlerCreator<D> ensureUnreachableTypeHandlerCreator()
		{
			return PersistenceUnreachableTypeHandlerCreator.New();
		}
		
		protected PersistenceLegacyTypeMapper<D> ensureLegacyTypeMapper()
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
				
		protected PersistenceLegacyTypeMappingResultor<D> ensureLegacyTypeMappingResultor()
		{
			return LoggingLegacyTypeMappingResultor.New(
				PersistenceLegacyTypeMappingResultor.New()
			);
		}
		
		protected PersistenceLegacyTypeHandlerCreator<D> ensureLegacyTypeHandlerCreator()
		{
			throw new MissingFoundationPartException(PersistenceLegacyTypeHandlerCreator.class);
		}
		
		protected PersistenceLegacyTypeHandlingListener<D> ensureLegacyTypeHandlingListener()
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

		protected PersistenceStorer.Creator<D> ensureStorerCreator()
		{
			throw new MissingFoundationPartException(PersistenceStorer.Creator.class);
		}

		protected PersistenceLoader.Creator<D> ensureBuilderCreator()
		{
			throw new MissingFoundationPartException(PersistenceLoader.Creator.class);
		}
		
		protected PersistenceTarget<D> ensurePersistenceTarget()
		{
			throw new MissingFoundationPartException(PersistenceTarget.class);
		}

		protected PersistenceSource<D> ensurePersistenceSource()
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

		protected PersistenceTypeHandlerCreator<D> ensureTypeHandlerCreator()
		{
			throw new MissingFoundationPartException(PersistenceTypeHandlerCreator.class);
		}

		protected PersistenceCustomTypeHandlerRegistry<D> ensureCustomTypeHandlerRegistry()
		{
			throw new MissingFoundationPartException(PersistenceCustomTypeHandlerRegistry.class);
		}
		
		protected PersistenceCustomTypeHandlerRegistryEnsurer<D> ensureCustomTypeHandlerRegistryEnsurer(
			final F foundation
		)
		{
			// ensure the ensurer! Aww... snap.
			throw new MissingFoundationPartException(PersistenceCustomTypeHandlerRegistryEnsurer.class);
		}

		protected PersistenceFieldLengthResolver ensureFieldFixedLengthResolver()
		{
			throw new MissingFoundationPartException(PersistenceFieldLengthResolver.class);
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
		
		protected PersistenceRootReferenceProvider<D> ensureRootReferenceProvider()
		{
			throw new MissingFoundationPartException(PersistenceRootReferenceProvider.class);
		}
		
		protected PersistenceRootsProvider<D> ensureRootsProviderInternal()
		{
			throw new MissingFoundationPartException(PersistenceRootsProvider.class);
		}

		protected PersistenceRootsProvider<D> ensureRootsProvider()
		{
			final PersistenceRootsProvider<D> rootsProvider = this.ensureRootsProviderInternal();
			rootsProvider.registerRootsTypeHandlerCreator(
				this.getCustomTypeHandlerRegistry(),
				this.getObjectRegistry()
			);
			
			return rootsProvider;
		}
		
		protected PersistenceInstantiator<D> ensureInstantiator()
		{
			return this.instantiator != null
				? this.instantiator
				: PersistenceInstantiator.New()
			;
		}
		
		protected PersistenceTypeInstantiatorProvider<D> ensureInstantiatorProvider()
		{
			// empty table check done inside (constructor method concern)
			return PersistenceTypeInstantiatorProvider.New(
				this.customTypeInstantiators,
				this.ensureInstantiator()
			);
		}
		
		protected PersistenceStorer.CreationObserver ensureStorerCreationObserver()
		{
			// may NOT register storers without storage layer since they would never get cleared!
			return PersistenceStorer.CreationObserver::noOp;
		}
		
		protected LambdaTypeRecognizer ensureLambdaTypeRecognizer()
		{
			return LambdaTypeRecognizer.New();
		}
		
		protected PersistenceAbstractTypeHandlerSearcher<D> ensureAbstractTypeHandlerSearcher()
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
		
		protected ObjectSwizzling ensureObjectRetriever()
		{
			// null by default, then the persistenceManager itself is the persister.
			return null;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods // (with logic worth mentioning)
		////////////

		@Override
		public void executeTypeHandlerRegistration(final PersistenceTypeHandlerRegistration<D> typeHandlerRegistration)
		{
			typeHandlerRegistration.registerTypeHandlers(
				this.getCustomTypeHandlerRegistry(),
				this.getSizedArrayLengthController()
			);
		}
		
		@Override
		public PersistenceManager<D> createPersistenceManager()
		{
			final PersistenceTypeHandlerManager<D> typeHandlerManager = this.getTypeHandlerManager();
			typeHandlerManager.initialize(); // initialize type handlers (i.e. import/validate type dictionary)

			final PersistenceManager<D> newPersistenceManager = PersistenceManager.New(
				this.getObjectRegistry(),
				this.getObjectManager(),
				typeHandlerManager,
				this.getContextDispatcher(),
				this.getStorerCreator(),
				this.getBuilderCreator(),
				this.getRegistererCreator(),
//				this.getObjectRetriever(),
				this.getPersister(),
				this.getPersistenceTarget(),
				this.getPersistenceSource(),
				this.ensureStorerCreationObserver(),
				this.getBufferSizeProvider(),
				this.getTargetByteOrder()
			);
			return newPersistenceManager;
		}

	}

}

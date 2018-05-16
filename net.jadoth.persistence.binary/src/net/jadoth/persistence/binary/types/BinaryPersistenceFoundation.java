package net.jadoth.persistence.binary.types;

import net.jadoth.functional.InstanceDispatcherLogic;
import net.jadoth.persistence.binary.internal.BinaryTypeDictionaryProviderDefaulting;
import net.jadoth.persistence.types.BufferSizeProvider;
import net.jadoth.persistence.types.PersistenceCustomTypeHandlerRegistry;
import net.jadoth.persistence.types.PersistenceFieldLengthResolver;
import net.jadoth.persistence.types.PersistenceFoundation;
import net.jadoth.persistence.types.PersistenceLoader;
import net.jadoth.persistence.types.PersistenceManager;
import net.jadoth.persistence.types.PersistenceRegisterer;
import net.jadoth.persistence.types.PersistenceSource;
import net.jadoth.persistence.types.PersistenceStorer;
import net.jadoth.persistence.types.PersistenceTarget;
import net.jadoth.persistence.types.PersistenceTypeDictionaryAssembler;
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
import net.jadoth.swizzling.types.SwizzleObjectIdProvider;
import net.jadoth.swizzling.types.SwizzleObjectManager;
import net.jadoth.swizzling.types.SwizzleRegistry;
import net.jadoth.swizzling.types.SwizzleTypeIdProvider;
import net.jadoth.swizzling.types.SwizzleTypeManager;



/**
 * Factory and master instance type for assembling and binary persistence layer.
 *
 * @author Thomas Muenz
 */
public interface BinaryPersistenceFoundation extends PersistenceFoundation<Binary>
{
	///////////////////////////////////////////////////////////////////////////
	// setters          //
	/////////////////////

	@Override
	public BinaryPersistenceFoundation setInstanceDispatcher(InstanceDispatcherLogic instanceDispatcher);

	@Override
	public BinaryPersistenceFoundation setObjectManager(SwizzleObjectManager objectManager);

	@Override
	public BinaryPersistenceFoundation setStorerCreator(PersistenceStorer.Creator<Binary> persisterCreator);

	@Override
	public BinaryPersistenceFoundation setTypeHandlerManager(PersistenceTypeHandlerManager<Binary> typeHandlerManager);

	@Override
	public BinaryPersistenceFoundation setObjectIdProvider(SwizzleObjectIdProvider oidProvider);

	@Override
	public BinaryPersistenceFoundation setTypeIdProvider(SwizzleTypeIdProvider tidProvider);

	@Override
	public BinaryPersistenceFoundation setTypeManager(SwizzleTypeManager typeManager);

	@Override
	public BinaryPersistenceFoundation setSwizzleRegistry(SwizzleRegistry swizzleRegistry);

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
	public BinaryPersistenceFoundation setTypeDictionaryProvider(PersistenceTypeDictionaryProvider typeDictionaryProvider);

	@Override
	public BinaryPersistenceFoundation setTypeDictionaryExporter(PersistenceTypeDictionaryExporter typeDictionaryExporter);

	@Override
	public BinaryPersistenceFoundation setTypeDictionaryParser(PersistenceTypeDictionaryParser typeDictionaryParser);

	@Override
	public BinaryPersistenceFoundation setTypeDictionaryLoader(PersistenceTypeDictionaryLoader typeDictionaryLoader);

	@Override
	public BinaryPersistenceFoundation setTypeDictionaryAssembler(PersistenceTypeDictionaryAssembler typeDictionaryAssembler);

	@Override
	public BinaryPersistenceFoundation setTypeDictionaryStorer(PersistenceTypeDictionaryStorer typeDictionaryStorer);

	@Override
	public BinaryPersistenceFoundation setTypeHandlerCreatorLookup(PersistenceTypeHandlerEnsurer<Binary> typeHandlerCreatorLookup);
	
	@Override
	public BinaryPersistenceFoundation setTypeHandlerCreator(PersistenceTypeHandlerCreator<Binary> typeHandlerCreator);

	@Override
	public BinaryPersistenceFoundation setBufferSizeProvider(BufferSizeProvider bufferSizeProvider);

	@Override
	public BinaryPersistenceFoundation setTypeEvaluatorPersistable(PersistenceTypeEvaluator getTypeEvaluatorPersistable);

	@Override
	public BinaryPersistenceFoundation setTypeEvaluatorTypeIdMappable(PersistenceTypeEvaluator getTypeEvaluatorTypeIdMappable);

	@Override
	public BinaryPersistenceFoundation setFieldFixedLengthResolver(PersistenceFieldLengthResolver resolver);

	@Override
	public <H extends PersistenceTypeDictionaryLoader & PersistenceTypeDictionaryStorer>
	BinaryPersistenceFoundation setDictionaryStorage(H typeDictionaryStorageHandler);

	@Override
	public <P extends SwizzleTypeIdProvider & SwizzleObjectIdProvider>
	BinaryPersistenceFoundation setSwizzleIdProvider(P swizzleTypeIdProvider);

	@Override
	public <S extends PersistenceTarget<Binary> & PersistenceSource<Binary>>
	BinaryPersistenceFoundation setPersistenceStorage(S persistenceStorage);

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
		public BinaryPersistenceFoundation setTypeDictionaryExporter(
			final PersistenceTypeDictionaryExporter typeDictionaryExporter
		)
		{
			super.setTypeDictionaryExporter(typeDictionaryExporter);
			return this;
		}

		@Override
		public BinaryPersistenceFoundation setTypeDictionaryParser(
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
		BinaryPersistenceFoundation setDictionaryStorage(final H typeDictionaryStorageHandler)
		{
			super.setTypeDictionaryLoader(typeDictionaryStorageHandler);
			super.setTypeDictionaryStorer(typeDictionaryStorageHandler);
			return this;
		}

		@Override
		public <S extends PersistenceTarget<Binary> & PersistenceSource<Binary>>
		BinaryPersistenceFoundation setPersistenceStorage(final S persistenceStorage)
		{
			super.setPersistenceSource(persistenceStorage);
			super.setPersistenceTarget(persistenceStorage);
			return this;
		}

		@Override
		public <P extends SwizzleTypeIdProvider & SwizzleObjectIdProvider>
		BinaryPersistenceFoundation.Implementation setSwizzleIdProvider(final P swizzleTypeIdProvider)
		{
			this.setTypeIdProvider    (swizzleTypeIdProvider);
			this.setObjectIdProvider  (swizzleTypeIdProvider);
			return this;
		}

		@Override
		public BinaryPersistenceFoundation.Implementation setBufferSizeProvider(
			final BufferSizeProvider bufferSizeProvider
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
		protected BinaryTypeHandlerEnsurer createTypeHandlerCreatorLookup()
		{
			/* note:
			 * registry is enough for native class type handler,
			 * type manager would cause initializer loop.
			 * Maybe the typing should be a little strengthened for this case
			 */
			return new BinaryTypeHandlerEnsurer.Implementation(
				this.getTypeHandlerCreator(),
				this.getCustomTypeHandlerRegistry(),
				this.getTypeHandlerRegistry()
			);
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
			// (18.10.2013 TM)FIXME: PersistenceRootResolver plug in here somewhere
			return BinaryPersistence.createDefaultCustomTypeHandlerRegistry();
		}

		@Override
		protected PersistenceTypeDictionaryProvider createTypeDictionaryProvider()
		{
			// pretty cool usage of factory architecture :D, hooking in a binary-level default mechanism
			return new BinaryTypeDictionaryProviderDefaulting(super.createTypeDictionaryProvider());
		}

		@Override
		protected BinaryFieldLengthResolver createFieldFixedLengthResolver()
		{
			return BinaryPersistence.createFieldLengthResolver();
		}

	}

}

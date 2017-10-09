package net.jadoth.persistence.binary.types;

import net.jadoth.functional.Dispatcher;
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
import net.jadoth.persistence.types.PersistenceTypeHandlerEnsurer;
import net.jadoth.persistence.types.PersistenceTypeHandlerManager;
import net.jadoth.persistence.types.PersistenceTypeHandlerProvider;
import net.jadoth.persistence.types.PersistenceTypeHandlerRegistry;
import net.jadoth.persistence.types.PersistenceTypeSovereignty;
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
	public BinaryPersistenceFoundation setInstanceDispatcher(Dispatcher instanceDispatcher);

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
	public BinaryPersistenceFoundation setTypeSovereignty(PersistenceTypeSovereignty typeSovereignty);

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
		// constructors //
		/////////////////

		protected Implementation()
		{
			super();
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// setters          //
		/////////////////////

		@Override
		public BinaryPersistenceFoundation.Implementation setTypeDictionaryAssembler(
			final PersistenceTypeDictionaryAssembler typeDictionaryAssembler
		)
		{
			this.internalSetTypeDictionaryAssembler(typeDictionaryAssembler);
			return this;
		}

		@Override
		public BinaryPersistenceFoundation.Implementation setTypeDictionaryStorer(
			final PersistenceTypeDictionaryStorer typeDictionaryStorer
		)
		{
			this.internalSetTypeDictionaryStorer(typeDictionaryStorer);
			return this;
		}

		@Override
		public BinaryPersistenceFoundation.Implementation setTypeDictionaryProvider(
			final PersistenceTypeDictionaryProvider typeDictionaryProvider
		)
		{
			this.internalSetTypeDictionaryProvider(typeDictionaryProvider);
			return this;
		}

		@Override
		public BinaryPersistenceFoundation.Implementation setTypeDictionaryManager(
			final PersistenceTypeDictionaryManager typeDictionaryManager
		)
		{
			this.internalSetTypeDictionaryManager(typeDictionaryManager);
			return this;
		}

		@Override
		public BinaryPersistenceFoundation.Implementation setTypeSovereignty(
			final PersistenceTypeSovereignty typeSovereignty
		)
		{
			this.internalSetTypeSovereignty(typeSovereignty);
			return this;
		}

		@Override
		public BinaryPersistenceFoundation.Implementation setInstanceDispatcher(final Dispatcher instanceDispatcher)
		{
			this.internalSetDispatcher(instanceDispatcher);
			return this;
		}

		@Override
		public BinaryPersistenceFoundation.Implementation setObjectManager(final SwizzleObjectManager objectManager)
		{
			this.internalSetObjectManager(objectManager);
			return this;
		}

		@Override
		public BinaryPersistenceFoundation.Implementation setSwizzleRegistry(final SwizzleRegistry swizzleRegistry)
		{
			this.internalSetSwizzleRegistry(swizzleRegistry);
			return this;
		}

		@Override
		public BinaryPersistenceFoundation.Implementation setStorerCreator(
			final PersistenceStorer.Creator<Binary> persisterCreator
		)
		{
			this.internalSetPersisterCreator(persisterCreator);
			return this;
		}

		@Override
		public BinaryPersistenceFoundation.Implementation setTypeHandlerManager(
			final PersistenceTypeHandlerManager<Binary> typeHandlerManager
		)
		{
			this.internalSetTypeHandlerManager(typeHandlerManager);
			return this;
		}

		@Override
		public BinaryPersistenceFoundation.Implementation setObjectIdProvider(final SwizzleObjectIdProvider oidProvider)
		{
			this.internalSetOidProvider(oidProvider);
			return this;
		}

		@Override
		public BinaryPersistenceFoundation.Implementation setTypeIdProvider(final SwizzleTypeIdProvider tidProvider)
		{
			this.internalSetTidProvider(tidProvider);
			return this;
		}

		@Override
		public BinaryPersistenceFoundation.Implementation setTypeManager(final SwizzleTypeManager typeManager)
		{
			this.internalSetTypeManager(typeManager);
			return this;
		}

		@Override
		public BinaryPersistenceFoundation.Implementation setTypeHandlerRegistry(
			final PersistenceTypeHandlerRegistry<Binary> typeHandlerRegistry
		)
		{
			this.internalSetTypeHandlerRegistry(typeHandlerRegistry);
			return this;
		}

		@Override
		public BinaryPersistenceFoundation.Implementation setTypeHandlerProvider(
			final PersistenceTypeHandlerProvider<Binary> typeHandlerProvider
		)
		{
			this.internalSetTypeHandlerProvider(typeHandlerProvider);
			return this;
		}

		@Override
		public BinaryPersistenceFoundation.Implementation setRegistererCreator(
			final PersistenceRegisterer.Creator registererCreator
		)
		{
			this.internalSetRegistererCreator(registererCreator);
			return this;
		}

		@Override
		public BinaryPersistenceFoundation.Implementation setBuilderCreator(
			final PersistenceLoader.Creator<Binary> builderCreator
		)
		{
			this.internalSetBuilderCreator(builderCreator);
			return this;
		}

		@Override
		public BinaryPersistenceFoundation.Implementation setPersistenceTarget(
			final PersistenceTarget<Binary> target
		)
		{
			this.internalSetTarget(target);
			return this;
		}

		@Override
		public BinaryPersistenceFoundation.Implementation setPersistenceSource(
			final PersistenceSource<Binary> source
		)
		{
			this.internalSetSource(source);
			return this;
		}

		@Override
		public BinaryPersistenceFoundation setTypeDictionaryExporter(
			final PersistenceTypeDictionaryExporter typeDictionaryExporter
		)
		{
			this.internalSetTypeDictionaryExporter(typeDictionaryExporter);
			return this;
		}

		@Override
		public BinaryPersistenceFoundation setTypeDictionaryParser(
			final PersistenceTypeDictionaryParser typeDictionaryParser
		)
		{
			this.internalSetTypeDictionaryParser(typeDictionaryParser);
			return this;
		}

		@Override
		public BinaryPersistenceFoundation setTypeDictionaryLoader(
			final PersistenceTypeDictionaryLoader typeDictionaryLoader
		)
		{
			this.internalSetTypeDictionaryLoader(typeDictionaryLoader);
			return this;
		}

		@Override
		public <H extends PersistenceTypeDictionaryLoader & PersistenceTypeDictionaryStorer>
		BinaryPersistenceFoundation setDictionaryStorage(final H typeDictionaryStorageHandler)
		{
			this.internalSetTypeDictionaryLoader(typeDictionaryStorageHandler);
			this.internalSetTypeDictionaryStorer(typeDictionaryStorageHandler);
			return this;
		}

		@Override
		public <S extends PersistenceTarget<Binary> & PersistenceSource<Binary>>
		BinaryPersistenceFoundation setPersistenceStorage(final S persistenceStorage)
		{
			this.internalSetSource(persistenceStorage);
			this.internalSetTarget(persistenceStorage);
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
			this.internalSetBufferSizeProvider(bufferSizeProvider);
			return this;
		}

		@Override
		public BinaryPersistenceFoundation.Implementation setTypeEvaluatorPersistable(
			final PersistenceTypeEvaluator getTypeEvaluatorPersistable
		)
		{
			this.internalSetTypeEvaluatorPersistable(getTypeEvaluatorPersistable);
			return this;
		}

		@Override
		public BinaryPersistenceFoundation.Implementation setTypeEvaluatorTypeIdMappable(
			final PersistenceTypeEvaluator getTypeEvaluatorTypeIdMappable
		)
		{
			this.internalSetTypeEvaluatorTypeIdMappable(getTypeEvaluatorTypeIdMappable);
			return this;
		}

		@Override
		public BinaryPersistenceFoundation.Implementation setFieldFixedLengthResolver(
			final PersistenceFieldLengthResolver resolver
		)
		{
			this.internalSetFieldFixedLengthResolver(resolver);
			return this;
		}
		


		///////////////////////////////////////////////////////////////////////////
		// override methods //
		/////////////////////

		@Override
		protected BinaryStorer.Creator createStorerCreator()
		{
			return new BinaryStorer.CreatorSimple();
		}

		@Override
		protected BinaryLoader.Creator createBuilderCreator()
		{
			return new BinaryLoader.CreatorSimple();
		}

		@Override
		protected PersistenceTypeHandlerEnsurer<Binary> createTypeHandlerEnsurer()
		{
			return BinaryTypeHandlerEnsurer.New(
				this.getCustomTypeHandlerRegistry(),
				this.getTypeAnalyzer(),
				this.getFieldFixedLengthResolver()
			);
		}

		@Override
		protected PersistenceCustomTypeHandlerRegistry<Binary> createCustomTypeHandlerRegistry()
		{
			return BinaryPersistence.createDefaultCustomTypeHandlerRegistry();
		}

		@Override
		protected PersistenceTypeDictionaryProvider createTypeDictionaryProvider()
		{
			return super.createTypeDictionaryProvider();
		}

		@Override
		protected BinaryFieldLengthResolver createFieldFixedLengthResolver()
		{
			return BinaryPersistence.createFieldLengthResolver();
		}
		
	}

}

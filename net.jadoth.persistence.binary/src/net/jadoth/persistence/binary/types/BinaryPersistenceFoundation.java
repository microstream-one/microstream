package net.jadoth.persistence.binary.types;

import net.jadoth.persistence.types.PersistenceCustomTypeHandlerRegistry;
import net.jadoth.persistence.types.PersistenceFoundation;
import net.jadoth.persistence.types.PersistenceLegacyTypeHandlerCreator;
import net.jadoth.persistence.types.PersistenceManager;
import net.jadoth.persistence.types.PersistenceRootsProvider;
import net.jadoth.persistence.types.PersistenceTypeHandlerCreator;


/**
 * Factory and master instance type for assembling and binary persistence layer.
 *
 * @author Thomas Muenz
 */
public interface BinaryPersistenceFoundation<F extends BinaryPersistenceFoundation<?>>
extends PersistenceFoundation<Binary, F>
{
	public BinaryValueTranslatorMappingProvider getValueTranslatorMappingProvider();
	
	public BinaryValueTranslatorProvider getValueTranslatorProvider();
	

	
	public F setValueTranslatorProvider(
		BinaryValueTranslatorProvider valueTranslatorProvider
	);
	
	public F setValueTranslatorMappingProvider(
		BinaryValueTranslatorMappingProvider valueTranslatorMappingProvider
	);


	
	@Override
	public PersistenceManager<Binary> createPersistenceManager();


	
	public static BinaryPersistenceFoundation<?> New()
	{
		return new BinaryPersistenceFoundation.Implementation<>();
	}

	public class Implementation<F extends BinaryPersistenceFoundation.Implementation<?>>
	extends PersistenceFoundation.AbstractImplementation<Binary, F>
	implements BinaryPersistenceFoundation<F>
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
		// getters //
		////////////
		
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
		// setters  //
		/////////////
		
		@Override
		public F setValueTranslatorProvider(
			final BinaryValueTranslatorProvider valueTranslatorProvider
		)
		{
			this.valueTranslatorProvider = valueTranslatorProvider;
			return this.$();
		}
		
		@Override
		public F setValueTranslatorMappingProvider(
			final BinaryValueTranslatorMappingProvider valueTranslatorMapping
		)
		{
			this.valueTranslatorMapping = valueTranslatorMapping;
			return this.$();
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

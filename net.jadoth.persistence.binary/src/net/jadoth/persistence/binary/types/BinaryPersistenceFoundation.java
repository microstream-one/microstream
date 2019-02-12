package net.jadoth.persistence.binary.types;

import java.nio.ByteOrder;

import net.jadoth.collections.EqHashEnum;
import net.jadoth.collections.EqHashTable;
import net.jadoth.collections.types.XEnum;
import net.jadoth.collections.types.XTable;
import net.jadoth.persistence.types.ByteOrderTargeting;
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
extends PersistenceFoundation<Binary, F>, ByteOrderTargeting.Mutable<F>
{

	@Override
	public BinaryPersistenceFoundation<F> Clone();
	
	public XTable<String, BinaryValueSetter> getCustomTranslatorLookup();
	
	public XEnum<BinaryValueTranslatorKeyBuilder> getTranslatorKeyBuilders();
	
	public BinaryValueTranslatorMappingProvider getValueTranslatorMappingProvider();
	
	public BinaryValueTranslatorProvider getValueTranslatorProvider();
		
	
	
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
	
	@Override
	public PersistenceManager<Binary> createPersistenceManager();


	
	public static BinaryPersistenceFoundation<?> New()
	{
		return new BinaryPersistenceFoundation.Implementation<>();
	}

	public class Implementation<F extends BinaryPersistenceFoundation.Implementation<?>>
	extends PersistenceFoundation.Implementation<Binary, F>
	implements BinaryPersistenceFoundation<F>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private XTable<String, BinaryValueSetter>      customTranslatorLookup ;
		private XEnum<BinaryValueTranslatorKeyBuilder> translatorKeyBuilders  ;
		private BinaryValueTranslatorMappingProvider   valueTranslatorMapping ;
		private BinaryValueTranslatorProvider          valueTranslatorProvider;
		private ByteOrder                              targetByteOrder        ;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		protected Implementation()
		{
			super();
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public BinaryPersistenceFoundation.Implementation<F> Clone()
		{
			return new BinaryPersistenceFoundation.Implementation<>();
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// getters //
		////////////
		
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
		public ByteOrder getTargetByteOrder()
		{
			if(this.targetByteOrder == null)
			{
				this.targetByteOrder = this.dispatch(this.ensureTargetByteOrder());
			}
			
			return this.targetByteOrder;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// setters  //
		/////////////
		
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
		public F setTargetByteOrder(final ByteOrder targetByteOrder)
		{
			this.targetByteOrder = targetByteOrder;
			return this.$();
		}
		
	

		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		protected BinaryStorer.Creator ensureStorerCreator()
		{
			return BinaryStorer.Creator(
				() -> 1,
				this.isByteOrderMismatch()
			);
		}

		@Override
		protected BinaryLoader.Creator ensureBuilderCreator()
		{
			return new BinaryLoader.CreatorSimple(
				this.isByteOrderMismatch()
			);
		}

		@Override
		protected PersistenceTypeHandlerCreator<Binary> ensureTypeHandlerCreator()
		{
			return new BinaryTypeHandlerCreator.Implementation(
				this.getTypeAnalyzer(),
				this.getFieldFixedLengthResolver(),
				this.getReferenceFieldMandatoryEvaluator(),
				this.isByteOrderMismatch()
			);
		}

		@Override
		protected PersistenceCustomTypeHandlerRegistry<Binary> ensureCustomTypeHandlerRegistry()
		{
			return BinaryPersistence.createDefaultCustomTypeHandlerRegistry(
				this.getSizedArrayLengthController()
			);
		}

		@Override
		protected BinaryFieldLengthResolver ensureFieldFixedLengthResolver()
		{
			return BinaryPersistence.createFieldLengthResolver();
		}
		
		@Override
		protected PersistenceRootsProvider<Binary> ensureRootsProviderInternal()
		{
			return BinaryPersistenceRootsProvider.New(
				this.getRootResolver()
			);
		}
		
		@Override
		protected PersistenceLegacyTypeHandlerCreator<Binary> ensureLegacyTypeHandlerCreator()
		{
			return BinaryLegacyTypeHandlerCreator.New(
				this.ensureValueTranslatorProvider(),
				this.getLegacyTypeHandlingListener(),
				this.isByteOrderMismatch()
			);
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
				this.getCustomTranslatorLookup()        ,
				this.getTranslatorKeyBuilders()         ,
				this.getValueTranslatorMappingProvider(),
				this.isByteOrderMismatch()
			);
		}
		
		protected ByteOrder ensureTargetByteOrder()
		{
			return ByteOrder.nativeOrder();
		}
		
	}

}

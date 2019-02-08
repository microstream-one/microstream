package net.jadoth.persistence.binary.types;

import net.jadoth.typing.TypeMapping;


/**
 * Since the value translator lookup might potentially get rather giant in the future, it is wrapped in a trivial
 * on-demand provider to ensure it is really only created (and held in memory forever) if necessary.
 * 
 * @author TM
 */
public interface BinaryValueTranslatorMappingProvider extends BinaryValueTranslatorLookupProvider
{
	@Override
	public TypeMapping<BinaryValueSetter> mapping(boolean switchByteOrder);
	
	
	
	public static BinaryValueTranslatorMappingProvider New()
	{
		return new BinaryValueTranslatorMappingProvider.Implementation();
	}
	
	public final class Implementation implements BinaryValueTranslatorMappingProvider
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private transient TypeMapping<BinaryValueSetter> typeMapping;

		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public synchronized TypeMapping<BinaryValueSetter> mapping(final boolean switchByteOrder)
		{
			if(this.typeMapping == null)
			{
				this.typeMapping = BinaryValueTranslators.createDefaultValueTranslators(switchByteOrder);
			}

			return this.typeMapping;
		}
		
	}
	
}

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
	public TypeMapping<BinaryValueSetter> mapping();
	
	
	
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
		public synchronized TypeMapping<BinaryValueSetter> mapping()
		{
			if(this.typeMapping == null)
			{
				this.typeMapping = BinaryValueTranslators.createDefaultValueTranslators();
			}

			return this.typeMapping;
		}
		
	}
	
}

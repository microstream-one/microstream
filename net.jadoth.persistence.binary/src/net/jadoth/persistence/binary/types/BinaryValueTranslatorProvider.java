package net.jadoth.persistence.binary.types;

import net.jadoth.persistence.types.PersistenceTypeDescriptionMember;

@FunctionalInterface
public interface BinaryValueTranslatorProvider
{
	public BinaryValueSetter provideValueTranslator(
		PersistenceTypeDescriptionMember sourceMember,
		PersistenceTypeDescriptionMember targetMember
	);
	
	
	
	public static BinaryValueTranslatorProvider New()
	{
		return new BinaryValueTranslatorProvider.Implementation();
	}
	
	public final class Implementation implements BinaryValueTranslatorProvider
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Implementation()
		{
			super();
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		@Override
		public final BinaryValueSetter provideValueTranslator(
			final PersistenceTypeDescriptionMember sourceMember,
			final PersistenceTypeDescriptionMember targetMember
		)
		{
			if(sourceMember.isReference())
			{
				return BinaryValueTranslators.provideReferenceValueTranslator(sourceMember, targetMember);
			}

			// identical types use direct copying
			return BinaryValueTranslators.providePrimitiveValueTranslator(sourceMember, targetMember);
		}
	}
	
}

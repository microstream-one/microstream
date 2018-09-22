package net.jadoth.persistence.binary.types;

import net.jadoth.persistence.types.PersistenceTypeDescriptionMember;


public interface BinaryValueTranslatorProvider
{
	public BinaryValueSetter provideReferenceResolver(
		PersistenceTypeDescriptionMember sourceMember,
		PersistenceTypeDescriptionMember targetMember
	);
	
	public BinaryValueSetter providePrimitiveValueTranslator(
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
		public BinaryValueSetter provideReferenceResolver(
			final PersistenceTypeDescriptionMember sourceMember,
			final PersistenceTypeDescriptionMember targetMember
		)
		{
			if(!sourceMember.isReference())
			{
				// (23.09.2018 TM)EXCP: proper exception
				throw new RuntimeException("Cannot resolve non-reference value as a reference: " + sourceMember.uniqueName());
			}
			
			if(targetMember == null)
			{
				return BinaryValueTranslators::skip_long;
			}
			
			if(!targetMember.isReference())
			{
				// (23.09.2018 TM)EXCP: proper exception
				throw new RuntimeException("Cannot resolve a reference into a non-reference field: " + targetMember.uniqueName());
			}
			
			return BinaryPersistence.getSetterReference();
		}
		
		@Override
		public final BinaryValueSetter providePrimitiveValueTranslator(
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

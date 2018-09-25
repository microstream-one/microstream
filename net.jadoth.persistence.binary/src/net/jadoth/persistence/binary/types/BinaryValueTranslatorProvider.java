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
			/* (25.09.2018 TM)FIXME: OGS-3: flexible value translators
			 * Shouldn't there be a even more flexible mapping?
			 * Ammong primitives, but also primitives to non-primitives (Wrappers).
			 * Objects to other objects (e.h. char[] <-> String etc.)
			 * 
			 * There should also be a custom translator registry with the usual translators as defaults.
			 * 
			 * Maybe even with an optional per-TID registry to register very specific translators for just specific
			 * types.
			 * 
			 * What about per target type? Per fieldname?
			 * Maybe a general purpose String-Key registry with the keys being assembled via a certain pattern,
			 * e.g. "TypeId 1000012". The pattern might even be completely application-specific, so no artificial
			 * convention has to be forced upon the developer.
			 * 
			 */
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

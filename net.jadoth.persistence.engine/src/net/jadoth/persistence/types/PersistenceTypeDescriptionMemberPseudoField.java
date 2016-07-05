package net.jadoth.persistence.types;



public interface PersistenceTypeDescriptionMemberPseudoField extends PersistenceTypeDescriptionMember
{
	public abstract class AbstractImplementation
	extends PersistenceTypeDescriptionMember.AbstractImplementation
	implements PersistenceTypeDescriptionMemberPseudoField
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		public AbstractImplementation(
			final String  typeName           ,
			final String  name               ,
			final boolean isReference        ,
			final boolean isPrimitive        ,
			final boolean hasReferences      ,
			final long    persistentMinLength,
			final long    persistentMaxLength
		)
		{
			super(typeName, name, isReference, isPrimitive, false, hasReferences, persistentMinLength, persistentMaxLength);
		}

	}

}

package one.microstream.persistence.types;



public interface PersistenceTypeDescriptionMemberPseudoField extends PersistenceTypeDescriptionMember
{
	public abstract class Abstract
	extends PersistenceTypeDescriptionMember.Abstract
	implements PersistenceTypeDescriptionMemberPseudoField
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		public Abstract(
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

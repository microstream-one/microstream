package one.microstream.persistence.types;



public interface PersistenceTypeDescriptionMemberFieldGeneric extends PersistenceTypeDescriptionMember
{
	public abstract class Abstract
	extends PersistenceTypeDescriptionMember.Abstract
	implements PersistenceTypeDescriptionMemberFieldGeneric
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		public Abstract(
			final String  typeName           ,
			final String  qualifier          ,
			final String  name               ,
			final boolean isReference        ,
			final boolean isPrimitive        ,
			final boolean hasReferences      ,
			final long    persistentMinLength,
			final long    persistentMaxLength
		)
		{
			super(
				typeName           ,
				qualifier          ,
				name               ,
				isReference        ,
				isPrimitive        ,
				false              ,
				hasReferences      ,
				persistentMinLength,
				persistentMaxLength
			);
		}

	}

}

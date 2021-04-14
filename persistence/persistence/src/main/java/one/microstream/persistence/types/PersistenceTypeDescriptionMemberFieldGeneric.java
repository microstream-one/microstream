package one.microstream.persistence.types;



public interface PersistenceTypeDescriptionMemberFieldGeneric extends PersistenceTypeDescriptionMemberField
{
	public abstract class Abstract
	extends PersistenceTypeDescriptionMemberField.Abstract
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
				hasReferences      ,
				persistentMinLength,
				persistentMaxLength
			);
		}

	}

}

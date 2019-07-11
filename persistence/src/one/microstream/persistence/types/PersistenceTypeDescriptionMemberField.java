package one.microstream.persistence.types;

public interface PersistenceTypeDescriptionMemberField extends PersistenceTypeDescriptionMember
{
	@Override
	public String typeName();
	
	/**
	 * A type-internal qualifier to distinct different members with equal "primary" name. E.g. reflection-based
	 * type handling where fields names are only unique in combination with their declaring class.
	 * <p>
	 * May never be {@code null}.
	 * 
	 * @return the member's qualifier string to ensure a unique {@link #identifier()} in a group of member fields.
	 */
	@Override
	public String qualifier();

	/**
	 * The simple or "primary" name of the member. E.g. "lastName".
	 * <p>
	 * May never be {@code null}.
	 * 
	 * @return the member field's simple name.
	 */
	@Override
	public String name();
			
	
	
	public abstract class Abstract
	extends PersistenceTypeDescriptionMember.Abstract
	implements PersistenceTypeDescriptionMemberField
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

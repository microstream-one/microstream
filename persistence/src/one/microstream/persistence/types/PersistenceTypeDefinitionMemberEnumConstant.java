package one.microstream.persistence.types;

import static one.microstream.X.notNull;

public interface PersistenceTypeDefinitionMemberEnumConstant
extends PersistenceTypeDescriptionMemberEnumConstant, PersistenceTypeDefinitionMember
{
	
	public static PersistenceTypeDefinitionMemberEnumConstant New(
		final String name
	)
	{
		return new PersistenceTypeDefinitionMemberEnumConstant.Default(
			notNull(name)
		);
	}

	public class Default
	extends PersistenceTypeDescriptionMemberEnumConstant.Default
	implements PersistenceTypeDefinitionMemberEnumConstant
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected Default(final String enumName)
		{
			super(enumName);
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
				
		@Override
		public final Class<?> type()
		{
			// a enum constant does not have a defined type per se. It's just about validating the field names.
			return null;
		}
		
	}

}

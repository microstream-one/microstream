package net.jadoth.persistence.types;

import net.jadoth.collections.types.XGettingSequence;

@FunctionalInterface
public interface PersistenceTypeDefinitionBuilder
{
	public <T> PersistenceTypeDefinition<T> buildTypeDefinition(
		String                                                       typeName,
		Class<T>                                                     type    ,
		long                                                         typeId ,
		XGettingSequence<? extends PersistenceTypeDescriptionMember> members
	);
	
	
	public static PersistenceTypeDefinitionBuilder.Implementation New()
	{
		return new PersistenceTypeDefinitionBuilder.Implementation();
	}
			
	public final class Implementation implements PersistenceTypeDefinitionBuilder
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
		public <T> PersistenceTypeDefinition<T> buildTypeDefinition(
			final String                                                       typeName,
			final Class<T>                                                     type    ,
			final long                                                         typeId ,
			final XGettingSequence<? extends PersistenceTypeDescriptionMember> members
		)
		{
			return PersistenceTypeDefinition.New(typeName, type, typeId, members);
		}
		
	}
	
}
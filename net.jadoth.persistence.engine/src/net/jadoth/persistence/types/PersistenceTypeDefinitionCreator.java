package net.jadoth.persistence.types;

import net.jadoth.collections.types.XGettingSequence;

@FunctionalInterface
public interface PersistenceTypeDefinitionCreator
{
	public <T> PersistenceTypeDefinition<T> createTypeDefinition(
		String                                                       typeName,
		Class<T>                                                     type    ,
		long                                                         typeId ,
		XGettingSequence<? extends PersistenceTypeDescriptionMember> members
	);
	
	
	
	public static PersistenceTypeDefinitionCreator.Implementation New()
	{
		return new PersistenceTypeDefinitionCreator.Implementation();
	}
			
	public final class Implementation implements PersistenceTypeDefinitionCreator
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
		public <T> PersistenceTypeDefinition<T> createTypeDefinition(
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
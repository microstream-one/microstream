package net.jadoth.persistence.types;

import net.jadoth.collections.types.XGettingEnum;

@FunctionalInterface
public interface PersistenceTypeDefinitionCreator
{
	public <T> PersistenceTypeDefinition<T> createTypeDefinition(
		String                                                   typeName,
		Class<T>                                                 type    ,
		long                                                     typeId ,
		XGettingEnum<? extends PersistenceTypeDescriptionMember> members
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
			final String                                                   typeName,
			final Class<T>                                                 type    ,
			final long                                                     typeId ,
			final XGettingEnum<? extends PersistenceTypeDescriptionMember> members
		)
		{
			return PersistenceTypeDefinition.New(typeName, type, typeId, members);
		}
		
	}
	
}
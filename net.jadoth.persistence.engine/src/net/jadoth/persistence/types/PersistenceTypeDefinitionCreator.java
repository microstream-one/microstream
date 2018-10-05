package net.jadoth.persistence.types;

import net.jadoth.collections.types.XGettingEnum;

@FunctionalInterface
public interface PersistenceTypeDefinitionCreator
{
	public <T> PersistenceTypeDefinition<T> createTypeDefinition(
		long                                                     typeId  ,
		String                                                   typeName,
		Class<T>                                                 type    ,
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
			final long                                                     typeId  ,
			final String                                                   typeName,
			final Class<T>                                                 type    ,
			final XGettingEnum<? extends PersistenceTypeDescriptionMember> members
		)
		{
			return PersistenceTypeDefinition.New(typeId, typeName, type, members);
		}
		
	}
	
}
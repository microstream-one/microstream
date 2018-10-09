package net.jadoth.persistence.types;

import net.jadoth.collections.types.XGettingEnum;

@FunctionalInterface
public interface PersistenceTypeDefinitionCreator
{
	public PersistenceTypeDefinition createTypeDefinition(
		long                                                    typeId         ,
		String                                                  typeName       ,
		String                                                  runtimeTypeName,
		Class<?>                                                runtimeType    ,
		XGettingEnum<? extends PersistenceTypeDefinitionMember> members
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
		public PersistenceTypeDefinition createTypeDefinition(
			final long                                                    typeId         ,
			final String                                                  typeName       ,
			final String                                                  runtimeTypeName,
			final Class<?>                                                runtimeType    ,
			final XGettingEnum<? extends PersistenceTypeDefinitionMember> members
		)
		{
			return PersistenceTypeDefinition.New(typeId, typeName, runtimeTypeName, runtimeType, members);
		}
		
	}
	
}
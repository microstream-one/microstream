package one.microstream.persistence.types;

import one.microstream.collections.types.XGettingEnum;

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
	
	
	
	public static PersistenceTypeDefinitionCreator.Default New()
	{
		return new PersistenceTypeDefinitionCreator.Default();
	}
			
	public final class Default implements PersistenceTypeDefinitionCreator
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default()
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

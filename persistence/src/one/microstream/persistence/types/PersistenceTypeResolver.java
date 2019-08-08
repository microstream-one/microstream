package one.microstream.persistence.types;


public interface PersistenceTypeResolver
{
	public default String substituteClassIdentifierSeparator()
	{
		return Persistence.substituteClassIdentifierSeparator();
	}
	
	public default String deriveTypeName(final Class<?> type)
	{
		return Persistence.derivePersistentTypeName(type, this.substituteClassIdentifierSeparator());
	}
	
	public default Class<?> resolveType(final String typeName)
	{
		return Persistence.resolveType(typeName, this.substituteClassIdentifierSeparator());
	}
	
	public default Class<?> tryResolveType(final String typeName)
	{
		return Persistence.tryResolveType(typeName);
	}
	
	
	public static PersistenceTypeResolver Default()
	{
		return new PersistenceTypeResolver.Default();
	}
	
	public final class Default implements PersistenceTypeResolver
	{
		Default()
		{
			super();
		}

		// well, lol
		
	}
		
}

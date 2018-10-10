package net.jadoth.persistence.types;

@FunctionalInterface
public interface PersistenceTypeResolver
{
	public String resolveRuntimeTypeName(PersistenceTypeDescription typeDescription);
	
	public default Class<?> resolveRuntimeType(final PersistenceTypeDescription typeDescription)
	{
		final String runtimeTypeName = this.resolveRuntimeTypeName(typeDescription);
		return this.resolveType(runtimeTypeName);
	}
	
	public default Class<?> tryResolveRuntimeType(final PersistenceTypeDescription typeDescription)
	{
		final String runtimeTypeName = this.resolveRuntimeTypeName(typeDescription);
		return this.tryResolveType(runtimeTypeName);
	}
	
	public default Class<?> resolveType(final String typeName)
	{
		return Persistence.resolveType(typeName);
	}
	
	public default Class<?> tryResolveType(final String typeName)
	{
		return Persistence.tryResolveType(typeName);
	}
		
}

package net.jadoth.persistence.types;

import net.jadoth.persistence.exceptions.PersistenceExceptionTypeConsistencyDefinitionResolveTypeName;
import net.jadoth.reflect.XReflect;

@FunctionalInterface
public interface PersistenceTypeResolver
{
	public String resolveRuntimeTypeName(PersistenceTypeDescription typeDescription);
	
	public default Class<?> resolveRuntimeType(final PersistenceTypeDescription typeDescription)
	{
		final String runtimeTypeName = this.resolveRuntimeTypeName(typeDescription);
		
		return runtimeTypeName == null
			? null
			: this.resolveType(runtimeTypeName)
		;
	}
	
	public default Class<?> resolveType(final String typeName)
	{
		try
		{
			return XReflect.classForName(typeName);
		}
		catch(final ClassNotFoundException e)
		{
			throw new PersistenceExceptionTypeConsistencyDefinitionResolveTypeName(typeName, e);
		}
	}
		
}

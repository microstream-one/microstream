package net.jadoth.persistence.types;

import net.jadoth.persistence.exceptions.PersistenceExceptionTypeConsistencyDefinitionResolveTypeName;
import net.jadoth.reflect.JadothReflect;

public interface PersistenceTypeResolver
{
	public Class<?> resolveType(String typeName);
	
	
	
	public static PersistenceTypeResolver Failing()
	{
		return new PersistenceTypeResolver.ImplementationFailing();
	}
	
	public static PersistenceTypeResolver Ignoring()
	{
		return new PersistenceTypeResolver.ImplementationIgnoring();
	}
	
	public final class ImplementationFailing implements PersistenceTypeResolver
	{
		@Override
		public Class<?> resolveType(final String typeName) throws PersistenceExceptionTypeConsistencyDefinitionResolveTypeName
		{
			try
			{
				return JadothReflect.classForName(typeName);
			}
			catch(final ClassNotFoundException e)
			{
				throw new PersistenceExceptionTypeConsistencyDefinitionResolveTypeName(typeName, e);
			}
		}
	}
	
	public final class ImplementationIgnoring implements PersistenceTypeResolver
	{
		@Override
		public Class<?> resolveType(final String typeName) throws PersistenceExceptionTypeConsistencyDefinitionResolveTypeName
		{
			try
			{
				return JadothReflect.classForName(typeName);
			}
			catch(final ClassNotFoundException e)
			{
				// intentionally return null
				return null;
			}
		}
	}
	
	
}

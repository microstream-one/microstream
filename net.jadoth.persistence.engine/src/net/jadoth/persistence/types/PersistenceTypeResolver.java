package net.jadoth.persistence.types;

import net.jadoth.persistence.exceptions.PersistenceExceptionTypeConsistencyDefinitionResolveTypeName;
import net.jadoth.reflect.XReflect;

public interface PersistenceTypeResolver
{
	public Class<?> resolveType(PersistenceTypeDescription typeDescription);
	
	
	
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
		public Class<?> resolveType(final PersistenceTypeDescription typeDescription)
			throws PersistenceExceptionTypeConsistencyDefinitionResolveTypeName
		{
			try
			{
				return XReflect.classForName(typeDescription.typeName());
			}
			catch(final ClassNotFoundException e)
			{
				throw new PersistenceExceptionTypeConsistencyDefinitionResolveTypeName(typeDescription.typeName(), e);
			}
		}
	}
	
	public final class ImplementationIgnoring implements PersistenceTypeResolver
	{
		@Override
		public Class<?> resolveType(final PersistenceTypeDescription typeDescription)
		{
			return XReflect.tryClassForName(typeDescription.typeName());
		}
	}
	
}

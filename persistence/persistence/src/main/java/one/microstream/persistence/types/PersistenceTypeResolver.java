package one.microstream.persistence.types;

import static one.microstream.X.notNull;

import one.microstream.reflect.ClassLoaderProvider;
import one.microstream.reflect.XReflect;

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
	
	public default ClassLoader getTypeResolvingClassLoader(final String typeName)
	{
		return XReflect.defaultTypeResolvingClassLoader();
	}
	
	public default Class<?> resolveType(final String typeName)
	{
		return Persistence.resolveType(
			typeName,
			this.getTypeResolvingClassLoader(typeName),
			this.substituteClassIdentifierSeparator()
		);
	}
	
	public default Class<?> tryResolveType(final String typeName)
	{
		return Persistence.tryResolveType(typeName, this.getTypeResolvingClassLoader(typeName));
	}
	
	
	
//	public static PersistenceTypeResolver New()
//	{
//		return New(
//			ClassLoaderProvider.New()
//		);
//	}
	
	public static PersistenceTypeResolver New(final ClassLoaderProvider classLoaderProvider)
	{
		return new PersistenceTypeResolver.Default(
			notNull(classLoaderProvider)
		);
	}
	
	public final class Default implements PersistenceTypeResolver
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final ClassLoaderProvider classLoaderProvider;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default(final ClassLoaderProvider classLoaderProvider)
		{
			super();
			this.classLoaderProvider = classLoaderProvider;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public ClassLoader getTypeResolvingClassLoader(final String typeName)
		{
			return this.classLoaderProvider.provideClassLoader(typeName);
		}
		
	}
			
}

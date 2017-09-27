package net.jadoth.persistence.types;

import net.jadoth.reflect.JadothReflect;

public interface PersistenceTypeDefinitionInitializerProvider
{
	public <T> PersistenceTypeDefinitionInitializer<T> lookupInitializer(String typeName);
	
	
	
	public final class Implementation implements PersistenceTypeDefinitionInitializerProvider
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		final PersistenceTypeHandlerEnsurer<?> typeHandlerEnsurer;
		final PersistenceTypeHandlerManager<?> typeHandlerManager;

		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Implementation(
			final PersistenceTypeHandlerEnsurer<?> typeHandlerEnsurer,
			final PersistenceTypeHandlerManager<?> typeHandlerManager
		)
		{
			super();
			this.typeHandlerEnsurer = typeHandlerEnsurer;
			this.typeHandlerManager = typeHandlerManager;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@SuppressWarnings("unchecked")
		private static <T> Class<T> resolveType(final String typename)
		{
			try
			{
				return (Class<T>)JadothReflect.classForName(typename);
			}
			catch (final ClassNotFoundException e)
			{
				throw new RuntimeException(e); // (30.04.2017 TM)EXCP: proper exception
			}
		}
		
		@Override
		public <T> PersistenceTypeDefinitionInitializer<T> lookupInitializer(final String typename)
		{
			final Class<T> type = resolveType(typename);
			final PersistenceTypeHandler<?, T> typeHandler = this.typeHandlerEnsurer.ensureTypeHandler(type);
						
			throw new net.jadoth.meta.NotImplementedYetError(); // FIXME PersistenceTypeDescription.InitializerLookup#lookupInitializer()
		}
		
		
	}
	
}
package net.jadoth.persistence.types;

import net.jadoth.reflect.JadothReflect;
import net.jadoth.swizzling.types.SwizzleTypeManager;

public interface PersistenceTypeDescriptionInitializerLookup
	{
		public <T> PersistenceTypeDescriptionInitializer<T> lookupInitializer(String typeName);
		
		
		public final class Implementation implements PersistenceTypeDescriptionInitializerLookup
		{
			///////////////////////////////////////////////////////////////////////////
			// instance fields //
			////////////////////
			
			private final PersistenceTypeHandlerEnsurerLookup<?> typeHandlerEnsurerLookup;
			private final SwizzleTypeManager                     typeManager             ;

			
			
			///////////////////////////////////////////////////////////////////////////
			// constructors //
			/////////////////
			
			Implementation(
				final PersistenceTypeHandlerEnsurerLookup<?> typeHandlerEnsurerLookup,
				final SwizzleTypeManager                     typeManager
			)
			{
				super();
				this.typeHandlerEnsurerLookup = typeHandlerEnsurerLookup;
				this.typeManager              = typeManager             ;
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
			public <T> PersistenceTypeDescriptionInitializer<T> lookupInitializer(final String typename)
			{
				final Class<T> type = resolveType(typename);
				
				final PersistenceTypeHandlerEnsurer<?> ensurer = this.typeHandlerEnsurerLookup.lookupEnsurer(type);
				
//				ensurer.ensureTypeHandler(type, 0, this.typeManager)
				
				throw new net.jadoth.meta.NotImplementedYetError(); // FIXME PersistenceTypeDescription.InitializerLookup#lookupInitializer()
			}
			
			
		}
		
	}
package net.jadoth.persistence.types;

import static net.jadoth.Jadoth.notNull;

public interface PersistenceTypeLineageBuilderProvider
{
	public <T> PersistenceTypeLineageBuilder<T> provideTypeLineageBuilder(String typeName);
	
	
	
	public static PersistenceTypeLineageBuilderProvider.Implementation New(
		final PersistenceTypeDefinitionBuilder             typeDefinitionBuilder            ,
		final PersistenceTypeChangeCallback                typeChangeCallback               ,
		final PersistenceTypeDefinitionInitializerProvider typeDefinitionInitializerProvider
	)
	{
		return new PersistenceTypeLineageBuilderProvider.Implementation(
			notNull(typeDefinitionBuilder)            ,
			notNull(typeChangeCallback)               ,
			notNull(typeDefinitionInitializerProvider)
		);
	}
	
	public final class Implementation implements PersistenceTypeLineageBuilderProvider
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		final PersistenceTypeDefinitionBuilder             typeDefinitionBuilder            ;
		final PersistenceTypeChangeCallback                typeChangeCallback               ;
		final PersistenceTypeDefinitionInitializerProvider typeDefinitionInitializerProvider;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Implementation(
			final PersistenceTypeDefinitionBuilder             typeDefinitionBuilder            ,
			final PersistenceTypeChangeCallback                typeChangeCallback               ,
			final PersistenceTypeDefinitionInitializerProvider typeDefinitionInitializerProvider
		)
		{
			super();
			this.typeDefinitionBuilder             = typeDefinitionBuilder            ;
			this.typeChangeCallback                = typeChangeCallback               ;
			this.typeDefinitionInitializerProvider = typeDefinitionInitializerProvider;
		}


		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final <T> PersistenceTypeLineageBuilder<T> provideTypeLineageBuilder(final String typeName)
		{
			final PersistenceTypeDefinitionInitializer<T> tdi =
				this.typeDefinitionInitializerProvider.lookupInitializer(typeName)
			;
			
			return PersistenceTypeLineageBuilder.New(
				this.typeDefinitionBuilder,
				this.typeChangeCallback   ,
				tdi
			);
		}

	}
	
}

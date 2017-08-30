package net.jadoth.persistence.types;

import static net.jadoth.Jadoth.notNull;

import net.jadoth.swizzling.types.SwizzleTypeManager;

public interface PersistenceRuntimeTypeDescriptionProvider
{
	public <T> PersistenceTypeDescription<T> provideRuntimeTypeDescription(
		PersistenceTypeDescriptionLineage<T>         lineage                       ,
		PersistenceTypeDescriptionMismatchHandler<T> typeDescriptionMismatchHandler,
		SwizzleTypeManager                           typeManager
	);
	
	
	public static PersistenceRuntimeTypeDescriptionProvider.Implementation New(
		final PersistenceTypeDescriptionInitializerLookup typeDescriptionInitializerLookup
	)
	{
		return new Implementation(
			notNull(typeDescriptionInitializerLookup)
		);
	}
	
	public final class Implementation implements PersistenceRuntimeTypeDescriptionProvider
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final PersistenceTypeDescriptionInitializerLookup typeDescriptionInitializerLookup;

		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Implementation(	final PersistenceTypeDescriptionInitializerLookup typeDescriptionInitializerLookup	)
		{
			super();
			this.typeDescriptionInitializerLookup = typeDescriptionInitializerLookup;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		private <T> PersistenceTypeDescriptionInitializer<T> provideInitializer(final String typeName)
		{
			final PersistenceTypeDescriptionInitializer<T> initializer =
				this.typeDescriptionInitializerLookup.lookupInitializer(typeName)
			;
			
			if(initializer == null)
			{
				// (27.04.2017 TM)EXCP: proper exception
				throw new RuntimeException(
					"No " + PersistenceTypeDescriptionInitializer.class.getSimpleName()
					+ " found for type " + typeName
				);
			}
			
			return initializer;
		}
		
		@Override
		public <T> PersistenceTypeDescription<T> provideRuntimeTypeDescription(
			final PersistenceTypeDescriptionLineage<T>         lineage                       ,
			final PersistenceTypeDescriptionMismatchHandler<T> typeDescriptionMismatchHandler,
			final SwizzleTypeManager                           typeManager
		)
		{
			final PersistenceTypeDescription<T> latestTypeDescription = lineage.latest();
			
			final PersistenceTypeDescriptionInitializer<T> initializer = this.provideInitializer(
				lineage.typeName()
			);
			
			final Class<T> type          = initializer.type()  ;
			final long     definedTypeId = initializer.typeId();
			
			final PersistenceTypeDescription<T> runtimeTypeDescription;
			if(definedTypeId != 0)
			{
				// intentionally no back-check with the type dictionary here, because the runtime takes precedence.
				typeManager.registerType(definedTypeId, type);
				runtimeTypeDescription = initializer.initialize(definedTypeId, lineage);
			}
			else if(PersistenceTypeDescriptionMember.equalMembers(latestTypeDescription.members(), initializer.members()))
			{
				runtimeTypeDescription = initializer.initialize(latestTypeDescription.typeId(), lineage);
			}
			else
			{
				final long newTypeId = typeManager.ensureTypeId(type);
				runtimeTypeDescription = initializer.initialize(newTypeId, lineage);
				typeDescriptionMismatchHandler.reportTypeMismatch(runtimeTypeDescription, latestTypeDescription);
			}
			
			return runtimeTypeDescription;
		}
		
	}
	
}

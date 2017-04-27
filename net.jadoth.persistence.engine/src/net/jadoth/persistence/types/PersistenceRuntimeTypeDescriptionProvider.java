package net.jadoth.persistence.types;

import net.jadoth.collections.types.XGettingSequence;
import net.jadoth.collections.types.XGettingTable;
import net.jadoth.swizzling.types.SwizzleTypeManager;

public interface PersistenceRuntimeTypeDescriptionProvider
{
	public <T> PersistenceTypeDescription<T> provideRuntimeTypeDescription(
		PersistenceTypeDescription<T>                      latestDictionaryEntry    ,
		XGettingTable<Long, PersistenceTypeDescription<T>> obsoleteDictionaryEntries,
		PersistenceTypeDescriptionMismatchCallback<T>      typeMismatchCallback
	);
	
	
	public final class Implementation implements PersistenceRuntimeTypeDescriptionProvider
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final PersistenceTypeDescription.InitializerLookup typeDescriptionInitializerLookup;
		private final SwizzleTypeManager                           typeManager                     ;

		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Implementation(
			final PersistenceTypeDescription.InitializerLookup typeDescriptionInitializerLookup,
			final SwizzleTypeManager                           typeManager
		)
		{
			super();
			this.typeDescriptionInitializerLookup = typeDescriptionInitializerLookup;
			this.typeManager                      = typeManager                     ;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		private <T> PersistenceTypeDescription.Initializer<T> provideInitializer(final String typeName)
		{
			final PersistenceTypeDescription.Initializer<T> initializer =
				this.typeDescriptionInitializerLookup.lookupInitializer(typeName)
			;
			
			if(initializer == null)
			{
				// (27.04.2017 TM)EXCP: proper exception
				throw new RuntimeException(
					"No " + PersistenceTypeDescription.Initializer.class.getSimpleName()
					+ " found for type " + typeName
				);
			}
			
			return initializer;
		}
		
		private long provideTypeId(final PersistenceTypeDescription.Initializer<?> initializer)
		{
			final Class<?> type = initializer.type();

			// initializer logic might for any reason define a specific type ID.
			long typeId = initializer.typeId();
			
			// intentionally 0 instead of Swizzle.nullId() to indicate JVM-level default value
			if(typeId == 0)
			{
				// if the initializer does not define a specific type ID, get one from the type manager
				typeId = this.typeManager.ensureTypeId(type);
			}
			else
			{
				// if the initializer defines a specific type ID, it has to be registed (and validated)
				this.typeManager.registerType(typeId, type);
			}
			
			return typeId;
		}

		@Override
		public <T> PersistenceTypeDescription<T> provideRuntimeTypeDescription(
			final PersistenceTypeDescription<T>                      latestDictionaryEntry    ,
			final XGettingTable<Long, PersistenceTypeDescription<T>> obsoleteDictionaryEntries,
			final PersistenceTypeDescriptionMismatchCallback<T>      typeMismatchCallback
		)
		{
			final PersistenceTypeDescription.Initializer<T> initializer = this.provideInitializer(
				latestDictionaryEntry.typeName()
			);
			
			final XGettingSequence<? extends PersistenceTypeDescriptionMember> latestEntryMembers =
				latestDictionaryEntry.members()
			;
			
			final XGettingSequence<? extends PersistenceTypeDescriptionMember> runtimeMembers =
				initializer.members()
			;
			
			/*
			 * if the latest entry description and the runtime description match,
			 * then discard the latest entry description and replace it with the runtime description,
			 * initialized with the latest description's type id and the so far determined obsolete descriptions.
			 */
			if(PersistenceTypeDescriptionMember.equalMembers(latestEntryMembers, runtimeMembers))
			{
				return initializer.initialize(latestDictionaryEntry.typeId(), obsoleteDictionaryEntries);
			}
			
			/*
			 * if the descriptions don't match, several things have to be done:
			 * - a new typeId has to be determined and registered
			 * - a runtime TypeDescription has to be created
			 * - the mismatch has to be reported to the caller, e.g. to add the latest desciption to the obsoletes
			 */
			
			final long typeId = this.provideTypeId(initializer);
			
			final PersistenceTypeDescription<T> runtimeTypeDescription = initializer.initialize(
				typeId                   ,
				obsoleteDictionaryEntries
			);
			
			typeMismatchCallback.reportMismatch(latestDictionaryEntry, runtimeTypeDescription);
			
			return runtimeTypeDescription;
		}
		
		
	}
}

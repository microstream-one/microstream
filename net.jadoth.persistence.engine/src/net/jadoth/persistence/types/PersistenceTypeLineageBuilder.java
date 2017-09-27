package net.jadoth.persistence.types;

import static net.jadoth.Jadoth.notNull;

import net.jadoth.collections.types.XGettingSequence;

public interface PersistenceTypeLineageBuilder<T>
{
	public boolean registerTypeDescription(
		long                                                         typeId ,
		XGettingSequence<? extends PersistenceTypeDescriptionMember> members
	);
	
	public PersistenceTypeLineage<T> buildTypeLineage();
	
	
	
	public static <T> PersistenceTypeLineageBuilder.Implementation<T> New(
		final PersistenceTypeDefinitionBuilder        typeDefinitionBuilder            ,
		final PersistenceTypeChangeCallback           typeChangeCallback               ,
		final PersistenceTypeDefinitionInitializer<T> runtimeTypeDescriptionInitializer
	)
	{
		return new PersistenceTypeLineageBuilder.Implementation<>(
			notNull(typeDefinitionBuilder)            ,
			notNull(typeChangeCallback)               ,
			notNull(runtimeTypeDescriptionInitializer)
		);
	}
	
	
	final class Implementation<T> implements PersistenceTypeLineageBuilder<T>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		final PersistenceTypeLineage.Implementation<T> typeLineage                     ;
		final PersistenceTypeChangeCallback            typeChangeCallback              ;
		final PersistenceTypeDefinitionInitializer<T>  runtimeTypeDefinitionInitializer;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Implementation(
			final PersistenceTypeDefinitionBuilder        typeDefinitionBuilder            ,
			final PersistenceTypeChangeCallback           typeChangeCallback               ,
			final PersistenceTypeDefinitionInitializer<T> runtimeTypeDescriptionInitializer
		)
		{
			super();
			this.typeLineage                      = PersistenceTypeLineage.New(
				runtimeTypeDescriptionInitializer.typeName(),
				typeDefinitionBuilder                       ,
				runtimeTypeDescriptionInitializer.type()
			);
			this.runtimeTypeDefinitionInitializer = runtimeTypeDescriptionInitializer;
			this.typeChangeCallback               = typeChangeCallback               ;
		}

		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public boolean registerTypeDescription(
			final long                                                         typeId ,
			final XGettingSequence<? extends PersistenceTypeDescriptionMember> members
		)
		{
			return this.typeLineage.registerTypeDescription(typeId, members);
		}
		
		@Override
		public PersistenceTypeLineage<T> buildTypeLineage()
		{
			final PersistenceTypeDefinition<T> latest = this.typeLineage.latest();

			final PersistenceTypeDefinition<T> runtimeTypeDef;
			if(latest == null)
			{
				// trivial case: no type dictionary entries so far, so the runtime type is the first and only definition.
				runtimeTypeDef = this.runtimeTypeDefinitionInitializer.initializeForNewTypeId(this.typeLineage);
			}
			else
			{
				if(PersistenceTypeDescriptionMember.equalMembers(latest.members(), this.runtimeTypeDefinitionInitializer.members()))
				{
					// case: latest type description fits the runtime type, so its typeId can be adopted.
					runtimeTypeDef = this.runtimeTypeDefinitionInitializer.initializeForExistingTypeId(
						latest.typeId(),
						this.typeLineage
					);
				}
				else
				{
					// validate the change before assigning and registering a new type id.
					this.typeChangeCallback.validateTypeChange(latest, this.runtimeTypeDefinitionInitializer);
					
					// case: runtime type differs from the latest type description.
					runtimeTypeDef = this.runtimeTypeDefinitionInitializer.initializeForNewTypeId(this.typeLineage);
				}
				
				if(runtimeTypeDef.typeId() != latest.typeId())
				{
					this.typeChangeCallback.registerTypeChange(latest, runtimeTypeDef);
				}
			}
			
			this.typeLineage.initializeRuntimeTypeDescription(runtimeTypeDef);
			
			return this.typeLineage;
		}
		
	}
	
}

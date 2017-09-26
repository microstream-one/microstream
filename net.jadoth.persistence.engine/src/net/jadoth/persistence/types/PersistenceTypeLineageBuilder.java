package net.jadoth.persistence.types;

import net.jadoth.collections.EqHashTable;
import net.jadoth.collections.types.XGettingSequence;
import net.jadoth.swizzling.types.SwizzleTypeManager;

public interface PersistenceTypeLineageBuilder<T>
{
	public boolean registerTypeDescription(long typeId, XGettingSequence<? extends PersistenceTypeDescriptionMember> members);
	
	
	public PersistenceTypeLineage<T> buildTypeLineage();
	
	
	
	public final class Implementation<T> implements PersistenceTypeLineageBuilder<T>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		final PersistenceTypeLineage<T>                        typeLineage                      ;
		final PersistenceTypeDescriptionInitializer<T>         runtimeTypeDescriptionInitializer;
		final SwizzleTypeManager                               typeManager                      ;
		final EqHashTable<Long, PersistenceTypeDescription<T>> typeDescriptions                 ;

		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Implementation(
			final PersistenceTypeLineage<T>                typeLineage                      ,
			final PersistenceTypeDescriptionInitializer<T> runtimeTypeDescriptionInitializer,
			final SwizzleTypeManager                       typeManager
		)
		{
			super();
			this.typeLineage                       = typeLineage                      ;
			this.runtimeTypeDescriptionInitializer = runtimeTypeDescriptionInitializer;
			this.typeManager                       = typeManager                      ;
			this.typeDescriptions                  = EqHashTable.New()                ;
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
			final PersistenceTypeDescription<T> td = PersistenceTypeDescription.New(this.typeLineage, typeId, members);
			return this.typeDescriptions.add(typeId, td);
		}



		@Override
		public PersistenceTypeLineage<T> buildTypeLineage()
		{
			this.typeDescriptions.keys().sort(Long::compare);
			
			final PersistenceTypeDescription<T> latest = this.typeDescriptions.values().last();
			
			final long currentTypeId;
			if(PersistenceTypeDescriptionMember.equalMembers(latest.members(), this.runtimeTypeDescriptionInitializer.members()))
			{
				// case: latest type description fits the runtime type
				currentTypeId = latest.typeId();
				this.typeManager.registerType(currentTypeId, this.runtimeTypeDescriptionInitializer.type());
			}
			else
			{
				// case: runtime type differes from latest type description. Handling (exception or refactoring) needed.
				// (26.09.2017 TM)FIXME: mark for handling
				currentTypeId = this.typeManager.ensureTypeId(this.runtimeTypeDescriptionInitializer.type());
			}
			
			final PersistenceTypeDescription<T> runtimeTypeDescription = this.runtimeTypeDescriptionInitializer.initialize(
				currentTypeId,
				this.typeLineage
			);
			this.typeLineage.initializeRuntimeTypeDescription(runtimeTypeDescription);
			
			throw new net.jadoth.meta.NotImplementedYetError(); // FIXME PersistenceTypeLineageBuilder<T>#buildTypeLineage()
		}
		
	}
	
}

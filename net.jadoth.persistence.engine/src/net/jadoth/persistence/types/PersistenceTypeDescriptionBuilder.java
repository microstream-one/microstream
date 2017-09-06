package net.jadoth.persistence.types;

import net.jadoth.collections.types.XGettingSequence;

@FunctionalInterface
public interface PersistenceTypeDescriptionBuilder
{
	public <T> PersistenceTypeDescription<T> build(
		PersistenceTypeDescriptionLineage<T>                         lineage,
		long                                                         typeId ,
		XGettingSequence<? extends PersistenceTypeDescriptionMember> members
	);
			
	public final class Implementation implements PersistenceTypeDescriptionBuilder
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Implementation()
		{
			super();
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public <T> PersistenceTypeDescription<T> build(
			final PersistenceTypeDescriptionLineage<T>                         lineage,
			final long                                                         typeId ,
			final XGettingSequence<? extends PersistenceTypeDescriptionMember> members
		)
		{
			return PersistenceTypeDescription.New(lineage, typeId, members);
		}
		
	}
	
}
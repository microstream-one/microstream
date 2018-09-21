package net.jadoth.persistence.types;

import net.jadoth.functional.Similator;

public interface PersistenceTypeMemberSimilator extends Similator<PersistenceTypeDescriptionMember>
{
	public static PersistenceTypeMemberSimilator New()
	{
		return new PersistenceTypeMemberSimilator.Implementation();
	}
	
	public final class Implementation implements PersistenceTypeMemberSimilator
	{

		@Override
		public double evaluate(final PersistenceTypeDescriptionMember m1, final PersistenceTypeDescriptionMember m2)
		{
			// FIXME: OGS-3: Similator<PersistenceTypeDescriptionMember>#evaluate()
			throw new net.jadoth.meta.NotImplementedYetError();
		}
		
	}
	
}

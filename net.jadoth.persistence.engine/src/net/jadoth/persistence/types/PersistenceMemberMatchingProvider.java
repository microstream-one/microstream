package net.jadoth.persistence.types;

import net.jadoth.equality.Equalator;
import net.jadoth.functional.Similator;
import net.jadoth.util.matching.MatchValidator;

public interface PersistenceMemberMatchingProvider
{
	public Equalator<PersistenceTypeDescriptionMember> provideMemberMatchingEqualator();
	
	public Similator<PersistenceTypeDescriptionMember> provideMemberMatchingSimilator(
		PersistenceRefactoringMapping refactoringMapping
	);
	
	public MatchValidator<PersistenceTypeDescriptionMember> provideMemberMatchValidator();
	
	
	
	public static PersistenceMemberMatchingProvider New()
	{
		// FIXME: OGS-3: PersistenceMemberMatchingProvider#New()
		throw new net.jadoth.meta.NotImplementedYetError();
	}
}

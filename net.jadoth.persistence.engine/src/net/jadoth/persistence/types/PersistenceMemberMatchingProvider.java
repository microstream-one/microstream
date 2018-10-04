package net.jadoth.persistence.types;

import net.jadoth.equality.Equalator;
import net.jadoth.functional.Similator;
import net.jadoth.typing.TypeMappingLookup;
import net.jadoth.util.matching.MatchValidator;

//@FunctionalInterface - well, lol.
public interface PersistenceMemberMatchingProvider
{
	public default Equalator<PersistenceTypeDescriptionMember> provideMemberMatchingEqualator()
	{
		// optional, null by default.
		return null;
	}
	
	public default Similator<PersistenceTypeDescriptionMember> provideMemberMatchingSimilator(
		final PersistenceRefactoringMapping refactoringMapping,
		final TypeMappingLookup<Float>      typeSimilarity
	)
	{
		return PersistenceMemberSimilator.New(refactoringMapping, typeSimilarity);
	}
	
	public default MatchValidator<PersistenceTypeDescriptionMember> provideMemberMatchValidator()
	{
		// optional, null by default.
		return null;
	}
	
	
	
	public static PersistenceMemberMatchingProvider New()
	{
		return new PersistenceMemberMatchingProvider.Implementation();
	}
	
	public class Implementation implements PersistenceMemberMatchingProvider
	{
		// since default methods, the ability to instantiate stateless instances from interfaces is missing
	}
	
}

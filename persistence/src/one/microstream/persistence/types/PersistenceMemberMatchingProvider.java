package one.microstream.persistence.types;

import one.microstream.equality.Equalator;
import one.microstream.typing.TypeMappingLookup;
import one.microstream.util.similarity.MatchValidator;
import one.microstream.util.similarity.Similator;

//@FunctionalInterface - well, lol.
public interface PersistenceMemberMatchingProvider
{
	public default Equalator<PersistenceTypeDefinitionMember> provideMemberMatchingEqualator()
	{
		// optional, null by default.
		return null;
	}
	
	public default Similator<PersistenceTypeDefinitionMember> provideMemberMatchingSimilator(
		final TypeMappingLookup<Float> typeSimilarity
	)
	{
		return PersistenceMemberSimilator.New(typeSimilarity);
	}
	
	public default MatchValidator<PersistenceTypeDefinitionMember> provideMemberMatchValidator()
	{
		// optional, null by default.
		return null;
	}
	
	
	
	public static PersistenceMemberMatchingProvider New()
	{
		return new PersistenceMemberMatchingProvider.Default();
	}
	
	public class Default implements PersistenceMemberMatchingProvider
	{
		// since default methods, the ability to instantiate stateless instances from interfaces is missing
	}
	
}

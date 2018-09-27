package net.jadoth.persistence.types;

import net.jadoth.equality.Equalator;
import net.jadoth.functional.Similator;
import net.jadoth.typing.TypeMappingLookup;
import net.jadoth.util.matching.MatchValidator;

public interface PersistenceMemberMatchingProvider
{
	public Equalator<PersistenceTypeDescriptionMember> provideMemberMatchingEqualator();
	
	public Similator<PersistenceTypeDescriptionMember> provideMemberMatchingSimilator(
		PersistenceRefactoringMapping refactoringMapping,
		TypeMappingLookup<Float>      typeSimilarity
	);
	
	public MatchValidator<PersistenceTypeDescriptionMember> provideMemberMatchValidator();
	
	
	
	public static PersistenceMemberMatchingProvider New()
	{
		return new PersistenceMemberMatchingProvider.Implementation();
	}
	
	public class Implementation implements PersistenceMemberMatchingProvider
	{

		@Override
		public Equalator<PersistenceTypeDescriptionMember> provideMemberMatchingEqualator()
		{
			// optional, null by default.
			return null;
		}

		@Override
		public Similator<PersistenceTypeDescriptionMember> provideMemberMatchingSimilator(
			final PersistenceRefactoringMapping refactoringMapping,
			final TypeMappingLookup<Float>      typeSimilarity
		)
		{
			return PersistenceMemberSimilator.New(refactoringMapping, typeSimilarity);
		}

		@Override
		public MatchValidator<PersistenceTypeDescriptionMember> provideMemberMatchValidator()
		{
			// optional, null by default.
			return null;
		}
		
	}
	
}

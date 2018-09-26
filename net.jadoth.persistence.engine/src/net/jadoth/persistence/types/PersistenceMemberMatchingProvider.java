package net.jadoth.persistence.types;

import net.jadoth.equality.Equalator;
import net.jadoth.functional.Similator;
import net.jadoth.typing.TypeMapping;
import net.jadoth.util.matching.MatchValidator;

public interface PersistenceMemberMatchingProvider
{
	public Equalator<PersistenceTypeDescriptionMember> provideMemberMatchingEqualator();
	
	public Similator<PersistenceTypeDescriptionMember> provideMemberMatchingSimilator(
		PersistenceRefactoringMapping refactoringMapping,
		TypeMapping<Float>            typeSimilarity
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
			final TypeMapping<Float>            typeSimilarity
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

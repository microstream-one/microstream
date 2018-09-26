package net.jadoth.persistence.types;

import static net.jadoth.X.notNull;

import net.jadoth.chars.Levenshtein;
import net.jadoth.functional.Similator;
import net.jadoth.reflect.XReflect;
import net.jadoth.typing.TypeMapping;

public interface PersistenceMemberSimilator extends Similator<PersistenceTypeDescriptionMember>
{
	public static PersistenceMemberSimilator New(
		final PersistenceRefactoringMapping refactoringMapping,
		final TypeMapping<Float>            typeSimilarity
	)
	{
		return new PersistenceMemberSimilator.Implementation(
			notNull(refactoringMapping),
			notNull(typeSimilarity)
		);
	}
	
	public final class Implementation implements PersistenceMemberSimilator
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		final PersistenceRefactoringMapping refactoringMapping;
		final TypeMapping<Float>            typeSimilarity    ;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Implementation(
			final PersistenceRefactoringMapping refactoringMapping,
			final TypeMapping<Float>            typeSimilarity
		)
		{
			super();
			this.refactoringMapping = refactoringMapping;
			this.typeSimilarity     = typeSimilarity    ;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public double evaluate(
			final PersistenceTypeDescriptionMember sourceMember,
			final PersistenceTypeDescriptionMember targetMember
		)
		{
			final float nameSimilarity = this.calculateSimilarityName(sourceMember, targetMember);
			final float typeSimilarity = this.calculateSimilarityType(sourceMember, targetMember);
			
			return (nameSimilarity + typeSimilarity ) / 2.0f;
		}
		
		private float calculateSimilarityName(
			final PersistenceTypeDescriptionMember sourceMember,
			final PersistenceTypeDescriptionMember targetMember
		)
		{
			/* (26.09.2018 TM)FIXME: OGS-3: declClass only for Field members and as a namespace for name
			 * But for true reflection fields, it is essential!
			 * 
			 * 1.0 for matching decl classes, including null.
			 * 0.5 for not matching decl classes.
			 * 
			 * Meaning:
			 * If the declaring classes do not match, the name weighs only half and the type gets more important,
			 * assuming the field is more probable to have been renamed instead of having been moved to another
			 * class.
			 */
			
			final float declaringClassSimilarity = 1.0f;
			
			return declaringClassSimilarity * Levenshtein.similarity(sourceMember.name(), targetMember.name());
		}
		
		private float calculateSimilarityType(
			final PersistenceTypeDescriptionMember sourceMember,
			final PersistenceTypeDescriptionMember targetMember
		)
		{
			final Class<?> sourceType = sourceMember.type();
			final Class<?> targetType = targetMember.type();
			
			if(sourceType != null && targetType != null)
			{
				return this.calculateTypeSimilarity(sourceType, targetType);
			}
			
			final String sourceTypeNameReplacement = this.refactoringMapping.entries().get(sourceMember.typeName());
			if(sourceTypeNameReplacement == null)
			{
				// not much point in doing half-wise guessing here.
				return 0.0f;
			}
			
			final Class<?> sourceTypeReplacement = XReflect.tryClassForName(sourceTypeNameReplacement);
			if(sourceTypeReplacement == null)
			{
				// not much point in doing half-wise guessing here.
				return 0.0f;
			}

			return this.calculateTypeSimilarity(sourceTypeReplacement, targetType);
			
		}
		
		private float calculateTypeSimilarity(final Class<?> type1, final Class<?> type2)
		{
			if(type1 == type2)
			{
				return 1.0f;
			}
			
			final Float mappedSimilarity = this.typeSimilarity.lookup(type1, type2);
			if(mappedSimilarity != null)
			{
				return mappedSimilarity;
			}
			
			return 0.0f;
		}
		
	}
	
}

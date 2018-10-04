package net.jadoth.persistence.types;

import static net.jadoth.X.notNull;

import net.jadoth.X;
import net.jadoth.chars.Levenshtein;
import net.jadoth.functional.Similator;
import net.jadoth.meta.XDebug;
import net.jadoth.reflect.XReflect;
import net.jadoth.typing.KeyValue;
import net.jadoth.typing.TypeMappingLookup;

public interface PersistenceMemberSimilator extends Similator<PersistenceTypeDescriptionMember>
{
	public static PersistenceMemberSimilator New(
		final PersistenceRefactoringMapping refactoringMapping,
		final TypeMappingLookup<Float>      typeSimilarity
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
		final TypeMappingLookup<Float>      typeSimilarity    ;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Implementation(
			final PersistenceRefactoringMapping refactoringMapping,
			final TypeMappingLookup<Float>      typeSimilarity
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
		public final double evaluate(
			final PersistenceTypeDescriptionMember sourceMember,
			final PersistenceTypeDescriptionMember targetMember
		)
		{
			final float nameSimilarity = this.calculateSimilarityByName(sourceMember, targetMember);
			final float typeSimilarity = this.calculateSimilaritybyType(sourceMember, targetMember);
			
			XDebug.debugln(
				sourceMember.name()
				+" ---["+nameSimilarity+","+typeSimilarity+"="+(nameSimilarity + typeSimilarity ) / 2.0f
				+"]---> "
				+targetMember.name()
			);
			
			return (nameSimilarity + typeSimilarity ) / 2.0f;
		}
		
		private float calculateSimilarityByName(
			final PersistenceTypeDescriptionMember sourceMember,
			final PersistenceTypeDescriptionMember targetMember
		)
		{
			/*
			 * Cannot do a quick-check for perfect matches, here, because a refactoring mapping
			 * might map a type name (qualifier) on the source side to another one on the target side.
			 * Doing a quick check on simple equality might cause an ambiguity for such cases.
			 */
			
			final KeyValue<String, String> sourceUniqueName = PersistenceTypeDictionary.splitFullQualifiedFieldName(
				sourceMember.uniqueName()
			);
			final KeyValue<String, String> targetUniqueName = PersistenceTypeDictionary.splitFullQualifiedFieldName(
				targetMember.uniqueName()
			);
			
			final float nameSimilarity = Levenshtein.similarity(
				sourceUniqueName.value(),
				targetUniqueName.value()
			);
			final float qualifierFactor = calculateQualifierSimilarityFactor(
				sourceUniqueName.key(),
				targetUniqueName.key()
			);
			
			return qualifierFactor * nameSimilarity;
		}
		
		private float calculateQualifierSimilarityFactor(
			final String sourceQualifier,
			final String targetQualifier
		)
		{
			if(sourceQualifier == null && targetQualifier == null)
			{
				// effectively "no factor".
				return 1.0f;
			}
			else if(X.isNull(targetQualifier) != X.isNull(targetQualifier))
			{
				// simply a qualifier mismatch, so name similarity reduced to 50%.
				return 0.5f;
			}
			
			final String effectiveSourceDeclaringType = X.coalesce(
				this.refactoringMapping.entries().get(sourceQualifier),
				sourceQualifier
			);
			
			return effectiveSourceDeclaringType.equals(targetQualifier)
				? 1.0f
				: 0.5f
			;
		}
		
		private float calculateSimilaritybyType(
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
			
			// (02.10.2018 TM)TODO: Legacy Type Mapping: This should not be done again here, but looked up instead.
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

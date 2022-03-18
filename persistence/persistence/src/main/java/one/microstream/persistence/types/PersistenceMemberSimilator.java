package one.microstream.persistence.types;

/*-
 * #%L
 * microstream-persistence
 * %%
 * Copyright (C) 2019 - 2022 MicroStream Software
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

import static one.microstream.X.notNull;

import one.microstream.chars.Levenshtein;
import one.microstream.typing.TypeMappingLookup;
import one.microstream.util.similarity.Similator;

public interface PersistenceMemberSimilator extends Similator<PersistenceTypeDefinitionMember>
{
	public static PersistenceMemberSimilator New(final TypeMappingLookup<Float>  typeSimilarity)
	{
		return new PersistenceMemberSimilator.Default(
			notNull(typeSimilarity)
		);
	}
	
	public final class Default implements PersistenceMemberSimilator
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		final TypeMappingLookup<Float> typeSimilarity;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(final TypeMappingLookup<Float>typeSimilarity)
		{
			super();
			this.typeSimilarity = typeSimilarity;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final double evaluate(
			final PersistenceTypeDefinitionMember sourceMember,
			final PersistenceTypeDefinitionMember targetMember
		)
		{
			if(sourceMember.isEnumConstant() != targetMember.isEnumConstant())
			{
				// may never even begin consider to match enum constant fields and non-enum-constant fields.
				return 0.0;
			}
			
			final float nameSimilarity = this.calculateSimilarityByName(sourceMember, targetMember);
			final float typeSimilarity = this.calculateSimilaritybyType(sourceMember, targetMember);
			
//			XDebug.println(
//				sourceMember.name()
//				+ "\t---[" + nameSimilarity + "," + typeSimilarity + "=" + (nameSimilarity + typeSimilarity ) / 2.0f
//				+ "]--->\t"
//				+ targetMember.name()
//			);
			
			return (nameSimilarity + typeSimilarity ) / 2.0f;
		}
		
		private float calculateSimilarityByName(
			final PersistenceTypeDefinitionMember sourceMember,
			final PersistenceTypeDefinitionMember targetMember
		)
		{
			/*
			 * Cannot do a quick-check for perfect matches, here, because a refactoring mapping
			 * might map a type name (qualifier) on the source side to another one on the target side.
			 * Doing a quick check on simple equality might cause an ambiguity for such cases.
			 */
			
			final float nameSimilarity = Levenshtein.similarity(
				sourceMember.name(),
				targetMember.name()
			);
			final float qualifierFactor = this.calculateQualifierSimilarityFactor(
				sourceMember.runtimeQualifier(),
				targetMember.runtimeQualifier()
			);
			
			return qualifierFactor * nameSimilarity;
		}
		
		private float calculateQualifierSimilarityFactor(
			final String sourceQualifier,
			final String targetQualifier
		)
		{
			// not much point in calculating similarity between qualifiers. Either they are equal or not.
			return sourceQualifier == null
				? targetQualifier == null
					? 1.0f
					: 0.5f
				: sourceQualifier.equals(targetQualifier)
					? 1.0f
					: 0.5f
			;
		}
		
		private float calculateSimilaritybyType(
			final PersistenceTypeDefinitionMember sourceMember,
			final PersistenceTypeDefinitionMember targetMember
		)
		{
			final Class<?> sourceType = sourceMember.type();
			final Class<?> targetType = targetMember.type();
			
			if(sourceType != null && targetType != null)
			{
				return this.calculateTypeSimilarity(sourceType, targetType);
			}

			// not much point in calculating similarity between unresolvable types. Either they are equal or not.
			return sourceMember.typeName().equals(targetMember.typeName())
				? 1.0f
				: 0.5f
			;
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

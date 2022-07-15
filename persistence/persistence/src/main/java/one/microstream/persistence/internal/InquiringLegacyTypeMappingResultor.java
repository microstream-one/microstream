package one.microstream.persistence.internal;

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

import java.util.Scanner;

import one.microstream.chars.VarString;
import one.microstream.chars.XChars;
import one.microstream.collections.types.XGettingMap;
import one.microstream.collections.types.XGettingSet;
import one.microstream.math.XMath;
import one.microstream.persistence.exceptions.PersistenceException;
import one.microstream.persistence.types.PersistenceLegacyTypeMappingResult;
import one.microstream.persistence.types.PersistenceLegacyTypeMappingResultor;
import one.microstream.persistence.types.PersistenceTypeDefinition;
import one.microstream.persistence.types.PersistenceTypeDefinitionMember;
import one.microstream.persistence.types.PersistenceTypeHandler;
import one.microstream.util.similarity.MultiMatch;
import one.microstream.util.similarity.Similarity;

public class InquiringLegacyTypeMappingResultor<D> implements PersistenceLegacyTypeMappingResultor<D>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static <D> InquiringLegacyTypeMappingResultor<D> New(
		final PersistenceLegacyTypeMappingResultor<D> delegate
	)
	{
		return New(delegate, 1.0);
	}
		
	public static <D> InquiringLegacyTypeMappingResultor<D> New(
		final PersistenceLegacyTypeMappingResultor<D> delegate,
		final double                minimumSimilarityThreshold
	)
	{
		return new InquiringLegacyTypeMappingResultor<>(
			notNull(delegate),
			XMath.notNegativeMax1(minimumSimilarityThreshold)
		);
	}
	
	public static char approvalToken()
	{
		return 'y';
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final PersistenceLegacyTypeMappingResultor<D> delegate;
	private final double                inquirySimilarityThreshold;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	InquiringLegacyTypeMappingResultor(
		final PersistenceLegacyTypeMappingResultor<D> delegate,
		final double                inquirySimilarityThreshold
	)
	{
		super();
		this.delegate                   = delegate                  ;
		this.inquirySimilarityThreshold = inquirySimilarityThreshold;
	}


	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	private boolean inquiryRequired(
		final PersistenceLegacyTypeMappingResult<D, ?>                                      result          ,
		final XGettingMap<PersistenceTypeDefinitionMember, PersistenceTypeDefinitionMember> explicitMappings
	)
	{
		final double lowestSimilarity = XMath.min_double(
			result.currentToLegacyMembers().values(),
			Similarity::_similarity
		);
				
		boolean hasUnmappedDiscardedMembers = false;
		for(final PersistenceTypeDefinitionMember discarded : result.discardedLegacyMembers())
		{
			if(!explicitMappings.keys().contains(discarded))
			{
				hasUnmappedDiscardedMembers = true;
				break;
			}
		}
		
		return lowestSimilarity < this.inquirySimilarityThreshold || hasUnmappedDiscardedMembers;
	}

	@Override
	public <T> PersistenceLegacyTypeMappingResult<D, T> createMappingResult(
		final PersistenceTypeDefinition                                                     legacyTypeDefinition,
		final PersistenceTypeHandler<D, T>                                                  currentTypeHandler  ,
		final XGettingMap<PersistenceTypeDefinitionMember, PersistenceTypeDefinitionMember> explicitMappings    ,
		final XGettingSet<PersistenceTypeDefinitionMember>                                  explicitNewMembers  ,
		final MultiMatch<PersistenceTypeDefinitionMember>                                   matchedMembers
	)
	{
		final PersistenceLegacyTypeMappingResult<D, T> result = this.delegate.createMappingResult(
			legacyTypeDefinition,
			currentTypeHandler  ,
			explicitMappings    ,
			explicitNewMembers  ,
			matchedMembers
		);
		
		final boolean inquiryRequired = this.inquiryRequired(result, explicitMappings);
		if(!inquiryRequired)
		{
			final String output = PrintingLegacyTypeMappingResultor.assembleMappingWithHeader(
				explicitMappings,
				matchedMembers  ,
				result
			);
			System.out.println(output);
			
			return result;
		}
		
		final boolean approved = this.inquireApproval(explicitMappings, matchedMembers, result);
		if(approved)
		{
			return result;
		}
		
		throw new PersistenceException(
			"User-aborted legacy type mapping for type " + legacyTypeDefinition.toTypeIdentifier()
		);
	}
		
	protected boolean inquireApproval(
		final XGettingMap<PersistenceTypeDefinitionMember, PersistenceTypeDefinitionMember> explicitMappings,
		final MultiMatch<PersistenceTypeDefinitionMember>                                   matchedMembers  ,
		final PersistenceLegacyTypeMappingResult<D, ?>                                      result
	)
	{
		System.out.println(this.buildInquiry(explicitMappings, matchedMembers, result));
		
		@SuppressWarnings("resource") // the bloody scanner would auto-close System.in. Hilarious JDK code again.
		final Scanner scanner = new Scanner(System.in);
		
		final String input = XChars.trimEmptyToNull(scanner.nextLine());
		if(input != null && Character.toLowerCase(input.charAt(0)) == approvalToken())
		{
			return true;
		}
		
		return false;
	}
	
	protected String buildInquiry(
		final XGettingMap<PersistenceTypeDefinitionMember, PersistenceTypeDefinitionMember> explicitMappings,
		final MultiMatch<PersistenceTypeDefinitionMember>                                   matchedMembers  ,
		final PersistenceLegacyTypeMappingResult<D, ?>                                      result
	)
	{
		// intentionally no instance field to not permanently occupy memory by an initializer part.
		final VarString vs = VarString.New(1000);
		PrintingLegacyTypeMappingResultor.assembleMappingHeader(vs, result);
		PrintingLegacyTypeMappingResultor.assembleMapping(vs, explicitMappings, matchedMembers, result);
		this.assembleInquiryEnd(vs, result);
		final String inquiry = vs.toString();
		return inquiry;
	}
	

		
	protected VarString assembleInquiryEnd(
		final VarString                                vs    ,
		final PersistenceLegacyTypeMappingResult<D, ?> result
	)
	{
		vs
		.add("---").lf()
		.add("Write '").add(approvalToken()).add("' to accept the mapping.")
		;
		return vs;
	}
	
}

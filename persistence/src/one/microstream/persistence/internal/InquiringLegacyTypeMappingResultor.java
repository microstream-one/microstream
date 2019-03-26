package one.microstream.persistence.internal;

import static one.microstream.X.notNull;

import java.util.Scanner;

import one.microstream.chars.VarString;
import one.microstream.chars.XChars;
import one.microstream.collections.types.XGettingEnum;
import one.microstream.collections.types.XGettingMap;
import one.microstream.collections.types.XGettingSet;
import one.microstream.collections.types.XGettingTable;
import one.microstream.math.XMath;
import one.microstream.meta.XDebug;
import one.microstream.persistence.types.PersistenceLegacyTypeMappingResult;
import one.microstream.persistence.types.PersistenceLegacyTypeMappingResultor;
import one.microstream.persistence.types.PersistenceTypeDefinition;
import one.microstream.persistence.types.PersistenceTypeDefinitionMember;
import one.microstream.persistence.types.PersistenceTypeHandler;
import one.microstream.util.matching.MultiMatch;

public class InquiringLegacyTypeMappingResultor<M> implements PersistenceLegacyTypeMappingResultor<M>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static <M> InquiringLegacyTypeMappingResultor<M> New(
		final PersistenceLegacyTypeMappingResultor<M> delegate
	)
	{
		return new InquiringLegacyTypeMappingResultor<>(delegate, 0.0);
	}
	
	public static <M> InquiringLegacyTypeMappingResultor<M> New(
		final PersistenceLegacyTypeMappingResultor<M> delegate,
		final boolean                     ignorePerfectMatches
	)
	{
		return New(
			delegate,
			ignorePerfectMatches
				? 1.0
				: 0.0
		);
	}
	
	public static <M> InquiringLegacyTypeMappingResultor<M> New(
		final PersistenceLegacyTypeMappingResultor<M> delegate,
		final double                inquerySimilarityThreshold
	)
	{
		return new InquiringLegacyTypeMappingResultor<>(
			notNull(delegate),
			XMath.notNegativePercentage(inquerySimilarityThreshold)
		);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final PersistenceLegacyTypeMappingResultor<M> delegate;
	private final double                                  inquerySimilarityThreshold;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	InquiringLegacyTypeMappingResultor(
		final PersistenceLegacyTypeMappingResultor<M> delegate,
		final double                inquerySimilarityThreshold
	)
	{
		super();
		this.delegate                   = delegate                  ;
		this.inquerySimilarityThreshold = inquerySimilarityThreshold;
	}


	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	private boolean inquiryRequired(final PersistenceLegacyTypeMappingResult<M, ?> result)
	{
//		final long maxMemberCount = Math.max(
//			matchedMembers.result().inputSources().size(),
//			matchedMembers.result().inputTargets().size()
//		);
		// (26.03.2019 TM)FIXME: inquiryRequired
		XDebug.println("FIXME");
		return true;
	}

	@Override
	public <T> PersistenceLegacyTypeMappingResult<M, T> createMappingResult(
		final PersistenceTypeDefinition                                                     legacyTypeDefinition,
		final PersistenceTypeHandler<M, T>                                                  currentTypeHandler  ,
		final XGettingMap<PersistenceTypeDefinitionMember, PersistenceTypeDefinitionMember> explicitMappings    ,
		final XGettingSet<PersistenceTypeDefinitionMember>                                  explicitNewMembers  ,
		final MultiMatch<PersistenceTypeDefinitionMember>                                   matchedMembers
	)
	{
		// (26.03.2019 TM)FIXME: /!\ DEBUG
		final VarString vs = VarString.New(currentTypeHandler.runtimeTypeName() + " Matching:").lf();
		XDebug.println(matchedMembers.assembler().assembleMappingSchemeVertical(vs).toString());
		
		final PersistenceLegacyTypeMappingResult<M, T> result = this.delegate.createMappingResult(
			legacyTypeDefinition,
			currentTypeHandler  ,
			explicitMappings    ,
			explicitNewMembers  ,
			matchedMembers
		);
				
		if(!this.inquiryRequired(result) || this.inquireApproval(explicitMappings, matchedMembers, result))
		{
			return result;
		}
		
		// (10.10.2018 TM)EXCP: proper exception
		throw new RuntimeException(
			"User-aborted legacy type mapping for type " + legacyTypeDefinition.toTypeIdentifier()
		);
	}
		
	protected boolean inquireApproval(
		final XGettingMap<PersistenceTypeDefinitionMember, PersistenceTypeDefinitionMember> explicitMappings,
		final MultiMatch<PersistenceTypeDefinitionMember>                                   matchedMembers  ,
		final PersistenceLegacyTypeMappingResult<M, ?>                                      result
	)
	{
		System.out.println(this.buildInquiry(explicitMappings, matchedMembers, result));
		
		@SuppressWarnings("resource") // the bloody scanner would auto-close System.in. Hilarious JDK code again.
		final Scanner scanner = new Scanner(System.in);
		
		final String input = XChars.trimEmptyToNull(scanner.nextLine());
		if(input != null && 'y' == Character.toLowerCase(input.charAt(0)))
		{
			return true;
		}
		
		return false;
	}
	
	protected String buildInquiry(
		final XGettingMap<PersistenceTypeDefinitionMember, PersistenceTypeDefinitionMember> explicitMappings,
		final MultiMatch<PersistenceTypeDefinitionMember>                                   matchedMembers  ,
		final PersistenceLegacyTypeMappingResult<M, ?>                                      result
	)
	{
		// intentionally no instance field to not permanently occupy memory by an initializer part.
		final VarString vs = VarString.New(1000);
		this.assembleInquiryStart(vs, result);
		this.assembleMapping(vs, explicitMappings, matchedMembers, result);
		this.assembleInquiryEnd(vs, result);
		final String inquiry = vs.toString();
		return inquiry;
	}
	
	protected VarString assembleInquiryStart(
		final VarString                                vs    ,
		final PersistenceLegacyTypeMappingResult<M, ?> result
	)
	{
		vs
		.lf()
		.add("----------").lf()
		.add("Legacy type mapping required for legacy type ").lf()
		.add(result.legacyTypeDefinition().toTypeIdentifier()).lf()
		.add("to current type ").lf()
		.add(result.currentTypeHandler().toTypeIdentifier()).lf()
		.add("Fields:").lf()
		;
		
		return vs;
	}
	
	private static void assembleMember(final VarString vs, final PersistenceTypeDefinitionMember member)
	{
		vs.add(member.typeName()).blank().add(member.uniqueName());
	}
	
	protected VarString assembleMapping(
		final VarString                                                                     vs              ,
		final XGettingMap<PersistenceTypeDefinitionMember, PersistenceTypeDefinitionMember> explicitMappings,
		final MultiMatch<PersistenceTypeDefinitionMember>                                   matchedMembers  ,
		final PersistenceLegacyTypeMappingResult<M, ?>                                      result
	)
	{
		final XGettingTable<PersistenceTypeDefinitionMember, PersistenceTypeDefinitionMember> currentToLegacyMembers =
			result.currentToLegacyMembers()
		;
		final XGettingEnum<PersistenceTypeDefinitionMember> newCurrentMembers = result.newCurrentMembers();
		
		for(final PersistenceTypeDefinitionMember member : result.currentTypeHandler().membersInDeclaredOrder())
		{
			final PersistenceTypeDefinitionMember mappedLegacyMember = currentToLegacyMembers.get(member);
			if(mappedLegacyMember != null)
			{
				assembleMember(vs, mappedLegacyMember);
				vs.add("\t----> ");
				assembleMember(vs, member);
				vs.lf();
				continue;
			}
			if(newCurrentMembers.contains(member))
			{
				vs.add("\t[new] ");
				assembleMember(vs, member);
				vs.lf();
				continue;
			}
			// (11.10.2018 TM)EXCP: proper exception
			throw new RuntimeException("Inconsistent current type member mapping: " + member.uniqueName());
		}
		
		for(final PersistenceTypeDefinitionMember e : result.discardedLegacyMembers())
		{
			assembleMember(vs, e);
			vs.add("\t[discarded]").lf();
		}
		
		return vs;
	}
	
	protected VarString assembleInquiryEnd(
		final VarString                                vs    ,
		final PersistenceLegacyTypeMappingResult<M, ?> result
	)
	{
		vs
		.add("---").lf()
		.add("Write 'y' to accept the mapping.")
		;
		return vs;
	}
	
}

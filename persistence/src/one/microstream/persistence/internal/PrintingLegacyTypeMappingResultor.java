package one.microstream.persistence.internal;

import static one.microstream.X.notNull;

import one.microstream.chars.VarString;
import one.microstream.collections.types.XGettingEnum;
import one.microstream.collections.types.XGettingMap;
import one.microstream.collections.types.XGettingSet;
import one.microstream.collections.types.XGettingTable;
import one.microstream.persistence.types.PersistenceLegacyTypeMapper;
import one.microstream.persistence.types.PersistenceLegacyTypeMappingResult;
import one.microstream.persistence.types.PersistenceLegacyTypeMappingResultor;
import one.microstream.persistence.types.PersistenceTypeDefinition;
import one.microstream.persistence.types.PersistenceTypeDefinitionMember;
import one.microstream.persistence.types.PersistenceTypeHandler;
import one.microstream.util.similarity.MultiMatch;
import one.microstream.util.similarity.Similarity;

public class PrintingLegacyTypeMappingResultor<M> implements PersistenceLegacyTypeMappingResultor<M>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static String assembleMappingWithHeader(
		final XGettingMap<PersistenceTypeDefinitionMember, PersistenceTypeDefinitionMember> explicitMappings,
		final MultiMatch<PersistenceTypeDefinitionMember>                                   matchedMembers  ,
		final PersistenceLegacyTypeMappingResult<?, ?>                                      result
	)
	{
		final VarString vs = VarString.New();
		assembleMappingHeader(vs, result);
		assembleMapping(vs, explicitMappings, matchedMembers, result);
		return vs.toString();
	}
	
	public static VarString assembleMappingHeader(
		final VarString                                vs    ,
		final PersistenceLegacyTypeMappingResult<?, ?> result
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
	
	public static VarString assembleMapping(
		final VarString                                                                     vs              ,
		final XGettingMap<PersistenceTypeDefinitionMember, PersistenceTypeDefinitionMember> explicitMappings,
		final MultiMatch<PersistenceTypeDefinitionMember>                                   matchedMembers  ,
		final PersistenceLegacyTypeMappingResult<?, ?>                                      result
	)
	{
		final XGettingTable<PersistenceTypeDefinitionMember, Similarity<PersistenceTypeDefinitionMember>> currentToLegacyMembers =
			result.currentToLegacyMembers()
		;
		final XGettingEnum<PersistenceTypeDefinitionMember> newCurrentMembers = result.newCurrentMembers();
		
		for(final PersistenceTypeDefinitionMember member : result.currentTypeHandler().membersInDeclaredOrder())
		{
			final Similarity<PersistenceTypeDefinitionMember> mappedLegacyMember = currentToLegacyMembers.get(member);
			if(mappedLegacyMember != null)
			{
				vs
				.add(mappedLegacyMember.sourceElement(), PrintingLegacyTypeMappingResultor::assembleMember)
				.add("\t-")
				.padRight(
					PersistenceLegacyTypeMapper.similarityToString(mappedLegacyMember),
					PersistenceLegacyTypeMapper.Defaults.defaultExplicitMappingString().length(),
					'-'
				)
				.add("-> ")
				.add(member, PrintingLegacyTypeMappingResultor::assembleMember)
				.lf()
				;
				continue;
			}
			
			if(newCurrentMembers.contains(member))
			{
				vs.add("\t[***new***] ");
				assembleMember(vs, member);
				vs.lf();
				continue;
			}
			
			// (11.10.2018 TM)EXCP: proper exception
			throw new RuntimeException("Inconsistent current type member mapping: " + member.identifier());
		}
		
		for(final PersistenceTypeDefinitionMember e : result.discardedLegacyMembers())
		{
			assembleMember(vs, e);
			vs.add("\t[discarded]").lf();
		}
		
		return vs;
	}
	
	public static final void assembleMember(final VarString vs, final PersistenceTypeDefinitionMember member)
	{
		vs.add(member.typeName()).blank().add(member.identifier());
	}
	
	public static <M> PrintingLegacyTypeMappingResultor<M> New(
		final PersistenceLegacyTypeMappingResultor<M> delegate
	)
	{
		return new PrintingLegacyTypeMappingResultor<>(
			notNull(delegate)
		);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	final PersistenceLegacyTypeMappingResultor<M> delegate;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	PrintingLegacyTypeMappingResultor(final PersistenceLegacyTypeMappingResultor<M> delegate)
	{
		super();
		this.delegate = delegate;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	public <T> PersistenceLegacyTypeMappingResult<M, T> createMappingResult(
		final PersistenceTypeDefinition                                                     legacyTypeDefinition,
		final PersistenceTypeHandler<M, T>                                                  currentTypeHandler  ,
		final XGettingMap<PersistenceTypeDefinitionMember, PersistenceTypeDefinitionMember> explicitMappings    ,
		final XGettingSet<PersistenceTypeDefinitionMember>                                  explicitNewMembers  ,
		final MultiMatch<PersistenceTypeDefinitionMember>                                   matchedMembers
	)
	{
		final PersistenceLegacyTypeMappingResult<M, T> result = this.delegate.createMappingResult(
			legacyTypeDefinition, currentTypeHandler, explicitMappings, explicitNewMembers, matchedMembers
		);
		
		final String output = assembleMappingWithHeader(explicitMappings, matchedMembers, result);
		System.out.println(output);
		
		return result;
	}
	
}

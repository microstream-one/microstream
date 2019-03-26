package one.microstream.persistence.internal;

import static one.microstream.X.notNull;

import one.microstream.chars.VarString;
import one.microstream.collections.types.XGettingMap;
import one.microstream.collections.types.XGettingSet;
import one.microstream.meta.XDebug;
import one.microstream.persistence.types.PersistenceLegacyTypeMappingResult;
import one.microstream.persistence.types.PersistenceLegacyTypeMappingResultor;
import one.microstream.persistence.types.PersistenceTypeDefinition;
import one.microstream.persistence.types.PersistenceTypeDefinitionMember;
import one.microstream.persistence.types.PersistenceTypeHandler;
import one.microstream.util.matching.MultiMatch;
import one.microstream.util.matching.MultiMatchAssembler;

public class PrintingLegacyTypeMappingResultor<M> implements PersistenceLegacyTypeMappingResultor<M>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static void printMatchedMapping(
		final PersistenceTypeDefinition                   legacyTypeDefinition,
		final PersistenceTypeHandler<?, ?>                currentTypeHandler  ,
		final MultiMatch<PersistenceTypeDefinitionMember> match
	)
	{
		System.out.println("INPUT:");
		XDebug.printCollection(legacyTypeDefinition.members(), null, "\t", null, null);
		XDebug.printCollection(currentTypeHandler.members()  , null, "\t", null, null);
		System.out.println();
		System.out.println("OUTPUT:");
		System.out.println(match.assembler().assembleMappingSchemeVertical(
			VarString.New(),
			MultiMatchAssembler.Defaults.defaultSimilarityFormatter(),
			MultiMatchAssembler.Defaults.defaultElementAssembler()
		));
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
		printMatchedMapping(legacyTypeDefinition, currentTypeHandler, matchedMembers);
		
		return this.delegate.createMappingResult(
			legacyTypeDefinition, currentTypeHandler, explicitMappings, explicitNewMembers, matchedMembers
		);
	}
	
}

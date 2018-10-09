package net.jadoth.test.legacy;

import net.jadoth.X;
import net.jadoth.chars.VarString;
import net.jadoth.collections.types.XGettingMap;
import net.jadoth.collections.types.XList;
import net.jadoth.meta.XDebug;
import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.types.PersistenceLegacyTypeMappingResult;
import net.jadoth.persistence.types.PersistenceLegacyTypeMappingResultor;
import net.jadoth.persistence.types.PersistenceMemberMatchingProvider;
import net.jadoth.persistence.types.PersistenceTypeDefinition;
import net.jadoth.persistence.types.PersistenceTypeDefinitionMember;
import net.jadoth.persistence.types.PersistenceTypeHandler;
import net.jadoth.reference.Reference;
import net.jadoth.storage.types.EmbeddedStorage;
import net.jadoth.storage.types.EmbeddedStorageManager;
import net.jadoth.test.corp.logic.Test;
import net.jadoth.util.matching.MatchValidator;
import net.jadoth.util.matching.MultiMatch;


public class MainTestStorageLegacyMapping
{
	static final Reference<XList<Object>> ROOT = X.Reference(null);
	
	// create a storage manager, link the root, start the "embedded" database
	static final EmbeddedStorageManager STORAGE = X.executeOn(EmbeddedStorage
		.createFoundation(),
		ecf -> ecf.getConnectionFoundation()
			.setLegacyTypeMappingResultor(new MappingPrinter())
			.setLegacyMemberMatchingProvider(new MatchProvider())
		)
//		.setRefactoringMappingProvider(
//			Persistence.RefactoringMapping(new File("D:/Refactorings.csv"))
//		)
		.start(ROOT)
	;
	
	static XList<Object> createTestModel()
	{
		return X.List(new SimpleClass(1, 3.14f, 'A'), new SimpleClass(2, 9.81f, 'B'));
//		return X.List(new Person());
//		return X.List(new NewClass(), new ChangedClass());
	}

	public static void main(final String[] args)
	{
		if(ROOT.get() == null)
		{
			Test.print("TEST: model data required." );
			ROOT.set(createTestModel());

			Test.print("STORAGE: storing ...");
			STORAGE.store(ROOT);
			Test.print("STORAGE: storing completed.");
		}
		else
		{
			ROOT.get().iterate(System.out::println);
		}
		System.exit(0); // no shutdown required, the storage concept is inherently crash-safe
	}
		
	static void printMatchedMapping(
		final PersistenceTypeDefinition                   legacyTypeDefinition,
		final PersistenceTypeHandler<Binary, ?>           currentTypeHandler  ,
		final MultiMatch<PersistenceTypeDefinitionMember> match
	)
	{
		System.out.println("INPUT:");
		XDebug.printCollection(legacyTypeDefinition.members(), null, "\t", null, null);
		XDebug.printCollection(currentTypeHandler.members(), null, "\t", null, null);
		System.out.println();
		System.out.println("OUTPUT:");
		System.out.println(match.assembler().assembleMappingSchemeVertical(VarString.New(), (vs, e) -> vs.add(e)));
	}
	
	static class MappingPrinter implements PersistenceLegacyTypeMappingResultor<Binary>
	{
		@Override
		public <T> PersistenceLegacyTypeMappingResult<Binary, T> createMappingResult(
			final PersistenceTypeDefinition                                                     legacyTypeDefinition,
			final PersistenceTypeHandler<Binary, T>                                             currentTypeHandler  ,
			final XGettingMap<PersistenceTypeDefinitionMember, PersistenceTypeDefinitionMember> explicitMappings    ,
			final MultiMatch<PersistenceTypeDefinitionMember>                                   matchedMembers
		)
		{
			printMatchedMapping(legacyTypeDefinition, currentTypeHandler, matchedMembers);
			return PersistenceLegacyTypeMappingResultor.super.createMappingResult(
				legacyTypeDefinition, currentTypeHandler, explicitMappings, matchedMembers
			);
		}
	}
	
	static class MatchValidator1 implements MatchValidator<PersistenceTypeDefinitionMember>
	{

		@Override
		public boolean isValidMatch(
			final PersistenceTypeDefinitionMember source              ,
			final PersistenceTypeDefinitionMember target              ,
			final double                          similarity          ,
			final int                             sourceCandidateCount,
			final int                             targetCandidateCount
		)
		{
			XDebug.debugln(
				"matching " + source.name()
				+ "\t<--"+ similarity+", "+sourceCandidateCount+"/"+targetCandidateCount+"-->\t"
				+ target.name()
			);
			
			return true;
		}
		
	}
	
	static class MatchProvider implements PersistenceMemberMatchingProvider
	{
		@Override
		public MatchValidator<PersistenceTypeDefinitionMember> provideMemberMatchValidator()
		{
			return new MatchValidator1();
		}
	}
			
}

package one.microstream.test.legacy;

import java.io.File;

import one.microstream.X;
import one.microstream.chars.VarString;
import one.microstream.collections.types.XGettingMap;
import one.microstream.collections.types.XGettingSet;
import one.microstream.collections.types.XList;
import one.microstream.meta.XDebug;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.Persistence;
import one.microstream.persistence.types.PersistenceLegacyTypeMappingResult;
import one.microstream.persistence.types.PersistenceLegacyTypeMappingResultor;
import one.microstream.persistence.types.PersistenceMemberMatchingProvider;
import one.microstream.persistence.types.PersistenceTypeDefinition;
import one.microstream.persistence.types.PersistenceTypeDefinitionMember;
import one.microstream.persistence.types.PersistenceTypeHandler;
import one.microstream.reference.Reference;
import one.microstream.storage.types.EmbeddedStorage;
import one.microstream.storage.types.EmbeddedStorageManager;
import one.microstream.test.corp.logic.Test;
import one.microstream.util.matching.MatchValidator;
import one.microstream.util.matching.MultiMatch;


public class MainTestStorageLegacyMapping
{
	static final Reference<XList<OldContact>> ROOT = X.Reference(null);
	
	// create a storage manager, link the root, start the "embedded" database
	static final EmbeddedStorageManager STORAGE = X.on(EmbeddedStorage
		.Foundation(),
		ecf -> ecf.getConnectionFoundation()
//			.setLegacyTypeMappingResultor(InquiringLegacyTypeMappingResultor.New(new MappingPrinter()))
//			.setLegacyMemberMatchingProvider(new MatchProvider())
			.setRefactoringMappingProvider(
				Persistence.RefactoringMapping(new File("Refactorings.csv"))
			)
		)
		.start(ROOT)
	;
	
	static XList<OldContact> createTestModel()
	{
		return X.List(new OldContact());
//		return X.List(
//			X.on(new SimpleClass(), e -> {
//				e.first  = 1;
//				e.second = 3.14f;
//				e.third  = 'A';
//				e.fourth = null;
//				e.fifth  = "String 1";
//			}),
//			new SimpleClass(2, 9.81f, 'B')
//		);
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
//			handleLoadedSimpleClass(ROOT.get());
			ROOT.get().iterate(System.out::println);
		}
		System.exit(0); // no shutdown required, the storage concept is inherently crash-safe
	}
	
	static void handleLoadedSimpleClass(final XList<SimpleClass> entities)
	{
		X.on(entities.get(), e -> {
			e.third = 'M';
//			e.fourth = null;
			e.fourth = new ToBeDeleted();
			STORAGE.store(e);
		});
		X.on(entities.at(1), e -> {
			e.third = '2';
//			e.fourth = null;
			e.fourth = new ToBeDeleted();
			STORAGE.store(e);
		});
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
			final XGettingSet<PersistenceTypeDefinitionMember>                                  explicitNewMembers  ,
			final MultiMatch<PersistenceTypeDefinitionMember>                                   matchedMembers
		)
		{
			printMatchedMapping(legacyTypeDefinition, currentTypeHandler, matchedMembers);
			return PersistenceLegacyTypeMappingResultor.super.createMappingResult(
				legacyTypeDefinition, currentTypeHandler, explicitMappings, explicitNewMembers, matchedMembers
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
			XDebug.println(
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

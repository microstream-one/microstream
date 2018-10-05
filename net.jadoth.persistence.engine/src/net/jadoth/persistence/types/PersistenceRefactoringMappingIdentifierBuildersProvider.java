package net.jadoth.persistence.types;

import net.jadoth.collections.types.XGettingSequence;

public interface PersistenceRefactoringMappingIdentifierBuildersProvider
{
	public XGettingSequence<? extends PersistenceRefactoringMappingIdentifierBuilder> provideSourceTypeIdentifierBuilders();
	
	public XGettingSequence<? extends PersistenceRefactoringMappingIdentifierBuilder> provideSourceMemberIdentifierBuilders();
	
	public XGettingSequence<? extends PersistenceRefactoringMappingIdentifierBuilder> provideTargetMemberIdentifierBuilders();
}

package net.jadoth.graphConsolidator;

import net.jadoth.hash.HashEqualator;

public interface SubjectEqualityProvider
{
	public <S> HashEqualator<? super S> providerEqualator(Class<S> subjectType);
}

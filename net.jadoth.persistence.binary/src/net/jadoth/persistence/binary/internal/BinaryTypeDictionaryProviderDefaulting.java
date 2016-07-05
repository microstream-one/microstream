package net.jadoth.persistence.binary.internal;

import net.jadoth.persistence.binary.types.BinaryPersistence;
import net.jadoth.persistence.types.PersistenceTypeDictionary;
import net.jadoth.persistence.types.PersistenceTypeDictionaryProvider;
import net.jadoth.swizzling.types.Swizzle;

public final class BinaryTypeDictionaryProviderDefaulting implements PersistenceTypeDictionaryProvider
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields  //
	/////////////////////

	private final PersistenceTypeDictionaryProvider delegate;



	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public BinaryTypeDictionaryProviderDefaulting(final PersistenceTypeDictionaryProvider delegate)
	{
		super();
		this.delegate = delegate;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public final PersistenceTypeDictionary provideDictionary()
	{
		// intended to auto-populate a default type dictionary for an initial empty type dictionary source
		PersistenceTypeDictionary typeDictionary = this.delegate.provideDictionary();
		if(typeDictionary.types().isEmpty())
		{
			typeDictionary = BinaryPersistence.createDefaultTypeDictionary(
				typeDictionary,
				Swizzle.createDefaultTypeLookup()
			);
		}
		return typeDictionary;
	}

}

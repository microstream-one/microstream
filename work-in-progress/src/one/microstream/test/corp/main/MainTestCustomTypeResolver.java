package one.microstream.test.corp.main;

import one.microstream.persistence.types.PersistenceTypeResolver;
import one.microstream.storage.types.EmbeddedStorage;
import one.microstream.storage.types.EmbeddedStorageManager;


public class MainTestCustomTypeResolver
{
	public static void main(final String[] args)
	{
		// start with custom type resolver
		final EmbeddedStorageManager storage = EmbeddedStorage
			.Foundation()
			.onConnectionFoundation(e ->
				e.setTypeResolver(new ReroutingTypeResolver())
			)
			.start()
		;
	}
	
}

class ReroutingTypeResolver implements PersistenceTypeResolver
{
	@Override
	public Class<?> resolveType(final String typeName)
	{
		// simple example for rerouted type name
		final String reroutedTypeName = typeName.replace(".test1.", ".test2.");
		return PersistenceTypeResolver.super.resolveType(reroutedTypeName);
	}
	
	// maybe more of the interface's methods need to reroute, not sure.
}
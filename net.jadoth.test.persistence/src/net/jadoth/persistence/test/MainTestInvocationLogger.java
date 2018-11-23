package net.jadoth.persistence.test;

import net.jadoth.persistence.types.PersistenceObjectIdProvider;


public class MainTestInvocationLogger extends InvocationLogging
{
	static final PersistenceObjectIdProvider OID_PROVIDER = dispatch(PersistenceObjectIdProvider.Transient());

	public static void main(final String[] args)
	{
		OID_PROVIDER.provideNextObjectId();
		OID_PROVIDER.provideNextObjectId();
		OID_PROVIDER.provideNextObjectId();
	}

}

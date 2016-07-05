package net.jadoth.persistence.test;

import net.jadoth.swizzling.types.SwizzleObjectIdProvider;


public class MainTestInvocationLogger extends InvocationLogging
{
	static final SwizzleObjectIdProvider OID_PROVIDER = dispatch(new TransientOidProvider());

	public static void main(final String[] args)
	{
		OID_PROVIDER.provideNextObjectId();
		OID_PROVIDER.provideNextObjectId();
		OID_PROVIDER.provideNextObjectId();
	}

}

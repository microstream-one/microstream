package net.jadoth.persistence.test;

import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.binary.types.BinaryPersistenceFoundation;
import net.jadoth.persistence.internal.TransientOidProvider;
import net.jadoth.persistence.types.PersistenceSource;
import net.jadoth.persistence.types.PersistenceTarget;
import net.jadoth.swizzling.types.SwizzleObjectIdProvider;
import net.jadoth.swizzling.types.SwizzleTypeIdProvider;

public class TestPersistenceFactory extends BinaryPersistenceFoundation.Implementation<TestPersistenceFactory>
{
	protected TestPersistenceFactory()
	{
		super();
	}

	@Override
	protected SwizzleObjectIdProvider createObjectIdProvider()
	{
		return TransientOidProvider.New();
	}

	@Override
	protected SwizzleTypeIdProvider createTypeIdProvider()
	{
		return new TransientTidProvider();
	}

	@Override
	protected PersistenceTarget<Binary> createPersistenceTarget()
	{
		// FIXME AbstractImplementation<Binary>#createPersistenceTarget()
		throw new net.jadoth.meta.NotImplementedYetError();
	}

	@Override
	protected PersistenceSource<Binary> createPersistenceSource()
	{
		// FIXME AbstractImplementation<Binary>#createPersistenceSource()
		throw new net.jadoth.meta.NotImplementedYetError();
	}

}

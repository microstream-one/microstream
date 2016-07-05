package net.jadoth.persistence.test;

import net.jadoth.swizzling.types.Swizzle;
import net.jadoth.swizzling.types.SwizzleObjectIdProvider;


/**
 * Simple thread UNsafe, volatile (i.e. non-persistent) implementation.
 * Useful only for testing and debugging.
 *
 * @author Thomas Muenz
 */
public class TransientOidProvider implements SwizzleObjectIdProvider
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields  //
	/////////////////////

	private long oid;



	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public TransientOidProvider()
	{
		this(Swizzle.defaultStartObjectId());
	}

	public TransientOidProvider(final long oid)
	{
		super();
		this.oid = Swizzle.validateObjectId(oid);
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public long provideNextObjectId()
	{
		return ++this.oid;
	}

	@Override
	public long currentObjectId()
	{
		return this.oid;
	}

	@Override
	public TransientOidProvider initializeObjectId()
	{
		return this;
	}

	@Override
	public SwizzleObjectIdProvider updateCurrentObjectId(final long currentObjectId)
	{
		this.oid = currentObjectId;
		return this;
	}

}

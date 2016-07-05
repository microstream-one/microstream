package net.jadoth.persistence.test;

import net.jadoth.swizzling.types.Swizzle;
import net.jadoth.swizzling.types.SwizzleTypeIdProvider;


/**
 * Simple thread UNsafe, volatile (i.e. non-persistent) implementation.
 * Useful only for testing and debugging.
 *
 * @author Thomas Muenz
 */
public class TransientTidProvider implements SwizzleTypeIdProvider
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields  //
	/////////////////////

	private long tid;



	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public TransientTidProvider()
	{
		this(Swizzle.defaultStartTypeId());
	}

	public TransientTidProvider(final long tid)
	{
		super();
		this.tid = Swizzle.validateTypeId(tid);
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public long provideNextTypeId()
	{
		return ++this.tid;
	}

	@Override
	public long currentTypeId()
	{
		return this.tid;
	}

	@Override
	public TransientTidProvider initializeTypeId()
	{
		return this;
	}

	@Override
	public SwizzleTypeIdProvider updateCurrentTypeId(final long currentTypeId)
	{
		this.tid = currentTypeId;
		return this;
	}

}

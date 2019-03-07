package net.jadoth.persistence.test;

import net.jadoth.persistence.types.PersistenceTypeIdProvider;
import net.jadoth.persistence.types.Persistence;


/**
 * Simple thread UNsafe, volatile (i.e. non-persistent) implementation.
 * Useful only for testing and debugging.
 *
 * @author Thomas Muenz
 */
public class TransientTidProvider implements PersistenceTypeIdProvider
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
		this(Persistence.defaultStartTypeId());
	}

	public TransientTidProvider(final long tid)
	{
		super();
		this.tid = Persistence.validateTypeId(tid);
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
	public PersistenceTypeIdProvider updateCurrentTypeId(final long currentTypeId)
	{
		this.tid = currentTypeId;
		return this;
	}

}

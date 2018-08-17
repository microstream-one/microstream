package net.jadoth.persistence.internal;

import net.jadoth.swizzling.types.Swizzle;
import net.jadoth.swizzling.types.SwizzleObjectIdProvider;


/**
 * Simple thread UNsafe, volatile (i.e. non-persistent) implementation.
 *
 * @author Thomas Muenz
 */
public class TransientOidProvider implements SwizzleObjectIdProvider
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static TransientOidProvider New()
	{
		return new TransientOidProvider(Swizzle.defaultStartObjectId());
	}
	
	public static TransientOidProvider New(final long startingOid)
	{
		return new TransientOidProvider(startingOid);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields  //
	/////////////////////

	private long oid;



	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	protected TransientOidProvider(final long startingOid)
	{
		super();
		this.oid = Swizzle.validateObjectId(startingOid);
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

package net.jadoth.persistence.binary.internal;

import static net.jadoth.X.notNull;

import net.jadoth.collections.types.XGettingCollection;
import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.exceptions.PersistenceExceptionTransfer;
import net.jadoth.persistence.types.PersistenceSource;
import net.jadoth.persistence.types.PersistenceTarget;
import net.jadoth.swizzling.types.SwizzleIdSet;

public final class BinaryFileStorage implements PersistenceSource<Binary>, PersistenceTarget<Binary>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields  //
	/////////////////////

	private final PersistenceSource<Binary> source;
	private final PersistenceTarget<Binary> target;



	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public BinaryFileStorage(final PersistenceSource<Binary> source, final PersistenceTarget<Binary> target)
	{
		super();
		this.source = notNull(source);
		this.target = notNull(target);
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public final void write(final Binary[] data) throws PersistenceExceptionTransfer
	{
		this.target.write(data);
	}

	@Override
	public final XGettingCollection<? extends Binary> readInitial() throws PersistenceExceptionTransfer
	{
		return this.source.readInitial();
	}

	@Override
	public final XGettingCollection<? extends Binary> readByObjectIds(final SwizzleIdSet[] oids)
		throws PersistenceExceptionTransfer
	{
		return this.source.readByObjectIds(oids);
	}

//	@Override
//	public XGettingCollection<? extends Binary> readByTypeId(final long typeId) throws PersistenceExceptionTransfer
//	{
//		return this.source.readByTypeId(typeId);
//	}

}

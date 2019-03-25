package one.microstream.persistence.binary.internal;

import static one.microstream.X.notNull;

import one.microstream.collections.types.XGettingCollection;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.exceptions.PersistenceExceptionTransfer;
import one.microstream.persistence.types.PersistenceChannel;
import one.microstream.persistence.types.PersistenceIdSet;
import one.microstream.persistence.types.PersistenceSource;
import one.microstream.persistence.types.PersistenceTarget;

public final class BinaryFileStorage implements PersistenceChannel<Binary>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields  //
	/////////////////////

	private final PersistenceSource<Binary> source;
	private final PersistenceTarget<Binary> target;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

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
	public final void write(final Binary data) throws PersistenceExceptionTransfer
	{
		this.target.write(data);
	}

	@Override
	public final XGettingCollection<? extends Binary> read() throws PersistenceExceptionTransfer
	{
		return this.source.read();
	}

	@Override
	public final XGettingCollection<? extends Binary> readByObjectIds(final PersistenceIdSet[] oids)
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

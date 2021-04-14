package one.microstream.persistence.internal;

import static one.microstream.X.notNull;
import static one.microstream.math.XMath.positive;

import one.microstream.afs.types.AFile;
import one.microstream.persistence.types.Persistence;
import one.microstream.persistence.types.PersistenceObjectIdProvider;


public final class FileObjectIdProvider extends AbstractIdProviderByFile implements PersistenceObjectIdProvider
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	public static FileObjectIdProvider New(final AFile file, final long increase)
	{
		return new FileObjectIdProvider(
			 notNull(file)                ,
			positive(increase)            ,
			Persistence.defaultStartObjectId()
		);
	}

	public static FileObjectIdProvider New(final AFile file, final long increase, final long startId)
	{
		return new FileObjectIdProvider(
			 notNull(file)                   ,
			positive(increase)               ,
			Persistence.validateObjectId(startId)
		);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	FileObjectIdProvider(final AFile file, final long increase, final long startId)
	{
		super(file, increase, startId);
	}
	
	

	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public final long provideNextObjectId()
	{
		return this.next();
	}

	@Override
	public final long currentObjectId()
	{
		return this.current();
	}

	@Override
	public final FileObjectIdProvider initializeObjectId()
	{
		this.internalInitialize();
		return this;
	}

	@Override
	public FileObjectIdProvider updateCurrentObjectId(final long currentObjectId)
	{
		this.internalUpdateId(currentObjectId);
		return this;
	}

}

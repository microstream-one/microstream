package one.microstream.persistence.internal;

import static one.microstream.X.notNull;
import static one.microstream.math.XMath.positive;

import one.microstream.afs.types.AFile;
import one.microstream.persistence.types.Persistence;
import one.microstream.persistence.types.PersistenceTypeIdProvider;


public final class FileTypeIdProvider extends AbstractIdProviderByFile implements PersistenceTypeIdProvider
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	public static FileTypeIdProvider New(final AFile file, final long increase)
	{
		return new FileTypeIdProvider(
			 notNull(file)              ,
			positive(increase)          ,
			Persistence.defaultStartTypeId()
		);
	}

	public static FileTypeIdProvider New(final AFile file, final long increase, final long startId)
	{
		return new FileTypeIdProvider(
			 notNull(file)                 ,
			positive(increase)             ,
			Persistence.validateTypeId(startId)
		);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	FileTypeIdProvider(final AFile file, final long increase, final long startId)
	{
		super(file, increase, startId);
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public final long provideNextTypeId()
	{
		return this.next();
	}

	@Override
	public final long currentTypeId()
	{
		return this.current();
	}

	@Override
	public final FileTypeIdProvider initializeTypeId()
	{
		this.internalInitialize();
		return this;
	}

	@Override
	public FileTypeIdProvider updateCurrentTypeId(final long currentTypeId)
	{
		this.internalUpdateId(currentTypeId);
		return this;
	}

}

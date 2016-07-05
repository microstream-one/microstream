package net.jadoth.persistence.internal;

import java.io.File;

import net.jadoth.swizzling.types.Swizzle;
import net.jadoth.swizzling.types.SwizzleObjectIdProvider;


public final class FileObjectIdProvider extends AbstractIdProviderByFile implements SwizzleObjectIdProvider
{
	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public FileObjectIdProvider(final File file)
	{
		this(file, DEFAULT_INCREASE);
	}

	public FileObjectIdProvider(final File file, final long increase)
	{
		this(file, increase, Swizzle.defaultStartObjectId());
	}

	public FileObjectIdProvider(final File file, final long increase, final long id)
	{
		super(file, increase, Swizzle.validateObjectId(id));
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

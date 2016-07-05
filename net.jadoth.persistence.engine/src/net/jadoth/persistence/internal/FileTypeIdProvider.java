package net.jadoth.persistence.internal;

import java.io.File;

import net.jadoth.swizzling.types.Swizzle;
import net.jadoth.swizzling.types.SwizzleTypeIdProvider;


public final class FileTypeIdProvider extends AbstractIdProviderByFile implements SwizzleTypeIdProvider
{
	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public FileTypeIdProvider(final File file)
	{
		this(file, DEFAULT_INCREASE);
	}

	public FileTypeIdProvider(final File file, final long increase)
	{
		this(file, increase, Swizzle.defaultStartTypeId());
	}

	public FileTypeIdProvider(final File file, final long increase, final long id)
	{
		super(file, increase, Swizzle.validateTypeId(id));
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

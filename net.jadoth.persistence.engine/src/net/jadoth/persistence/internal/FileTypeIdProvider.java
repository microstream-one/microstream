package net.jadoth.persistence.internal;

import static net.jadoth.X.notNull;
import static net.jadoth.math.XMath.positive;

import java.io.File;

import net.jadoth.swizzling.types.Swizzle;
import net.jadoth.swizzling.types.SwizzleTypeIdProvider;


public final class FileTypeIdProvider extends AbstractIdProviderByFile implements SwizzleTypeIdProvider
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static FileTypeIdProvider New(final File file)
	{
		return new FileTypeIdProvider(
			notNull(file)               ,
			DEFAULT_INCREASE            ,
			Swizzle.defaultStartTypeId()
		);
	}

	public static FileTypeIdProvider New(final File file, final long increase)
	{
		return new FileTypeIdProvider(
			 notNull(file)              ,
			positive(increase)          ,
			Swizzle.defaultStartTypeId()
		);
	}

	public static FileTypeIdProvider New(final File file, final long increase, final long startId)
	{
		return new FileTypeIdProvider(
			 notNull(file)                 ,
			positive(increase)             ,
			Swizzle.validateTypeId(startId)
		);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	FileTypeIdProvider(final File file, final long increase, final long startId)
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

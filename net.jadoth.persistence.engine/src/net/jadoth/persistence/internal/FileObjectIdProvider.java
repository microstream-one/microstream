package net.jadoth.persistence.internal;

import static net.jadoth.X.notNull;
import static net.jadoth.math.XMath.positive;

import java.io.File;

import net.jadoth.swizzling.types.Swizzle;
import net.jadoth.swizzling.types.SwizzleObjectIdProvider;


public final class FileObjectIdProvider extends AbstractIdProviderByFile implements SwizzleObjectIdProvider
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static FileObjectIdProvider New(final File file)
	{
		return new FileObjectIdProvider(
			notNull(file)                 ,
			DEFAULT_INCREASE              ,
			Swizzle.defaultStartObjectId()
		);
	}

	public static FileObjectIdProvider New(final File file, final long increase)
	{
		return new FileObjectIdProvider(
			 notNull(file)                ,
			positive(increase)            ,
			Swizzle.defaultStartObjectId()
		);
	}

	public static FileObjectIdProvider New(final File file, final long increase, final long startId)
	{
		return new FileObjectIdProvider(
			 notNull(file)                   ,
			positive(increase)               ,
			Swizzle.validateObjectId(startId)
		);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	FileObjectIdProvider(final File file, final long increase, final long startId)
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

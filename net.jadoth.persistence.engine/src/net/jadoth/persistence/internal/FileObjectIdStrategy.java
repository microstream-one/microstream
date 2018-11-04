package net.jadoth.persistence.internal;

import static net.jadoth.X.notNull;

import java.io.File;

import net.jadoth.persistence.types.Persistence;
import net.jadoth.swizzling.types.SwizzleObjectIdProvider;
import net.jadoth.swizzling.types.SwizzleObjectIdStrategy;

public class FileObjectIdStrategy implements SwizzleObjectIdStrategy
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static FileObjectIdStrategy NewInDirectory(final File directory)
	{
		return New(
			directory                            ,
			Persistence.defaultFilenameObjectId()
		);
	}
	
	public static FileObjectIdStrategy New(final File directory, final String objectIdFilename)
	{
		return new FileObjectIdStrategy(
			new File(directory, objectIdFilename)
		);
	}
	
	
	public static FileObjectIdStrategy New(final File objectIdFile)
	{
		return new FileObjectIdStrategy(
			notNull(objectIdFile)
		);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final File objectIdFile;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	FileObjectIdStrategy(final File objectIdFile)
	{
		super();
		this.objectIdFile = objectIdFile;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	public SwizzleObjectIdProvider createObjectIdProvider()
	{
		return FileObjectIdProvider.New(this.objectIdFile);
	}
	
}

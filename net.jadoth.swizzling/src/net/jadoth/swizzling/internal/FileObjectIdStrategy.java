package net.jadoth.swizzling.internal;

import static net.jadoth.X.notNull;

import java.io.File;

import net.jadoth.swizzling.types.SwizzleObjectIdProvider;
import net.jadoth.swizzling.types.SwizzleObjectIdStrategy;

public class FileObjectIdStrategy implements SwizzleObjectIdStrategy
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static String defaultFilename()
	{
		// why permanently occupy additional memory with fields and instances for constant values?
		return "ObjectId.oid";
	}
	
	public static FileObjectIdStrategy NewInDirectory(final File directory)
	{
		return New(
			directory        ,
			defaultFilename()
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

package net.jadoth.swizzling.internal;

import static net.jadoth.X.notNull;

import java.io.File;

import net.jadoth.swizzling.types.SwizzleTypeIdProvider;
import net.jadoth.swizzling.types.SwizzleTypeIdStrategy;

public class FileTypeIdStrategy implements SwizzleTypeIdStrategy
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static String defaultFilename()
	{
		// why permanently occupy additional memory with fields and instances for constant values?
		return "TypeId.tid";
	}
	
	public static FileTypeIdStrategy NewInDirectory(final File directory)
	{
		return New(
			directory        ,
			defaultFilename()
		);
	}
	
	public static FileTypeIdStrategy New(final File directory, final String typeIdFilename)
	{
		return new FileTypeIdStrategy(
			new File(directory, typeIdFilename)
		);
	}
	
	
	public static FileTypeIdStrategy New(final File typeIdFile)
	{
		return new FileTypeIdStrategy(
			notNull(typeIdFile)
		);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final File typeIdFile;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	FileTypeIdStrategy(final File typeIdFile)
	{
		super();
		this.typeIdFile = typeIdFile;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	public SwizzleTypeIdProvider createTypeIdProvider()
	{
		return FileTypeIdProvider.New(this.typeIdFile);
	}
	
}

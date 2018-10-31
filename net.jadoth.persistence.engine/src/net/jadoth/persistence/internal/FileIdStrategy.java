package net.jadoth.persistence.internal;

import static net.jadoth.X.notNull;

import java.io.File;

import net.jadoth.persistence.types.Persistence;
import net.jadoth.swizzling.types.SwizzleIdStrategy;
import net.jadoth.swizzling.types.SwizzleObjectIdProvider;
import net.jadoth.swizzling.types.SwizzleTypeIdProvider;

public class FileIdStrategy implements SwizzleIdStrategy
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static FileIdStrategy New(final File directory)
	{
		return New(
			directory                            ,
			Persistence.defaultFilenameObjectId(),
			Persistence.defaultFilenameTypeId()
		);
	}
	
	public static FileIdStrategy New(final File directory, final String objectIdFilename, final String typeIdFilename)
	{
		return new FileIdStrategy(
			new File(directory, objectIdFilename),
			new File(directory, typeIdFilename)
		);
	}
	
	
	public static FileIdStrategy New(final File objectIdFile, final File typeIdFile)
	{
		return new FileIdStrategy(
			notNull(objectIdFile),
			notNull(typeIdFile)
		);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final File objectIdFile;
	private final File typeIdFile  ;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	FileIdStrategy(final File objectIdFile, final File typeIdFile)
	{
		super();
		this.objectIdFile = objectIdFile;
		this.typeIdFile   = typeIdFile  ;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public SwizzleObjectIdProvider createObjectIdProvider()
	{
		return FileObjectIdProvider.New(this.objectIdFile);
	}

	@Override
	public SwizzleTypeIdProvider createTypeIdProvider()
	{
		return FileTypeIdProvider.New(this.typeIdFile);
	}
	
}

package net.jadoth.persistence.internal;

import static net.jadoth.X.notNull;

import java.io.File;

import net.jadoth.persistence.types.PersistenceTypeIdProvider;
import net.jadoth.persistence.types.PersistenceTypeIdStrategy;

public class FileTypeIdStrategy implements PersistenceTypeIdStrategy
{
	///////////////////////////////////////////////////////////////////////////
	// constants        //
	/////////////////////

	private static final long DEFAULT_INCREASE = 1000;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static String strategyTypeName()
	{
		// intentionally not the class name since it must stay the same, even if the class should get renamed.
		return "FilePersistence";
	}
	
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
	public final String strategyTypeNameTypeId()
	{
		return FileTypeIdStrategy.strategyTypeName();
	}
	
	@Override
	public final PersistenceTypeIdProvider createTypeIdProvider()
	{
		return FileTypeIdProvider.New(this.typeIdFile, DEFAULT_INCREASE);
	}
	
}

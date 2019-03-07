package one.microstream.persistence.internal;

import static one.microstream.X.notNull;

import java.io.File;

import one.microstream.persistence.types.PersistenceObjectIdProvider;
import one.microstream.persistence.types.PersistenceObjectIdStrategy;

public final class FileObjectIdStrategy implements PersistenceObjectIdStrategy
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
	public final String strategyTypeNameObjectId()
	{
		return FileObjectIdStrategy.strategyTypeName();
	}
	
	@Override
	public final PersistenceObjectIdProvider createObjectIdProvider()
	{
		return FileObjectIdProvider.New(this.objectIdFile, DEFAULT_INCREASE);
	}
	
}

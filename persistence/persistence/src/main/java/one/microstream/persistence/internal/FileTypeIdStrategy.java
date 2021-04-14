package one.microstream.persistence.internal;

import static one.microstream.X.notNull;

import one.microstream.afs.types.ADirectory;
import one.microstream.afs.types.AFile;
import one.microstream.persistence.types.PersistenceTypeIdProvider;
import one.microstream.persistence.types.PersistenceTypeIdStrategy;

public class FileTypeIdStrategy implements PersistenceTypeIdStrategy
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////

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
	
	public static FileTypeIdStrategy NewInDirectory(final ADirectory directory)
	{
		return New(
			directory        ,
			defaultFilename()
		);
	}
	
	public static FileTypeIdStrategy New(final ADirectory directory, final String typeIdFilename)
	{
		return New(
			directory.ensureFile(typeIdFilename)
		);
	}
	
	
	public static FileTypeIdStrategy New(final AFile typeIdFile)
	{
		return new FileTypeIdStrategy(
			notNull(typeIdFile)
		);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final AFile typeIdFile;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	FileTypeIdStrategy(final AFile typeIdFile)
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

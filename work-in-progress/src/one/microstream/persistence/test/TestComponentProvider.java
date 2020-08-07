package one.microstream.persistence.test;

import static one.microstream.X.notNull;

import one.microstream.afs.ADirectory;
import one.microstream.afs.AFS;
import one.microstream.afs.nio.NioFileSystem;
import one.microstream.io.XIO;
import one.microstream.persistence.binary.internal.BinaryStorageChannel;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.internal.CompositeIdProvider;
import one.microstream.persistence.internal.FileObjectIdStrategy;
import one.microstream.persistence.internal.FileTypeIdStrategy;
import one.microstream.persistence.internal.PersistenceTypeDictionaryFileHandler;
import one.microstream.persistence.types.Persistence;
import one.microstream.persistence.types.PersistenceFoundation;

public class TestComponentProvider extends InvocationLogging
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////

	protected static final ADirectory TEST_DIRECTORY = NioFileSystem.New().ensureDirectory(XIO.Path("c:/Files/"));

	// application- (test-) specific components //
	protected static final TestComponentProvider TEST = new TestComponentProvider(
		 AFS.ensureExists(TEST_DIRECTORY)
		,Persistence.defaultFilenameTypeDictionary()
		,FileObjectIdStrategy.defaultFilename()
		,FileTypeIdStrategy.defaultFilename()
	);


	protected static Object testObjects()
	{
		return TestBinaryObjects.objects;
	}



	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final ADirectory directory;
	private final String     filenameTypeDictionary;
	private final String     filenameTypeId;
	private final String     filenameObjectId;

	private String filenameData;

	private transient CompositeIdProvider                  idProvider         = null;
	private transient BinaryStorageChannel                    persistenceStorage = null;
	private transient PersistenceTypeDictionaryFileHandler dictionaryStorage  = null;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	protected TestComponentProvider()
	{
		this(null, null, null, null, null); // trolling default constructor, lol
	}

	protected TestComponentProvider(
		final ADirectory directory,
		final String     filenameTypeDictionary,
		final String     filenameTypeId,
		final String     filenameObjectId
	)
	{
		this(
			directory             ,
			filenameTypeDictionary,
			filenameTypeId        ,
			filenameObjectId      ,
			null
		);
	}

	TestComponentProvider(
		final ADirectory directory,
		final String     filenameTypeDictionary,
		final String     filenameTypeId,
		final String     filenameObjectId,
		final String     filenameData
	)
	{
		super();
		this.directory              = notNull(directory)             ;
		this.filenameTypeDictionary = notNull(filenameTypeDictionary);
		this.filenameTypeId         = notNull(filenameTypeId)        ;
		this.filenameObjectId       = notNull(filenameObjectId)      ;
		this.filenameData           =         filenameData           ;
	}


	public final TestComponentProvider setFilenameData(final String filenameData)
	{
		this.filenameData = filenameData;
		return this;
	}

	final CompositeIdProvider idProvider()
	{
		if(this.idProvider == null)
		{
			this.idProvider = CompositeIdProvider.New(
				dispatch(FileTypeIdStrategy.New  (this.directory.ensureFile(this.filenameTypeId  )).createTypeIdProvider()),
				dispatch(FileObjectIdStrategy.New(this.directory.ensureFile(this.filenameObjectId)).createObjectIdProvider())
			).initialize();
		}
		return this.idProvider;
	}

	final BinaryStorageChannel persistenceStorage()
	{
		if(this.persistenceStorage == null && this.filenameData != null)
		{
			this.persistenceStorage = new BinaryStorageChannel(
				dispatch(new DEBUG_BinaryFileSource(System.out, this.directory.ensureFile(this.filenameData))),
				dispatch(new DEBUG_BinaryFileTarget(System.out, this.directory.ensureFile(this.filenameData)))
			);
		}
		return this.persistenceStorage;
	}

	final PersistenceTypeDictionaryFileHandler dictionaryStorage()
	{
		if(this.dictionaryStorage == null)
		{
			this.dictionaryStorage = dispatch(
				PersistenceTypeDictionaryFileHandler.New(
					this.directory.ensureFile(this.filenameTypeDictionary)
				)
			);
		}
		return this.dictionaryStorage;
	}

	public final <F extends PersistenceFoundation<Binary, ?>> F initialize(final F foundation)
	{
		foundation
			.setIdProvider              (this.idProvider()         )
			.setTypeDictionaryIoHandling(this.dictionaryStorage()  )
			.setPersistenceChannel      (this.persistenceStorage() )
			.setTypeEvaluatorPersistable(Persistence::isPersistable)
		;
		return foundation;
	}

}

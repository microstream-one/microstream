package net.jadoth.persistence.test;

import static net.jadoth.X.notNull;
import static net.jadoth.files.XFiles.ensureDirectory;

import java.io.File;

import net.jadoth.persistence.binary.internal.BinaryFileStorage;
import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.internal.FileObjectIdProvider;
import net.jadoth.persistence.internal.CompositeSwizzleIdProvider;
import net.jadoth.persistence.internal.FileTypeIdProvider;
import net.jadoth.persistence.internal.PersistenceTypeDictionaryFileHandler;
import net.jadoth.persistence.types.Persistence;
import net.jadoth.persistence.types.PersistenceFoundation;

public class TestComponentProvider extends InvocationLogging
{
	///////////////////////////////////////////////////////////////////////////
	// constants        //
	/////////////////////

	protected static final File TEST_DIRECTORY = new File("c:/Files/");

	// application- (test-) specific components //
	protected static final TestComponentProvider TEST = new TestComponentProvider(
		 ensureDirectory(TEST_DIRECTORY)
		,Persistence.defaultFilenameTypeDictionary()
		,Persistence.defaultFilenameTypeId()
		,Persistence.defaultFilenameObjectId()
	);


	protected static Object testObjects()
	{
		return TestBinaryObjects.objects;
	}



	///////////////////////////////////////////////////////////////////////////
	// instance fields  //
	/////////////////////

	private final File directory;
	private final String filenameTypeDictionary;
	private final String filenameTypeId;
	private final String filenameObjectId;

	private String filenameData;

	private transient CompositeSwizzleIdProvider         swizzleIdProvider  = null;
	private transient BinaryFileStorage             persistenceStorage = null;
	private transient PersistenceTypeDictionaryFileHandler dictionaryStorage  = null;



	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	protected TestComponentProvider()
	{
		this(null, null, null, null, null); // trolling default constructor, lol
	}

	protected TestComponentProvider(
		final File   directory,
		final String filenameTypeDictionary,
		final String filenameTypeId,
		final String filenameObjectId
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
		final File directory,
		final String filenameTypeDictionary,
		final String filenameTypeId,
		final String filenameObjectId,
		final String filenameData
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

	final CompositeSwizzleIdProvider swizzleIdProvider()
	{
		if(this.swizzleIdProvider == null)
		{
			this.swizzleIdProvider = new CompositeSwizzleIdProvider(
				dispatch(new FileTypeIdProvider    (new File(this.directory, this.filenameTypeId    ))),
				dispatch(new FileObjectIdProvider  (new File(this.directory, this.filenameObjectId  )))
			).initialize();
		}
		return this.swizzleIdProvider;
	}

	final BinaryFileStorage persistenceStorage()
	{
		if(this.persistenceStorage == null && this.filenameData != null)
		{
			this.persistenceStorage = new BinaryFileStorage(
				dispatch(new DEBUG_BinaryFileSource(System.out, new File(this.directory, this.filenameData))),
				dispatch(new DEBUG_BinaryFileTarget(System.out, new File(this.directory, this.filenameData)))
			);
		}
		return this.persistenceStorage;
	}

	final PersistenceTypeDictionaryFileHandler dictionaryStorage()
	{
		if(this.dictionaryStorage == null)
		{
			this.dictionaryStorage = dispatch(
				PersistenceTypeDictionaryFileHandler.New(new File(this.directory, this.filenameTypeDictionary))
			);
		}
		return this.dictionaryStorage;
	}

	public final <F extends PersistenceFoundation<Binary>> F initialize(final F foundation)
	{
		foundation
			.setSwizzleIdProvider          (this.swizzleIdProvider()     )
			.setDictionaryStorage          (this.dictionaryStorage()     )
			.setPersistenceChannel         (this.persistenceStorage()    )
			.setTypeEvaluatorPersistable   (Persistence::isPersistable   )
			.setTypeEvaluatorTypeIdMappable(Persistence::isTypeIdMappable)
		;
		return foundation;
	}

}

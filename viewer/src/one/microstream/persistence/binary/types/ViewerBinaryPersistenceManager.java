package one.microstream.persistence.binary.types;

import java.nio.ByteOrder;
import java.util.function.Consumer;

import one.microstream.persistence.binary.types.BinaryLoader.CreatorSimple;
import one.microstream.persistence.types.Persistence;
import one.microstream.persistence.types.PersistenceAcceptor;
import one.microstream.persistence.types.PersistenceContextDispatcher;
import one.microstream.persistence.types.PersistenceLoader;
import one.microstream.persistence.types.PersistenceManager;
import one.microstream.persistence.types.PersistenceObjectRegistry;
import one.microstream.persistence.types.PersistenceRegisterer;
import one.microstream.persistence.types.PersistenceRetrieving;
import one.microstream.persistence.types.PersistenceSource;
import one.microstream.persistence.types.PersistenceStorer;
import one.microstream.persistence.types.PersistenceStorer.Creator;
import one.microstream.persistence.types.PersistenceTarget;
import one.microstream.persistence.types.PersistenceTypeDefinition;
import one.microstream.persistence.types.PersistenceTypeDictionary;

public class ViewerBinaryPersistenceManager implements PersistenceManager<Binary>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final PersistenceManager<Binary> persistenceManager;
	private final PersistenceObjectRegistry objectRegistry;
	private final PersistenceLoader.Creator<Binary> loaderCreator;
	private final PersistenceContextDispatcher<Binary> contextDispatcher;
	private final ViewerBinaryTypeHandlerManager typeHandlerManager;
	private final PersistenceObjectRegistry constantRegistry;

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public ViewerBinaryPersistenceManager(final PersistenceManager<Binary> persistenceManager)
	{
		super();

		this.persistenceManager = persistenceManager;
		this.objectRegistry = new ViewerObjectRegistryDisabled();
		this.constantRegistry = PersistenceObjectRegistry.New();
		Persistence.registerJavaConstants(this.constantRegistry);

		this.loaderCreator = new CreatorSimple(persistenceManager.isByteOrderMismatch());
		this.contextDispatcher = PersistenceContextDispatcher.PassThrough();

		this.typeHandlerManager = new ViewerBinaryTypeHandlerManager(persistenceManager);
	}

	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public long ensureObjectId(final Object object, final PersistenceAcceptor newObjectIdCallback)
	{
		return this.persistenceManager.ensureObjectId(object, newObjectIdCallback);
	}

	@Override
	public void consolidate()
	{
		this.persistenceManager.consolidate();
	}

	@Override
	public long lookupObjectId(final Object object)
	{
		return this.persistenceManager.lookupObjectId(object);
	}

	@Override
	public Object lookupObject(final long objectId)
	{
		return this.persistenceManager.lookupObject(objectId);
	}

	@Override
	public Object get()
	{
		return this.persistenceManager.get();
	}

	@Override
	public Object getObject(final long objectId)
	{
		return this.persistenceManager.getObject(objectId);
	}

	@Override
	public <C extends Consumer<Object>> C collect(final C collector, final long... objectIds)
	{
		return this.persistenceManager.collect(collector, objectIds);
	}

	@Override
	public long store(final Object instance)
	{
		return this.persistenceManager.store(instance);
	}

	@Override
	public long[] storeAll(final Object... instances)
	{
		return this.persistenceManager.storeAll(instances);
	}

	@Override
	public void storeAll(final Iterable<?> instances)
	{
		this.persistenceManager.storeAll(instances);
	}

	@Override
	public PersistenceRegisterer createRegisterer()
	{
		return this.persistenceManager.createRegisterer();
	}

	@Override
	public PersistenceLoader createLoader()
	{
		return this.persistenceManager.createLoader();
	}

	@Override
	public PersistenceStorer createLazyStorer()
	{
		return this.persistenceManager.createLazyStorer();
	}

	@Override
	public PersistenceStorer createStorer()
	{
		return this.persistenceManager.createStorer();
	}

	@Override
	public PersistenceStorer createEagerStorer()
	{
		return this.persistenceManager.createEagerStorer();
	}

	@Override
	public PersistenceStorer createStorer(final Creator<Binary> storerCreator)
	{
		return this.persistenceManager.createStorer(storerCreator);
	}

	@Override
	public void updateMetadata(final PersistenceTypeDictionary typeDictionary, final long highestTypeId, final long highestObjectId)
	{
		this.persistenceManager.updateMetadata(typeDictionary, highestTypeId, highestObjectId);

	}

	@Override
	public PersistenceObjectRegistry objectRegistry()
	{
		return this.persistenceManager.objectRegistry();
	}

	@Override
	public PersistenceTypeDictionary typeDictionary()
	{
		return this.persistenceManager.typeDictionary();
	}

	@Override
	public long currentObjectId()
	{
		return this.persistenceManager.currentObjectId();
	}

	@Override
	public PersistenceManager<Binary> updateCurrentObjectId(final long currentObjectId)
	{
		return this.persistenceManager.updateCurrentObjectId(currentObjectId);
	}

	@Override
	public PersistenceSource<Binary> source()
	{
		return this.persistenceManager.source();
	}

	@Override
	public PersistenceTarget<Binary> target()
	{
		return this.persistenceManager.target();
	}

	@Override
	public void close()
	{
		this.persistenceManager.close();
	}

	@Override
	public ByteOrder getTargetByteOrder()
	{
		return this.persistenceManager.getTargetByteOrder();
	}

	public ViewerObjectDescription getStorageObject(final long objectId)
	{
		return  (ViewerObjectDescription) this.createViewerLoader().getObject(objectId);
	}

	private PersistenceRetrieving createViewerLoader()
	{
		this.objectRegistry.clear();

		return this.loaderCreator.createLoader(
				this.contextDispatcher.dispatchTypeHandlerLookup(this.typeHandlerManager),
				this.contextDispatcher.dispatchObjectRegistry(this.objectRegistry),
				this, this.persistenceManager);
	}

	public ViewerObjectDescription getStorageConstant(final long objectId)
	{
		 final Object object = this.constantRegistry.lookupObject(objectId);
		 final PersistenceTypeDefinition type = this.typeDictionary().lookupTypeByName(object.getClass().getTypeName());

		 final ViewerObjectDescription objectDescription = new ViewerObjectDescription();

		 objectDescription.setPersistenceTypeDefinition(type);
		 objectDescription.setObjectId(objectId);
		 objectDescription.setValues(new Object[] {object});
		 objectDescription.setLength(1);

		 return objectDescription;
	}
}

package one.microstream.storage.restadapter;

import java.util.ArrayList;
import java.util.List;

import one.microstream.persistence.types.PersistenceTypeDefinition;

/**
 * This class encapsulates the type definition and all field values retrieved
 * for an object received from the ViewerPersistenceManager.
 *
 */
public class ObjectDescription
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private long objectId;
	private Object[] values;
	private Object primitiveInstance;
	private PersistenceTypeDefinition persistenceTypeDefinition;
	private long length;
	private ObjectDescription[] references;

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public ObjectDescription()
	{
		super();
	}

	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////


	/**
	 * Get the microstream object id for this object
	 *
	 * @return object id as long
	 */
	public long getObjectId()
	{
		return this.objectId;
	}

	public void setObjectId(final long objectId)
	{
		this.objectId = objectId;
	}

	public Object[] getValues()
	{
		return this.values;
	}

	public void setValues(final Object[] values)
	{
		this.values = values;
	}

	public long getLength()
	{
		return this.length;
	}

	public void setLength(final long variableSize)
	{
		this.length = variableSize;
	}

	public ObjectDescription[] getReferences()
	{
		return this.references;
	}

	public void setReferences(final ObjectDescription[] references)
	{
		this.references = references;
	}

	/**
	 * Get the assigned primitive value instance or null if it is not available
	 *
	 * @return Object
	 */
	public Object getPrimitiveInstance()
	{
		return this.primitiveInstance;
	}

	public void setPrimitiveInstance(final Object primitiveInstance)
	{
		this.primitiveInstance = primitiveInstance;
	}

	public PersistenceTypeDefinition getPersistenceTypeDefinition()
	{
		return this.persistenceTypeDefinition;
	}

	public void setPersistenceTypeDefinition(final PersistenceTypeDefinition persistenceTypeDefinition)
	{
		this.persistenceTypeDefinition = persistenceTypeDefinition;
	}

	/**
	 * Check if an primitive value instance is assigned to this object
	 *
	 * @return true if the object has a primitive value instance
	 */
	public boolean hasPrimitiveObjectInstance()
	{
		return this.primitiveInstance != null;
	}


	/**
	 * Resolve references by fetching there object description and
	 * populate the references array.
	 *
	 * @param referenceOffset index of the first reference to be resolved
	 * @param referenceLength number of references to be resolved
	 * @param storageRestAdapter storage adapter that provides "one.microstream.storage.restadapter.EmbeddedStorageRestAdapter.getStorageObject(long)"
	 */
	public void resolveReferences(
		final long referenceOffset,
		final long referenceLength,
		final EmbeddedStorageRestAdapter storageRestAdapter)
	{
		final Integer referenceIndex = 0;
		final List<ObjectDescription> resolvedReferences = new ArrayList<>();

		this.collectReferences(
			this.values,
			new CollectReferencesParameters(
				storageRestAdapter,
				resolvedReferences,
				referenceIndex,
				referenceOffset,
				referenceLength));

		this.references = resolvedReferences.toArray(new ObjectDescription[0]);
	}

	/**
	 * Resolve a single ObjectReferenceWrapper reference
	 *
	 * @param reference
	 * @param storageRestAdapter
	 * @return the resolved reference
	 */
	private ObjectDescription resolveReference(final ObjectReferenceWrapper reference, final EmbeddedStorageRestAdapter storageRestAdapter)
	{
		final long oid = reference.getObjectId();
		if(oid > 0)
		{
			return storageRestAdapter.getStorageObject(oid);
		}

		return null;
	}


	/**
	 *
	 * Private helper class for recursive traversal of one.microstream.storage.restadapter.ObjectDescription.values array
	 *
	 */
	private class CollectReferencesParameters
	{
		public EmbeddedStorageRestAdapter storageRestAdapter;
		public List<ObjectDescription> resolvedReferences;
		public Integer referenceIndex;
		public Long referenceOffset;
		public Long referenceLength;

		public CollectReferencesParameters(
			final EmbeddedStorageRestAdapter storageRestAdapter,
			final List<ObjectDescription> resolvedReferences,
			final Integer referenceIndex,
			final Long referenceOffset,
			final Long referenceLength)
		{
			this.storageRestAdapter = storageRestAdapter;
			this.resolvedReferences = resolvedReferences;
			this.referenceIndex = referenceIndex;
			this.referenceOffset = referenceOffset;
			this.referenceLength = referenceLength;
		}
	}


	/**
	 * Traverse the provided Object array "dataArray" until the requested number of resolved references is collected
	 *
	 * @param dataArray
	 * @param parameterObject
	 */
	private void collectReferences(
		final Object[] dataArray,
		final CollectReferencesParameters parameterObject)
	{
		for(int i = 0; i < dataArray.length; i++)
		{
			final Object valueObject = dataArray[i];

			if(valueObject instanceof ObjectReferenceWrapper)
			{
				if(parameterObject.referenceIndex++ < parameterObject.referenceOffset) continue;
				if(parameterObject.resolvedReferences.size() >= parameterObject.referenceLength) break;

				parameterObject.resolvedReferences.add(this.resolveReference((ObjectReferenceWrapper) valueObject, parameterObject.storageRestAdapter));
			}
			else if(valueObject.getClass().isArray())
			{
				final Object[] nextDataArray = (Object[]) valueObject;
				this.collectReferences(nextDataArray, parameterObject);
			}

		}
	}
}

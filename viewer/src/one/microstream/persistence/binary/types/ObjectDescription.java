package one.microstream.persistence.binary.types;

import java.util.ArrayList;
import java.util.List;

import one.microstream.persistence.types.PersistenceTypeDefinition;
import one.microstream.viewer.EmbeddedStorageRestAdapter;

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
	 * @return
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

	public void resolveReferences(final long referenceOffset, final long referenceLength, final EmbeddedStorageRestAdapter storageRestAdapter)
	{
		int referenceIndex = 0;
		int referenceCount = 0;

		final List<ObjectDescription> resolvedReferences = new ArrayList<>();

		for(int i = 0; i < this.values.length; i++)
		{
			if(this.values[i] instanceof ObjectReferenceWrapper)
			{
				if(referenceIndex++ < referenceOffset) continue;
				if(referenceCount++ >= referenceLength) break;

				final long oid = ((ObjectReferenceWrapper) this.values[i]).getObjectId();
				if(oid > 0)
				{
					resolvedReferences.add(storageRestAdapter.getStorageObject(oid));
				}
				else
				{
					resolvedReferences.add(null);
				}
			}
		}

		this.references = resolvedReferences.toArray(new ObjectDescription[0]);
	}
}

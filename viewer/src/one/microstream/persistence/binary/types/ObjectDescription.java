package one.microstream.persistence.binary.types;

import java.util.ArrayList;
import java.util.List;

import one.microstream.persistence.types.PersistenceTypeDefinition;
import one.microstream.viewer.EmbeddedStorageRestAdapter;
import one.microstream.viewer.ViewerObjectDescription;

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

	private long ObjectId;
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
		return this.ObjectId;
	}

	public void setObjectId(final long objectId)
	{
		this.ObjectId = objectId;
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

	public ViewerObjectDescription postProcess(final long dataOffset, final long dataLength)
	{
		return this.postProcess(this, dataOffset, dataLength);
	}

	private ViewerObjectDescription postProcess(final ObjectDescription description, final long dataOffset, final long dataLength)
	{
		final ViewerObjectDescription objDesc = new ViewerObjectDescription();

		this.setObjectHeader(description, objDesc);

		if(description.hasPrimitiveObjectInstance())
		{
			this.setPrimitiveValue(description, objDesc, dataOffset, dataLength);
		}
		else
		{
			objDesc.setData(this.simplifyObjectArray(description.getValues(), dataOffset, dataLength));
		}

		this.setReferences(description, objDesc, dataOffset, dataLength);
		return objDesc;
	}

	private void setObjectHeader(final ObjectDescription description, final ViewerObjectDescription objDesc)
	{
		objDesc.setObjectId(Long.toString(description.getObjectId()));
		objDesc.setTypeId(Long.toString(description.getPersistenceTypeDefinition().typeId()));
		objDesc.setLength(Long.toString(description.getLength()));
	}

	private void setPrimitiveValue(
		final ObjectDescription description,
		final ViewerObjectDescription objDesc,
		final long dataOffset,
		final long dataLength)
	{
		final String stringValue = description.getPrimitiveInstance().toString();
		final String subString = this.limitsPrimitiveType(stringValue, dataOffset, dataLength);
		objDesc.setData(new String[] { subString } );
	}

	private String limitsPrimitiveType(final String data, final long dataOffset, final long dataLength)
	{
		int offset = 0;
		int length = 0;

		//dataOffset may not exceed object length
		if(dataOffset > data.length()) offset = data.length();
		else offset = (int) dataOffset;

		//dataLength may not exceed object length
		if(dataLength > data.length()) length = data.length();
		else length = (int) dataLength;

		//length + offset may not exceed object length
		long end = offset + length;
		if(end  > data.length()) end = data.length();

		return data.substring(offset, (int) end);
	}

	private void setReferences(
		final ObjectDescription description,
		final ViewerObjectDescription objDesc,
		final long dataOffset,
		final long dataLength)
	{
		final ObjectDescription refs[] = description.getReferences();
		if(refs != null)
		{
			final List<ViewerObjectDescription> refList = new ArrayList<>(refs.length);

			for (final ObjectDescription desc : refs)
			{
				if(desc != null)
				{
					refList.add(this.postProcess(desc, dataOffset, dataLength));
				}
				else
				{
					refList.add(null);
				}
			}

			objDesc.setReferences(refList.toArray(new ViewerObjectDescription[0]));
		}
	}

	private Object[] simplifyObjectArray(final Object[] obj, final long dataOffset, final long dataLength)
	{
		final int startIndex = (int) dataOffset;
		final int realLength = (int) Math.max(Math.min(obj.length - startIndex, dataLength), 0);
		final int endIndex = startIndex + realLength;

		final Object[] dataArray = new Object[realLength];
		int counter = 0;

		for(int i = startIndex; i < endIndex; i++)
		{
			if(obj[i] instanceof ObjectReferenceWrapper)
			{
				dataArray[counter] = Long.toString(((ObjectReferenceWrapper) obj[i]).getObjectId());
			}
			else if(obj[i].getClass().isArray())
			{
				dataArray[counter] = this.simplifyObjectArray((Object[]) obj[i], dataOffset, dataLength);
			}
			else
			{
				dataArray[counter] = obj[i].toString();
			}

			counter++;
		}

		return dataArray;
	}
}

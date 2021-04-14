package one.microstream.storage.restadapter.types;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import one.microstream.persistence.types.PersistenceTypeDefinition;
import one.microstream.reference.Swizzling;

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
	private Long[] variableLength;

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

	public void setVariableLength(final Long[] objects)
	{
		this.variableLength = objects;
	}

	public Long[] getVariableLength()
	{
		return this.variableLength;
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

	public void resolveReferences(
		final long fixedOffset,
		final long fixedLength,
		final long variableOffset,
		final long variableLength,
		final EmbeddedStorageRestAdapter storageRestAdapter
	)
	{
		final List<ObjectDescription> resolvedReferences = new ArrayList<>();
		
		Arrays.stream(this.values)
			.skip(fixedOffset)
			.limit(fixedLength)
			.filter(ObjectReferenceWrapper.class::isInstance)
			.map(ObjectReferenceWrapper.class::cast)
			.map(wrapper -> this.resolveReference(wrapper, storageRestAdapter))
			.forEach(resolvedReferences::add);
			
		int variableIndex;
		if(variableLength > 0 && (variableIndex = (int)this.length) < this.values.length)
		{
			Arrays.stream((Object[])this.values[variableIndex])
				.skip(variableOffset)
				.limit(variableLength)
				.flatMap(this::flatMapToWrappers)
				.map(wrapper -> this.resolveReference(wrapper, storageRestAdapter))
				.forEach(resolvedReferences::add);
		}
		
		this.references = resolvedReferences.toArray(new ObjectDescription[resolvedReferences.size()]);
	}
	
	private Stream<ObjectReferenceWrapper> flatMapToWrappers(final Object data)
	{
		if(data instanceof ObjectReferenceWrapper)
		{
			return Stream.of((ObjectReferenceWrapper)data);
		}
		
		if(data instanceof Object[])
		{
			return Arrays.stream((Object[])data)
				.flatMap(this::flatMapToWrappers);
		}
		
		return Stream.empty();
	}
	
	private ObjectDescription resolveReference(
		final ObjectReferenceWrapper wrapper,
		final EmbeddedStorageRestAdapter storageRestAdapter
	)
	{
		final long oid = wrapper.getObjectId();
		return oid == Swizzling.nullId()
			? null
			: storageRestAdapter.getStorageObject(oid);
	}
}

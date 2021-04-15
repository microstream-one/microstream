package one.microstream.examples.customlegacytypehandler;

import one.microstream.X;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.binary.types.BinaryLegacyTypeHandler;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceReferenceLoader;

public class LegacyTypeHandlerNicePlace extends BinaryLegacyTypeHandler.AbstractCustom<NicePlace>
{
	//need to know the binary layout of the persisted legacy class
	private static final long
		BINARY_OFFSET_name = 0,
		BINARY_OFFSET_directions = BINARY_OFFSET_name + Binary.objectIdByteLength();

	
	public LegacyTypeHandlerNicePlace()
	{
		//introduce the field names of the legacy class
		super(NicePlace.class,
				X.List(CustomField(String.class,"name"),
						CustomField(String.class,"directions")));
	}

	@Override
	public boolean hasPersistedReferences()
	{
		// persisted data records have references to other persisted data records
		return true;
	}

	@Override
	public boolean hasVaryingPersistedLengthInstances()
	{
		/*the same instance can never have a varying persisted length, since the NicePlace class
		*only has to references as member.
		*
		* Collections are an example for variable length instances.
		* The same collection instance can contain 2 elements at one store and 3 at another store.
		*/
			
		return false;
	}
				
	
	@Override
	public NicePlace create(
		final Binary bytes,
		final PersistenceLoadHandler loadHandler
	)
	{
		//required instances may not be available, yet, at creation time. Thus create dummy and fill in #update.
		return new NicePlace();
	}
	
	@Override
	public void updateState(
		final Binary bytes,
		final NicePlace instance,
		final PersistenceLoadHandler handler
	)
	{
		//get the data of the legacy NicePlace fields
		final String name = (String)handler.lookupObject(bytes.read_long(BINARY_OFFSET_name));
		final String directions  = (String)handler.lookupObject(bytes.read_long(BINARY_OFFSET_directions));
			
		//initialize the new version of our NicePlace
		instance.name = name;
		instance.location = new Location(directions, 0, 0);
	}
		
	@Override
	public void iterateLoadableReferences(
		final Binary bytes,
		final PersistenceReferenceLoader iterator
	)
	{
		iterator.acceptObjectId(bytes.read_long(BINARY_OFFSET_name));
		iterator.acceptObjectId(bytes.read_long(BINARY_OFFSET_directions));
	}
}

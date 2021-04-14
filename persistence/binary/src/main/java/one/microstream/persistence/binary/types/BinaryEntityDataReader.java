package one.microstream.persistence.binary.types;

import java.nio.ByteBuffer;

public interface BinaryEntityDataReader
{
	/**
	 * Expects a raw memory address pointing to the location of a entity raw binary data, starting with its header.
	 *
	 * @param entitiesData
	 */
	public void readBinaryEntities(ByteBuffer entitiesData);
}

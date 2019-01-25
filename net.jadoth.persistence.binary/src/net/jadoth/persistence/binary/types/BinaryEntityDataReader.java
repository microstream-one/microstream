package net.jadoth.persistence.binary.types;

public interface BinaryEntityDataReader
{
	/**
	 * Expects a raw memory address pointing to the location of a entity raw binary data, starting with its header.
	 * 
	 * @param address
	 */
	public void readBinaryEntityData(long entityAddress);
}

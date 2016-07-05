package net.jadoth.persistence.binary.types;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

import net.jadoth.memory.Chunks;

// CHECKSTYLE.OFF: AbstractClassName: this is kind of a hacky solution to improve readability on the use site
public abstract class Binary implements Chunks
{
	@Override
	public abstract ByteBuffer[] buffers();

	/**
	 * Writes the header (etc...).
	 * <p>
	 * Returns a memory address that is guaranteed to be safe for writing {@literal len} bytes.
	 * Writing any more bytes will lead to unpredictable results, from (most likely) destroying
	 * the byte stream's consistency up to crashing the VM immediately or at some point in the future.
	 * <p>
	 * DO NOT WRITE MORE THEN {@literal len} BYTES TO THE RETURNED ADDRESS!
	 *
	 * @param entityContentLength
	 * @param entityTypeId
	 * @param entityObjectId
	 * @return
	 */
	public abstract long storeEntityHeader(
		final long entityContentLength,
		final long entityTypeId       ,
		final long entityObjectId
	);

	public abstract long[] startOffsets();

	public abstract long[] boundOffsets();

	public abstract long   buildItemAddress();

	/* sneaky hardcoded field for performance reasons.
	 * Used only by build items for create/update address.
	 * A little hacky, but worth it.
	 */
	long entityContentAddress;

	/**
	 * Some binary entries serve as a skip entry, so that an entry for a particular object id already exists.
	 * Naturally, those entries don't have data then, which must be checked (be checkable) later on.
	 *
	 * @return whether this instances carries (actually "knows") binary build data or not.
	 */
	public final boolean hasData()
	{
		return this.entityContentAddress != 0;
	}

	// only for debug purposes!
	protected abstract void internalIterateCurrentData(Consumer<byte[]> iterator);

	protected abstract long[] internalGetStartOffsets();

	protected abstract long[] internalGetBoundOffsets();
}
//CHECKSTYLE.ON: AbstractClassName


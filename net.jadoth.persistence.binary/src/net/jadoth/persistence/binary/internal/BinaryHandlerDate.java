package net.jadoth.persistence.binary.internal;

import java.util.Date;

import net.jadoth.memory.Memory;
import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.swizzling.types.SwizzleBuildLinker;
import net.jadoth.swizzling.types.SwizzleStoreLinker;

public final class BinaryHandlerDate extends AbstractBinaryHandlerNativeCustom<Date>
{
	///////////////////////////////////////////////////////////////////////////
	// constants        //
	/////////////////////

	private static final int  BITS_3           = 3                   ;
	private static final long LENGTH_TIMESTAMP = Long.SIZE >>> BITS_3;



	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public BinaryHandlerDate(final long typeId)
	{
		super(typeId, Date.class, pseudoFields(
			pseudoField(long.class, "timestamp")
		));
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public boolean isVariableBinaryLengthType()
	{
		return false;
	}

	@Override
	public void store(final Binary bytes, final Date instance, final long oid, final SwizzleStoreLinker linker)
	{
		// the data content of a date is simple the timestamp long, nothing else
		Memory.set_long(
			bytes.storeEntityHeader(LENGTH_TIMESTAMP, this.typeId(), oid),
			instance.getTime()
		);
	}

	@Override
	public Date create(final Binary bytes)
	{
		return new Date(Memory.get_long(bytes.buildItemAddress()));
	}

	@Override
	public void update(final Binary bytes, final Date instance, final SwizzleBuildLinker builder)
	{
		instance.setTime(Memory.get_long(bytes.buildItemAddress()));
	}

	@Override
	public final boolean hasInstanceReferences()
	{
		return false;
	}

	@Override
	public boolean hasVariableBinaryLengthInstances()
	{
		return false;
	}

}

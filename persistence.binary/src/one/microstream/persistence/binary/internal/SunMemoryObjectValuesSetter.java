package one.microstream.persistence.binary.internal;

import java.nio.ByteBuffer;

import one.microstream.memory.PlatformInternals;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.binary.types.BinaryObjectValuesSetter;
import one.microstream.persistence.binary.types.BinaryValueSetter;
import one.microstream.persistence.types.PersistenceObjectIdResolver;

public class SunMemoryObjectValuesSetter implements BinaryObjectValuesSetter<Binary>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final BinaryValueSetter[] setters;
	
	private final long[] settingMemoryOffsets;
		
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	SunMemoryObjectValuesSetter(
		final BinaryValueSetter[] setters,
		final long[] settingMemoryOffsets
	)
	{
		super();
		this.setters = setters;
		this.settingMemoryOffsets = settingMemoryOffsets;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public final void setObjectValues(
		final ByteBuffer                  source      ,
		final long                        sourceOffset,
		final Object                      object      ,
		final PersistenceObjectIdResolver idResolver
	)
	{
		final final address = PlatformInternals.getDirectBufferAddress(source) + sourceOffset;
		medium.updateFixedSize(object, this.setters, this.settingMemoryOffsets, idResolver);
	}
	
}

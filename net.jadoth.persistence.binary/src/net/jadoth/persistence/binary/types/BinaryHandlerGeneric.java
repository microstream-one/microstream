package net.jadoth.persistence.binary.types;

import static net.jadoth.Jadoth.notNull;

import java.lang.reflect.Field;

import net.jadoth.collections.types.XGettingEnum;
import net.jadoth.persistence.types.PersistenceFieldLengthResolver;

public final class BinaryHandlerGeneric<T> extends AbstractGenericBinaryHandler<T>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final BinaryInstantiator<T> instantiator;



	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	protected BinaryHandlerGeneric(
		final Class<T>                       type          ,
		final long                           tid           ,
		final BinaryInstantiator<T>          instantiator  ,
		final XGettingEnum<Field>            allFields     ,
		final PersistenceFieldLengthResolver lengthResolver
	)
	{
		super(type, tid, allFields, lengthResolver);
		this.instantiator = notNull(instantiator);
	}

	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
		
	@Override
	public final T create(final Binary bytes)
	{
		return this.instantiator.newInstance(bytes.buildItemAddress());
	}

}

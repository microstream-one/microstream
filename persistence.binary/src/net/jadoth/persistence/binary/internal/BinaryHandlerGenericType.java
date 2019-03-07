package net.jadoth.persistence.binary.internal;

import static net.jadoth.X.notNull;

import java.lang.reflect.Field;

import net.jadoth.collections.types.XGettingEnum;
import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.binary.types.BinaryInstantiator;
import net.jadoth.persistence.types.PersistenceEagerStoringFieldEvaluator;
import net.jadoth.persistence.types.PersistenceFieldLengthResolver;

public final class BinaryHandlerGenericType<T> extends AbstractBinaryHandlerReflective<T>
{
	public static <T> BinaryHandlerGenericType<T> New(
		final Class<T>                              type                      ,
		final XGettingEnum<Field>                   allFields                 ,
		final PersistenceFieldLengthResolver        lengthResolver            ,
		final PersistenceEagerStoringFieldEvaluator eagerStoringFieldEvaluator,
		final BinaryInstantiator<T>                 instantiator              ,
		final boolean                               switchByteOrder
	)
	{
		return new BinaryHandlerGenericType<>(
			type                      ,
			allFields                 ,
			lengthResolver            ,
			eagerStoringFieldEvaluator,
			instantiator              ,
			switchByteOrder
		);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final BinaryInstantiator<T> instantiator;



	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	protected BinaryHandlerGenericType(
		final Class<T>                              type                      ,
		final XGettingEnum<Field>                   allFields                 ,
		final PersistenceFieldLengthResolver        lengthResolver            ,
		final PersistenceEagerStoringFieldEvaluator eagerStoringFieldEvaluator,
		final BinaryInstantiator<T>                 instantiator              ,
		final boolean                               switchByteOrder
	)
	{
		super(type, allFields, lengthResolver, eagerStoringFieldEvaluator, switchByteOrder);
		this.instantiator = notNull(instantiator);
	}

	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	public final T create(final Binary bytes)
	{
		return this.instantiator.newInstance(bytes.loadItemEntityContentAddress());
	}

}

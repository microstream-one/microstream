package one.microstream.persistence.binary.internal;

import static one.microstream.X.notNull;

import java.lang.reflect.Field;

import one.microstream.collections.types.XGettingEnum;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceEagerStoringFieldEvaluator;
import one.microstream.persistence.types.PersistenceFieldLengthResolver;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceTypeInstantiator;

public final class BinaryHandlerGenericType<T> extends AbstractBinaryHandlerReflective<T>
{
	public static <T> BinaryHandlerGenericType<T> New(
		final Class<T>                               type                      ,
		final String                                 typeName                  ,
		final XGettingEnum<Field>                    persistableFields         ,
		final XGettingEnum<Field>                    persisterFields           ,
		final PersistenceFieldLengthResolver         lengthResolver            ,
		final PersistenceEagerStoringFieldEvaluator  eagerStoringFieldEvaluator,
		final PersistenceTypeInstantiator<Binary, T> instantiator              ,
		final boolean                                switchByteOrder
	)
	{
		return new BinaryHandlerGenericType<>(
			type                      ,
			typeName                  ,
			persistableFields         ,
			persisterFields           ,
			lengthResolver            ,
			eagerStoringFieldEvaluator,
			instantiator              ,
			switchByteOrder
		);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final PersistenceTypeInstantiator<Binary, T> instantiator;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	protected BinaryHandlerGenericType(
		final Class<T>                               type                      ,
		final String                                 typeName                  ,
		final XGettingEnum<Field>                    persistableFields         ,
		final XGettingEnum<Field>                    persisterFields           ,
		final PersistenceFieldLengthResolver         lengthResolver            ,
		final PersistenceEagerStoringFieldEvaluator  eagerStoringFieldEvaluator,
		final PersistenceTypeInstantiator<Binary, T> instantiator              ,
		final boolean                                switchByteOrder
	)
	{
		super(type, typeName, persistableFields, persisterFields, lengthResolver, eagerStoringFieldEvaluator, switchByteOrder);
		this.instantiator = notNull(instantiator);
	}

	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	public final T create(final Binary data, final PersistenceLoadHandler handler)
	{
		return this.instantiator.instantiate(data);
	}

}

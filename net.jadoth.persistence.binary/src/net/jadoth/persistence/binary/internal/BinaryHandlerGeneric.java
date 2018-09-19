package net.jadoth.persistence.binary.internal;

import static net.jadoth.X.notNull;

import java.lang.reflect.Field;

import net.jadoth.collections.types.XGettingEnum;
import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.binary.types.BinaryInstantiator;
import net.jadoth.persistence.types.PersistenceEagerStoringFieldEvaluator;
import net.jadoth.persistence.types.PersistenceFieldLengthResolver;

public final class BinaryHandlerGeneric<T> extends AbstractBinaryHandlerReflective<T>
{
	public static <T> BinaryHandlerGeneric<T> New(
		final Class<T>                              type                   ,
		final XGettingEnum<Field>                   allFields              ,
		final PersistenceFieldLengthResolver        lengthResolver         ,
		final PersistenceEagerStoringFieldEvaluator mandatoryFieldEvaluator,
		final BinaryInstantiator<T>                 instantiator
	)
	{
		return new BinaryHandlerGeneric<>(
			type                   ,
			allFields              ,
			lengthResolver         ,
			mandatoryFieldEvaluator,
			instantiator
		);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final BinaryInstantiator<T> instantiator;



	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	protected BinaryHandlerGeneric(
		final Class<T>                              type                   ,
		final XGettingEnum<Field>                   allFields              ,
		final PersistenceFieldLengthResolver        lengthResolver         ,
		final PersistenceEagerStoringFieldEvaluator mandatoryFieldEvaluator,
		final BinaryInstantiator<T>                 instantiator
	)
	{
		super(type, allFields, lengthResolver, mandatoryFieldEvaluator);
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

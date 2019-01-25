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
		final Class<T>                              type                   ,
		final XGettingEnum<Field>                   allFields              ,
		final PersistenceFieldLengthResolver        lengthResolver         ,
		final PersistenceEagerStoringFieldEvaluator mandatoryFieldEvaluator,
		final BinaryInstantiator<T>                 instantiator
	)
	{
		return new BinaryHandlerGenericType<>(
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

	protected BinaryHandlerGenericType(
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
		return this.instantiator.newInstance(bytes.loadItemEntityContentAddress());
	}

}

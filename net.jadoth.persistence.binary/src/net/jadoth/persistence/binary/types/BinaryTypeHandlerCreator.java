package net.jadoth.persistence.binary.types;

import static net.jadoth.X.notNull;

import java.lang.reflect.Field;

import net.jadoth.collections.types.XGettingEnum;
import net.jadoth.persistence.binary.internal.BinaryHandlerEnum;
import net.jadoth.persistence.binary.internal.BinaryHandlerGenericType;
import net.jadoth.persistence.binary.internal.BinaryHandlerNativeArrayObject;
import net.jadoth.persistence.exceptions.PersistenceExceptionTypeNotPersistable;
import net.jadoth.persistence.types.PersistenceEagerStoringFieldEvaluator;
import net.jadoth.persistence.types.PersistenceFieldLengthResolver;
import net.jadoth.persistence.types.PersistenceTypeAnalyzer;
import net.jadoth.persistence.types.PersistenceTypeHandler;
import net.jadoth.persistence.types.PersistenceTypeHandlerCreator;


public interface BinaryTypeHandlerCreator extends PersistenceTypeHandlerCreator<Binary>
{
	@Override
	public <T> PersistenceTypeHandler<Binary, T> createTypeHandler(Class<T> type)
		throws PersistenceExceptionTypeNotPersistable;


	// (06.02.2019 TM)FIXME: JET-49: mandatoryFieldEvaluator -> eagerStoringFieldEvaluator projectwide
	public static BinaryTypeHandlerCreator New(
		final PersistenceTypeAnalyzer               typeAnalyzer              ,
		final PersistenceFieldLengthResolver        lengthResolver            ,
		final PersistenceEagerStoringFieldEvaluator eagerStoringFieldEvaluator,
		final boolean                               switchByteOrder
	)
	{
		return new BinaryTypeHandlerCreator.Implementation(
			notNull(typeAnalyzer)              ,
			notNull(lengthResolver)            ,
			notNull(eagerStoringFieldEvaluator),
			switchByteOrder
		);
	}

	public class Implementation
	extends PersistenceTypeHandlerCreator.AbstractImplementation<Binary>
	implements BinaryTypeHandlerCreator
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		final boolean switchByteOrder;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors     //
		/////////////////////

		Implementation(
			final PersistenceTypeAnalyzer               typeAnalyzer           ,
			final PersistenceFieldLengthResolver        lengthResolver         ,
			final PersistenceEagerStoringFieldEvaluator mandatoryFieldEvaluator,
			final boolean                               switchByteOrder
		)
		{
			super(typeAnalyzer, lengthResolver, mandatoryFieldEvaluator);
			this.switchByteOrder = switchByteOrder;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		protected <T> PersistenceTypeHandler<Binary, T> createTypeHandlerArray(final Class<T> type)
		{
			// array types can never change and therefore can never have obsolete types.
			return new BinaryHandlerNativeArrayObject<>(type);
		}
		
		@Override
		protected <T> PersistenceTypeHandler<Binary, T> createTypeHandlerReflective(
			final Class<T>            type             ,
			final XGettingEnum<Field> persistableFields
		)
		{
			if(type.isEnum())
			{
				/* (09.06.2017 TM)TODO: enum BinaryHandler special case implementation once completed
				 * (10.06.2017 TM)NOTE: not sure if handling enums (constants) in an entity graph
				 * makes sense in the first place. The whole enum concept (the identity of an instance depending
				 * on the name and/or the order of the field referencing it) is just too wacky for an entity graph.
				 * Use enums for logic, if you must, but keep them out of proper entity graphs.
				 */
//				return this.createEnumHandler(type, persistableFields);
			}

			// default implementation simply always uses a blank memory instantiator
			return BinaryHandlerGenericType.New(
				type                                           ,
				persistableFields                              ,
				this.lengthResolver()                          ,
				this.eagerStoringFieldEvaluator()                 ,
				BinaryPersistence.blankMemoryInstantiator(type),
				this.switchByteOrder
			);
		}

		@SuppressWarnings("unchecked") // required generics crazy sh*t tinkering
		final <T, E extends Enum<E>> PersistenceTypeHandler<Binary, T> createEnumHandler(
			final Class<?>            type     ,
			final XGettingEnum<Field> allFields
		)
		{
			return (PersistenceTypeHandler<Binary, T>)BinaryHandlerEnum.New(
				(Class<E>)type                ,
				allFields                     ,
				this.lengthResolver()         ,
				this.eagerStoringFieldEvaluator(),
				this.switchByteOrder
			);
		}

	}

}

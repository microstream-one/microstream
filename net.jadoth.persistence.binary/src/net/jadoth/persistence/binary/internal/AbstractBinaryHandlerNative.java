package net.jadoth.persistence.binary.internal;

import java.lang.reflect.Field;

import net.jadoth.collections.X;
import net.jadoth.collections.types.XGettingEnum;
import net.jadoth.collections.types.XGettingSequence;
import net.jadoth.collections.types.XImmutableSequence;
import net.jadoth.functional._longProcedure;
import net.jadoth.memory.objectstate.ObjectStateDescriptor;
import net.jadoth.memory.objectstate.ObjectStateHandlerLookup;
import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.binary.types.BinaryPersistence;
import net.jadoth.persistence.binary.types.BinaryTypeHandler;
import net.jadoth.persistence.exceptions.PersistenceExceptionTypeConsistencyDefinitionValidationArrayType;
import net.jadoth.persistence.types.PersistenceTypeDescription;
import net.jadoth.persistence.types.PersistenceTypeDescriptionMemberPseudoField;
import net.jadoth.persistence.types.PersistenceTypeDescriptionMemberPseudoFieldComplex;
import net.jadoth.persistence.types.PersistenceTypeDescriptionMemberPseudoFieldSimple;
import net.jadoth.persistence.types.PersistenceTypeDescriptionMemberPseudoFieldVariableLength;
import net.jadoth.swizzling.types.SwizzleBuildLinker;
import net.jadoth.swizzling.types.SwizzleFunction;
import net.jadoth.swizzling.types.SwizzleStoreLinker;


public abstract class AbstractBinaryHandlerNative<T>
extends BinaryTypeHandler.AbstractImplementation<T>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods    //
	/////////////////////

	public static final XImmutableSequence<PersistenceTypeDescriptionMemberPseudoField>
	defineValueType(final Class<?> valueType)
	{
		return X.Constant(pseudoField(valueType, "value"));
	}

	public static final PersistenceTypeDescriptionMemberPseudoField pseudoField(
		final Class<?> type,
		final String   name
	)
	{
		return PersistenceTypeDescriptionMemberPseudoFieldSimple.Implementation.New(
			type.getName(),
			name,
			!type.isPrimitive(),
			BinaryPersistence.resolveFieldBinaryLength(type),
			BinaryPersistence.resolveFieldBinaryLength(type)
		);
	}

	public static final PersistenceTypeDescriptionMemberPseudoField chars(final String name)
	{
		return PersistenceTypeDescriptionMemberPseudoFieldVariableLength.Chars(
			name,
			BinaryPersistence.binaryArrayMinimumLength(),
			BinaryPersistence.binaryArrayMaximumLength()
		);
	}

	public static final PersistenceTypeDescriptionMemberPseudoField bytes(final String name)
	{
		return PersistenceTypeDescriptionMemberPseudoFieldVariableLength.Bytes(
			name,
			BinaryPersistence.binaryArrayMinimumLength(),
			BinaryPersistence.binaryArrayMaximumLength()
		);
	}

	public static final XImmutableSequence<PersistenceTypeDescriptionMemberPseudoField>
	pseudoFields(final PersistenceTypeDescriptionMemberPseudoField... pseudoFields)
	{
		return X.ConstList(pseudoFields);
	}

	public static final PersistenceTypeDescriptionMemberPseudoFieldComplex
	complex(
		final String name,
		final PersistenceTypeDescriptionMemberPseudoField... pseudoFields
	)
	{
		return new PersistenceTypeDescriptionMemberPseudoFieldComplex.Implementation(
			name,
			pseudoFields(pseudoFields),
			BinaryPersistence.binaryArrayMinimumLength(),
			BinaryPersistence.binaryArrayMaximumLength()
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// instance fields  //
	/////////////////////

	private final PersistenceTypeDescription<T> typeDescription;



	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public AbstractBinaryHandlerNative(
		final long                                                                    typeId      ,
		final Class<T>                                                                type        ,
		final XGettingSequence<? extends PersistenceTypeDescriptionMemberPseudoField> pseudoFields
	)
	{
		super(type, typeId);
		this.typeDescription = PersistenceTypeDescription.New(
			typeId               ,
			type.getName()       ,
			type                 ,
			pseudoFields.immure()
		);
	}


	///////////////////////////////////////////////////////////////////////////
	// getters          //
	/////////////////////

	@Override
	public PersistenceTypeDescription<T> typeDescription()
	{
		return this.typeDescription;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public abstract void store(Binary bytes, T instance, long oid, SwizzleStoreLinker linker);

	@Override
	public void validateFields(final XGettingSequence<Field> fieldDescriptions)
		throws PersistenceExceptionTypeConsistencyDefinitionValidationArrayType
	{
		if(fieldDescriptions.isEmpty())
		{
			return;
		}
		throw new PersistenceExceptionTypeConsistencyDefinitionValidationArrayType(this.type());
	}

	@Override
	public void iterateInstanceReferences(final T instance, final SwizzleFunction iterator)
	{
		// no-op, no references
	}

	@Override
	public void iteratePersistedReferences(final Binary offset, final _longProcedure iterator)
	{
		// no-op, no references
	}

	@Override
	public final XGettingEnum<Field> getInstanceFields()
	{
		return X.empty();
	}

	@Override
	public final XGettingEnum<Field> getInstancePrimitiveFields()
	{
		return X.empty();
	}

	@Override
	public final XGettingEnum<Field> getInstanceReferenceFields()
	{
		return X.empty();
	}

	@Override
	public final XGettingEnum<Field> getAllFields()
	{
		return X.empty();
	}

	@Override
	public abstract T create(Binary bytes);

	@Override
	public void update(final Binary bytes, final T instance, final SwizzleBuildLinker builder)
	{
		/* No-op update logic by default. This is useful for all immutable value types (String, Integer, etc.).
		 * Value types never get updated. The value is only set once at instance creation time.
		 * Subsequently provided (potentially different) values are ignored intentionally.
		 */
	}

	@Override
	public void complete(final Binary medium, final T instance, final SwizzleBuildLinker builder)
	{
		// no-op for normal implementation (see non-reference-hashing collections for other examples)
	}

	@Override
	public boolean isEqual(final T source, final T target, final ObjectStateHandlerLookup instanceStateHandlerLookup)
	{
		return source == null ? target == null : source.equals(target);
	}

	@Override
	public final ObjectStateDescriptor<T> getStateDescriptor()
	{
		return this;
	}

}

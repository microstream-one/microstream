package one.microstream.persistence.binary.internal;

import java.util.function.Consumer;

import one.microstream.X;
import one.microstream.collections.types.XGettingEnum;
import one.microstream.collections.types.XGettingSequence;
import one.microstream.collections.types.XImmutableEnum;
import one.microstream.collections.types.XImmutableSequence;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.binary.types.BinaryPersistence;
import one.microstream.persistence.binary.types.BinaryTypeHandler;
import one.microstream.persistence.types.PersistenceFunction;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceObjectIdAcceptor;
import one.microstream.persistence.types.PersistenceStoreHandler;
import one.microstream.persistence.types.PersistenceTypeDefinitionMember;
import one.microstream.persistence.types.PersistenceTypeDefinitionMemberPseudoField;
import one.microstream.persistence.types.PersistenceTypeDefinitionMemberPseudoFieldComplex;
import one.microstream.persistence.types.PersistenceTypeDefinitionMemberPseudoFieldSimple;
import one.microstream.persistence.types.PersistenceTypeDefinitionMemberPseudoFieldVariableLength;
import one.microstream.persistence.types.PersistenceTypeDescriptionMember;
import one.microstream.persistence.types.PersistenceTypeDescriptionMemberPseudoField;


public abstract class AbstractBinaryHandlerCustom<T>
extends BinaryTypeHandler.AbstractImplementation<T>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods   //
	/////////////////////
	
	public static final XImmutableSequence<PersistenceTypeDefinitionMemberPseudoField>
	defineValueType(final Class<?> valueType)
	{
		return X.Constant(pseudoField(valueType, "value"));
	}
	
	public static final PersistenceTypeDefinitionMemberPseudoFieldSimple pseudoField(
		final Class<?> type,
		final String   name
	)
	{
		return PersistenceTypeDefinitionMemberPseudoFieldSimple.New(
			name,
			type.getName(),
			type,
			!type.isPrimitive(),
			BinaryPersistence.resolveFieldBinaryLength(type),
			BinaryPersistence.resolveFieldBinaryLength(type)
		);
	}
	
	public static final PersistenceTypeDefinitionMemberPseudoField chars(final String name)
	{
		return PersistenceTypeDefinitionMemberPseudoFieldVariableLength.Chars(
			name,
			Binary.binaryListMinimumLength(),
			Binary.binaryListMaximumLength()
		);
	}

	public static final PersistenceTypeDefinitionMemberPseudoField bytes(final String name)
	{
		return PersistenceTypeDefinitionMemberPseudoFieldVariableLength.Bytes(
			name,
			Binary.binaryListMinimumLength(),
			Binary.binaryListMaximumLength()
		);
	}

	public static final XImmutableSequence<PersistenceTypeDefinitionMemberPseudoField>
	pseudoFields(final PersistenceTypeDefinitionMemberPseudoField... pseudoFields)
	{
		return X.ConstList(pseudoFields);
	}

	public static final PersistenceTypeDefinitionMemberPseudoFieldComplex
	complex(
		final String name,
		final PersistenceTypeDescriptionMemberPseudoField... pseudoFields
	)
	{
		return PersistenceTypeDefinitionMemberPseudoFieldComplex.New(
			name,
			X.ConstList(pseudoFields),
			Binary.binaryListMinimumLength(),
			Binary.binaryListMaximumLength()
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// instance fields  //
	/////////////////////

	private final XImmutableEnum<? extends PersistenceTypeDefinitionMember> members;
	private final long binaryLengthMinimum;
	private final long binaryLengthMaximum;



	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	protected AbstractBinaryHandlerCustom(
		final Class<T>                                                    type   ,
		final XGettingSequence<? extends PersistenceTypeDefinitionMember> members
	)
	{
		super(type);
		this.members = validateAndImmure(members);
		this.binaryLengthMinimum = PersistenceTypeDescriptionMember.calculatePersistentMinimumLength(0, members);
		this.binaryLengthMaximum = PersistenceTypeDescriptionMember.calculatePersistentMaximumLength(0, members);
	}
	
	

	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	public boolean isPrimitiveType()
	{
		return false;
	}
	
	@Override
	public XGettingEnum<? extends PersistenceTypeDefinitionMember> members()
	{
		return this.members;
	}
	
	@Override
	public long membersPersistedLengthMinimum()
	{
		return this.binaryLengthMinimum;
	}
	
	@Override
	public long membersPersistedLengthMaximum()
	{
		return this.binaryLengthMaximum;
	}

	@Override
	public abstract void store(Binary bytes, T instance, long oid, PersistenceStoreHandler handler);

	@Override
	public void iterateInstanceReferences(final T instance, final PersistenceFunction iterator)
	{
		// no-op, no references
	}

	@Override
	public void iteratePersistedReferences(final Binary offset, final PersistenceObjectIdAcceptor iterator)
	{
		// no-op, no references
	}

	@Override
	public abstract T create(Binary bytes, PersistenceLoadHandler handler);

	@Override
	public void update(final Binary bytes, final T instance, final PersistenceLoadHandler handler)
	{
		/* No-op update logic by default. This is useful for all immutable value types (String, Integer, etc.).
		 * Value types never get updated. The value is only set once at instance creation time.
		 * Subsequently provided (potentially different) values are ignored intentionally.
		 */
	}

	@Override
	public void complete(final Binary bytes, final T instance, final PersistenceLoadHandler handler)
	{
		// no-op for normal implementation (see non-reference-hashing collections for other examples)
	}
	
	@Override
	public <C extends Consumer<? super Class<?>>> C iterateMemberTypes(final C logic)
	{
		// native handling logic should normally not have any member types that have to be iterated here
		return logic;
	}

}

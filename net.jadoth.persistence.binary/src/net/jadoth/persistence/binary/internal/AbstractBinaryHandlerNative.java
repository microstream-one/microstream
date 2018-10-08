package net.jadoth.persistence.binary.internal;

import java.util.function.Consumer;

import net.jadoth.X;
import net.jadoth.collections.types.XGettingEnum;
import net.jadoth.collections.types.XGettingSequence;
import net.jadoth.collections.types.XImmutableEnum;
import net.jadoth.collections.types.XImmutableSequence;
import net.jadoth.functional._longProcedure;
import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.binary.types.BinaryPersistence;
import net.jadoth.persistence.binary.types.BinaryTypeHandler;
import net.jadoth.persistence.types.PersistenceTypeDefinitionMember;
import net.jadoth.persistence.types.PersistenceTypeDefinitionMemberPseudoField;
import net.jadoth.persistence.types.PersistenceTypeDefinitionMemberPseudoFieldComplex;
import net.jadoth.persistence.types.PersistenceTypeDefinitionMemberPseudoFieldSimple;
import net.jadoth.persistence.types.PersistenceTypeDefinitionMemberPseudoFieldVariableLength;
import net.jadoth.persistence.types.PersistenceTypeDescriptionMember;
import net.jadoth.persistence.types.PersistenceTypeDescriptionMemberPseudoField;
import net.jadoth.swizzling.types.SwizzleBuildLinker;
import net.jadoth.swizzling.types.SwizzleFunction;
import net.jadoth.swizzling.types.SwizzleHandler;


public abstract class AbstractBinaryHandlerNative<T>
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
			BinaryPersistence.binaryArrayMinimumLength(),
			BinaryPersistence.binaryArrayMaximumLength()
		);
	}

	public static final PersistenceTypeDefinitionMemberPseudoField bytes(final String name)
	{
		return PersistenceTypeDefinitionMemberPseudoFieldVariableLength.Bytes(
			name,
			BinaryPersistence.binaryArrayMinimumLength(),
			BinaryPersistence.binaryArrayMaximumLength()
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
			BinaryPersistence.binaryArrayMinimumLength(),
			BinaryPersistence.binaryArrayMaximumLength()
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

	protected AbstractBinaryHandlerNative(
		final Class<T>                                                    type   ,
		final XGettingSequence<? extends PersistenceTypeDefinitionMember> members
	)
	{
		super(type);
		this.members = PersistenceTypeDescriptionMember.validateAndImmure(members);
		
		long binaryLengthMinimum = 0, binaryLengthMaximum = 0;
		for(final PersistenceTypeDescriptionMember member : this.members)
		{
			binaryLengthMinimum += member.persistentMinimumLength();
			binaryLengthMaximum += member.persistentMaximumLength();
		}
		this.binaryLengthMinimum = binaryLengthMinimum;
		this.binaryLengthMaximum = binaryLengthMaximum;
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
	public abstract void store(Binary bytes, T instance, long oid, SwizzleHandler linker);

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
	public <C extends Consumer<? super Class<?>>> C iterateMemberTypes(final C logic)
	{
		// native handling logic should normally not have any member types that have to be iterated here
		return logic;
	}

}

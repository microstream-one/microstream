package one.microstream.persistence.binary.internal;

import java.lang.reflect.Field;
import java.util.function.Consumer;

import one.microstream.X;
import one.microstream.collections.XArrays;
import one.microstream.collections.types.XGettingEnum;
import one.microstream.collections.types.XGettingSequence;
import one.microstream.collections.types.XImmutableEnum;
import one.microstream.collections.types.XImmutableSequence;
import one.microstream.exceptions.NoSuchFieldRuntimeException;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.binary.types.BinaryPersistence;
import one.microstream.persistence.binary.types.BinaryTypeHandler;
import one.microstream.persistence.exceptions.PersistenceException;
import one.microstream.persistence.types.PersistenceFunction;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceStoreHandler;
import one.microstream.persistence.types.PersistenceTypeDefinitionMember;
import one.microstream.persistence.types.PersistenceTypeDefinitionMemberFieldGeneric;
import one.microstream.persistence.types.PersistenceTypeDefinitionMemberFieldGenericComplex;
import one.microstream.persistence.types.PersistenceTypeDefinitionMemberFieldGenericSimple;
import one.microstream.persistence.types.PersistenceTypeDefinitionMemberFieldGenericVariableLength;
import one.microstream.persistence.types.PersistenceTypeDescriptionMember;
import one.microstream.persistence.types.PersistenceTypeDescriptionMemberFieldGeneric;
import one.microstream.reflect.XReflect;


public abstract class AbstractBinaryHandlerCustom<T>
extends BinaryTypeHandler.Abstract<T>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static final XImmutableSequence<PersistenceTypeDefinitionMemberFieldGeneric>
	defineValueType(final Class<?> valueType)
	{
		return X.Constant(CustomField(valueType, "value"));
	}
	
	public static final PersistenceTypeDefinitionMemberFieldGenericSimple CustomField(
		final Class<?> type,
		final String   name
	)
	{
		return CustomField(type, null, name);
	}
	
	public static final PersistenceTypeDefinitionMemberFieldGenericSimple CustomField(
		final Class<?> type     ,
		final String   qualifier,
		final String   name
	)
	{
		return PersistenceTypeDefinitionMemberFieldGenericSimple.New(
			type.getName(),
			qualifier,
			name,
			type,
			!type.isPrimitive(),
			BinaryPersistence.resolveFieldBinaryLength(type),
			BinaryPersistence.resolveFieldBinaryLength(type)
		);
	}
	
	public static final PersistenceTypeDefinitionMemberFieldGeneric chars(final String name)
	{
		return PersistenceTypeDefinitionMemberFieldGenericVariableLength.Chars(
			name,
			Binary.binaryListMinimumLength(),
			Binary.binaryListMaximumLength()
		);
	}

	public static final PersistenceTypeDefinitionMemberFieldGeneric bytes(final String name)
	{
		return PersistenceTypeDefinitionMemberFieldGenericVariableLength.Bytes(
			name,
			Binary.binaryListMinimumLength(),
			Binary.binaryListMaximumLength()
		);
	}

	public static final XImmutableSequence<PersistenceTypeDefinitionMemberFieldGeneric>
	CustomFields(final PersistenceTypeDefinitionMemberFieldGeneric... customFields)
	{
		return X.ConstList(customFields);
	}

	public static final PersistenceTypeDefinitionMemberFieldGenericComplex
	Complex(
		final String                                          name        ,
		final PersistenceTypeDescriptionMemberFieldGeneric... customFields
	)
	{
		return PersistenceTypeDefinitionMemberFieldGenericComplex.New(
			name,
			X.ConstList(customFields),
			Binary.binaryListMinimumLength(),
			Binary.binaryListMaximumLength()
		);
	}
	
	public static final XGettingSequence<? extends PersistenceTypeDefinitionMemberFieldGeneric> SizedArrayFields(
		final PersistenceTypeDefinitionMemberFieldGeneric... preHeaderFields
	)
	{
		return SimpleArrayFields(
			XArrays.add(
				preHeaderFields,
				AbstractBinaryHandlerCustom.CustomField(long.class, "capacity")
			)
		);
	}

	public static final XGettingSequence<? extends PersistenceTypeDefinitionMemberFieldGeneric> SimpleArrayFields(
		final PersistenceTypeDefinitionMemberFieldGeneric... preHeaderFields
	)
	{
		return AbstractBinaryHandlerCustom.CustomFields(
			XArrays.add(
				preHeaderFields,
				AbstractBinaryHandlerCustom.Complex("elements",
					AbstractBinaryHandlerCustom.CustomField(Object.class, "element")
				)
			)
		);
	}
	
	public static final XGettingSequence<? extends PersistenceTypeDefinitionMemberFieldGeneric> keyValuesFields(
		final PersistenceTypeDefinitionMemberFieldGeneric... preHeaderFields
	)
	{
		return AbstractBinaryHandlerCustom.CustomFields(
			XArrays.add(
				preHeaderFields,
				AbstractBinaryHandlerCustom.Complex("elements",
					AbstractBinaryHandlerCustom.CustomField(Object.class, "key"),
					AbstractBinaryHandlerCustom.CustomField(Object.class, "value")
				)
			)
		);
	}
		
	
	protected static final Field getInstanceFieldOfType(
		final Class<?> declaringType,
		final Class<?> fieldType
	)
		throws NoSuchFieldRuntimeException
	{
		return XReflect.setAccessible(
			XReflect.getInstanceFieldOfType(declaringType, fieldType)
		);
	}

	
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private XImmutableEnum<? extends PersistenceTypeDefinitionMember> members;
	
	private long binaryLengthMinimum, binaryLengthMaximum;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	protected AbstractBinaryHandlerCustom(final Class<T> type)
	{
		this(type, null);
	}

	protected AbstractBinaryHandlerCustom(
		final Class<T>                                                    type   ,
		final XGettingSequence<? extends PersistenceTypeDefinitionMember> members
	)
	{
		this(type, deriveTypeName(type), members);
	}
	
	protected AbstractBinaryHandlerCustom(
		final Class<T>                                                    type    ,
		final String                                                      typeName,
		final XGettingSequence<? extends PersistenceTypeDefinitionMember> members
	)
	{
		super(type, typeName);
		this.members = validateAndImmure(members);
		this.calculcateBinaryLengths();
	}
	
	

	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	protected void calculcateBinaryLengths()
	{
		if(this.members == null)
		{
			// members may be null to allow delayed on-demand BinaryField initialization.
			return;
		}
		
		this.binaryLengthMinimum = PersistenceTypeDescriptionMember.calculatePersistentMinimumLength(0, this.members);
		this.binaryLengthMaximum = PersistenceTypeDescriptionMember.calculatePersistentMaximumLength(0, this.members);
	}
	
	@Override
	public boolean isPrimitiveType()
	{
		return false;
	}
	
	@Override
	public XGettingEnum<? extends PersistenceTypeDefinitionMember> allMembers()
	{
		// with the exception of some special types (primitive definition and enums), there are only instance members.
		return this.instanceMembers();
	}
	
	@Override
	public synchronized XGettingEnum<? extends PersistenceTypeDefinitionMember> instanceMembers()
	{
		this.ensureInitializeInstanceMembers();
		
		return this.members;
	}
	
	protected final void ensureInitializeInstanceMembers()
	{
		if(this.members != null)
		{
			return;
		}
		this.members = this.initializeInstanceMembers();
		this.calculcateBinaryLengths();
	}
	
	protected XImmutableEnum<? extends PersistenceTypeDefinitionMember> initializeInstanceMembers()
	{
		// (09.01.2020 TM)EXCP: proper exception
		throw new PersistenceException(
			"type definition members may not be null for non-"
			+ AbstractBinaryHandlerCustom2.class.getSimpleName()
			+ "-implmenentations"
		);
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
	public abstract void store(Binary data, T instance, long objectId, PersistenceStoreHandler handler);

	@Override
	public void iterateInstanceReferences(final T instance, final PersistenceFunction iterator)
	{
		// no-op, no references
	}

	@Override
	public abstract T create(Binary data, PersistenceLoadHandler handler);

	@Override
	public void complete(final Binary data, final T instance, final PersistenceLoadHandler handler)
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

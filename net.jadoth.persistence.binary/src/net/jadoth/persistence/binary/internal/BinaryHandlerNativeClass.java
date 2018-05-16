package net.jadoth.persistence.binary.internal;

import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.binary.types.BinaryPersistence;
import net.jadoth.persistence.exceptions.PersistenceExceptionTypeConsistency;
import net.jadoth.reflect.XReflect;
import net.jadoth.swizzling.types.PersistenceStoreFunction;
import net.jadoth.swizzling.types.SwizzleBuildLinker;

public final class BinaryHandlerNativeClass extends AbstractBinaryHandlerNative<Class<?>>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods    //
	/////////////////////

	@SuppressWarnings({"unchecked",  "rawtypes"})
	private static Class<Class<?>> typeWorkaround()
	{
		// no idea how to get ".class" to work otherwise
		return (Class)Class.class;
	}
	


	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public BinaryHandlerNativeClass()
	{
		super(
			typeWorkaround(),
			pseudoFields(
				chars("name")
			)
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public void store(final Binary bytes, final Class<?> instance, final long oid, final PersistenceStoreFunction linker)
	{
		// no-op, static state is not stored
	}

	@Override
	public Class<?> create(final Binary bytes)
	{
		// as an entity, a class/type is identified by its unique name, not by a TypeId.
		final String typeName = BinaryPersistence.buildString(bytes);
		
		try
		{
			return XReflect.classForName(typeName);
		}
		catch(final ReflectiveOperationException e)
		{
			final long typeId = BinaryPersistence.getBuildItemObjectId(bytes);
			
			// (16.05.2018 TM)EXCP: proper exception
			throw new PersistenceExceptionTypeConsistency(
				"Type cannot be resolved: " + typeName + " (TypeId " + typeId + ")"
			);
		}
	}

	@Override
	public void update(final Binary bytes, final Class<?> instance, final SwizzleBuildLinker builder)
	{
		// no-op, see create()
	}
	
	@Override
	public final boolean hasPersistedReferences()
	{
		return false;
	}

	@Override
	public final boolean hasInstanceReferences()
	{
		return false;
	}
	
	@Override
	public final boolean hasPersistedVariableLength()
	{
		return true;
	}

	@Override
	public boolean hasVaryingPersistedLengthInstances()
	{
		return false;
	}

}

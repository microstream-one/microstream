package net.jadoth.persistence.binary.internal;

import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.binary.types.BinaryPersistence;
import net.jadoth.persistence.exceptions.PersistenceExceptionTypeConsistency;
import net.jadoth.persistence.types.PersistenceBuildLinker;
import net.jadoth.persistence.types.PersistenceHandler;
import net.jadoth.reflect.XReflect;


// (18.09.2018 TM)NOTE: removed from BinaryPersistence#defaultHandlers as of today. See comment below for the rationale.
@Deprecated
public final class BinaryHandlerNativeClass extends AbstractBinaryHandlerNativeCustom<Class<?>>
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
	public void store(final Binary bytes, final Class<?> instance, final long oid, final PersistenceHandler handler)
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
			/* (18.05.2018 TM)FIX-ME: Changed TypeId of persisted class
			 * What about the case that a class instance gets persisted (i.e. just its full qualified name)
			 * with a certain TypeId as its ObjectId and then the TypeId for the class changes?
			 * Should class instances even be stored with the TypeId representing the type as their ObjectId?
			 * Isn't the class instance itself a mere instance that represents the type and should therefore
			 * have a normal ObjectId assigned?
			 * 
			 * (18.09.2018 TM)NOTE: Or should instances of Class be unpersistable in the first place to avoid such
			 * problems? Is it wise to generically store a meta structure name as a data value in a data base?
			 * What is a sane use case that needs to persist a type (identifier) as part of a persistent entity graph?
			 * Maybe a good solution might be:
			 * If an explicit (user-defined) handler is registered for the type, it is used.
			 * If not, encountering instances of Class during type analysis throws an exception.
			 * 
			 * Done. ]:->
			 * See PersistenceTypeHandlerEnsurer (lookup, on fail call creator)
			 * and PersistenceTypeHandlerCreator (exception for Class instance)
			 */
			return XReflect.resolveType(typeName);
		}
		catch(final ReflectiveOperationException e)
		{
			final long typeId = BinaryPersistence.getBuildItemObjectId(bytes);
			
			// (16.05.2018 TM)EXcP: proper exception
			throw new PersistenceExceptionTypeConsistency(
				"Type cannot be resolved: " + typeName + " (TypeId " + typeId + ")"
			);
		}
	}

	@Override
	public void update(final Binary bytes, final Class<?> instance, final PersistenceBuildLinker builder)
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

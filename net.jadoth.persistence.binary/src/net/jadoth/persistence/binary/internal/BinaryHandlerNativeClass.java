package net.jadoth.persistence.binary.internal;

import static net.jadoth.Jadoth.notNull;

import net.jadoth.memory.objectstate.ObjectStateHandlerLookup;
import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.binary.types.BinaryPersistence;
import net.jadoth.persistence.binary.types.BinaryTypeHandlerCreator;
import net.jadoth.persistence.exceptions.PersistenceExceptionTypeNotPersistable;
import net.jadoth.persistence.types.PersistenceTypeHandler;
import net.jadoth.persistence.types.PersistenceTypeHandlerLookup;
import net.jadoth.swizzling.types.SwizzleBuildLinker;
import net.jadoth.swizzling.types.SwizzleStoreLinker;
import net.jadoth.swizzling.types.SwizzleTypeManager;

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
	// instance fields  //
	/////////////////////

	private final PersistenceTypeHandlerLookup<Binary> typeLookup;



	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	BinaryHandlerNativeClass(final long typeId)
	{
		this(null, typeId); // only needed for automated reflective use when initializing the default type dictionary
	}

	public BinaryHandlerNativeClass(final PersistenceTypeHandlerLookup<Binary> typeLookup, final long typeId)
	{
		super(typeId, typeWorkaround(), pseudoFields(
			chars("name")
		));
		this.typeLookup = typeLookup;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public void store(final Binary bytes, final Class<?> instance, final long oid, final SwizzleStoreLinker linker)
	{
		// no-op, static state is not stored
	}

	@Override
	public Class<?> create(final Binary bytes)
	{
		/* classes get registered before instance data is processed,
		 * hence it is enough to lookup the class by its oid, which is this class instance's tid.
		 */
		return this.typeLookup.lookupType(BinaryPersistence.getBuildItemObjectId(bytes));
	}

	@Override
	public void update(final Binary bytes, final Class<?> instance, final SwizzleBuildLinker builder)
	{
		// no-op, see create()
	}

	@Override
	public boolean isEqual(
		final Class<?>                 source                    ,
		final Class<?>                 target                    ,
		final ObjectStateHandlerLookup instanceStateHandlerLookup
	)
	{
		return source == target; // class instances are always singletons, hence reference comparison
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



	public static final class Creator implements BinaryTypeHandlerCreator
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		private final PersistenceTypeHandlerLookup<Binary> typeLookup;



		///////////////////////////////////////////////////////////////////////////
		// constructors     //
		/////////////////////

		public Creator(final PersistenceTypeHandlerLookup<Binary> typeLookup)
		{
			super();
			this.typeLookup = notNull(typeLookup);
		}



		///////////////////////////////////////////////////////////////////////////
		// override methods //
		/////////////////////

		@SuppressWarnings("unchecked") // necessary to cheat the type system because of too high abstraction
		@Override
		public <T> PersistenceTypeHandler<Binary, T> createTypeHandler(
			final Class<T>           type       ,
			final long               typeId     ,
			final SwizzleTypeManager typeManager
		)
			throws PersistenceExceptionTypeNotPersistable
		{
			return (PersistenceTypeHandler<Binary, T>)new BinaryHandlerNativeClass(this.typeLookup, typeId);
		}
	}

}

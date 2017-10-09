package net.jadoth.persistence.binary.internal;

import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.binary.types.BinaryPersistence;
import net.jadoth.swizzling.types.SwizzleTypeLookup;

public final class BinaryHandlerNativeClass extends AbstractBinaryHandlerStateless<Class<?>>
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

	private SwizzleTypeLookup typeLookup;



	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public BinaryHandlerNativeClass()
	{
		super(
			typeWorkaround()
//			pseudoFields(
//				chars("name")
//			)
		);
//		this.typeLookup = typeLookup;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	/*
	 * Can't store the class name because names are meant to be structually irrelevant
	 * and be handled solely in the type dictionary. TypeIds identify types, not their names.
	 */

	@Override
	public Class<?> create(final Binary bytes)
	{
		/*
		 * Classes get registered before instance data ist processed,
		 * hence it is enough to lookup the class by its oid, which is this class instance's tid.
		 * Note:
		 * Can't just store the class name and resolve it via reflection, because obsolete types
		 * have multiple (obsolet) type id for the same class file
		 */
		return this.typeLookup.lookupType(BinaryPersistence.getBuildItemObjectId(bytes));
	}
	
}

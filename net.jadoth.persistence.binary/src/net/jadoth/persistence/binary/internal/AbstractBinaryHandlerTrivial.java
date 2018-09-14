package net.jadoth.persistence.binary.internal;

import java.lang.reflect.Field;

import net.jadoth.X;
import net.jadoth.collections.types.XGettingEnum;
import net.jadoth.functional._longProcedure;
import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.binary.types.BinaryTypeHandler;
import net.jadoth.persistence.types.PersistenceTypeDescriptionMember;
import net.jadoth.swizzling.types.SwizzleBuildLinker;
import net.jadoth.swizzling.types.SwizzleFunction;

public abstract class AbstractBinaryHandlerTrivial<T> extends BinaryTypeHandler.AbstractImplementation<T>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public AbstractBinaryHandlerTrivial(final Class<T> type)
	{
		super(type);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public void update(final Binary medium, final T instance, final SwizzleBuildLinker builder)
	{
		// no-op, no state to update
	}
	
	@Override
	public final void complete(final Binary medium, final T instance, final SwizzleBuildLinker builder)
	{
		/* any "trival" implementation cannot have the need for a completion step
		 * (see non-reference-hashing collections for other examples)
		 */
	}

	@Override
	public final void iterateInstanceReferences(final T instance, final SwizzleFunction iterator)
	{
		// no-op, no references
	}

	@Override
	public final void iteratePersistedReferences(final Binary offset, final _longProcedure iterator)
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
	public XGettingEnum<? extends PersistenceTypeDescriptionMember> members()
	{
		return X.empty();
	}
	
	@Override
	public boolean isPrimitiveType()
	{
		return false;
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
		return false;
	}

	@Override
	public final boolean hasVaryingPersistedLengthInstances()
	{
		return false;
	}

}

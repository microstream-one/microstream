package net.jadoth.persistence.binary.internal;

import java.lang.reflect.Field;

import net.jadoth.collections.X;
import net.jadoth.collections.types.XGettingEnum;
import net.jadoth.collections.types.XGettingSequence;
import net.jadoth.functional._longProcedure;
import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.binary.types.BinaryTypeHandler;
import net.jadoth.swizzling.exceptions.SwizzleExceptionConsistency;
import net.jadoth.swizzling.types.SwizzleBuildLinker;
import net.jadoth.swizzling.types.SwizzleFunction;

public abstract class AbstractBinaryHandlerTrivial<T> extends BinaryTypeHandler.AbstractImplementation<T>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public AbstractBinaryHandlerTrivial(final Class<T> type, final long typeId)
	{
		super(type, typeId);
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public AbstractBinaryHandlerTrivial<T> getStateDescriptor()
	{
		return this;
	}

	@Override
	public final void validateFields(final XGettingSequence<Field> fieldDescriptions)
		throws SwizzleExceptionConsistency
	{
		if(fieldDescriptions.isEmpty())
		{
			return;
		}
		throw new SwizzleExceptionConsistency();
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
	public final XGettingEnum<Field> getAllFields()
	{
		return X.empty();
	}

//	@Override
//	public final XGettingEnum<Field> getStaticFinalFields()
//	{
//		return X.empty();
//	}
//
//	@Override
//	public final XGettingEnum<Field> getStaticFinalReferenceFields()
//	{
//		return X.empty();
//	}
//
//	@Override
//	public final XGettingEnum<Field> getStaticFinalPrimitiveFields()
//	{
//		return X.empty();
//	}
//
//	@Override
//	public final XGettingEnum<Field> getStaticMutableFields()
//	{
//		return X.empty();
//	}
//
//	@Override
//	public final XGettingEnum<Field> getStaticMutableReferenceFields()
//	{
//		return X.empty();
//	}
//
//	@Override
//	public final XGettingEnum<Field> getStaticMutablePrimitiveFields()
//	{
//		return X.empty();
//	}
//
//	@Override
//	public final XGettingEnum<Field> getStaticAllFields()
//	{
//		return X.empty();
//	}
//
//	@Override
//	public final XGettingEnum<Field> getStaticAllReferenceFields()
//	{
//		return X.empty();
//	}
//
//	@Override
//	public final XGettingEnum<Field> getStaticAllPrimitiveFields()
//	{
//		return X.empty();
//	}

//	@Override
//	public final long getFixedBinaryContentLength()
//	{
//		return 0L;
//	}

	@Override
	public boolean isVariableBinaryLengthType()
	{
		return false;
	}

	@Override
	public boolean hasVariableBinaryLengthInstances()
	{
		return false;
	}

//	@Override
//	public final void staticUpdate(final Binary bytes, final SwizzleBuilder builder)
//	{
//		// no-op, nothing to update
//	}

//	@Override
//	public final void staticStore(final Binary bytes, final PersistenceStorer<Binary> persister)
//	{
//		// no-op, nothing to store
//	}

}

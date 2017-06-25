package net.jadoth.traversal.handlers;

import java.lang.reflect.Field;
import java.util.function.Consumer;
import java.util.function.Predicate;

import net.jadoth.collections.JadothArrays;
import net.jadoth.reflect.JadothReflect;
import net.jadoth.traversal.TraversalHandler;

public final class TraversalHandlerReflective<T> extends TraversalHandler.AbstractImplementation<T>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	public static <T> TraversalHandlerReflective<T> New(final Predicate<? super T> logic , final Field... fields)
	{
		return new TraversalHandlerReflective<>(
			logic                    , // logic may be null
			JadothArrays.copy(fields)
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final Field[] referenceFields;
//	private final long[]  refFieldOffsets; // see comment below



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	TraversalHandlerReflective(final Predicate<? super T> logic, final Field[] relevantFields)
	{
		super(logic);
		this.referenceFields = relevantFields;
//		this.refFieldOffsets = Memory.objectFieldOffsets(relevantFields); // see comment below
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public final void traverseReferences(final T instance, final Consumer<Object> referenceHandler)
	{
//		for(final long fieldOffset : this.refFieldOffsets)
//		{
//			final Object referent = Memory.getObject(instance, fieldOffset);
//			referenceHandler.accept(referent);
//		}
		/* (12.04.2016 TM)NOTE: note on performance:
		 * using direct field offsets via net.jadoth.memory.Memory (sun.misc.Unsage in the end) confusingly proved
		 * sometimes to be faster than reflection as expected, but sometimes even 5% slower.
		 * So for now, the typewise cleaner reflective approach is kept instead of the direct memory access approach.
		 */
		for(final Field field : this.referenceFields)
		{
			final Object referent = JadothReflect.getFieldValue(field, instance);
			referenceHandler.accept(referent);
		}
	}

}

package net.jadoth.traversal2;

import static net.jadoth.Jadoth.notNull;

import java.lang.reflect.Field;
import java.util.function.Predicate;

import net.jadoth.collections.HashEnum;
import net.jadoth.reflect.JadothReflect;

public interface TraversalHandlerCreator
{
	public TraversalHandler createTraversalHandler(Class<?> type);
	
	
	
	public static TraversalHandlerCreator New(final Predicate<? super Field> fieldSelector)
	{
		return new TraversalHandlerCreator.Reflective(
			notNull(fieldSelector)
		);
	}
	
	public final class Reflective implements TraversalHandlerCreator
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final Predicate<? super Field> fieldSelector;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Reflective(final Predicate<? super Field> fieldSelector)
		{
			super();
			this.fieldSelector = fieldSelector;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		private final Field[] collectFields(final Class<?> type)
		{
			final HashEnum<Field> selectedFields = HashEnum.New();
			JadothReflect.collectTypedFields(selectedFields, type, field ->
				JadothReflect.isInstanceField(field)
				&& this.fieldSelector.test(field))
			;
			return selectedFields.toArray(Field.class);
		}

		@Override
		public final TraversalHandler createTraversalHandler(final Class<?> type)
		{
			final Field[] collectedFields = collectFields(type);
			
			return collectedFields.length != 0
				? new TraverserReflective(type, collectedFields)
				: null
			;
		}
		
	}
	
}

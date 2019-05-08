package one.microstream.reflect;

import java.lang.reflect.Field;

import one.microstream.exceptions.IllegalAccessRuntimeException;
import one.microstream.reflect.XReflect;



/**
 *
 * @author Thomas Muenz
 */
public interface CopyHandler
{

	/**
	 * Copies the content of Field <code>fieldToCopy</code> from object <code>source</code>
	 * to object <code>target</code>.
	 * <p>
	 * Implementation hint:<br>
	 * Use {@link ReflectionTools.setFieldValue(Field f, Object obj, Object value)} to set the value eventually
	 *
	 * @param fieldToCopy the field to copy
	 * @param source the source
	 * @param target the target
	 */
	public void copy(Field fieldToCopy, Object source, Object target);



	/**
	 * Default implementation of CopyHandler.<br>
	 * Little meaningful because it does the exact same thing as ReflectionTools.copy() does.
	 *
	 * @author Thomas Muenz
	 */
	public static class Default implements CopyHandler
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		/**
		 * Trivial default constructor.
		 */
		public Default()
		{
			super();
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		/**
		 * Default copy implementation. Little meaningful because it does the exact same thing as
		 * ReflectionTools.copy() does.
		 *
		 * @param fieldToCopy the field to copy
		 * @param source the source
		 * @param target the target
		 * @throws IllegalAccessRuntimeException the illegal access runtime exception
		 */
		@Override
		public void copy(final Field fieldToCopy, final Object source, final Object target)
			throws IllegalAccessRuntimeException
		{
			XReflect.setFieldValue(fieldToCopy, target, XReflect.getFieldValue(fieldToCopy, source));
		}

	}

}

/*
 * Copyright (c) 2008-2010, Thomas Muenz
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.jadoth.reflect;

import java.lang.reflect.Field;

import net.jadoth.exceptions.IllegalAccessRuntimeException;



/**
 * The Interface CopyHandler.
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
	public static class DefaultImplementation implements CopyHandler
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		/**
		 * Trivial default constructor.
		 */
		public DefaultImplementation()
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
			JadothReflect.setFieldValue(fieldToCopy, target, JadothReflect.getFieldValue(fieldToCopy, source));
		}

	}

}
